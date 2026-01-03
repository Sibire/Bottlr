package com.bottlr.app.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.MainActivity
import com.bottlr.app.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for basic navigation flows.
 *
 * Tests cover:
 * - App launches successfully
 * - Home screen displays correctly
 * - Navigation menu opens and navigates to gallery
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun appLaunches_homeScreenDisplayed() {
        // Verify the app title is displayed
        onView(withId(R.id.Title_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Bottlr")))
    }

    @Test
    fun menuButton_opensNavigationDrawer() {
        // Click the menu button
        onView(withId(R.id.menu_icon))
            .perform(click())

        // Verify the navigation menu is visible (check for a nav item)
        onView(withId(R.id.menu_liquorcab_button))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigationToGallery_displaysGalleryScreen() {
        // Open menu
        onView(withId(R.id.menu_icon))
            .perform(click())

        // Click Liquor Cabinet
        onView(withId(R.id.menu_liquorcab_button))
            .perform(click())

        // Verify we're on the gallery screen (check for the RecyclerView)
        onView(withId(R.id.liquorRecycler))
            .check(matches(isDisplayed()))
    }
}
