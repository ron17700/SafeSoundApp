package com.example.safesound.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.safesound.R
import com.example.safesound.databinding.ActivityMainBinding
import com.example.safesound.network.NetworkModule
import com.example.safesound.ui.auth.AuthViewModel
import com.example.safesound.ui.auth.AuthenticationActivity
import com.example.safesound.ui.records.RecordCreationDialogFragment
import com.example.safesound.ui.user.UsersViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val authViewModel: AuthViewModel by viewModels()
    private val usersViewModel: UsersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            val dialog = RecordCreationDialogFragment()
            dialog.show(supportFragmentManager, "RecordCreationDialog")
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_records_list,
                R.id.nav_shared_records,
                R.id.nav_records_map,
                R.id.nav_user_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevelDestination =
                appBarConfiguration.topLevelDestinations.contains(destination.id)
            if (isTopLevelDestination) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            val shouldShowAddRecordButton = destination.id == R.id.nav_records_list
            if (!shouldShowAddRecordButton) {
                binding.appBarMain.fab.visibility = View.GONE
            }
            else {
                binding.appBarMain.fab.visibility = View.VISIBLE
            }
        }
        usersViewModel.getCurrentUser()
        observeViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                authViewModel.logoutResult.observe(this) { result ->
                    if (result.success) {
                        val intent = Intent(this, AuthenticationActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    } else {
                        Snackbar.make(
                            binding.root,
                            result.errorMessage ?: "Logout failed",
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("Action", null)
                            .show()
                    }
                }
                authViewModel.logout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeViewModel() {
        val headerView = binding.navView.getHeaderView(0)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.email)
        val userNameTextView = headerView.findViewById<TextView>(R.id.username)
        val profileImageView = headerView.findViewById<ImageView>(R.id.profile_image)
        usersViewModel.userResult.observe(this) { result ->
            if (result.success && result.data != null) {
                val currentUser = result.data
                if (userNameTextView.text.toString() != currentUser.userName) {
                    userNameTextView.text = currentUser.userName
                }

                if (userEmailTextView.text.toString() != currentUser.email) {
                    userEmailTextView.text = currentUser.email
                }
                val profileImageUrl = NetworkModule.BASE_URL + currentUser.profileImage
                if (profileImageView.tag != profileImageUrl) {
                    profileImageView.tag = profileImageUrl
                    Picasso.get()
                        .load(profileImageUrl)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_broken_image)
                        .into(profileImageView)
                }
            } else {
                userNameTextView.visibility = View.GONE
                userEmailTextView.visibility = View.GONE
                profileImageView.visibility = View.GONE
            }
        }
    }
}