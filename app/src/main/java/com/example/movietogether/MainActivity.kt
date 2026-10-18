package com.example.movietogether

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.movietogether.data.Session
import com.example.movietogether.databinding.ActivityMainBinding
import com.example.movietogether.presentation.auth.LoginFragment
import com.example.movietogether.presentation.collections.CollectionsFragment
import com.example.movietogether.presentation.home.HomeFragment
import com.example.movietogether.presentation.profile.ProfileFragment
import com.example.movietogether.presentation.search.SearchFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.postDelayed({
            keepSplash = false
        }, 800)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            if (Session.isLoggedIn) {
                openHome()
            } else {
                showAuth(LoginFragment())
            }
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            if (!Session.isLoggedIn) {
                showAuth(LoginFragment())
                return@setOnItemSelectedListener false
            }

            val fragment = when (item.itemId) {
                R.id.homeFragment -> HomeFragment()
                R.id.searchFragment -> SearchFragment()
                R.id.collectionsFragment -> CollectionsFragment()
                R.id.profileFragment -> ProfileFragment()
                else -> return@setOnItemSelectedListener false
            }
            showMain(fragment)
            true
        }
    }

    fun openHome() {
        binding.bottomNav.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()
        binding.bottomNav.selectedItemId = R.id.homeFragment
    }

    private fun showAuth(fragment: Fragment) {
        binding.bottomNav.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showMain(fragment: Fragment) {
        binding.bottomNav.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}