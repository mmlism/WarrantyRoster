package com.xeniac.warrantyroster_manager.landingactivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import com.xeniac.warrantyroster_manager.R
import com.xeniac.warrantyroster_manager.databinding.ActivityLandingBinding
import com.xeniac.warrantyroster_manager.mainactivity.MainActivity
import com.xeniac.warrantyroster_manager.util.Constants.Companion.PREFERENCE_IS_LOGGED_IN_KEY
import com.xeniac.warrantyroster_manager.util.Constants.Companion.PREFERENCE_LOGIN
import com.xeniac.warrantyroster_manager.util.LocaleModifier

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen()
    }

    private fun splashScreen() {
        installSplashScreen()

        val loginPrefs = getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE)
        val isLoggedIn = loginPrefs.getBoolean(PREFERENCE_IS_LOGGED_IN_KEY, false)

        if (isLoggedIn) {
            Intent(this, MainActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        } else {
            landingInit()
        }
    }

    private fun landingInit() {
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LocaleModifier.setLocale(this)
        setTitle()
    }

    private fun setTitle() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.fcv.id) as NavHostFragment

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            destination.label?.let {
                when (it) {
                    "LoginFragment" -> {
                        binding.tvTitle.text = getString(R.string.login_text_title)
                    }
                    "RegisterFragment" -> {
                        binding.tvTitle.text = getString(R.string.register_text_title)
                    }
                    "ForgotPwFragment" -> {
                        binding.tvTitle.text = getString(R.string.forgot_pw_text_title)
                    }
                    "ForgotPwSentFragment" -> {
                        binding.tvTitle.text = getString(R.string.forgot_pw_sent_text_title)
                    }
                }
            }
        }
    }
}