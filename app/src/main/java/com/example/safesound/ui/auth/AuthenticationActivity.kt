package com.example.safesound.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.safesound.R
import com.example.safesound.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_authentication)

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
