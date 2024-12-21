package com.example.safesound.ui.my_records

import androidx.lifecycle.*
import com.example.safesound.data.my_records.CreateRecordResponse
import com.example.safesound.data.my_records.RecordsRepository
import com.example.safesound.data.my_records.UploadChunkResponse
import com.example.safesound.data.my_records.Record
import com.example.safesound.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MyRecordsViewModel @Inject constructor(
    private val recordsRepository: RecordsRepository
) : ViewModel() {

    private val _createRecordResult = MutableLiveData<Result<CreateRecordResponse>>()
    val createRecordResult: LiveData<Result<CreateRecordResponse>> get() = _createRecordResult

    private val _uploadChunkResult = MutableLiveData<Result<UploadChunkResponse>>()
    val uploadChunkResult: LiveData<Result<UploadChunkResponse>> get() = _uploadChunkResult

    private val _allRecordsResult = MutableLiveData<Result<List<Record>>>()
    val allRecordsResult: LiveData<Result<List<Record>>> get() = _allRecordsResult

    fun createRecord(name: String, imageFile: File?) {
        viewModelScope.launch {
            val result = recordsRepository.createRecord(name, imageFile)
            _createRecordResult.postValue(result)
        }
    }

    fun uploadRecordChunk(recordId: String, audioFile: MultipartBody.Part, startTime: String, endTime: String) {
        viewModelScope.launch {
            val result = recordsRepository.uploadRecordChunk(recordId, audioFile, startTime, endTime)
            _uploadChunkResult.postValue(result)
        }
    }

    fun fetchAllRecords() {
        viewModelScope.launch {
            val result = recordsRepository.getAllRecords()
            _allRecordsResult.postValue(result)
        }
    }
}