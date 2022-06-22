package com.xeniac.warrantyroster_manager.ui.landing.fragments

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.google.common.truth.Truth.assertThat
import com.xeniac.warrantyroster_manager.R
import com.xeniac.warrantyroster_manager.getOrAwaitValue
import com.xeniac.warrantyroster_manager.launchFragmentInHiltContainer
import com.xeniac.warrantyroster_manager.repositories.FakePreferencesRepository
import com.xeniac.warrantyroster_manager.repositories.FakeUserRepository
import com.xeniac.warrantyroster_manager.ui.landing.LandingFragmentFactory
import com.xeniac.warrantyroster_manager.ui.landing.viewmodels.LandingViewModel
import com.xeniac.warrantyroster_manager.utils.Constants.URL_PRIVACY_POLICY
import com.xeniac.warrantyroster_manager.utils.Status
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class RegisterFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var fragmentFactory: LandingFragmentFactory

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun pressBack_popsBackStack() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        launchFragmentInHiltContainer<RegisterFragment>(fragmentFactory = fragmentFactory) {
            navController.setGraph(R.navigation.nav_graph_landing)
            Navigation.setViewNavController(requireView(), navController)
            navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        pressBack()
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.loginFragment)
    }

    @Test
    fun clickOnAgreementBtn_opensLinkInBrowser() {
        Intents.init()

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        launchFragmentInHiltContainer<RegisterFragment>(fragmentFactory = fragmentFactory) {
            navController.setGraph(R.navigation.nav_graph_landing)
            Navigation.setViewNavController(requireView(), navController)
            navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        onView(withId(R.id.btn_agreement)).perform(click())
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(URL_PRIVACY_POLICY)))

        Intents.release()
    }

    @Test
    fun clickOnLoginBtn_popsBackStack() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        launchFragmentInHiltContainer<RegisterFragment>(fragmentFactory = fragmentFactory) {
            navController.setGraph(R.navigation.nav_graph_landing)
            Navigation.setViewNavController(requireView(), navController)
            navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        onView(withId(R.id.btn_login)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.loginFragment)
    }

    @Test
    fun clickOnRegisterBtnWithErrorStatus_returnsError() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        val testViewModel = LandingViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeUserRepository(),
            FakePreferencesRepository()
        )

        launchFragmentInHiltContainer<RegisterFragment>(fragmentFactory = fragmentFactory) {
            navController.setGraph(R.navigation.nav_graph_landing)
            Navigation.setViewNavController(requireView(), navController)
            viewModel = testViewModel
        }

        onView(withId(R.id.ti_edit_email)).perform(replaceText("email"))
        onView(withId(R.id.ti_edit_password)).perform(replaceText("password"))
        onView(withId(R.id.ti_edit_retype_password)).perform(replaceText("retype_password"))
        onView(withId(R.id.btn_register)).perform(click())

        val responseEvent = testViewModel.registerLiveData.getOrAwaitValue()
        assertThat(responseEvent.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun clickOnRegisterBtnWithSuccessStatus_returnsSuccess() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        val testViewModel = LandingViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeUserRepository(),
            FakePreferencesRepository()
        )

        launchFragmentInHiltContainer<LoginFragment>(fragmentFactory = fragmentFactory) {
            navController.setGraph(R.navigation.nav_graph_landing)
            Navigation.setViewNavController(requireView(), navController)
            viewModel = testViewModel
        }

        val email = "email@test.com"
        val password = "password"

        onView(withId(R.id.ti_edit_email)).perform(replaceText(email))
        onView(withId(R.id.ti_edit_password)).perform(replaceText(password))
        onView(withId(R.id.ti_edit_retype_password)).perform(replaceText(password))
        onView(withId(R.id.btn_register)).perform(click())

        val responseEvent = testViewModel.registerLiveData.getOrAwaitValue()
        assertThat(responseEvent.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}