package com.bottlr.app.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.MainActivity
import com.bottlr.app.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for viewing and editing bottles.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ViewEditBottleFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun navigateToGallery() {
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.menu_liquorcab_button)).perform(click())
        Thread.sleep(500)
    }

    private fun createTestBottle(): String {
        val bottleName = "EditTest${System.currentTimeMillis()}"

        navigateToGallery()

        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(1000)

        onView(withId(R.id.bottleNameField)).perform(replaceText(bottleName))
        onView(withId(R.id.distillerField)).perform(replaceText("Original Distillery"))

        closeSoftKeyboard()
        Thread.sleep(300)

        onView(withId(R.id.saveButton)).perform(scrollTo(), click())

        // Wait for save and navigation back to gallery
        Thread.sleep(3000)

        // Verify we're back in gallery
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))

        return bottleName
    }

    @Test
    fun gallery_displaysRecyclerView() {
        navigateToGallery()
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))
    }

    @Test
    fun gallery_fabAndSearchVisible() {
        navigateToGallery()
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.search_liquor_button)).check(matches(isDisplayed()))
    }

    @Test
    fun viewBottle_displaysDetails() {
        val name = createTestBottle()

        // Tap on bottle
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Verify details screen
        onView(withId(R.id.tvBottleName)).check(matches(withText(name)))
        onView(withId(R.id.tvDistillery)).check(matches(isDisplayed()))
    }

    @Test
    fun editBottle_changesAreSaved() {
        val name = createTestBottle()
        val updatedName = "Updated$name"

        // Navigate to details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Click edit
        onView(withId(R.id.editButton)).perform(click())
        Thread.sleep(1000)

        // Update name
        onView(withId(R.id.bottleNameField)).perform(replaceText(updatedName))

        closeSoftKeyboard()
        Thread.sleep(300)

        // Save
        onView(withId(R.id.saveButton)).perform(scrollTo(), click())
        Thread.sleep(3000)

        // After edit, navigateUp() returns to details screen (not gallery)
        // Verify the updated name appears on details screen
        onView(withId(R.id.tvBottleName)).check(matches(withText(updatedName)))
    }

    @Test
    fun viewBottle_backReturnsToGallery() {
        val name = createTestBottle()

        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        onView(withId(R.id.tvBottleName)).check(matches(isDisplayed()))

        pressBack()
        Thread.sleep(1000)

        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))
    }
}
