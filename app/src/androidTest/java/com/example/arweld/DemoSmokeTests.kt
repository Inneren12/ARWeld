package com.example.arweld

import android.Manifest
import android.util.Log
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DemoSmokeTests {

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
    fun setUp() = runBlocking {
        hiltRule.inject()
        authRepository.logout()
        dbSeedInitializer.seedIfEmpty()
        waitForSeededUsers()
    }

    @Test
    fun loginNavigatesToHomeScreen() {
        loginAndWaitForHome("u-asm-1")

        composeRule.onNodeWithText("Role: ASSEMBLER").assertExists()
        composeRule.onNodeWithText("Navigation Demo").assertExists()
    }

    @Test
    fun homeTilesChangePerRole() {
        loginAndWaitForHome("u-asm-1") {
            composeRule.onNodeWithText("Assembler Queue").assertExists()
            assertTrue(composeRule.onAllNodesWithText("QC Queue").fetchSemanticsNodes().isEmpty())
        }

        logoutAndWaitForLogin()

        loginAndWaitForHome("u-qc-1") {
            composeRule.onNodeWithText("QC Queue").assertExists()
            assertTrue(composeRule.onAllNodesWithText("Assembler Queue").fetchSemanticsNodes().isEmpty())
        }

        logoutAndWaitForLogin()

        loginAndWaitForHome("u-sup-1") {
            composeRule.onNodeWithText("Supervisor Dashboard").assertExists()
            composeRule.onNodeWithText("Role: SUPERVISOR", substring = true).assertExists()
        }

        logoutAndWaitForLogin()

        loginAndWaitForHome("u-dir-1") {
            composeRule.onNodeWithText("Supervisor Dashboard").assertExists()
            composeRule.onNodeWithText("Role: DIRECTOR", substring = true).assertExists()
        }
    }

    @Test
    fun tappingHomeTileNavigatesToTimeline() {
        loginAndWaitForHome("u-asm-1")

        composeRule.onNodeWithText("Timeline").performClick()

        waitForText("Timeline stub")
        composeRule.onNodeWithText("Timeline stub").assertExists()
    }

    private fun selectUser(userId: String) {
        waitForLoginScreen()
        composeRule.onNodeWithTag(userTag(userId)).performClick()
    }

    private fun loginAndWaitForHome(userId: String, assertions: (() -> Unit)? = null) {
        selectUser(userId)

        waitForHomeScreen()

        assertions?.invoke()
    }

    private fun logoutAndWaitForLogin() {
        composeRule.onNodeWithTag("logout_button").performClick()
        composeRule.waitForIdle()
        waitForLoginScreen()
    }

    private fun waitForLoginScreen() {
        waitForTag("login_screen", timeoutMillis = 25_000)
    }

    private fun waitForHomeScreen() {
        waitForTag("home_screen", timeoutMillis = 15_000)
    }

    private fun waitForText(text: String, timeoutMillis: Long = 5_000) {
        waitForTextInternal(text, substring = false, timeoutMillis = timeoutMillis)
    }

    private fun waitForTextInternal(
        text: String,
        substring: Boolean = false,
        timeoutMillis: Long = 10_000
    ) {
        waitUntil(timeoutMillis, "text=$text") {
            composeRule.onAllNodesWithText(text, substring = substring)
        }
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
