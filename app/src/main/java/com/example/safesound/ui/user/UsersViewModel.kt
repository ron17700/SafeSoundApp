package com.example.safesound.ui.user;

import android.net.Uri
import androidx.lifecycle.*
import com.example.safesound.data.user.User
import com.example.safesound.data.user.UsersRepository
import com.example.safesound.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
        private val userRepository: UsersRepository
) : ViewModel() {

    private val _updateUserResult = MutableLiveData<Result<User>?>()
    val updateUserResult: LiveData<Result<User>?> get() = _updateUserResult

    private val _userResult = MutableLiveData<Result<User>>()
    val userResult: LiveData<Result<User>> get() = _userResult

    fun getCurrentUser() {
        viewModelScope.launch {
            val result = userRepository.getCurrentUser()
            _userResult.postValue(result)
        }
    }

    fun updateCurrentUser(userName: String, profileImage: Uri?) {
        viewModelScope.launch {
            val result = userRepository.updateCurrentUser(userName, profileImage)
            _updateUserResult.postValue(result)
            _userResult.postValue(result)
        }
    }

    fun clearUpdateUserResult() {
        _updateUserResult.value = null
    }
}