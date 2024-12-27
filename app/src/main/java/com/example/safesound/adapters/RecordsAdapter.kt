package com.example.safesound.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safesound.R
import com.example.safesound.databinding.ItemRecordBinding
import com.squareup.picasso.Picasso
import com.example.safesound.data.my_records.Record
import com.example.safesound.network.NetworkModule
import java.text.SimpleDateFormat
import java.util.Locale

import javax.inject.Inject

class RecordsAdapter @Inject constructor() : ListAdapter<Record, RecordsAdapter.RecordViewHolder>(RecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecordViewHolder(private val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root) {
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
                "Natural" -> R.drawable.ic_menu_camera
                "Good" -> R.drawable.ic_menu_gallery
                "Bad" -> R.drawable.ic_menu_slideshow
                else -> R.drawable.ic_menu_camera
            }
            binding.imageViewClassIcon.setImageResource(iconResId)

            binding.imageViewRecordPhoto.visibility = View.VISIBLE
            Picasso.get()
                .load(NetworkModule.BASE_URL + record.image)
                .fit()
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.holo_red_dark)
                .into(binding.imageViewRecordPhoto)
        }
    }

    class RecordDiffCallback : DiffUtil.ItemCallback<Record>() {
        override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean = oldItem._id == newItem._id

        override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean = oldItem == newItem
    }
}