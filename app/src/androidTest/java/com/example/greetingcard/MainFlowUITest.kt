package com.example.greetingcard

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class MainFlowUITest {

    private lateinit var device: UiDevice

    @Before
    fun startMainActivityFromHomeScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Go to the home screen
        device.pressHome()

        // Try clicking the launcher icon by its label first
        val icon = device.wait(Until.findObject(By.text(APP_LABEL)), 2000)
            ?: device.findObject(By.desc(APP_LABEL))

        if (icon != null) {
            icon.click()
        } else {
            // Fallback: icon is not on the visible home screen (e.g. in the app drawer),
            // so launch via the app's launcher intent, as in the official BasicSample.
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val intent = context.packageManager.getLaunchIntentForPackage(APP_PACKAGE)
            assertNotNull("Launcher intent not found for $APP_PACKAGE", intent)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT)
    }

    @Test
    fun secondActivityShowsAChallenge() {
        // Step 2: click "Start Activity Explicitly" in the main activity
        val button = device.wait(Until.findObject(By.text(EXPLICIT_BUTTON_TEXT)), UI_TIMEOUT)
        assertNotNull("'$EXPLICIT_BUTTON_TEXT' button not found", button)
        button.click()

        // The main activity's button should disappear once the second activity is up
        val left = device.wait(Until.gone(By.text(EXPLICIT_BUTTON_TEXT)), UI_TIMEOUT)
        assertTrue("Did not navigate away from the main activity", left)

        // Step 3: verify at least one challenge is displayed in the second activity
        val found = CHALLENGES.any { challenge ->
            device.wait(Until.hasObject(By.pkg(APP_PACKAGE).textContains(challenge)), 1500)
        }
        assertTrue(
            "None of the expected mobile SE challenges were displayed in the second activity",
            found
        )
    }

    companion object {

        private const val APP_PACKAGE = "com.example.greetingcard"
        private const val APP_LABEL = "Greeting Card"
        private const val EXPLICIT_BUTTON_TEXT = "Start Activity Explicitly"

        private val CHALLENGES = listOf(
            "1. Device & OS Fragmentation",
            "2. Unstable & Dynamic Environment",
            "3. Rapid Changes",
            "4. Limited Tool Support",
            "5. Low Awareness of Security & Privacy"
        )
        // ==========================================

        private const val LAUNCH_TIMEOUT = 5000L
        private const val UI_TIMEOUT = 5000L
    }
}
