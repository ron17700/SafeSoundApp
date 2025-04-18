package com.example.safesound.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safesound.R
import com.example.safesound.data.records.Chunk
import com.example.safesound.databinding.ItemChunkBinding
import com.example.safesound.databinding.ItemSilentGroupBinding
import com.example.safesound.ui.records.ChunkItem
import com.example.safesound.utils.TimestampFormatter.formatIsoToTime

class ChunksAdapter(
    private val onChunkClick: (Chunk) -> Unit
) : ListAdapter<ChunkItem, RecyclerView.ViewHolder>(ChunkItemDiffCallback()) {

    companion object {
        private const val TYPE_REGULAR = 0
        private const val TYPE_SILENT_GROUP = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChunkItem.Regular -> TYPE_REGULAR
            is ChunkItem.SilentGroup -> TYPE_SILENT_GROUP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_REGULAR -> {
                val binding = ItemChunkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                RegularViewHolder(binding, onChunkClick)
            }
            TYPE_SILENT_GROUP -> {
                val binding = ItemSilentGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SilentGroupViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ChunkItem.Regular -> (holder as RegularViewHolder).bind(item.chunk)
            is ChunkItem.SilentGroup -> (holder as SilentGroupViewHolder).bind(item)
        }
    }

    class RegularViewHolder(
        private val binding: ItemChunkBinding,
        private val onChunkClick: (Chunk) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chunk: Chunk) {
            binding.textViewChunkName.text = chunk.name
            val start = formatIsoToTime(chunk.startTime)
            val end = formatIsoToTime(chunk.endTime)
            binding.textViewChunkDuration.text = "$start - $end"

            val isSilent = chunk.summary == "No meaningful audio detected"
            val iconResId = when {
                isSilent -> R.drawable.ic_silent
                chunk.chunkClass == "Natural" -> R.drawable.ic_natural
                chunk.chunkClass == "Good" -> R.drawable.ic_good
                chunk.chunkClass == "Bad" -> R.drawable.ic_bad
                else -> R.drawable.ic_natural
            }
            binding.imageViewChunkClassIcon.setImageResource(iconResId)

            binding.root.setOnClickListener {
                if (!isSilent) onChunkClick(chunk)
            }
        }
    }

    class SilentGroupViewHolder(
        private val binding: ItemSilentGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: ChunkItem.SilentGroup) {
            binding.textViewTimeRange.text = "${formatIsoToTime(group.start)} - ${formatIsoToTime(group.end)}"
        }
    }

    class ChunkItemDiffCallback : DiffUtil.ItemCallback<ChunkItem>() {
        override fun areItemsTheSame(oldItem: ChunkItem, newItem: ChunkItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChunkItem, newItem: ChunkItem): Boolean {
            return oldItem == newItem
        }
    }
}
