package com.example.safesound.ui.my_records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safesound.adapters.RecordsAdapter
import com.example.safesound.databinding.FragmentMyRecordsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyRecordsFragment : Fragment() {

    private var _binding: FragmentMyRecordsBinding? = null
    private val binding get() = _binding!!

    private val myRecordsViewModel: MyRecordsViewModel by viewModels()
    private lateinit var recordsAdapter: RecordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        myRecordsViewModel.fetchAllRecords()
    }

    private fun setupRecyclerView() {
        recordsAdapter = RecordsAdapter()
        binding.recyclerViewRecords.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }

    private fun observeViewModel() {
        myRecordsViewModel.allRecordsResult.observe(viewLifecycleOwner) { result ->
            if (result.success && !result.data.isNullOrEmpty()) {
                result.data.forEach { re -> re.recordClass = "Good" }
                recordsAdapter.submitList(result.data)
                showRecordsList()
            } else {
                // No records available, show placeholder
                showPlaceholder()
            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener("refreshRecords", this) { _, _ ->
            myRecordsViewModel.fetchAllRecords()
        }
    }

    private fun showRecordsList() {
        binding.recyclerViewRecords.visibility = View.VISIBLE
        binding.textViewPlaceholder.visibility = View.GONE
    }

    private fun showPlaceholder() {
        binding.recyclerViewRecords.visibility = View.GONE
        binding.textViewPlaceholder.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}