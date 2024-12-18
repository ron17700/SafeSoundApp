package com.example.safesound.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.safesound.R
import com.example.safesound.data.AuthRepository
import com.example.safesound.databinding.FragmentLoginBinding
import com.example.safesound.ui.main.MainActivity

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val factory = AuthViewModelFactory(AuthRepository(requireContext()))
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                authViewModel.login(email, password)
            }
        }

        authViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            if (result.success) {
                requireActivity().finish()
                startActivity(Intent(requireContext(), MainActivity::class.java))
            } else {
                Toast.makeText(requireContext(), result.errorMessage ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
