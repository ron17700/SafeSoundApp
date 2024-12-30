package com.example.safesound.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safesound.R
import com.example.safesound.data.records.Chunk
import com.example.safesound.databinding.ItemChunkBinding
import com.example.safesound.utils.TimestampFormatter.formatIsoToTime

class ChunksAdapter(
    private val onChunkClick: (Chunk) -> Unit
) : ListAdapter<Chunk, ChunksAdapter.ChunkViewHolder>(ChunkDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChunkViewHolder {
        val binding = ItemChunkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChunkViewHolder(binding, onChunkClick)
    }

    override fun onBindViewHolder(holder: ChunkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChunkViewHolder(private val binding: ItemChunkBinding,
        private val onChunkClick: (Chunk) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chunk: Chunk) {
            // Set chunk name
            binding.textViewChunkName.text = chunk.name

            // Set time frame
            val formattedStartTime = formatIsoToTime(chunk.startTime)
            val formattedEndTime = formatIsoToTime(chunk.endTime)
            binding.textViewChunkDuration.text = "$formattedStartTime - $formattedEndTime"

            // Set class icon based on chunkClass
            val iconResId = when (chunk.chunkClass) {
                "Natural" -> R.drawable.ic_natural
                "Good" -> R.drawable.ic_good
                "Bad" -> R.drawable.ic_bad
                else -> R.drawable.ic_natural
            }
            binding.imageViewChunkClassIcon.setImageResource(iconResId)

            binding.root.setOnClickListener {
                onChunkClick(chunk)
            }
        }
    }

    class ChunkDiffCallback : DiffUtil.ItemCallback<Chunk>() {
        override fun areItemsTheSame(oldItem: Chunk, newItem: Chunk): Boolean = oldItem._id == newItem._id
        override fun areContentsTheSame(oldItem: Chunk, newItem: Chunk): Boolean = oldItem == newItem
    }
}