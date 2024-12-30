package com.example.safesound.ui.records_list

import android.net.Uri
import androidx.lifecycle.*
import com.example.safesound.data.records.Chunk
import com.example.safesound.data.records.RecordsRepository
import com.example.safesound.data.records.Record
import com.example.safesound.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okio.Okio
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val recordsRepository: RecordsRepository
) : ViewModel() {

    private val _createRecordResult = MutableLiveData<Result<Record>>()
    val createRecordResult: LiveData<Result<Record>> get() = _createRecordResult

    private val _updateRecordResult = MutableLiveData<Result<Record>>()
    val updateRecordResult: LiveData<Result<Record>> get() = _updateRecordResult

    private val _deleteRecordResult = MutableLiveData<Result<Okio>?>()
    val deleteRecordResult: LiveData<Result<Okio>?> get() = _deleteRecordResult

    private val _allChunksResult = MutableLiveData<Result<List<Chunk>>>()
    val allChunksResult: LiveData<Result<List<Chunk>>> get() = _allChunksResult

    private val _allRecordsResult = MutableLiveData<Result<List<Record>>>()
    val allRecordsResult: LiveData<Result<List<Record>>> get() = _allRecordsResult

    fun createRecord(name: String, isPublic: Boolean, imageFile: Uri?) {
        viewModelScope.launch {
            val result = recordsRepository.createRecord(name, isPublic, imageFile)
            _createRecordResult.postValue(result)
        }
    }

    fun updateRecord(recordId: String, recordName: String, isPublic: Boolean, imageUri: Uri?) {
        viewModelScope.launch {
            val result = recordsRepository.updateRecord(recordId, recordName, isPublic, imageUri)
            _updateRecordResult.postValue(result)
        }
    }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            val result = recordsRepository.deleteRecord(recordId)
            _deleteRecordResult.postValue(result)
        }
    }

    fun fetchAllChunks(recordId: String) {
        viewModelScope.launch {
            val result = recordsRepository.getAllChunks(recordId)
            _allChunksResult.postValue(result)
        }
    }

    fun fetchAllRecords(isMyRecords: Boolean) {
        viewModelScope.launch {
            val result = recordsRepository.getAllRecords()
            _allRecordsResult.postValue(result)
        }
    }

    fun clearDeleteRecordResult() {
        _deleteRecordResult.value = null
    }
}