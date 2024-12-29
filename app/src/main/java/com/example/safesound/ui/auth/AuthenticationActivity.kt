package com.example.safesound.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.safesound.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_authentication)

        val navController = findNavController(R.id.auth_nav_host_fragment)

        authViewModel.authState.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navController.navigate(R.id.action_loginFragment_to_mainActivity)
            }
        }

        authViewModel.checkUserLoggedIn()
    }
}

