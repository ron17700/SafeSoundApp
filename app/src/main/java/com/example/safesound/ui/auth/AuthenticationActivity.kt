package com.example.safesound.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safesound.R
import com.example.safesound.data.AuthRepository
import com.example.safesound.ui.main.MainActivity

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_authentication)

        val authRepository = AuthRepository(this)
        val authViewModelFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]

        authViewModel.authState.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navigateToMainActivity()
            } else {
                showLoginPage()
            }
        }

        authViewModel.checkUserLoggedIn()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoginPage() {
        if (supportFragmentManager.findFragmentById(R.id.auth_container) !is LoginFragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .commit()
        }
    }
}
