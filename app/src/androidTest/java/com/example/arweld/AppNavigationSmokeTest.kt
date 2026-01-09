package com.example.arweld

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AppNavigationSmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loginScreenShowsSeededUsers() {
        waitForText("Select a user")

        composeRule.onNodeWithText("Select a user").assertIsDisplayed()
        composeRule.onNodeWithText("Assembler 1").assertIsDisplayed()
        composeRule.onNodeWithText("QC 1").assertIsDisplayed()
    }

    @Test
    fun assemblerLoginNavigatesToHome() {
        performLogin("Assembler 1")
        waitForHomeScreen()

        composeRule.onNodeWithText("ARWeld MVP").assertIsDisplayed()
        composeRule.onNodeWithText("Assembler Queue").assertIsDisplayed()
        composeRule.onNodeWithText("Work Item Summary").assertIsDisplayed()
    }

    @Test
    fun qcUserCanOpenQcQueue() {
        performLogin("QC 1")
        waitForHomeScreen()

        composeRule.onNodeWithText("QC Queue").performClick()
        waitForText("QC Queue")

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Loading QC queue...").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Начать проверку").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun performLogin(userName: String) {
        waitForText(userName)
        composeRule.onNodeWithText(userName).performClick()
    }

    private fun waitForHomeScreen() {
        waitForText("ARWeld MVP")
    }

    private fun waitForText(text: String, substring: Boolean = false, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            runCatching {
                composeRule.onAllNodesWithText(text, substring = substring)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }.getOrDefault(false)
        }
    }
}
