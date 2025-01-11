package com.example.safesound.ui.records_map

import android.app.AlertDialog
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.safesound.databinding.FragmentRecordsMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.example.safesound.data.records.Record
import com.example.safesound.ui.records.RecordsViewModel
import com.example.safesound.utils.TimestampFormatter.formatIsoToTime
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class RecordsMapFragment : Fragment() {

    private var _binding: FragmentRecordsMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private val pendingRecords = mutableListOf<Record>()

    private val recordsViewModel: RecordsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecordsMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        setupMap()
//        setupSearch()
        observeRecords()
    }

    private fun setupMap() {
        binding.mapView.getMapAsync { map ->
            googleMap = map
            googleMap.uiSettings.isZoomControlsEnabled = true

            // Display any pending records
            if (pendingRecords.isNotEmpty()) {
                displayRecordsOnMap(pendingRecords)
                pendingRecords.clear() // Clear the list after displaying the markers
            }

            // Handle marker click events
            googleMap.setOnMarkerClickListener { marker ->
                val record = marker.tag as? Record
                record?.let {
                    showRecordDetails(it)
                }
                true
            }
        }
    }

//    private fun setupSearch() {
//        val searchBar = binding.root.findViewById<EditText>(R.id.searchBar)
//        searchBar.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                val location = searchBar.text.toString()
//                if (location.isNotBlank()) {
//                    searchLocation(location)
//                }
//                true
//            } else {
//                false
//            }
//        }
//    }

    private fun searchLocation(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addressList = geocoder.getFromLocationName(query, 1)
                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    withContext(Dispatchers.Main) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                        googleMap.addMarker(MarkerOptions().position(latLng).title(query))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error finding location", Toast.LENGTH_SHORT).show()
                }
                Log.e("RecordsMapFragment", "Geocoder error", e)
            }
        }
    }

    private fun observeRecords() {
        recordsViewModel.allRecordsResult.observe(viewLifecycleOwner) { result ->
            if (result.success && !result.data.isNullOrEmpty()) {
                if (::googleMap.isInitialized) {
                    displayRecordsOnMap(result.data)
                } else {
                    pendingRecords.addAll(result.data)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to load records", Toast.LENGTH_SHORT).show()
            }
        }
        recordsViewModel.fetchAllRecords(false)
    }

    private fun displayRecordsOnMap(records: List<Record>) {
        records.forEach { record ->
            if (record.latitude != null && record.longitude != null) {
                val latLng = LatLng(record.latitude, record.longitude)
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(record.name)
                )
                marker?.tag = record // Attach record details to the marker
            }
        }
    }

    private fun showRecordDetails(record: Record) {
        val details = """
            Name: ${record.name}
            Created By: ${record.userId?.email}
            Creation Date: ${formatIsoToTime(record.createdAt, true)}
            Class: ${record.recordClass}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Record Details")
            .setMessage(details)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}