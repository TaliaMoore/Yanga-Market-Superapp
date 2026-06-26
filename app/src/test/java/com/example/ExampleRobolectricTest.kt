package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Yanga Market", appName)
  }

  @Test
  fun `test auth flow changes MainViewModel state`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val viewModel = com.example.ui.MainViewModel(context)
    
    // Assert initial unauthenticated defaults
    assertEquals(false, viewModel.isUserAuthenticated.value)
    assertEquals("", viewModel.userName.value)
    
    // Simulate setting Google login credentials
    viewModel.setLoginDetails("eniolaagbeyindo@gmail.com", "Google")
    assertEquals("eniolaagbeyindo@gmail.com", viewModel.userPhoneOrEmail.value)
    assertEquals("Google", viewModel.loginMethod.value)
    assertEquals("Eniolaagbeyindo", viewModel.userName.value) // defaults to capitalized local part before @
    
    // Complete profile details and pinpoint coordinates
    viewModel.completeProfileAndLogIn(
        name = "Eniola Agbeyindo",
        location = "Lekki Phase 1, Lagos, Nigeria",
        lat = 6.4281,
        lng = 3.4219
    )
    
    // Confirm session status is correctly updated and synchronized
    assertEquals(true, viewModel.isUserAuthenticated.value)
    assertEquals("Eniola Agbeyindo", viewModel.userName.value)
    assertEquals("Lekki Phase 1, Lagos, Nigeria", viewModel.userLocation.value)
    assertEquals(6.4281, viewModel.userLatitude.value, 0.0001)
    assertEquals(3.4219, viewModel.userLongitude.value, 0.0001)
    
    // Log out user and verify state clearance
    viewModel.logOutUser()
    assertEquals(false, viewModel.isUserAuthenticated.value)
    assertEquals("", viewModel.userName.value)
    assertEquals("", viewModel.userLocation.value)
  }
}
