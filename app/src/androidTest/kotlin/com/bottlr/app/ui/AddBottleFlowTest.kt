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
 * UI tests for the Add Bottle flow.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddBottleFlowTest {

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

    private fun navigateToAddBottle() {
        navigateToGallery()
        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(500)
    }

    @Test
    fun addBottle_formFieldsAreDisplayed() {
        navigateToAddBottle()

        onView(withId(R.id.bottleNameField)).check(matches(isDisplayed()))
        onView(withId(R.id.distillerField)).check(matches(isDisplayed()))
        onView(withId(R.id.spiritTypeField)).check(matches(isDisplayed()))
        onView(withId(R.id.regionField)).check(matches(isDisplayed()))
        onView(withId(R.id.ageField)).check(matches(isDisplayed()))
        onView(withId(R.id.abvField)).check(matches(isDisplayed()))
        onView(withId(R.id.saveButton)).check(matches(isDisplayed()))
    }

    @Test
    fun addBottle_fillFormAndSave_bottleAppearsInGallery() {
        val bottleName = "TestWhisky${System.currentTimeMillis()}"

        navigateToAddBottle()

        // Fill form - use replaceText instead of typeText (faster, more reliable)
        onView(withId(R.id.bottleNameField)).perform(replaceText(bottleName))
        onView(withId(R.id.distillerField)).perform(replaceText("Test Distillery"))
        onView(withId(R.id.spiritTypeField)).perform(replaceText("Single Malt"))
        onView(withId(R.id.regionField)).perform(replaceText("Islay"))

        closeSoftKeyboard()
        Thread.sleep(300)

        // Save
        onView(withId(R.id.saveButton)).perform(scrollTo(), click())

        // Wait for save and navigation
        Thread.sleep(3000)

        // Verify we're back in gallery
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))
        onView(withText(bottleName)).check(matches(isDisplayed()))
    }

    @Test
    fun addBottle_withAllFields_savesSuccessfully() {
        val bottleName = "CompleteBottle${System.currentTimeMillis()}"

        navigateToAddBottle()

        // Fill all fields using replaceText
        onView(withId(R.id.bottleNameField)).perform(replaceText(bottleName))
        onView(withId(R.id.distillerField)).perform(replaceText("Lagavulin"))
        onView(withId(R.id.spiritTypeField)).perform(replaceText("Single Malt Scotch"))
        onView(withId(R.id.regionField)).perform(replaceText("Islay"))
        onView(withId(R.id.ageField)).perform(replaceText("16"))
        onView(withId(R.id.abvField)).perform(replaceText("43"))
        onView(withId(R.id.ratingField)).perform(replaceText("9.5"))

        closeSoftKeyboard()

        onView(withId(R.id.tastingNotesField)).perform(scrollTo(), replaceText("Smoky peaty"))
        onView(withId(R.id.keywordsField)).perform(scrollTo(), replaceText("peaty smoky"))

        closeSoftKeyboard()
        Thread.sleep(300)

        // Save
        onView(withId(R.id.saveButton)).perform(scrollTo(), click())

        Thread.sleep(3000)

        // Verify in gallery
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))
        onView(withText(bottleName)).check(matches(isDisplayed()))
    }

    @Test
    fun addBottle_systemBack_returnsToGallery() {
        navigateToAddBottle()

        onView(withId(R.id.bottleNameField)).check(matches(isDisplayed()))

        pressBack()
        Thread.sleep(500)

        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))
    }
}
