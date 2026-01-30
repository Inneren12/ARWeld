package com.example.arweld.ui.ar

import android.Manifest
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.rule.GrantPermissionRule
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import com.example.arweld.navigation.AppNavigation
import com.example.arweld.navigation.ROUTE_AR_VIEW
import com.example.arweld.ui.theme.ArWeldTheme
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ARViewNavigationSmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @BindValue
    @JvmField
    val authRepository: AuthRepository = object : AuthRepository {
        private val testUser = User(
            id = "user-1",
            username = "tester",
            displayName = "AR Test",
            role = Role.ASSEMBLER,
        )
        private val currentUserState = MutableStateFlow<User?>(testUser)

        override suspend fun loginMock(role: Role): User = testUser.copy(role = role)

        override suspend fun availableUsers(): List<User> = listOf(testUser)

        override suspend fun loginWithUserId(userId: String): User = testUser

        override suspend fun currentUser(): User? = currentUserState.value

        override val currentUserFlow: StateFlow<User?> = currentUserState

        override suspend fun logout() {
            currentUserState.value = null
        }
    }

    @BindValue
    @JvmField
    val eventRepository: EventRepository = object : EventRepository {
        override suspend fun appendEvent(event: Event) = Unit
        override suspend fun appendEvents(events: List<Event>) = Unit
        override suspend fun getEventsForWorkItem(workItemId: String): List<Event> = emptyList()
        override suspend fun getLastEventsByUser(userId: String): List<Event> = emptyList()
    }

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun appNavigatesToArViewWithoutCrashing() {
        composeRule.setContent {
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                navController.navigate("$ROUTE_AR_VIEW?workItemId=WORK-1")
            }

            ArWeldTheme {
                AppNavigation(
                    navController = navController,
                    authRepository = authRepository,
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ar_view_screen_root").assertIsDisplayed()
        composeRule.onNodeWithText("ARCore disabled for instrumentation tests").assertIsDisplayed()
    }
}
