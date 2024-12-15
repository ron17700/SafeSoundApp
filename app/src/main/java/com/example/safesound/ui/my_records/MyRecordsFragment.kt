package com.example.safesound.ui.my_records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.safesound.databinding.FragmentMyRecordsBinding

class MyRecordsFragment : Fragment() {

    private var _binding: FragmentMyRecordsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myRecordsViewModel =
            ViewModelProvider(this).get(MyRecordsViewModel::class.java)

        _binding = FragmentMyRecordsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMyRecords
        myRecordsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}