package com.example.arweld

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.arweld.core.data.seed.DbSeedInitializer
import com.example.arweld.core.domain.auth.AuthRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DemoSmokeTests {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var dbSeedInitializer: DbSeedInitializer

    @Inject
    lateinit var authRepository: AuthRepository

    @Before
    fun setUp() = runBlocking {
        hiltRule.inject()
        authRepository.logout()
        dbSeedInitializer.seedIfEmpty()
    }

    @Test
    fun loginNavigatesToHomeAndShowsRoleTile() {
        selectUser("Assembler 1")

        waitForText("Navigation Demo")

        composeRule.onNodeWithText("Role: ASSEMBLER").assertExists()
        composeRule.onNodeWithText("Assembler Queue").assertExists()
    }

    @Test
    fun homeTileOpensTimeline() {
        selectUser("Assembler 1")

        waitForText("Navigation Demo")
        composeRule.onNodeWithText("Timeline").performClick()

        waitForText("Timeline stub")
        composeRule.onNodeWithText("Timeline stub").assertExists()
    }

    @Test
    fun workItemSummaryHandlesMissingIdGracefully() {
        selectUser("Assembler 1")

        waitForText("Navigation Demo")
        composeRule.onNodeWithText("Work Item Summary").performClick()

        waitForText("Work item not found")
        composeRule.onNodeWithText("Work item not found").assertExists()
        composeRule.onNodeWithText("Tap to retry").assertExists()
    }

    private fun selectUser(name: String) {
        waitForText("Select a user")
        composeRule.onNodeWithText(name).performClick()
    }

    private fun waitForText(text: String, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
