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
import androidx.compose.ui.test.printToLog
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
import androidx.compose.ui.test.onAllNodesWithText

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
        // "QC Queue" есть на home-плитке, поэтому он не доказывает навигацию.
        waitUntilCondition(timeoutMillis = 10_000, targetDescription = "leave home_screen") {
            runCatching {
                composeRule.onAllNodesWithTag("home_screen").fetchSemanticsNodes().isEmpty()
            }.getOrDefault(false)
        }

        // Smoke: нам важно, что экран QC открылся. Контент может быть пустым/без "Loading..." текста.
        waitUntilCondition(timeoutMillis = 20_000, targetDescription = "QC queue screen") {
            val byTag = runCatching {
                composeRule.onAllNodesWithTag("qc_queue_screen").fetchSemanticsNodes().isNotEmpty()
            }.getOrDefault(false)
            val byTitle = runCatching {
                composeRule.onAllNodesWithText("QC Queue", substring = true).fetchSemanticsNodes().isNotEmpty()
            }.getOrDefault(false)
            byTag || byTitle
        }
    }

    private fun waitUntilCondition(timeoutMillis: Long, targetDescription: String, condition: () -> Boolean) {
        try {
            composeRule.waitUntil(timeoutMillis = timeoutMillis) { runCatching(condition).getOrDefault(false) }
        } catch (t: Throwable) {
            logDiagnostics("Timeout waiting for $targetDescription")
            throw t
        }
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
