package com.example.safesound.ui.my_records

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.safesound.databinding.FragmentRecordCreationDialogBinding
import com.example.safesound.utils.AudioRecorder
import com.example.safesound.utils.PermissionUtils
import com.example.safesound.utils.UploadManager
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class RecordCreationDialogFragment : DialogFragment() {

    private var _binding: FragmentRecordCreationDialogBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var audioRecorder: AudioRecorder
    @Inject
    lateinit var uploadManager: UploadManager

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    private val myRecordsViewModel: MyRecordsViewModel by viewModels()


    private var recordId: String? = null
    private var selectedImageFile: File? = null
    private var elapsedTime = 0L
    private var elapsedTimer: CountDownTimer? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val CHUNK_INTERVAL_MS = 10 * 60 * 1000L // 10 minutes
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Prevent dismissing by tapping outside
        dialog.setCanceledOnTouchOutside(false)
        // Prevent dismissing with back button
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecordCreationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPermissions()
        setupUI()
        initializeImagePicker()
        observeViewModel()
    }

    private fun setupPermissions() {
        if (!PermissionUtils.hasPermissions(
                requireContext(),
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            PermissionUtils.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setupUI() {
        binding.buttonUploadPhoto.setOnClickListener { openImageChooser() }
        binding.buttonStartRecord.setOnClickListener { startRecordProcess() }
        binding.buttonFinishRecording.setOnClickListener { stopRecording() }
        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    private fun observeViewModel() {
        myRecordsViewModel.createRecordResult.observe(viewLifecycleOwner) { result ->
            if (result.success) {
                recordId = result.data?._id
                startRecording()
            } else {
                Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    private fun startRecordProcess() {
        val recordName = binding.editTextRecordName.text.toString().trim()
        if (recordName.isEmpty()) {
            binding.editTextRecordName.error = "Record name is required"
            return
        }
        myRecordsViewModel.createRecord(recordName, selectedImageFile)
    }

    private fun startRecording() {
        recordId?.let {
            audioRecorder.start(it, requireContext())
            updateUIForRecording()
            startElapsedTimer()
            scheduleChunkUploads()
        } ?: Toast.makeText(requireContext(), "Error: Record ID missing", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        cancelTimers()
        val finalChunkFile = audioRecorder.stop()
        val currentTimeMillis = System.currentTimeMillis()
        val recordingStartTimeMillis = currentTimeMillis - elapsedTime

        val finalStartTimeMillis = if (elapsedTime > CHUNK_INTERVAL_MS) {
            currentTimeMillis - CHUNK_INTERVAL_MS
        } else {
            recordingStartTimeMillis
        }

        uploadChunk(recordId, finalChunkFile, finalStartTimeMillis, currentTimeMillis) { success ->
            if (success) {
                dismissSafely()
            } else {
                Log.e("RecordDialog", "Failed to upload final chunk.")
                dismissSafely()
            }
        }
    }

    private fun startElapsedTimer() {
        elapsedTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (_binding == null) return
                elapsedTime += 1000
                val minutes = (elapsedTime / 60000).toInt()
                val seconds = ((elapsedTime / 1000) % 60).toInt()
                binding.textViewRecordingTime.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {}
        }.start()
    }

    private fun scheduleChunkUploads() {
        val job = CoroutineScope(Dispatchers.IO).launch {
            var startTime = System.currentTimeMillis() - elapsedTime

            while (isActive) {
                delay(CHUNK_INTERVAL_MS)
                val chunkFile = audioRecorder.createChunk(requireContext())
                if (chunkFile == null) {
                    Log.e("RecordDialog", "Failed to generate chunk file. Halting uploads.")
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to generate chunk file. Uploads halted.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    cancelTimers()
                    break
                }
                val endTime = System.currentTimeMillis()
                uploadChunk(recordId, chunkFile, startTime, endTime) { success ->
                    if (!success) {
                        Log.e("RecordDialog", "Chunk upload failed. Halting uploads.")
                        launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Chunk upload failed. Uploads halted.",
                                Toast.LENGTH_LONG
                            ).show()
                            dismissSafely()
                        }
                        cancelTimers()
                        this@launch.cancel()
                    }
                }
                startTime = endTime
            }
        }

        job.invokeOnCompletion { throwable ->
            if (throwable != null) {
                Log.e("RecordDialog", "Coroutine cancelled due to error: ${throwable.message}")
                dismissSafely()
            }
        }
    }

    private fun cancelTimers() {
        elapsedTimer?.cancel()
        elapsedTimer = null
    }

    private fun updateUIForRecording() {
        binding.layoutRecording.visibility = View.VISIBLE
        binding.editTextRecordName.visibility = View.GONE
        binding.buttonStartRecord.visibility = View.GONE
        binding.buttonCancel.visibility = View.GONE
        binding.buttonUploadPhoto.visibility = View.GONE
        binding.imageViewPreview.visibility = View.GONE
    }

    private fun initializeImagePicker() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.data?.let { uri ->
                    selectedImageFile = uriToFile(uri)
                    Picasso.get()
                        .load(uri)
                        .fit()
                        .centerCrop()
                        .into(binding.imageViewPreview)
                    binding.imageViewPreview.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun uriToFile(uri: Uri): File {
        val contentResolver = requireContext().contentResolver
        val file = File(requireContext().cacheDir, "temp_image.jpg")
        contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    private fun dismissSafely() {
        if (isAdded && !isStateSaved) {
            dismiss()
        } else {
            Log.w("RecordDialog", "Dialog fragment is not attached. Cannot dismiss.")
        }
    }

    private fun uploadChunk(
        recordId: String?,
        chunkFile: File?,
        startTime: Long,
        endTime: Long,
        onCompletion: ((Boolean) -> Unit)? = null
    ) {
        uploadManager.uploadChunk(recordId, chunkFile, startTime, endTime) { success, errorMessage ->
            if (success) {
                Log.d("RecordDialog", "Chunk uploaded successfully.")
                onCompletion?.invoke(true)
            } else {
                Log.e("RecordDialog", "Failed to upload chunk: $errorMessage")
                Toast.makeText(requireContext(), "Upload failed: $errorMessage", Toast.LENGTH_SHORT).show()
                onCompletion?.invoke(false)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().supportFragmentManager.setFragmentResult("refreshRecords", Bundle.EMPTY)
        cancelTimers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelTimers()
        _binding = null
    }
}
