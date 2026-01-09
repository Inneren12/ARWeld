package com.example.arweld

import android.Manifest
import android.util.Log
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.example.arweld.core.data.seed.DbSeedInitializer
import com.example.arweld.core.domain.auth.AuthRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AppNavigationSmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var dbSeedInitializer: DbSeedInitializer

    @Inject
    lateinit var authRepository: AuthRepository

    @Before
    fun setup() = runBlocking {
        hiltRule.inject()
        authRepository.logout()
        dbSeedInitializer.seedIfEmpty()
        waitForSeededUsers()
    }

    @Test
    fun loginScreenShowsSeededUsers() {
        waitForLoginScreen()

        composeRule.onNodeWithTag("login_screen").assertIsDisplayed()
        composeRule.onNodeWithTag(userTag("u-asm-1")).assertIsDisplayed()
        composeRule.onNodeWithTag(userTag("u-qc-1")).assertIsDisplayed()
    }

    @Test
    fun assemblerLoginNavigatesToHome() {
        performLogin("u-asm-1")
        waitForHomeScreen()

        composeRule.onNodeWithText("ARWeld MVP").assertIsDisplayed()
        composeRule.onNodeWithText("Assembler Queue").assertIsDisplayed()
        composeRule.onNodeWithText("Work Item Summary").assertIsDisplayed()
    }

    @Test
    fun qcUserCanOpenQcQueue() {
        performLogin("u-qc-1")
        waitForHomeScreen()

        composeRule.onNodeWithText("QC Queue").performClick()
        runCatching {
            composeRule.onRoot(useUnmergedTree = true).printToLog("SMOKE")
        }.onFailure { throwable ->
            Log.d("SMOKE", "Unable to print semantics: ${throwable.message}")
        }
        waitForTag("qc_queue_screen", timeoutMillis = 20_000)
    }

    private fun performLogin(userId: String) {
        waitForLoginScreen()
        composeRule.onNodeWithTag(userTag(userId)).performClick()
    }

    private fun waitForHomeScreen() {
        waitForTag("home_screen", timeoutMillis = 15_000)
    }

    private fun waitForLoginScreen() {
        waitForTag("login_screen", timeoutMillis = 25_000)
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = 10_000) {
        waitUntil(timeoutMillis, "tag=$tag") {
            composeRule.onAllNodesWithTag(tag)
        }
    }

    private fun waitUntil(
        timeoutMillis: Long,
        targetDescription: String,
        nodeQuery: () -> SemanticsNodeInteractionCollection
    ) {
        try {
            composeRule.waitUntil(timeoutMillis = timeoutMillis) {
                runCatching { nodeQuery().fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
            }
        } catch (throwable: Throwable) {
            logDiagnostics("Timeout waiting for $targetDescription")
            throw throwable
        }
    }

    private fun userTag(userId: String) = "login_user_$userId"

    private suspend fun waitForSeededUsers() {
        val maxAttempts = 10
        repeat(maxAttempts) { attempt ->
            val users = runCatching { authRepository.availableUsers() }.getOrDefault(emptyList())
            if (users.isNotEmpty()) {
                return
            }
            Log.d("SMOKE", "Seeded users not available yet (attempt ${attempt + 1}/$maxAttempts)")
            delay(200)
        }
    }

    private fun logDiagnostics(message: String) {
        Log.d("SMOKE", message)
        runCatching {
            composeRule.onRoot(useUnmergedTree = true).printToLog("SMOKE")
        }.onFailure { throwable ->
            Log.d("SMOKE", "Unable to print semantics: ${throwable.message}")
        }
        composeRule.activityRule.scenario.onActivity { activity ->
            Log.d("SMOKE", "Current activity: ${activity::class.java.name}")
        }
    }
}
