package com.example.safesound.ui.records

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
import androidx.lifecycle.ViewModel
import com.example.safesound.data.user.User

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

    private val _allRecordsResult = MutableLiveData<List<Record>>()
    val allRecordsResult: LiveData<List<Record>> get() = _allRecordsResult

    private val _likeRecordsResult = MutableLiveData<Result<Okio>>()
    val likeRecordsResult: LiveData<Result<Okio>> get() = _likeRecordsResult

    fun createRecord(name: String, isPublic: Boolean, latitude: Double?, longitude: Double?, imageFile: Uri?) {
        viewModelScope.launch {
            val result = recordsRepository.createRecord(name, isPublic, latitude, longitude, imageFile)
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

    fun likeRecord(recordId: String) {
        viewModelScope.launch {
            val result = recordsRepository.likeRecord(recordId)
            _likeRecordsResult.postValue(result)
        }
    }

    fun fetchAllChunks(recordId: String) {
        viewModelScope.launch {
            val result = recordsRepository.getAllChunks(recordId)
            _allChunksResult.postValue(result)
        }
    }

    fun fetchAllRecords(isMyRecords: Boolean, refresh: Boolean = false) {
        viewModelScope.launch {
            val result = recordsRepository.getAllRecordsCached(isMyRecords, refresh)
            val records = result.map { recordEntity ->
                Record(recordEntity.id, recordEntity.name, recordEntity.createdAt, recordEntity.recordClass, recordEntity.public, recordEntity.favorite, User(recordEntity.userId, "", "", "", ""), recordEntity.latitude, recordEntity.longitude, recordEntity.image)
            }
            _allRecordsResult.postValue(records)
        }
    }

    fun clearDeleteRecordResult() {
        _deleteRecordResult.value = null
    }
}