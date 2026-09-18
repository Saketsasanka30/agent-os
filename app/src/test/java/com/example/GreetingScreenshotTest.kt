package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.AgentEntity
import com.example.data.model.AgentStatus
import com.example.ui.screens.HomeScreen
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
  fun home_screen_screenshot() {
    val sampleAgent = AgentEntity(
        id = "sample",
        name = "Research Assistant",
        role = "Autonomous deep search and intelligence gathering",
        status = AgentStatus.ACTIVE,
        lastRun = "10m ago",
        tasksCompleted = 14,
        connectedTools = listOf("Web Search", "Google Scholar"),
        successRate = 0.98f,
        instructions = "Conduct thorough analysis and summarize key findings.",
        goals = "Provide concise, accurate reports."
    )
    composeTestRule.setContent {
        MyApplicationTheme {
            HomeScreen(
                agents = listOf(sampleAgent),
                tasks = emptyList(),
                pendingApprovals = emptyList(),
                onSelectAgentChat = {},
                onSelectAgentRun = {},
                onSelectTask = {},
                onApprove = {},
                onReject = {},
                onOpenNewTask = {},
                onNavigateToAssistants = {},
                onNavigateToTasks = {}
            )
        }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home.png")
  }
}
