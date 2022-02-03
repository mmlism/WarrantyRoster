package com.xeniac.warrantyroster_manager.ui.main.fragments

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.xeniac.warrantyroster_manager.R
import com.xeniac.warrantyroster_manager.databinding.FragmentChangeEmailBinding
import com.xeniac.warrantyroster_manager.models.Status
import com.xeniac.warrantyroster_manager.ui.main.MainActivity
import com.xeniac.warrantyroster_manager.ui.main.viewmodels.SettingsViewModel
import com.xeniac.warrantyroster_manager.utils.Constants.ERROR_FIREBASE_AUTH_ACCOUNT_EXISTS
import com.xeniac.warrantyroster_manager.utils.Constants.ERROR_FIREBASE_AUTH_CREDENTIALS
import com.xeniac.warrantyroster_manager.utils.Constants.ERROR_NETWORK_CONNECTION

class ChangeEmailFragment : Fragment(R.layout.fragment_change_email) {

    private var _binding: FragmentChangeEmailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChangeEmailBinding.bind(view)
        (requireContext() as MainActivity).hideNavBar()
        viewModel = (activity as MainActivity).settingsViewModel

        textInputsBackgroundColor()
        textInputsStrokeColor()
        returnToMainActivity()
        changeEmailOnClick()
        changeEmailActionDone()
        reAuthenticateUserObserver()
        changeUserEmailObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun textInputsBackgroundColor() {
        binding.tiEditPassword.setOnFocusChangeListener { _, isFocused ->
            if (isFocused) {
                binding.tiLayoutPassword.boxBackgroundColor =
                    ContextCompat.getColor(requireContext(), R.color.background)
            } else {
                binding.tiLayoutPassword.boxBackgroundColor =
                    ContextCompat.getColor(requireContext(), R.color.grayLight)
            }
        }

        binding.tiEditNewEmail.setOnFocusChangeListener { _, isFocused ->
            if (isFocused) {
                binding.tiLayoutNewEmail.boxBackgroundColor =
                    ContextCompat.getColor(requireContext(), R.color.background)
            } else {
                binding.tiLayoutNewEmail.boxBackgroundColor =
                    ContextCompat.getColor(requireContext(), R.color.grayLight)
            }
        }
    }

    private fun textInputsStrokeColor() {
        binding.tiEditPassword.addTextChangedListener {
            binding.tiLayoutPassword.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.blue)
        }

        binding.tiEditNewEmail.addTextChangedListener {
            binding.tiLayoutNewEmail.isErrorEnabled = false
            binding.tiLayoutNewEmail.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.blue)
        }
    }

    private fun returnToMainActivity() = binding.toolbar.setNavigationOnClickListener {
        requireActivity().onBackPressed()
    }

    private fun changeEmailOnClick() = binding.btnChangeEmail.setOnClickListener {
        getChangeUserEmailInputs()
    }

    private fun changeEmailActionDone() =
        binding.tiEditNewEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getChangeUserEmailInputs()
            }
            false
        }

    private fun getChangeUserEmailInputs() {
        val inputMethodManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.applicationWindowToken, 0)

        val password = binding.tiEditPassword.text.toString().trim()
        val newEmail = binding.tiEditNewEmail.text.toString().trim().lowercase()

        if (password.isBlank()) {
            binding.tiLayoutPassword.requestFocus()
            binding.tiLayoutPassword.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.red)
        } else if (newEmail.isBlank()) {
            binding.tiLayoutNewEmail.requestFocus()
            binding.tiLayoutNewEmail.boxStrokeColor =
                ContextCompat.getColor(requireContext(), R.color.red)
        } else {
            if (!isEmailValid(newEmail)) {
                binding.tiLayoutNewEmail.requestFocus()
                binding.tiLayoutNewEmail.error =
                    requireContext().getString(R.string.change_email_error_new_email)
            } else if (newEmail == FirebaseAuth.getInstance().currentUser?.email) {
                binding.tiLayoutNewEmail.requestFocus()
                binding.tiLayoutNewEmail.error =
                    requireContext().getString(R.string.change_email_error_email_same)
            } else {
                showLoadingAnimation()
                reAuthenticateUser(password, newEmail)
            }
        }
    }

    private fun reAuthenticateUser(password: String, newEmail: String) =
        viewModel.reAuthenticateUser(password, newEmail)

    private fun reAuthenticateUserObserver() =
        viewModel.reAuthenticateUserLiveData.observe(viewLifecycleOwner) { responseEvent ->
            responseEvent.getContentIfNotHandled()?.let { response ->
                when (response.status) {
                    Status.LOADING -> showLoadingAnimation()
                    Status.SUCCESS -> {
                        response.data?.let { newEmail ->
                            changeUserEmail(newEmail)
                        }
                    }
                    Status.ERROR -> {
                        hideLoadingAnimation()
                        response.message?.let {
                            when {
                                it.contains(ERROR_NETWORK_CONNECTION) -> {
                                    Snackbar.make(
                                        binding.root,
                                        requireContext().getString(R.string.network_error_connection),
                                        LENGTH_LONG
                                    ).apply {
                                        setAction(requireContext().getString(R.string.network_error_retry)) {
                                            getChangeUserEmailInputs()
                                        }
                                        show()
                                    }
                                }
                                it.contains(ERROR_FIREBASE_AUTH_CREDENTIALS) -> {
                                    Snackbar.make(
                                        binding.root,
                                        requireContext().getString(R.string.change_email_error_credentials),
                                        LENGTH_LONG
                                    ).show()
                                }
                                else -> {
                                    Snackbar.make(
                                        binding.root,
                                        requireContext().getString(R.string.network_error_failure),
                                        LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun changeUserEmail(newEmail: String) = viewModel.changeUserEmail(newEmail)

    private fun changeUserEmailObserver() =
        viewModel.changeUserEmailLiveData.observe(viewLifecycleOwner) { responseEvent ->
            responseEvent.getContentIfNotHandled()?.let { response ->
                when (response.status) {
                    Status.LOADING -> showLoadingAnimation()
                    Status.SUCCESS -> {
                        hideLoadingAnimation()
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setMessage(requireContext().getString(R.string.change_email_dialog_message))
                            setPositiveButton(requireContext().getString(R.string.change_email_dialog_positive)) { _, _ -> }
                            setOnDismissListener { requireActivity().onBackPressed() }
                            show()
                        }
                    }
                    Status.ERROR -> {
                        hideLoadingAnimation()
                        response.message?.let {
                            when {
                                it.contains(ERROR_NETWORK_CONNECTION) -> {
                                    Snackbar.make(
                                        binding.root,
                                        requireContext().getString(R.string.network_error_connection),
                                        LENGTH_LONG
                                    ).apply {
                                        setAction(requireContext().getString(R.string.network_error_retry)) {
                                            getChangeUserEmailInputs()
                                        }
                                        show()
                                    }
                                }
                                it.contains(ERROR_FIREBASE_AUTH_ACCOUNT_EXISTS) -> {
                                    Snackbar.make(
                                        binding.root,
                                        requireContext().getString(R.string.change_email_error_email_exists),
                                        LENGTH_LONG
                                    ).show()
                                }
                                else -> {
                                    Snackbar.make(
                                        binding.root,
                                        requireContext().getString(R.string.network_error_failure),
                                        LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun showLoadingAnimation() {
        binding.tiEditPassword.isEnabled = false
        binding.tiEditNewEmail.isEnabled = false
        binding.btnChangeEmail.isClickable = false
        binding.btnChangeEmail.text = null
        binding.cpiChangeEmail.visibility = VISIBLE
    }

    private fun hideLoadingAnimation() {
        binding.cpiChangeEmail.visibility = GONE
        binding.tiEditPassword.isEnabled = true
        binding.tiEditNewEmail.isEnabled = true
        binding.btnChangeEmail.isClickable = true
        binding.btnChangeEmail.text = requireContext().getString(R.string.change_email_btn_change)
    }

    private fun isEmailValid(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
}