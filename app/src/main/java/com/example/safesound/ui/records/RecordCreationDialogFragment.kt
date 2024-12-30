package com.example.safesound.ui.records

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.RECORD_AUDIO
import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.safesound.R
import com.example.safesound.databinding.FragmentRecordCreationDialogBinding
import com.example.safesound.network.NetworkModule
import com.example.safesound.utils.AudioRecorder
import com.example.safesound.utils.PermissionUtils
import com.example.safesound.utils.UploadManager
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    private val recordsViewModel: RecordsViewModel by viewModels()


    private var recordId: String? = null
    private var recordName: String = ""
    private var isPublic: Boolean = false
    private var selectedImageUri: Uri? = null
    private var isEditMode: Boolean = false

    private var elapsedTime = 0L
    private var elapsedTimer: CountDownTimer? = null
    private var recordingJob: Job? = null

    companion object {
        fun newInstance(
            isEditMode: Boolean = false,
            recordId: String? = null,
            recordName: String? = null,
            isPublic: Boolean = false,
            imageUri: Uri? = null
        ): RecordCreationDialogFragment {
            return RecordCreationDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isEditMode", isEditMode)
                    putString("recordId", recordId)
                    putString("recordName", recordName)
                    putBoolean("isPublic", isPublic)
                    putParcelable("imageUri", imageUri)
                }
            }
        }

        private const val CHUNK_INTERVAL_MS = 10 * 60 * 1000L // 10 minutes
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(binding.buttonUploadPhoto)
            }
        }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val audioGranted = permissions[RECORD_AUDIO] == true
        if (audioGranted) {
            setupUI()
            observeViewModel()
        } else {
            Toast.makeText(requireContext(), "Mandatory permissions not granted.", Toast.LENGTH_SHORT).show()
            dismissSafely()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            isEditMode = it.getBoolean("isEditMode", false)
            recordId = it.getString("recordId")
            recordName = it.getString("recordName", "")
            isPublic = it.getBoolean("isPublic", false)
            selectedImageUri = it.getParcelable("imageUri")
        }

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val window = dialog.window
            if (window != null) {
                val layoutParams = window.attributes
                layoutParams.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                window.attributes = layoutParams
            }
        }
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
        requestPermissions()
    }

    private fun requestPermissions() {
        val requiredPermissions = mutableListOf<String>()
        requiredPermissions.addAll(arrayOf(RECORD_AUDIO, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        requestPermissionsLauncher.launch(requiredPermissions.toTypedArray())
    }

    private fun setupUI() {
        if (isEditMode) {
            binding.editTextRecordName.setText(recordName)
            binding.checkBoxPublic.isChecked = isPublic
            selectedImageUri?.let {
                Picasso.get()
                    .load(NetworkModule.BASE_URL + it)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_broken_image)
                    .into(binding.buttonUploadPhoto)
            }
            binding.textViewDialogTitle.text = "Edit Record"
            binding.buttonStartRecord.text = "Save"
        } else {
            binding.textViewDialogTitle.text = "Create New Record"
            binding.buttonStartRecord.text = "Record"
        }
        binding.buttonStartRecord.setOnClickListener {
            if (isEditMode) {
                updateRecord()
            } else {
                startRecordProcess()
            }
        }
        binding.buttonFinishRecording.setOnClickListener { stopRecording() }
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonUploadPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun updateRecord() {
        recordName = binding.editTextRecordName.text.toString().trim()
        if (recordName.isEmpty()) {
            binding.editTextRecordName.error = "Record name is required"
            return
        }

        val isPublic = binding.checkBoxPublic.isChecked
        recordsViewModel.updateRecord(recordId!!, recordName, isPublic, selectedImageUri)
    }

    private fun observeViewModel() {
        recordsViewModel.createRecordResult.observe(viewLifecycleOwner) { result ->
            if (result.success) {
                recordId = result.data?._id
                startRecording()
            } else {
                Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
        recordsViewModel.updateRecordResult.observe(viewLifecycleOwner) { result ->
            dismissSafely()
        }
    }

    private fun startRecordProcess() {
        recordName = binding.editTextRecordName.text.toString().trim()
        if (recordName.isEmpty()) {
            binding.editTextRecordName.error = "Record name is required"
            return
        }
        isPublic = binding.checkBoxPublic.isChecked;
        val location = if (PermissionUtils.hasLocationPermissions(requireContext())) {
            PermissionUtils.getLastKnownLocation(requireContext())
        } else null
        val latitude = location?.latitude
        val longitude = location?.longitude
        recordsViewModel.createRecord(recordName, isPublic, latitude, longitude, selectedImageUri)

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
        recordingJob?.cancel()
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
        recordingJob = CoroutineScope(Dispatchers.IO).launch {
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

        recordingJob?.invokeOnCompletion { throwable ->
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
        binding.checkBoxPublic.visibility = View.GONE
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
        uploadManager.uploadChunk(
            recordId,
            chunkFile,
            startTime,
            endTime
        ) { success, errorMessage ->
            if (success) {
                Log.d("RecordDialog", "Chunk uploaded successfully.")
                onCompletion?.invoke(true)
            } else {
                Log.e("RecordDialog", "Failed to upload chunk: $errorMessage")
                Toast.makeText(requireContext(), "Upload failed: $errorMessage", Toast.LENGTH_SHORT)
                    .show()
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
