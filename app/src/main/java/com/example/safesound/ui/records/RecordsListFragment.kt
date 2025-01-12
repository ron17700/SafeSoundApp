package com.example.safesound.ui.records

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safesound.adapters.RecordsAdapter
import com.example.safesound.databinding.FragmentRecordsListBinding
import com.example.safesound.data.records.Record
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordsListFragment : Fragment() {

    private var _binding: FragmentRecordsListBinding? = null
    private val binding get() = _binding!!

    private val recordsViewModel: RecordsViewModel by viewModels()
    private lateinit var recordsAdapter: RecordsAdapter
    private var isMyRecords: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecordsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isMyRecords = arguments?.getBoolean("isMyRecords") ?: true
        setupRecyclerView()
        observeViewModel()
        setupFragmentResultListener()
        recordsViewModel.fetchAllRecords(isMyRecords)
    }

    private fun setupRecyclerView() {
        recordsAdapter = RecordsAdapter(
            onRecordClick = { record, isMyRecords ->
                navigateToRecordChunks(record._id, record.name, isMyRecords)
            },
            onEditClick = { record -> showEditRecordDialog(record)},
            onDeleteClick = { record ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this record?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        recordsViewModel.deleteRecord(record._id)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onStarClick = { record -> recordsViewModel.likeRecord(record._id)}
        )
        recordsAdapter.setIsMyRecords(isMyRecords)
        binding.recyclerViewRecords.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }

    private fun observeViewModel() {
        recordsViewModel.allRecordsResult.observe(viewLifecycleOwner) { result ->
            if (result.success && !result.data.isNullOrEmpty()) {
                recordsAdapter.submitList(result.data)
                showRecordsList()
            } else {
                showPlaceholder()
            }
        }

        recordsViewModel.deleteRecordResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            if (result.success) {
                Toast.makeText(requireContext(), "Record deleted", Toast.LENGTH_SHORT).show()
                recordsViewModel.fetchAllRecords(isMyRecords, true)
            } else {
                Toast.makeText(requireContext(), "Failed to delete record", Toast.LENGTH_SHORT).show()
            }
            recordsViewModel.clearDeleteRecordResult()
        }

        recordsViewModel.likeRecordsResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            if (result.success) {
                recordsViewModel.fetchAllRecords(isMyRecords, true)
            }
        }
    }

    private fun navigateToRecordChunks(recordId: String, recordName: String, isMyRecords: Boolean) {
        val action = RecordsListFragmentDirections.actionRecordsListFragmentToRecordChunksFragment(recordId, recordName, isMyRecords)
        findNavController().navigate(action)
    }

    private fun showRecordsList() {
        binding.recyclerViewRecords.visibility = View.VISIBLE
        binding.textViewPlaceholder.visibility = View.GONE
    }

    private fun showPlaceholder() {
        binding.recyclerViewRecords.visibility = View.GONE
        binding.textViewPlaceholder.visibility = View.VISIBLE
    }

    private fun setupFragmentResultListener() {
        requireActivity().supportFragmentManager.setFragmentResultListener("refreshRecords", this) { _, _ ->
            recordsViewModel.fetchAllRecords(isMyRecords, true)
        }
    }

    private fun showEditRecordDialog(record: Record) {
        val dialog = RecordCreationDialogFragment.newInstance(
            isEditMode = true,
            recordId = record._id,
            recordName = record.name,
            isPublic = record.public,
            imageUri = Uri.parse(record.image)
        )
        dialog.show(parentFragmentManager, "EditRecordDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
