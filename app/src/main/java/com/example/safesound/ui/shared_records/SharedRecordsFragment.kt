package com.example.safesound.ui.shared_records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.safesound.databinding.FragmentSharedRecordsBinding

class SharedRecordsFragment : Fragment() {

    private var _binding: FragmentSharedRecordsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedRecordsViewModel =
            ViewModelProvider(this).get(SharedRecordsViewModel::class.java)

        _binding = FragmentSharedRecordsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSharedRecords
        sharedRecordsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}