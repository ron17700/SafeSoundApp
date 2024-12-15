package com.example.safesound.ui.shared_records

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedRecordsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is shared with me Fragment"
    }
    val text: LiveData<String> = _text
}