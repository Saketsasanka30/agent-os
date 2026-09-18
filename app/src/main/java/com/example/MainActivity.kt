package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AgentEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AssistantChatSheet
import com.example.ui.components.NewAssistantSheet
import com.example.ui.components.NewTaskSheet
import com.example.ui.components.TaskDetailSheet
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.AssistantsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AgentOsViewModel
import com.example.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AgentOsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentOsApp(
    viewModel: AgentOsViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val agents by viewModel.agents.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val approvals by viewModel.approvals.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val tools by viewModel.tools.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val testChatMessages by viewModel.testChatMessages.collectAsStateWithLifecycle()
    val isAgentThinking by viewModel.isAgentThinking.collectAsStateWithLifecycle()

    // Sheet states
    var selectedTaskForDetail by remember { mutableStateOf<TaskEntity?>(null) }
    var activeChatAgent by remember { mutableStateOf<AgentEntity?>(null) }
    var isNewTaskSheetOpen by remember { mutableStateOf(false) }
    var preselectedAgentForTask by remember { mutableStateOf<AgentEntity?>(null) }
    var isNewAssistantSheetOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AppBottomNavigation(
                currentDestination = currentScreen,
                pendingApprovalsCount = approvals.size,
                onNavigate = { destination ->
                    viewModel.navigateTo(destination)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (currentScreen) {
                ScreenDestination.HOME -> {
                    HomeScreen(
                        agents = agents,
                        tasks = tasks,
                        pendingApprovals = approvals,
                        onSelectAgentChat = { agent ->
                            viewModel.selectAgentForBuilder(agent)
                            activeChatAgent = agent
                        },
                        onSelectAgentRun = { agent ->
                            preselectedAgentForTask = agent
                            isNewTaskSheetOpen = true
                        },
                        onSelectTask = { task ->
                            selectedTaskForDetail = task
                        },
                        onApprove = { approval ->
                            viewModel.handleApproval(approval, true)
                        },
                        onReject = { approval ->
                            viewModel.handleApproval(approval, false)
                        },
                        onOpenNewTask = {
                            preselectedAgentForTask = null
                            isNewTaskSheetOpen = true
                        },
                        onNavigateToAssistants = {
                            viewModel.navigateTo(ScreenDestination.AGENTS)
                        },
                        onNavigateToTasks = {
                            viewModel.navigateTo(ScreenDestination.TASKS)
                        }
                    )
                }

                ScreenDestination.AGENTS, ScreenDestination.AGENT_BUILDER -> {
                    AssistantsScreen(
                        agents = agents,
                        onSelectAgentChat = { agent ->
                            viewModel.selectAgentForBuilder(agent)
                            activeChatAgent = agent
                        },
                        onSelectAgentRun = { agent ->
                            preselectedAgentForTask = agent
                            isNewTaskSheetOpen = true
                        },
                        onAddNewAssistant = {
                            isNewAssistantSheetOpen = true
                        }
                    )
                }

                ScreenDestination.TASKS, ScreenDestination.WORKFLOWS -> {
                    TasksScreen(
                        tasks = tasks,
                        onSelectTask = { task ->
                            selectedTaskForDetail = task
                        },
                        onOpenNewTask = {
                            preselectedAgentForTask = null
                            isNewTaskSheetOpen = true
                        }
                    )
                }

                ScreenDestination.ACTIVITY, ScreenDestination.APPROVALS, ScreenDestination.TOOLS, ScreenDestination.SETTINGS, ScreenDestination.KNOWLEDGE, ScreenDestination.ANALYTICS, ScreenDestination.LANDING_AUTH -> {
                    ActivityScreen(
                        approvals = approvals,
                        activities = activities,
                        tools = tools,
                        onApprove = { approval ->
                            viewModel.handleApproval(approval, true)
                        },
                        onReject = { approval ->
                            viewModel.handleApproval(approval, false)
                        },
                        onToggleTool = { tool ->
                            viewModel.toggleToolConnection(tool)
                        }
                    )
                }
            }
        }

        // 1. Task Detail Sheet
        selectedTaskForDetail?.let { task ->
            TaskDetailSheet(
                task = task,
                onDismiss = { selectedTaskForDetail = null }
            )
        }

        // 2. Interactive Chat Sheet
        activeChatAgent?.let { agent ->
            AssistantChatSheet(
                agent = agent,
                messages = testChatMessages,
                isThinking = isAgentThinking,
                onSendMessage = { prompt ->
                    viewModel.sendTestMessage(agent, prompt)
                },
                onDismiss = { activeChatAgent = null }
            )
        }

        // 3. New Task Sheet
        if (isNewTaskSheetOpen) {
            val agentList = if (preselectedAgentForTask != null) {
                listOf(preselectedAgentForTask!!) + agents.filter { it.id != preselectedAgentForTask!!.id }
            } else {
                agents
            }
            NewTaskSheet(
                agents = agentList,
                onDismiss = {
                    isNewTaskSheetOpen = false
                    preselectedAgentForTask = null
                },
                onStartTask = { agent, input ->
                    viewModel.runAgentTask(agent, input)
                }
            )
        }

        // 4. New Assistant Sheet
        if (isNewAssistantSheetOpen) {
            NewAssistantSheet(
                onDismiss = { isNewAssistantSheetOpen = false },
                onSaveAssistant = { newAgent ->
                    viewModel.saveAgent(newAgent)
                }
            )
        }
    }
}
