package com.xeniac.warrantyroster_manager.authentication.presentation.login

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.xeniac.warrantyroster_manager.BuildConfig
import com.xeniac.warrantyroster_manager.R
import com.xeniac.warrantyroster_manager.core.data.repository.NetworkConnectivityObserver
import com.xeniac.warrantyroster_manager.core.domain.repository.ConnectivityObserver
import com.xeniac.warrantyroster_manager.core.presentation.main.MainActivity
import com.xeniac.warrantyroster_manager.databinding.FragmentLoginBinding
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_FIREBASE_403
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_FIREBASE_AUTH_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIALS
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_FIREBASE_AUTH_ACCOUNT_NOT_FOUND
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_FIREBASE_AUTH_CREDENTIALS
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_FIREBASE_DEVICE_BLOCKED
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_GOOGLE_SIGN_IN_API_NOT_AVAILABLE
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_GOOGLE_SIGN_IN_CLIENT_CANCELED
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_GOOGLE_SIGN_IN_CLIENT_OFFLINE
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_INPUT_BLANK_EMAIL
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_INPUT_BLANK_PASSWORD
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_INPUT_EMAIL_INVALID
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_NETWORK_CONNECTION
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_TWITTER_O_AUTH_PROVIDER_CANCELED
import com.xeniac.warrantyroster_manager.util.Constants.ERROR_TWITTER_O_AUTH_PROVIDER_NETWORK_CONNECTION
import com.xeniac.warrantyroster_manager.util.Constants.FIREBASE_AUTH_PROVIDER_ID_TWITTER
import com.xeniac.warrantyroster_manager.util.Constants.SAVE_INSTANCE_LOGIN_EMAIL
import com.xeniac.warrantyroster_manager.util.Constants.SAVE_INSTANCE_LOGIN_PASSWORD
import com.xeniac.warrantyroster_manager.util.Resource
import com.xeniac.warrantyroster_manager.util.SnackBarHelper.show403Error
import com.xeniac.warrantyroster_manager.util.SnackBarHelper.showActionSnackbarError
import com.xeniac.warrantyroster_manager.util.SnackBarHelper.showFirebaseDeviceBlockedError
import com.xeniac.warrantyroster_manager.util.SnackBarHelper.showIntentAppNotFoundError
import com.xeniac.warrantyroster_manager.util.SnackBarHelper.showNetworkFailureError
import com.xeniac.warrantyroster_manager.util.SnackBarHelper.showNormalSnackbarError
import com.xeniac.warrantyroster_manager.util.SnackBarHelper.showSomethingWentWrongError
import com.xeniac.warrantyroster_manager.util.SnackBarHelper.showUnavailableNetworkConnectionError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    var viewModel: LoginViewModel? = null
) : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    val binding get() = _binding!!

    private lateinit var currentAppLanguage: String

    private lateinit var connectivityObserver: ConnectivityObserver
    private var networkStatus: ConnectivityObserver.Status = ConnectivityObserver.Status.UNAVAILABLE

    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        viewModel = viewModel ?: ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        connectivityObserver = NetworkConnectivityObserver(requireContext())

        networkConnectivityObserver()
        textInputsBackgroundColor()
        textInputsStrokeColor()
        subscribeToObservers()
        getCurrentAppLanguage()
        forgotPwOnClick()
        registerOnClick()
        loginOnClick()
        loginActionDone()
        googleOnClick()
        twitterOnClick()
        facebookOnClick()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackbar?.dismiss()
        _binding = null
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            requireActivity().finishAffinity()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        _binding?.let {
            val email = binding.tiEditEmail.text.toString().trim().lowercase(Locale.US)
            val password = binding.tiEditPassword.text.toString().trim()

            if (email.isNotBlank()) {
                outState.putString(SAVE_INSTANCE_LOGIN_EMAIL, email)
            }

            if (password.isNotBlank()) {
                outState.putString(SAVE_INSTANCE_LOGIN_PASSWORD, password)
            }
        }

        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            it.getString(SAVE_INSTANCE_LOGIN_EMAIL)?.let { restoredEmail ->
                binding.tiEditEmail.setText(restoredEmail)
            }

            it.getString(SAVE_INSTANCE_LOGIN_PASSWORD)?.let { restoredPassword ->
                binding.tiEditPassword.setText(restoredPassword)
            }
        }
        super.onViewStateRestored(savedInstanceState)
    }

    private fun networkConnectivityObserver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityObserver.observe().onEach {
                networkStatus = it
                Timber.i("Network connectivity status inside of observer is $it")
            }.launchIn(lifecycleScope)
        } else {
            networkStatus = ConnectivityObserver.Status.AVAILABLE
        }
    }

    private fun textInputsBackgroundColor() = binding.apply {
        tiEditEmail.setOnFocusChangeListener { _, isFocused ->
            if (isFocused) {
                tiLayoutEmail.boxBackgroundColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.background
                )
            } else {
                tiLayoutEmail.boxBackgroundColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.grayLight
                )
            }
        }

        tiEditPassword.setOnFocusChangeListener { _, isFocused ->
            if (isFocused) {
                tiLayoutPassword.boxBackgroundColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.background
                )
            } else {
                tiLayoutPassword.boxBackgroundColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.grayLight
                )
            }
        }
    }

    private fun textInputsStrokeColor() = binding.apply {
        tiEditEmail.addTextChangedListener {
            tiLayoutEmail.isErrorEnabled = false
            tiLayoutEmail.boxStrokeColor = ContextCompat.getColor(
                requireContext(),
                R.color.blue
            )
        }

        tiEditPassword.addTextChangedListener {
            tiLayoutPassword.isErrorEnabled = false
            tiLayoutPassword.boxStrokeColor = ContextCompat.getColor(
                requireContext(),
                R.color.blue
            )
        }
    }

    private fun subscribeToObservers() {
        currentAppLanguageObserver()
        loginWithEmailObserver()
        loginWithGoogleAccountObserver()
        loginWithTwitterAccountObserver()
        loginWithFacebookAccountObserver()
    }

    private fun getCurrentAppLanguage() = viewModel!!.getCurrentAppLanguage()

    private fun currentAppLanguageObserver() =
        viewModel!!.currentAppLanguageLiveData.observe(viewLifecycleOwner) { responseEvent ->
            responseEvent.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        response.data?.let { language ->
                            currentAppLanguage = language
                        }
                    }
                    is Resource.Error -> {
                        snackbar = showSomethingWentWrongError(requireContext(), requireView())
                    }
                }
            }
        }

    private fun forgotPwOnClick() = binding.btnForgotPw.setOnClickListener {
        navigateToForgotPw()
    }

    private fun navigateToForgotPw() = findNavController().navigate(
        LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()
    )

    private fun registerOnClick() = binding.btnRegister.setOnClickListener {
        navigateToRegister()
    }

    private fun navigateToRegister() = findNavController().navigate(
        LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
    )

    private fun loginOnClick() = binding.btnLogin.setOnClickListener {
        validateLoginWithEmailInputs()
    }

    private fun loginActionDone() =
        binding.tiEditPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateLoginWithEmailInputs()
            }
            false
        }

    private fun validateLoginWithEmailInputs() {
        val inputMethodManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireView().applicationWindowToken, 0)

        if (networkStatus == ConnectivityObserver.Status.AVAILABLE) {
            val email = binding.tiEditEmail.text.toString().trim().lowercase(Locale.US)
            val password = binding.tiEditPassword.text.toString().trim()

            viewModel!!.validateLoginWithEmailInputs(email, password)
        } else {
            snackbar = showUnavailableNetworkConnectionError(
                requireContext(), requireView()
            ) { validateLoginWithEmailInputs() }
            Timber.e("validateLoginWithEmailInputs Error: Offline")
        }
    }

    private fun loginWithEmailObserver() =
        viewModel!!.loginWithEmailLiveData.observe(viewLifecycleOwner) { responseEvent ->
            responseEvent.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Loading -> showLoginWithEmailLoadingAnimation()
                    is Resource.Success -> {
                        hideLoginWithEmailLoadingAnimation()
                        navigateToMainActivity()
                    }
                    is Resource.Error -> {
                        hideLoginWithEmailLoadingAnimation()
                        response.message?.asString(requireContext())?.let {
                            when {
                                it.contains(ERROR_INPUT_BLANK_EMAIL) -> {
                                    binding.tiLayoutEmail.error =
                                        requireContext().getString(R.string.login_error_blank_email)
                                    binding.tiLayoutEmail.requestFocus()
                                }
                                it.contains(ERROR_INPUT_BLANK_PASSWORD) -> {
                                    binding.tiLayoutPassword.error =
                                        requireContext().getString(R.string.login_error_blank_password)
                                    binding.tiLayoutPassword.requestFocus()
                                }
                                it.contains(ERROR_INPUT_EMAIL_INVALID) -> {
                                    binding.tiLayoutEmail.requestFocus()
                                    binding.tiLayoutEmail.error =
                                        requireContext().getString(R.string.login_error_email)
                                }
                                it.contains(ERROR_NETWORK_CONNECTION) -> {
                                    snackbar = showNetworkFailureError(
                                        requireContext(), requireView()
                                    )
                                }
                                it.contains(ERROR_FIREBASE_403) -> {
                                    snackbar = show403Error(requireContext(), requireView())
                                }
                                it.contains(ERROR_FIREBASE_DEVICE_BLOCKED) -> {
                                    snackbar = showFirebaseDeviceBlockedError(
                                        requireContext(), requireView()
                                    )
                                }
                                it.contains(ERROR_FIREBASE_AUTH_ACCOUNT_NOT_FOUND) -> {
                                    snackbar = showActionSnackbarError(
                                        requireView(),
                                        requireContext().getString(R.string.login_error_not_found),
                                        requireContext().getString(R.string.login_btn_register)
                                    ) { navigateToRegister() }
                                }
                                it.contains(ERROR_FIREBASE_AUTH_CREDENTIALS) -> {
                                    snackbar = showNormalSnackbarError(
                                        requireView(),
                                        requireContext().getString(R.string.login_error_credentials)
                                    )
                                }
                                else -> {
                                    snackbar = showSomethingWentWrongError(
                                        requireContext(), requireView()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun navigateToMainActivity() = requireActivity().apply {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun googleOnClick() = binding.btnGoogle.setOnClickListener {
        loginWithGoogleAccount()
    }

    private fun loginWithGoogleAccount() {
        if (networkStatus == ConnectivityObserver.Status.AVAILABLE) {
            launchGoogleSignInClient()
        } else {
            snackbar = showUnavailableNetworkConnectionError(
                requireContext(), requireView()
            ) { loginWithGoogleAccount() }
            Timber.e("loginWithGoogleAccount Error: Offline")
        }
    }

    private fun launchGoogleSignInClient() = CoroutineScope(Dispatchers.Main).launch {
        try {
            showGoogleLoadingAnimation()
            val options = GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
            ).apply {
                requestIdToken(BuildConfig.GOOGLE_AUTH_SERVER_CLIENT_ID)
                requestId()
                requestEmail()
            }.build()

            val googleSignInClient = GoogleSignIn.getClient(requireContext(), options)
            googleSignInClient.signOut().await()

            loginWithGoogleAccountResultLauncher.launch(googleSignInClient.signInIntent)
        } catch (e: Exception) {
            hideGoogleLoadingAnimation()
            e.message?.let { message ->
                snackbar = if (message.contains(ERROR_GOOGLE_SIGN_IN_API_NOT_AVAILABLE)) {
                    showIntentAppNotFoundError(requireContext(), requireView())
                } else {
                    showSomethingWentWrongError(requireContext(), requireView())
                }
                Timber.e("launchGoogleSignInClient Exception: $message")
            }
        }
    }

    private val loginWithGoogleAccountResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
            account?.let { viewModel!!.loginWithGoogleAccount(account) }
        } catch (e: Exception) {
            hideGoogleLoadingAnimation()
            e.message?.let {
                when {
                    it.contains(ERROR_GOOGLE_SIGN_IN_CLIENT_CANCELED) -> {
                        /* NO-OP */
                    }
                    it.contains(ERROR_GOOGLE_SIGN_IN_CLIENT_OFFLINE) -> {
                        snackbar = showUnavailableNetworkConnectionError(
                            requireContext(), requireView()
                        ) { loginWithGoogleAccount() }
                    }
                    else -> {
                        snackbar = showSomethingWentWrongError(requireContext(), requireView())
                    }
                }
                Timber.e("loginWithGoogleAccountResultLauncher Exception: $it")
            }
        }
    }

    private fun loginWithGoogleAccountObserver() =
        viewModel!!.loginWithGoogleAccountLiveData.observe(viewLifecycleOwner) { responseEvent ->
            responseEvent.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Loading -> showGoogleLoadingAnimation()
                    is Resource.Success -> {
                        hideGoogleLoadingAnimation()
                        navigateToMainActivity()
                    }
                    is Resource.Error -> {
                        hideGoogleLoadingAnimation()
                        response.message?.asString(requireContext())?.let {
                            snackbar = when {
                                it.contains(ERROR_NETWORK_CONNECTION) -> {
                                    showNetworkFailureError(requireContext(), requireView())
                                }
                                it.contains(ERROR_FIREBASE_403) -> {
                                    show403Error(requireContext(), requireView())
                                }
                                it.contains(ERROR_FIREBASE_DEVICE_BLOCKED) -> {
                                    showFirebaseDeviceBlockedError(requireContext(), requireView())
                                }
                                it.contains(
                                    ERROR_FIREBASE_AUTH_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIALS
                                ) -> {
                                    showActionSnackbarError(
                                        requireView(),
                                        requireContext().getString(R.string.login_error_email_exists_with_different_credentials),
                                        requireContext().getString(R.string.error_btn_confirm)
                                    ) { snackbar?.dismiss() }
                                }
                                else -> {
                                    showSomethingWentWrongError(requireContext(), requireView())
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun twitterOnClick() = binding.btnTwitter.setOnClickListener {
        checkPendingLinkTwitterAccountAuthResult()
    }

    private fun checkPendingLinkTwitterAccountAuthResult() {
        if (networkStatus == ConnectivityObserver.Status.AVAILABLE) {
            showTwitterLoadingAnimation()

            val pendingAuthResult = firebaseAuth.pendingAuthResult

            if (pendingAuthResult != null) {
                pendingLoginWithTwitterAccountAuthResult(pendingAuthResult)
            } else {
                loginWithTwitterAccount()
            }
        } else {
            snackbar = showUnavailableNetworkConnectionError(
                requireContext(), requireView()
            ) { checkPendingLinkTwitterAccountAuthResult() }
            Timber.e("checkPendingLinkTwitterAccountAuthResult Error: Offline")
        }
    }

    private fun pendingLoginWithTwitterAccountAuthResult(pendingAuthResult: Task<AuthResult>) {
        pendingAuthResult.addOnCompleteListener { task ->
            handleLoginWithTwitterAccountAuthResult(task)
        }
    }

    private fun loginWithTwitterAccount() {
        val oAuthProvider = OAuthProvider.newBuilder(FIREBASE_AUTH_PROVIDER_ID_TWITTER)
        oAuthProvider.addCustomParameter("lang", currentAppLanguage)

        firebaseAuth.startActivityForSignInWithProvider(requireActivity(), oAuthProvider.build())
            .addOnCompleteListener { task ->
                handleLoginWithTwitterAccountAuthResult(task)
            }
    }

    private fun handleLoginWithTwitterAccountAuthResult(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            val authCredential = task.result.credential
            authCredential?.let { credential ->
                viewModel!!.loginWithTwitterAccount(credential)
            }
        } else {
            hideTwitterLoadingAnimation()
            task.exception?.message?.let { message ->
                Timber.e("handleLoginWithTwitterAccountAuthResult Exception: $message")
                if (message.contains(ERROR_TWITTER_O_AUTH_PROVIDER_CANCELED)) {
                    /* NO-OP */
                } else {
                    snackbar = when {
                        message.contains(ERROR_TWITTER_O_AUTH_PROVIDER_NETWORK_CONNECTION) -> {
                            showNetworkFailureError(requireContext(), requireView())
                        }
                        message.contains(
                            ERROR_FIREBASE_AUTH_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIALS
                        ) -> {
                            showActionSnackbarError(
                                requireView(),
                                requireContext().getString(R.string.login_error_email_exists_with_different_credentials),
                                requireContext().getString(R.string.error_btn_confirm)
                            ) { snackbar?.dismiss() }
                        }
                        else -> {
                            showSomethingWentWrongError(requireContext(), requireView())
                        }
                    }
                }
            }
        }
    }

    private fun loginWithTwitterAccountObserver() =
        viewModel!!.loginWithTwitterAccountLiveData.observe(viewLifecycleOwner) { responseEvent ->
            responseEvent.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Loading -> showTwitterLoadingAnimation()
                    is Resource.Success -> {
                        hideTwitterLoadingAnimation()
                        navigateToMainActivity()
                    }
                    is Resource.Error -> {
                        hideTwitterLoadingAnimation()
                        response.message?.asString(requireContext())?.let {
                            snackbar = when {
                                it.contains(ERROR_NETWORK_CONNECTION) -> {
                                    showNetworkFailureError(requireContext(), requireView())
                                }
                                it.contains(ERROR_FIREBASE_403) -> {
                                    show403Error(requireContext(), requireView())
                                }
                                it.contains(ERROR_FIREBASE_DEVICE_BLOCKED) -> {
                                    showFirebaseDeviceBlockedError(requireContext(), requireView())
                                }
                                it.contains(
                                    ERROR_FIREBASE_AUTH_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIALS
                                ) -> {
                                    showActionSnackbarError(
                                        requireView(),
                                        requireContext().getString(R.string.login_error_email_exists_with_different_credentials),
                                        requireContext().getString(R.string.error_btn_confirm)
                                    ) { snackbar?.dismiss() }
                                }
                                else -> {
                                    showSomethingWentWrongError(requireContext(), requireView())
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun facebookOnClick() = binding.btnFacebook.setOnClickListener {
        loginWithFacebookAccount()
    }

    private fun loginWithFacebookAccount() {
        if (networkStatus == ConnectivityObserver.Status.AVAILABLE) {
            launchFacebookLoginManager()
        } else {
            snackbar = showUnavailableNetworkConnectionError(
                requireContext(), requireView()
            ) { loginWithFacebookAccount() }
            Timber.e("loginWithFacebookAccount Error: Offline")
        }
    }

    private fun launchFacebookLoginManager() {
        showFacebookLoadingAnimation()

        val callbackManager = CallbackManager.Factory.create()

        val loginManager = LoginManager.getInstance()
        loginManager.logInWithReadPermissions(this, callbackManager, listOf("email"))

        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                viewModel!!.loginWithFacebookAccount(result.accessToken)
            }

            override fun onCancel() {
                hideFacebookLoadingAnimation()
                Timber.i("launchFacebookLoginManager canceled.")
            }

            override fun onError(error: FacebookException) {
                hideFacebookLoadingAnimation()
                snackbar = showSomethingWentWrongError(requireContext(), requireView())
                Timber.e("launchFacebookLoginManager Callback Exception: ${error.message}")
            }
        })
    }

    private fun loginWithFacebookAccountObserver() =
        viewModel!!.loginWithFacebookAccountLiveData.observe(viewLifecycleOwner) { responseEvent ->
            responseEvent.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Loading -> showFacebookLoadingAnimation()
                    is Resource.Success -> {
                        hideFacebookLoadingAnimation()
                        navigateToMainActivity()
                    }
                    is Resource.Error -> {
                        hideFacebookLoadingAnimation()
                        response.message?.asString(requireContext())?.let {
                            snackbar = when {
                                it.contains(ERROR_NETWORK_CONNECTION) -> {
                                    showNetworkFailureError(requireContext(), requireView())
                                }
                                it.contains(ERROR_FIREBASE_403) -> {
                                    show403Error(requireContext(), requireView())
                                }
                                it.contains(ERROR_FIREBASE_DEVICE_BLOCKED) -> {
                                    showFirebaseDeviceBlockedError(requireContext(), requireView())
                                }
                                it.contains(
                                    ERROR_FIREBASE_AUTH_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIALS
                                ) -> {
                                    showActionSnackbarError(
                                        requireView(),
                                        requireContext().getString(R.string.login_error_email_exists_with_different_credentials),
                                        requireContext().getString(R.string.error_btn_confirm)
                                    ) { snackbar?.dismiss() }
                                }
                                else -> {
                                    showSomethingWentWrongError(requireContext(), requireView())
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun showLoginWithEmailLoadingAnimation() = binding.apply {
        tiEditEmail.isEnabled = false
        tiEditPassword.isEnabled = false
        btnLogin.isClickable = false
        btnGoogle.isClickable = false
        btnTwitter.isClickable = false
        btnFacebook.isClickable = false
        btnLogin.text = null
        cpiLogin.visibility = VISIBLE
    }

    private fun hideLoginWithEmailLoadingAnimation() = binding.apply {
        cpiLogin.visibility = GONE
        tiEditEmail.isEnabled = true
        tiEditPassword.isEnabled = true
        btnLogin.isClickable = true
        btnGoogle.isClickable = true
        btnTwitter.isClickable = true
        btnFacebook.isClickable = true
        btnLogin.text = requireContext().getString(R.string.login_btn_login)
    }

    private fun showGoogleLoadingAnimation() = binding.apply {
        tiEditEmail.isEnabled = false
        tiEditPassword.isEnabled = false
        btnLogin.isClickable = false
        btnGoogle.isClickable = false
        btnTwitter.isClickable = false
        btnFacebook.isClickable = false
        btnGoogle.icon = null
        cpiGoogle.visibility = VISIBLE
    }

    private fun hideGoogleLoadingAnimation() = binding.apply {
        cpiGoogle.visibility = GONE
        tiEditEmail.isEnabled = true
        tiEditPassword.isEnabled = true
        btnLogin.isClickable = true
        btnGoogle.isClickable = true
        btnTwitter.isClickable = true
        btnFacebook.isClickable = true
        btnGoogle.setIconResource(R.drawable.ic_auth_google)
    }

    private fun showTwitterLoadingAnimation() = binding.apply {
        tiEditEmail.isEnabled = false
        tiEditPassword.isEnabled = false
        btnLogin.isClickable = false
        btnGoogle.isClickable = false
        btnTwitter.isClickable = false
        btnFacebook.isClickable = false
        btnTwitter.icon = null
        cpiTwitter.visibility = VISIBLE
    }

    private fun hideTwitterLoadingAnimation() = binding.apply {
        cpiTwitter.visibility = GONE
        tiEditEmail.isEnabled = true
        tiEditPassword.isEnabled = true
        btnLogin.isClickable = true
        btnGoogle.isClickable = true
        btnTwitter.isClickable = true
        btnFacebook.isClickable = true
        btnTwitter.setIconResource(R.drawable.ic_auth_twitter)
    }

    private fun showFacebookLoadingAnimation() = binding.apply {
        tiEditEmail.isEnabled = false
        tiEditPassword.isEnabled = false
        btnLogin.isClickable = false
        btnGoogle.isClickable = false
        btnTwitter.isClickable = false
        btnFacebook.isClickable = false
        btnFacebook.icon = null
        cpiFacebook.visibility = VISIBLE
    }

    private fun hideFacebookLoadingAnimation() = binding.apply {
        cpiFacebook.visibility = GONE
        tiEditEmail.isEnabled = true
        tiEditPassword.isEnabled = true
        btnLogin.isClickable = true
        btnGoogle.isClickable = true
        btnTwitter.isClickable = true
        btnFacebook.isClickable = true
        btnFacebook.setIconResource(R.drawable.ic_auth_facebook)
    }
}