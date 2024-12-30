package com.example.safesound.ui.main;

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

    private val _userResult = MutableLiveData<Result<User>>()
    val userResult: LiveData<Result<User>> get() = _userResult

    fun getCurrentUser() {
        viewModelScope.launch {
            val result = userRepository.getCurrentUser()
            _userResult.postValue(result)
        }
    }
}