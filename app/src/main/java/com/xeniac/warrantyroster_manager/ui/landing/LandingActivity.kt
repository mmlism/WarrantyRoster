package com.xeniac.warrantyroster_manager.ui.landing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import com.xeniac.warrantyroster_manager.R
import com.xeniac.warrantyroster_manager.databinding.ActivityLandingBinding
import com.xeniac.warrantyroster_manager.ui.main.MainActivity
import com.xeniac.warrantyroster_manager.utils.Constants.PREFERENCE_IS_LOGGED_IN_KEY
import com.xeniac.warrantyroster_manager.utils.Constants.PREFERENCE_LOGIN
import com.xeniac.warrantyroster_manager.utils.LocaleModifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen()
    }

    private fun splashScreen() {
        installSplashScreen()

        if (isUserLoggedIn()) {
            Intent(this, MainActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        } else {
            landingInit()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val loginPrefs = getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE)
        return loginPrefs.getBoolean(PREFERENCE_IS_LOGGED_IN_KEY, false)
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
            when (destination.id) {
                R.id.loginFragment -> {
                    binding.tvTitle.text = getString(R.string.login_text_title)
                }
                R.id.registerFragment -> {
                    binding.tvTitle.text = getString(R.string.register_text_title)
                }
                R.id.forgotPwFragment -> {
                    binding.tvTitle.text = getString(R.string.forgot_pw_text_title)
                }
                R.id.forgotPwSentFragment -> {
                    binding.tvTitle.text = getString(R.string.forgot_pw_sent_text_title)
                }
            }
        }
    }
}