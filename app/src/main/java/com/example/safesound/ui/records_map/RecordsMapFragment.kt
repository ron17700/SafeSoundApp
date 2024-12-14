package com.example.safesound.ui.records_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.safesound.databinding.FragmentRecordsMapBinding

class RecordsMapFragment : Fragment() {

    private var _binding: FragmentRecordsMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val recordsMapViewModel =
            ViewModelProvider(this).get(RecordsMapViewModel::class.java)

        _binding = FragmentRecordsMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textRecordsMap
        recordsMapViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}