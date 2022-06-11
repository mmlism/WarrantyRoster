package com.xeniac.warrantyroster_manager.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.xeniac.warrantyroster_manager.di.DefaultPreferencesRepository
import com.xeniac.warrantyroster_manager.repositories.PreferencesRepository
import com.xeniac.warrantyroster_manager.utils.SettingsHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {

    @DefaultPreferencesRepository
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PreferencesRepositoryProviderEntryPoint {
        fun getPreferencesRepository(): PreferencesRepository
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            val preferencesRepositoryProviderEntryPoint = EntryPointAccessors
                .fromApplication(it, PreferencesRepositoryProviderEntryPoint::class.java)

            val preferencesRepository =
                preferencesRepositoryProviderEntryPoint.getPreferencesRepository()

            val currentAppLanguage = preferencesRepository.getCurrentAppLanguageSynchronously()
            val currentAppCountry = preferencesRepository.getCurrentAppCountrySynchronously()

            val localeUpdatedContext = SettingsHelper.setAppLocale(
                it, currentAppLanguage, currentAppCountry
            )

            super.attachBaseContext(localeUpdatedContext)
        }
    }
}