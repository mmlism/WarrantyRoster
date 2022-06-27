package com.xeniac.warrantyroster_manager.ui.landing.fragments

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.common.truth.Truth.assertThat
import com.xeniac.warrantyroster_manager.R
import com.xeniac.warrantyroster_manager.databinding.FragmentLoginBinding
import com.xeniac.warrantyroster_manager.getOrAwaitValue
import com.xeniac.warrantyroster_manager.launchFragmentInHiltContainer
import com.xeniac.warrantyroster_manager.data.repository.FakePreferencesRepository
import com.xeniac.warrantyroster_manager.data.repository.FakeUserRepository
import com.xeniac.warrantyroster_manager.ui.landing.viewmodels.LandingViewModel
import com.xeniac.warrantyroster_manager.utils.Status
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@HiltAndroidTest
class LoginFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var testViewModel: LandingViewModel

    private lateinit var navController: TestNavHostController
    private lateinit var testBinding: FragmentLoginBinding

    @Before
    fun setUp() {
        hiltRule.inject()

        context = ApplicationProvider.getApplicationContext()
        fakeUserRepository = FakeUserRepository()
        testViewModel = LandingViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeUserRepository,
            FakePreferencesRepository()
        )

        navController = TestNavHostController(context)
        navController.setGraph(R.navigation.nav_graph_landing)

        launchFragmentInHiltContainer<LoginFragment> {
            Navigation.setViewNavController(requireView(), navController)
            viewModel = testViewModel
            testBinding = binding
        }
    }

    @Test
    fun clickOnEmailEditText_changesBoxBackgroundColor() {
        onView(withId(testBinding.tiEditEmail.id)).perform(click())

        assertThat(testBinding.tiLayoutEmail.boxBackgroundColor).isEqualTo(context.getColor(R.color.background))
        assertThat(testBinding.tiLayoutPassword.boxBackgroundColor).isEqualTo(context.getColor(R.color.grayLight))
    }

    @Test
    fun clickOnPasswordEditText_changesBoxBackgroundColor() {
        onView(withId(testBinding.tiEditPassword.id)).perform(click())

        assertThat(testBinding.tiLayoutEmail.boxBackgroundColor).isEqualTo(context.getColor(R.color.grayLight))
        assertThat(testBinding.tiLayoutPassword.boxBackgroundColor).isEqualTo(context.getColor(R.color.background))
    }

    @Test
    fun clickOnEmailEditText_changesBoxStrokeColor() {
        onView(withId(testBinding.tiEditEmail.id)).perform(click())
        assertThat(testBinding.tiLayoutEmail.boxStrokeColor).isEqualTo(context.getColor(R.color.blue))
    }

    @Test
    fun clickOnPasswordEditText_changesBoxStrokeColor() {
        onView(withId(testBinding.tiEditPassword.id)).perform(click())
        assertThat(testBinding.tiLayoutPassword.boxStrokeColor).isEqualTo(context.getColor(R.color.blue))
    }

    @Test
    fun pressImeActionOnPasswordEditTextWithErrorStatus_returnsError() {
        onView(withId(testBinding.tiEditEmail.id)).perform(replaceText("email"))
        onView(withId(testBinding.tiEditPassword.id)).perform(replaceText("password"))
        onView(withId(testBinding.tiEditPassword.id)).perform(pressImeActionButton())

        val responseEvent = testViewModel.loginLiveData.getOrAwaitValue()
        assertThat(responseEvent.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun pressImeActionOnPasswordEditTextWithSuccessStatus_returnsSuccess() {
        val email = "email@test.com"
        val password = "password"
        fakeUserRepository.addUser(email, password)

        onView(withId(testBinding.tiEditEmail.id)).perform(replaceText(email))
        onView(withId(testBinding.tiEditPassword.id)).perform(replaceText(password))
        onView(withId(testBinding.tiEditPassword.id)).perform(pressImeActionButton())

        val responseEvent = testViewModel.loginLiveData.getOrAwaitValue()
        assertThat(responseEvent.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun clickOnForgotPwBtn_navigatesToForgotPwFragment() {
        onView(withId(testBinding.btnForgotPw.id)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.forgotPwFragment)
    }

    @Test
    fun clickOnRegisterBtn_navigatesToRegisterFragment() {
        onView(withId(testBinding.btnRegister.id)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.registerFragment)
    }

    @Test
    fun clickOnLoginBtnWithErrorStatus_returnsError() {
        onView(withId(testBinding.tiEditEmail.id)).perform(replaceText("email"))
        onView(withId(testBinding.tiEditPassword.id)).perform(replaceText("password"))
        onView(withId(testBinding.btnLogin.id)).perform(click())

        val responseEvent = testViewModel.loginLiveData.getOrAwaitValue()
        assertThat(responseEvent.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun clickOnLoginBtnWithSuccessStatus_returnsSuccess() {
        val email = "email@test.com"
        val password = "password"
        fakeUserRepository.addUser(email, password)

        onView(withId(testBinding.tiEditEmail.id)).perform(replaceText(email))
        onView(withId(testBinding.tiEditPassword.id)).perform(replaceText(password))
        onView(withId(testBinding.btnLogin.id)).perform(click())

        val responseEvent = testViewModel.loginLiveData.getOrAwaitValue()
        assertThat(responseEvent.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}