package com.xeniac.warrantyroster_manager.ui.viewmodels

import android.os.Build
import android.util.LayoutDirection
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.text.layoutDirection
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.xeniac.warrantyroster_manager.R
import com.xeniac.warrantyroster_manager.domain.repository.PreferencesRepository
import com.xeniac.warrantyroster_manager.domain.repository.UserRepository
import com.xeniac.warrantyroster_manager.utils.Constants.LOCALE_ENGLISH_GREAT_BRITAIN
import com.xeniac.warrantyroster_manager.utils.Constants.LOCALE_ENGLISH_UNITED_STATES
import com.xeniac.warrantyroster_manager.utils.Constants.LOCALE_INDEX_ENGLISH_GREAT_BRITAIN
import com.xeniac.warrantyroster_manager.utils.Constants.LOCALE_INDEX_ENGLISH_UNITED_STATES
import com.xeniac.warrantyroster_manager.utils.Constants.LOCALE_INDEX_PERSIAN_IRAN
import com.xeniac.warrantyroster_manager.utils.Constants.LOCALE_PERSIAN_IRAN
import com.xeniac.warrantyroster_manager.utils.Event
import com.xeniac.warrantyroster_manager.utils.Resource
import com.xeniac.warrantyroster_manager.utils.SettingsHelper
import com.xeniac.warrantyroster_manager.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _currentLanguageLiveData: MutableLiveData<Event<UiText>> = MutableLiveData()
    val currentLanguageLiveData: LiveData<Event<UiText>> = _currentLanguageLiveData

    private val _currentLocaleIndexLiveData: MutableLiveData<Event<Int>> = MutableLiveData()
    val currentLocaleIndexLiveData: LiveData<Event<Int>> = _currentLocaleIndexLiveData

    private val _changeCurrentLocaleLiveData: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val changeCurrentLocaleLiveData: LiveData<Event<Boolean>> = _changeCurrentLocaleLiveData

    private val _currentAppThemeLiveData: MutableLiveData<Event<Int>> = MutableLiveData()
    val currentAppThemeLiveData: LiveData<Event<Int>> = _currentAppThemeLiveData

    private val _accountDetailsLiveData:
            MutableLiveData<Event<Resource<FirebaseUser>>> = MutableLiveData()
    val accountDetailsLiveData: LiveData<Event<Resource<FirebaseUser>>> = _accountDetailsLiveData

    private val _sendVerificationEmailLiveData:
            MutableLiveData<Event<Resource<Nothing>>> = MutableLiveData()
    val sendVerificationEmailLiveData:
            LiveData<Event<Resource<Nothing>>> = _sendVerificationEmailLiveData

    private val _logoutLiveData: MutableLiveData<Event<Resource<Nothing>>> = MutableLiveData()
    val logoutLiveData: LiveData<Event<Resource<Nothing>>> = _logoutLiveData

    fun getCurrentLanguage() = viewModelScope.launch {
        safeGetCurrentLanguage()
    }

    private fun safeGetCurrentLanguage() {
        val localeList = AppCompatDelegate.getApplicationLocales()

        if (localeList.isEmpty) {
            changeCurrentLocale(0)
            Timber.i("Locale list is Empty.")
        } else {
            val localeString = localeList[0].toString()
            Timber.i("Current language is $localeString")

            when (localeString) {
                "en_US" -> {
                    _currentLanguageLiveData.postValue(
                        Event(UiText.StringResource(R.string.settings_text_settings_language_english_us))
                    )
                    _currentLocaleIndexLiveData.postValue(Event(LOCALE_INDEX_ENGLISH_UNITED_STATES))
                    Timber.i("Current locale index is 0 (en_US).")
                }
                "en_GB" -> {
                    _currentLanguageLiveData.postValue(
                        Event(UiText.StringResource(R.string.settings_text_settings_language_english_gb))
                    )
                    _currentLocaleIndexLiveData.postValue(Event(LOCALE_INDEX_ENGLISH_GREAT_BRITAIN))
                    Timber.i("Current locale index is 1 (en_GB).")
                }
                "fa_IR" -> {
                    _currentLanguageLiveData.postValue(
                        Event(UiText.StringResource(R.string.settings_text_settings_language_persian_ir))
                    )
                    _currentLocaleIndexLiveData.postValue(Event(LOCALE_INDEX_PERSIAN_IRAN))
                    Timber.i("Current locale index is 2 (fa_IR).")
                }
                else -> {
                    changeCurrentLocale(0)
                    Timber.i("Current language is System Default.")
                }
            }
        }
    }

    fun getCurrentAppTheme() = viewModelScope.launch {
        safeGetCurrentAppTheme()
    }

    private suspend fun safeGetCurrentAppTheme() {
        _currentAppThemeLiveData.postValue(Event(preferencesRepository.getCurrentAppTheme()))
    }

    fun changeCurrentLocale(index: Int) = viewModelScope.launch {
        safeChangeCurrentLocale(index)
    }

    private suspend fun safeChangeCurrentLocale(index: Int) {
        var isActivityRestartNeeded = false

        when (index) {
            0 -> {
                preferencesRepository.setCategoryTitleMapKey(LOCALE_ENGLISH_UNITED_STATES)
                isActivityRestartNeeded = isActivityRestartNeeded(LayoutDirection.LTR)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(LOCALE_ENGLISH_UNITED_STATES)
                )
            }
            1 -> {
                preferencesRepository.setCategoryTitleMapKey(LOCALE_ENGLISH_GREAT_BRITAIN)
                isActivityRestartNeeded = isActivityRestartNeeded(LayoutDirection.LTR)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(LOCALE_ENGLISH_GREAT_BRITAIN)
                )
            }
            2 -> {
                preferencesRepository.setCategoryTitleMapKey(LOCALE_PERSIAN_IRAN)
                isActivityRestartNeeded = isActivityRestartNeeded(LayoutDirection.RTL)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(LOCALE_PERSIAN_IRAN)
                )
            }
        }

        _changeCurrentLocaleLiveData.postValue(Event(isActivityRestartNeeded))
        Timber.i("isActivityRestartNeeded = $isActivityRestartNeeded}")
        Timber.i("App locale index changed to $index")
    }

    fun changeCurrentTheme(index: Int) = viewModelScope.launch {
        safeChangeCurrentTheme(index)
    }

    private suspend fun safeChangeCurrentTheme(index: Int) {
        preferencesRepository.setCurrentAppTheme(index)
        _currentAppThemeLiveData.postValue(Event(index))
        SettingsHelper.setAppTheme(index)
    }

    fun getAccountDetails() = viewModelScope.launch {
        safeGetAccountDetails()
    }

    private suspend fun safeGetAccountDetails() {
        _accountDetailsLiveData.postValue(Event(Resource.Loading()))
        try {
            val currentUser = userRepository.getCurrentUser() as FirebaseUser
            var email = currentUser.email
            var isVerified = currentUser.isEmailVerified
            _accountDetailsLiveData.postValue(Event(Resource.Success(currentUser)))
            Timber.i("Current user is $email and isVerified: $isVerified")

            userRepository.reloadCurrentUser()
            if (email != currentUser.email || isVerified != currentUser.isEmailVerified) {
                email = currentUser.email
                isVerified = currentUser.isEmailVerified
                _accountDetailsLiveData.postValue(Event(Resource.Success(currentUser)))
                Timber.i("Updated user is $email and isVerified: $isVerified")
            }
        } catch (e: Exception) {
            Timber.e("safeGetAccountDetails Exception: ${e.message}")
            _accountDetailsLiveData.postValue(Event(Resource.Error(UiText.DynamicString(e.message.toString()))))
        }
    }

    fun sendVerificationEmail() = viewModelScope.launch {
        safeSendVerificationEmail()
    }

    private suspend fun safeSendVerificationEmail() {
        _sendVerificationEmailLiveData.postValue(Event(Resource.Loading()))
        try {
            userRepository.sendVerificationEmail()
            _sendVerificationEmailLiveData.postValue(Event(Resource.Success()))
            Timber.i("Verification email sent.")
        } catch (e: Exception) {
            Timber.e("safeSendVerificationEmail Exception: ${e.message}")
            _sendVerificationEmailLiveData.postValue(Event(Resource.Error(UiText.DynamicString(e.message.toString()))))
        }
    }

    fun logoutUser() = viewModelScope.launch {
        safeLogoutUser()
    }

    private suspend fun safeLogoutUser() {
        _logoutLiveData.postValue(Event(Resource.Loading()))
        try {
            userRepository.logoutUser()
            preferencesRepository.isUserLoggedIn(false)
            _logoutLiveData.postValue(Event(Resource.Success()))
            Timber.i("User successfully logged out.")
        } catch (e: Exception) {
            Timber.e("safeLogoutUser Exception: ${e.message}")
            _logoutLiveData.postValue(Event(Resource.Error(UiText.DynamicString(e.message.toString()))))
        }
    }

    private fun isActivityRestartNeeded(newLayoutDirection: Int): Boolean {
        val currentLocale = AppCompatDelegate.getApplicationLocales()[0]
        val currentLayoutDirection = currentLocale?.layoutDirection

        return if (Build.VERSION.SDK_INT >= 33) {
            false
        } else currentLayoutDirection != newLayoutDirection
    }
}