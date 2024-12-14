package com.example.safesound.ui.my_records

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyRecordsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is my records Fragment"
    }
    val text: LiveData<String> = _text
}