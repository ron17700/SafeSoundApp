package com.example.safesound.ui.auth

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.safesound.data.AuthRepository
import com.example.safesound.utils.Result
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result>()
    val loginResult: LiveData<Result> get() = _loginResult

    private val _registrationResult = MutableLiveData<Result>()
    val registrationResult: LiveData<Result> get() = _registrationResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _loginResult.postValue(result)
        }
    }

    fun register(email: String, password: String, profileImageUri: Uri?) {
        viewModelScope.launch {
            val result = authRepository.register(email, password, profileImageUri)
            _registrationResult.postValue(result)
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