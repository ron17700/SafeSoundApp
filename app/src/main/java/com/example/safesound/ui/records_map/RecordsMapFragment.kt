package com.example.safesound.ui.records_map

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
        observeRecords()
    }

    private fun setupMap() {
        binding.mapView.getMapAsync { map ->
            googleMap = map
            googleMap.uiSettings.isZoomControlsEnabled = true

            // Display any pending records
            if (pendingRecords.isNotEmpty()) {
                displayRecordsOnMap(pendingRecords)
                setupSearch()
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

    private fun setupSearch() {
        val searchBar = binding.searchBar

        // Extract display names with emails for suggestions
        val recordDisplayNames = pendingRecords.map { record ->
            val email = record.userId?.email ?: "No Email"
            "${record.name} ($email)"
        }

        // Set up the adapter with display names
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, recordDisplayNames)
        searchBar.setAdapter(adapter)

        // Handle item selection based on the selected display name
        searchBar.setOnItemClickListener { _, _, _, _ ->
            val selectedDisplayName = searchBar.text.toString()
            val selectedRecord = pendingRecords.firstOrNull { record ->
                val email = record.userId?.email ?: "No Email"
                "${record.name} ($email)" == selectedDisplayName
            }

            if (selectedRecord != null) {
                searchLocation(selectedRecord) // Pass the selected record to searchLocation
            } else {
                println("Error: Record not found for display name: $selectedDisplayName")
            }
        }
    }

    private fun searchLocation(record: Record) {
        if (record.latitude != null && record.longitude != null) {
            val latLng = LatLng(record.latitude, record.longitude)

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
        } else {
            Toast.makeText(requireContext(), "Location not found in records", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeRecords() {
        recordsViewModel.allRecordsResult.observe(viewLifecycleOwner) { records ->
            if (records.isNotEmpty()) {
                pendingRecords.clear()
                pendingRecords.addAll(records.filter { it.latitude != null && it.longitude != null })
                if (::googleMap.isInitialized) {
                    displayRecordsOnMap(pendingRecords)
                    setupSearch()
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