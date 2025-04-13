package com.example.safesound.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safesound.adapters.ChunksAdapter
import com.example.safesound.data.records.Chunk
import com.example.safesound.databinding.FragmentRecordChunksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordChunksFragment : Fragment() {

    private var _binding: FragmentRecordChunksBinding? = null
    private val binding get() = _binding!!

    private val recordsViewModel: RecordsViewModel by viewModels()
    private lateinit var chunksAdapter: ChunksAdapter

    private val recordId: String by lazy {
        requireArguments().getString("recordId") ?: throw IllegalArgumentException("Missing recordId")
    }
    private val isMyRecords: Boolean by lazy {
        requireArguments().getBoolean("isMyRecords", true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecordChunksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        recordsViewModel.fetchAllChunks(recordId)
    }

    private fun setupRecyclerView() {
        chunksAdapter = ChunksAdapter { chunk ->
            navigateToChunkDetail(chunk)
        }
        binding.recyclerViewChunks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chunksAdapter
        }
    }

    private fun groupChunks(chunks: List<Chunk>): List<ChunkItem> {
        val grouped = mutableListOf<ChunkItem>()
        var silentStart: String? = null
        var silentEnd: String? = null

        for (chunk in chunks) {
            if (chunk.summary == "No meaningful audio detected") {
                if (silentStart == null) silentStart = chunk.startTime
                silentEnd = chunk.endTime
            } else {
                if (silentStart != null) {
                    grouped.add(ChunkItem.SilentGroup(silentStart, silentEnd ?: silentStart))
                    silentStart = null
                }
                grouped.add(ChunkItem.Regular(chunk))
            }
        }

        if (silentStart != null) {
            grouped.add(ChunkItem.SilentGroup(silentStart, silentEnd ?: silentStart))
        }

        return grouped
    }


    private fun observeViewModel() {
        recordsViewModel.allChunksResult.observe(viewLifecycleOwner) { result ->
            if (result.success && !result.data.isNullOrEmpty()) {
                val processedList = groupChunks(result.data)
                chunksAdapter.submitList(processedList)
            }
        }
    }

    private fun navigateToChunkDetail(chunk: Chunk) {
        val action = RecordChunksFragmentDirections.actionRecordChunksFragmentToChunkDetailFragment(
            chunkId = chunk._id,
            chunkName = chunk.name,
            audioFilePath = chunk.audioFilePath,
            summary = chunk.summary ?: "",
            isMyRecords = isMyRecords
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
