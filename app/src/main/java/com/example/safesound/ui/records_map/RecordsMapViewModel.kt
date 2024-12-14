package com.example.safesound.ui.records_map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordsMapViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is records map Fragment"
    }
    val text: LiveData<String> = _text
}