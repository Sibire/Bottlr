package com.bottlr.app.ui

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests to verify the app launches without crashing.
 *
 * These tests catch critical issues like:
 * - Navigation graph parsing errors
 * - Hilt injection failures
 * - Missing resources or layouts
 * - Fragment instantiation errors
 *
 * Run these tests on every PR to catch startup crashes early.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun app_launches_without_crashing() {
        // Launch MainActivity - this will crash if:
        // - Navigation graph has errors
        // - Hilt can't inject dependencies
        // - Layout inflation fails
        // - Any onCreate() throws an exception
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull("MainActivity should not be null", activity)
            }

            // Verify activity reached RESUMED state (fully visible)
            assertTrue(
                "Activity should reach RESUMED state",
                scenario.state.isAtLeast(Lifecycle.State.RESUMED)
            )
        }
    }

    @Test
    fun app_survives_recreation() {
        // Tests that the app handles configuration changes (like rotation)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Simulate activity recreation (like a rotation)
            scenario.recreate()

            scenario.onActivity { activity ->
                assertNotNull("Activity should survive recreation", activity)
            }

            assertTrue(
                "Activity should reach RESUMED state after recreation",
                scenario.state.isAtLeast(Lifecycle.State.RESUMED)
            )
        }
    }
}
