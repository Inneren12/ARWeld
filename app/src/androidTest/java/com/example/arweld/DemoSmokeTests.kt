package com.example.arweld

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.arweld.core.data.seed.DbSeedInitializer
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.navigation.AppNavigation
import com.example.arweld.ui.components.AppErrorBoundary
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
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
        composeRule.setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppErrorBoundary {
                        AppNavigation()
                    }
                }
            }
        }
        authRepository.logout()
        dbSeedInitializer.seedIfEmpty()
    }

    @Test
    fun loginNavigatesToHomeScreen() {
        loginAndWaitForHome("Assembler 1")

        composeRule.onNodeWithText("Role: ASSEMBLER").assertExists()
        composeRule.onNodeWithText("Navigation Demo").assertExists()
    }

    @Test
    fun homeTilesChangePerRole() {
        loginAndWaitForHome("Assembler 1") {
            composeRule.onNodeWithText("Assembler Queue").assertExists()
            assertTrue(composeRule.onAllNodesWithText("QC Queue").fetchSemanticsNodes().isEmpty())
        }

        relaunchFromSplash()

        loginAndWaitForHome("QC 1") {
            composeRule.onNodeWithText("QC Queue").assertExists()
            assertTrue(composeRule.onAllNodesWithText("Assembler Queue").fetchSemanticsNodes().isEmpty())
        }

        relaunchFromSplash()

        loginAndWaitForHome("Supervisor 1") {
            composeRule.onNodeWithText("Supervisor Dashboard").assertExists()
            composeRule.onNodeWithText("Role: SUPERVISOR", substring = true).assertExists()
        }

        relaunchFromSplash()

        loginAndWaitForHome("Director 1") {
            composeRule.onNodeWithText("Supervisor Dashboard").assertExists()
            composeRule.onNodeWithText("Role: DIRECTOR", substring = true).assertExists()
        }
    }

    @Test
    fun tappingHomeTileNavigatesToTimeline() {
        loginAndWaitForHome("Assembler 1")

        composeRule.onNodeWithText("Timeline").performClick()

        waitForText("Timeline stub")
        composeRule.onNodeWithText("Timeline stub").assertExists()
    }

    private fun selectUser(name: String) {
        waitForText("Select a user")
        composeRule.onNodeWithText(name).performClick()
    }

    private fun loginAndWaitForHome(name: String, assertions: (() -> Unit)? = null) {
        selectUser(name)

        waitForText("Navigation Demo")

        assertions?.invoke()
    }

    private fun relaunchFromSplash() {
        composeRule.activityRule.scenario.recreate()
        waitForText("Select a user")
    }

    private fun waitForText(text: String, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            runCatching {
                composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
            }.getOrDefault(false)
        }
    }
}
