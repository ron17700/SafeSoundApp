package com.example.safesound.ui.auth

import android.net.Uri
import androidx.lifecycle.*
import com.example.safesound.data.AuthRepository
import com.example.safesound.utils.Result
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> get() = _authState

    private val _loginResult = MutableLiveData<Result>()
    val loginResult: LiveData<Result> get() = _loginResult

    private val _registerResult = MutableLiveData<Result>()
    val registerResult: LiveData<Result> get() = _registerResult

    private val _logoutResult = MutableLiveData<Result>()
    val logoutResult: LiveData<Result> get() = _logoutResult

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

    fun register(email: String, password: String, profileImage: Uri?) {
        viewModelScope.launch {
            val result = authRepository.register(email, password, profileImage)
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

class AuthViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
