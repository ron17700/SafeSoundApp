package com.example.safesound.ui.user

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.safesound.R
import com.example.safesound.databinding.FragmentUserProfileBinding
import com.example.safesound.network.NetworkModule
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val usersViewModel: UsersViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Picasso.get()
                .load(uri)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .into(binding.buttonChangeProfileImage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonChangeProfileImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.buttonUpdate.setOnClickListener {
            val updatedUsername = binding.editTextUsername.text.toString().trim()
            if (updatedUsername.isEmpty()) {
                binding.editTextUsername.error = "Username cannot be empty"
                return@setOnClickListener
            }

            usersViewModel.updateCurrentUser(updatedUsername, selectedImageUri)
        }

        usersViewModel.getCurrentUser()
    }

    private fun observeViewModel() {
        usersViewModel.updateUserResult.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe
            binding.textViewEmail.text = "Email: ${user.data?.email}"
            binding.textViewRole.text = "Role: ${user.data?.role}"
            binding.editTextUsername.setText(user.data?.userName)

            if (user.data?.profileImage.toString().isNotEmpty()) {
                Picasso.get()
                    .load(NetworkModule.BASE_URL + user.data?.profileImage)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_broken_image)
                    .into(binding.buttonChangeProfileImage)
            }
        }

        usersViewModel.userResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                binding.editTextUsername.setText(result.data?.userName)
                binding.textViewEmail.text = result.data?.email
                binding.textViewRole.text = result.data?.role

                Picasso.get()
                    .load(NetworkModule.BASE_URL + result.data?.profileImage)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_broken_image)
                    .into(binding.buttonChangeProfileImage)
            }
        }

        usersViewModel.updateUserResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            if (result.success) {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
            usersViewModel.clearUpdateUserResult()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
