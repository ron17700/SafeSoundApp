package com.example.safesound.ui.auth

import android.net.Uri
import androidx.lifecycle.*
import com.example.safesound.data.auth.AuthRepository
import com.example.safesound.data.auth.AuthResponse
import com.example.safesound.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> get() = _authState

    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> get() = _loginResult

    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> get() = _registerResult

    private val _logoutResult = MutableLiveData<Result<AuthResponse>>()
    val logoutResult: LiveData<Result<AuthResponse>> get() = _logoutResult

    fun checkUserLoggedIn() {
        _authState.postValue(authRepository.isUserLoggedIn())
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _loginResult.postValue(result)
            if (result.success) {
                _authState.postValue(true)
            }
        }
    }

    fun register(userName: String, email: String, password: String, profileImage: Uri?) {
        viewModelScope.launch {
            val result = authRepository.register(userName, email, password, profileImage)
            _registerResult.postValue(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            val result = authRepository.logout()
            _logoutResult.postValue(result)
            if (result.success) {
                _authState.postValue(false)
            }
        }
    }
}