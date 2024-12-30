package com.example.safesound.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safesound.R
import com.example.safesound.databinding.ItemRecordBinding
import com.squareup.picasso.Picasso
import com.example.safesound.data.records.Record
import com.example.safesound.network.NetworkModule
import java.text.SimpleDateFormat
import java.util.Locale

import javax.inject.Inject

class RecordsAdapter @Inject constructor(
    private val onRecordClick: (Record, Boolean) -> Unit,
    private val onEditClick: (Record) -> Unit,
    private val onDeleteClick: (Record) -> Unit,
) : ListAdapter<Record, RecordsAdapter.RecordViewHolder>(RecordDiffCallback()) {

    private var isMyRecords: Boolean = true

    fun setIsMyRecords(isMyRecords: Boolean) {
        this.isMyRecords = isMyRecords
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding, onRecordClick, onEditClick, onDeleteClick, isMyRecords)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class RecordViewHolder(
        private val binding: ItemRecordBinding,
        private val onRecordClick: (Record, Boolean) -> Unit,
        private val onEditClick: (Record) -> Unit,
        private val onDeleteClick: (Record) -> Unit,
        private val isMyRecords: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: Record) {
            binding.textViewRecordName.text = record.name
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
            val formattedDate = try {
                val date = inputFormat.parse(record.createdAt)
                outputFormat.format(date ?: record.createdAt)
            } catch (e: Exception) {
                record.createdAt
            }
            binding.textViewCreatedDate.text = formattedDate
            val iconResId = when (record.recordClass) {
                "Natural" -> R.drawable.ic_natural
                "Good" -> R.drawable.ic_good
                "Bad" -> R.drawable.ic_bad
                else -> R.drawable.ic_natural
            }
            binding.imageViewClassIcon.setImageResource(iconResId)

            binding.imageViewRecordPhoto.visibility = View.VISIBLE
            Picasso.get()
                .load(NetworkModule.BASE_URL + record.image)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .into(binding.imageViewRecordPhoto)

            if (isMyRecords) {
                binding.dropdownMenu.visibility = View.VISIBLE
                binding.dropdownMenu.setOnClickListener {
                    showPopupMenu(it, record)
                }
            }

            binding.root.setOnClickListener {
                onRecordClick(record, isMyRecords)
            }
        }

        private fun showPopupMenu(view: View, record: Record) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.record_item_menu) // Define menu resource
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onEditClick(record)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(record)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    class RecordDiffCallback : DiffUtil.ItemCallback<Record>() {
        override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean = oldItem._id == newItem._id
        override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean = oldItem == newItem
    }
}