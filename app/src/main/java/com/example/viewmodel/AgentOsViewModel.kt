package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AgentOsDatabase
import com.example.data.model.ActivityEntity
import com.example.data.model.AgentEntity
import com.example.data.model.AgentStatus
import com.example.data.model.ApprovalEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.ChatMessage
import com.example.data.model.MemoryEntity
import com.example.data.model.SecurityLevel
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.data.model.ToolEntity
import com.example.data.model.WorkflowEntity
import com.example.data.repository.AgentOsRepository
import com.example.data.service.GeminiAgentService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    HOME,
    AGENTS,
    AGENT_BUILDER,
    WORKFLOWS,
    TASKS,
    KNOWLEDGE,
    TOOLS,
    APPROVALS,
    ACTIVITY,
    ANALYTICS,
    SETTINGS,
    LANDING_AUTH
}

class AgentOsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AgentOsDatabase.getInstance(application)
    private val repository = AgentOsRepository(database.agentOsDao())
    private val agentService = GeminiAgentService()

    // Persistent Room Flows
    val agents: StateFlow<List<AgentEntity>> = repository.agents.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val tasks: StateFlow<List<TaskEntity>> = repository.tasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val workflows: StateFlow<List<WorkflowEntity>> = repository.workflows.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val memories: StateFlow<List<MemoryEntity>> = repository.memories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val tools: StateFlow<List<ToolEntity>> = repository.tools.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val approvals: StateFlow<List<ApprovalEntity>> = repository.approvals.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val activities: StateFlow<List<ActivityEntity>> = repository.activities.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(ScreenDestination.HOME)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Command Palette State (CMD/CTRL + K)
    private val _isCommandPaletteOpen = MutableStateFlow(false)
    val isCommandPaletteOpen: StateFlow<Boolean> = _isCommandPaletteOpen.asStateFlow()

    // Selected Agent for Builder / Preview
    private val _selectedAgentForBuilder = MutableStateFlow<AgentEntity?>(null)
    val selectedAgentForBuilder: StateFlow<AgentEntity?> = _selectedAgentForBuilder.asStateFlow()

    // Selected Task for Detail Sheet
    private val _selectedTaskDetail = MutableStateFlow<TaskEntity?>(null)
    val selectedTaskDetail: StateFlow<TaskEntity?> = _selectedTaskDetail.asStateFlow()

    // Chat Conversation in Agent Builder
    private val _testChatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            id = "msg-0",
            sender = "system",
            text = "AGENTOS Runtime Sandbox initialized. Ready for test execution.",
            timestamp = "09:00"
        )
    ))
    val testChatMessages: StateFlow<List<ChatMessage>> = _testChatMessages.asStateFlow()

    private val _isAgentThinking = MutableStateFlow(false)
    val isAgentThinking: StateFlow<Boolean> = _isAgentThinking.asStateFlow()

    // Filter states
    val taskFilterStatus = MutableStateFlow<TaskStatus?>(null)
    val taskSearchQuery = MutableStateFlow("")
    val agentSearchQuery = MutableStateFlow("")
    val memorySearchQuery = MutableStateFlow("")

    // Toast / Feedback Message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun openCommandPalette() {
        _isCommandPaletteOpen.value = true
    }

    fun closeCommandPalette() {
        _isCommandPaletteOpen.value = false
    }

    fun selectAgentForBuilder(agent: AgentEntity?) {
        _selectedAgentForBuilder.value = agent
        _testChatMessages.value = listOf(
            ChatMessage(
                id = "msg-${System.currentTimeMillis()}",
                sender = "system",
                text = if (agent != null) "Loaded '${agent.name}' context (${agent.model}). Connected tools: ${agent.connectedTools.joinToString()}." else "New Agent Sandbox Ready.",
                timestamp = "Now"
            )
        )
        _currentScreen.value = ScreenDestination.AGENT_BUILDER
    }

    fun selectTaskDetail(task: TaskEntity?) {
        _selectedTaskDetail.value = task
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Save or update an agent
    fun saveAgent(agent: AgentEntity) {
        viewModelScope.launch {
            repository.insertAgent(agent)
            _selectedAgentForBuilder.value = agent
            repository.addActivity(agent.name, "Configuration saved & model deployed", "Configuration", "SUCCESS")
            repository.addAuditLog("user:operator", "AGENT_UPDATE", "agent://${agent.id}", "ALLOW")
            showToast("Agent '${agent.name}' configured successfully")
        }
    }

    // Interactive Test Conversation in Agent Builder
    fun sendTestMessage(agent: AgentEntity, promptText: String) {
        if (promptText.isBlank()) return
        val userMsg = ChatMessage(
            id = "msg-u-${System.currentTimeMillis()}",
            sender = "user",
            text = promptText,
            timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date())
        )
        _testChatMessages.value = _testChatMessages.value + userMsg
        _isAgentThinking.value = true

        viewModelScope.launch {
            val result = agentService.executeAgentPrompt(agent, promptText, agent.connectedTools)
            val agentMsg = ChatMessage(
                id = "msg-a-${System.currentTimeMillis()}",
                sender = "agent",
                text = result.reply,
                timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date()),
                toolUsed = result.toolInvoked,
                executionMs = result.latencyMs
            )
            _testChatMessages.value = _testChatMessages.value + agentMsg
            _isAgentThinking.value = false

            // Also record an activity entry
            repository.addActivity(agent.name, "Executed test prompt: '${promptText.take(30)}...'", "Execution", "INFO")
        }
    }

    // Run an Agent from Command Palette or Agent Card
    fun runAgentTask(agent: AgentEntity, userInstruction: String = "Automated execution run") {
        viewModelScope.launch {
            val taskId = "TSK-" + (1000..9999).random()
            val newTask = TaskEntity(
                id = taskId,
                title = "$userInstruction (${agent.name})",
                agentId = agent.id,
                agentName = agent.name,
                status = TaskStatus.RUNNING,
                input = userInstruction,
                reasoningSummary = "Initializing security context, validating tool permissions, running ${agent.model} inference.",
                toolActions = listOf(
                    "Connected to ${agent.connectedTools.firstOrNull() ?: "Workspace Kernel"}",
                    "Policy verification passed",
                    "Executing task payload..."
                ),
                outputs = "Execution in progress...",
                duration = "1.2s",
                timestamp = System.currentTimeMillis()
            )
            repository.insertTask(newTask)
            repository.addActivity(agent.name, "Started task $taskId: $userInstruction", "Execution", "INFO")
            repository.addAuditLog("agent:${agent.name}", "TASK_SPAWN", "task://$taskId", "ALLOW")
            showToast("Launched task $taskId on ${agent.name}")

            // Simulate completed or waiting state after brief delay
            kotlinx.coroutines.delay(1200)
            val completedTask = newTask.copy(
                status = TaskStatus.COMPLETED,
                reasoningSummary = "Autonomous execution completed safely with zero permission violations.",
                outputs = "Task verified and results synchronized with AGENTOS workspace.",
                duration = "2.4s"
            )
            repository.updateTask(completedTask)
            repository.addActivity(agent.name, "Completed task $taskId successfully", "Execution", "SUCCESS")
        }
    }

    // Approve / Reject human-in-the-loop action
    fun handleApproval(approval: ApprovalEntity, approved: Boolean) {
        viewModelScope.launch {
            repository.resolveApproval(approval, approved)
            showToast(if (approved) "Approved action '${approval.action}'" else "Rejected action '${approval.action}'")
        }
    }

    // Toggle tool connection
    fun toggleToolConnection(tool: ToolEntity) {
        viewModelScope.launch {
            val updated = tool.copy(
                connectedStatus = !tool.connectedStatus,
                lastPing = if (!tool.connectedStatus) "18ms (Nominal)" else "Disconnected"
            )
            repository.updateTool(updated)
            repository.addActivity("Integration Engine", "${if (updated.connectedStatus) "Connected" else "Disconnected"} tool ${tool.name}", "Tool", if (updated.connectedStatus) "SUCCESS" else "WARNING")
            repository.addAuditLog("user:operator", "TOOL_STATUS_CHANGE", "tool://${tool.id}", "ALLOW")
            showToast("${tool.name} ${if (updated.connectedStatus) "connected" else "disconnected"}")
        }
    }

    fun toggleToolById(id: String, enabled: Boolean) {
        val tool = tools.value.find { it.id == id } ?: return
        viewModelScope.launch {
            val updated = tool.copy(
                connectedStatus = enabled,
                lastPing = if (enabled) "12ms (Active)" else "Disconnected"
            )
            repository.updateTool(updated)
            showToast("${tool.name} ${if (enabled) "connected" else "disconnected"}")
        }
    }

    fun resolveApprovalById(id: String, approved: Boolean) {
        val approval = approvals.value.find { it.id == id } ?: return
        handleApproval(approval, approved)
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memories.value.forEach { repository.deleteMemory(it) }
            showToast("Memories vault cleared")
        }
    }

    // Memory actions
    fun addMemory(key: String, info: String, usedBy: String) {
        viewModelScope.launch {
            val mem = MemoryEntity(
                id = "MEM-" + (100..999).random(),
                key = key,
                savedInformation = info,
                source = "User manual memory inject",
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date()),
                usedByAgent = usedBy,
                isProtected = true
            )
            repository.insertMemory(mem)
            repository.addActivity("Memory Workspace", "Injected memory key '$key'", "Memory", "INFO")
            showToast("Memory saved into secure vault")
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
            repository.addActivity("Memory Workspace", "Purged memory key '${memory.key}'", "Memory", "WARNING")
            repository.addAuditLog("user:operator", "MEMORY_PURGE", "memory://${memory.id}", "ALLOW")
            showToast("Memory deleted")
        }
    }

    // Workflow actions
    fun toggleWorkflow(workflow: WorkflowEntity) {
        viewModelScope.launch {
            val updated = workflow.copy(enabled = !workflow.enabled)
            repository.updateWorkflow(updated)
            showToast("Workflow '${workflow.name}' ${if (updated.enabled) "enabled" else "disabled"}")
        }
    }

    fun runWorkflow(workflow: WorkflowEntity) {
        viewModelScope.launch {
            repository.addActivity(workflow.agentName, "Workflow triggered: ${workflow.name}", "Workflow", "INFO")
            repository.addAuditLog("system:workflow", "WORKFLOW_EXECUTE", "wf://${workflow.id}", "ALLOW")
            showToast("Triggered workflow: ${workflow.name}")
        }
    }
}
