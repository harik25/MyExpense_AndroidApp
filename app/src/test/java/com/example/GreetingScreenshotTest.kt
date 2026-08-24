package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.Goal
import com.example.ui.DashboardUiState
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dashboard_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        DashboardScreen(
          uiState = DashboardUiState(
            totalBalance = 3156.0,
            todayExpense = 0.0,
            weekExpense = 0.0,
            monthExpense = 349.0,
            topGoal = Goal(
              title = "Food Budget",
              cadence = "monthly",
              category = "Food",
              targetAmount = 5000.0
            ),
            topGoalSpent = 200.0
          ),
          onNavigateToGoals = {},
          onOpenNewTransaction = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

