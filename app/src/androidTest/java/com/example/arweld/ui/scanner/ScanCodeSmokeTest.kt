package com.example.arweld.ui.scanner

import android.Manifest
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import androidx.test.rule.GrantPermissionRule
import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.model.WorkItemType
import com.example.arweld.core.domain.work.ResolveWorkItemByCodeUseCase
import com.example.arweld.navigation.ROUTE_SCAN_CODE
import com.example.arweld.navigation.ROUTE_WORK_ITEM_SUMMARY
import com.example.arweld.ui.theme.ArWeldTheme
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ScanCodeSmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @BindValue
    @JvmField
    val resolveWorkItemByCodeUseCase: ResolveWorkItemByCodeUseCase = ResolveWorkItemByCodeUseCase { code ->
        WorkItem(
            id = "WORK-1",
            code = code,
            type = WorkItemType.PART,
            description = "Test work item",
            createdAt = 0L,
        )
    }

    @BindValue
    @JvmField
    val appLogger: AppLogger = object : AppLogger {
        override fun logNavigation(route: String) = Unit
        override fun logLoginAttempt(userId: String) = Unit
        override fun logLoginSuccess(user: com.example.arweld.core.domain.model.User) = Unit
        override fun logRepositoryError(operation: String, throwable: Throwable) = Unit
        override fun logUnhandledError(throwable: Throwable) = Unit
    }

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun scannerNavigatesOnFakeDecode() {
        val navController = TestNavHostController(composeRule.activity).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
        }

        composeRule.setContent {
            ArWeldTheme {
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_SCAN_CODE,
                ) {
                    composable(ROUTE_SCAN_CODE) {
                        ScanCodeRoute(navController = navController)
                    }
                    composable(
                        route = "$ROUTE_WORK_ITEM_SUMMARY?workItemId={workItemId}",
                        arguments = listOf(
                            navArgument("workItemId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) {
                        Text("Work item summary")
                    }
                }
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            navController.currentBackStackEntry?.arguments?.getString("workItemId") == "WORK-1"
        }

        composeRule.onNodeWithText("Work item summary").assertIsDisplayed()
    }
}
