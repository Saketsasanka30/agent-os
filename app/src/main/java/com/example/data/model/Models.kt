package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AgentStatus {
    ACTIVE, IDLE, BUSY, PAUSED
}

enum class TaskStatus {
    RUNNING, WAITING, COMPLETED, FAILED
}

enum class SecurityLevel {
    CRITICAL, HIGH, MEDIUM, LOW
}

enum class ApprovalStatus {
    PENDING, APPROVED, REJECTED
}

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val status: AgentStatus,
    val lastRun: String,
    val tasksCompleted: Int,
    val connectedTools: List<String>,
    val successRate: Float,
    val instructions: String,
    val goals: String,
    val memoryScope: String = "Workspace",
    val permissions: String = "Supervised",
    val triggers: String = "Event & Manual",
    val outputFormat: String = "Structured Markdown",
    val model: String = "gemini-3.5-flash",
    val isCustom: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val agentId: String,
    val agentName: String,
    val status: TaskStatus,
    val input: String,
    val reasoningSummary: String,
    val toolActions: List<String>,
    val outputs: String,
    val duration: String,
    val timestamp: Long,
    val costEstimate: String = "$0.0024"
) {
    val outputSummary: String get() = outputs
    val durationMs: Long get() = duration.filter { it.isDigit() }.toLongOrNull() ?: 0L
}

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val trigger: String,
    val agentId: String,
    val agentName: String,
    val tool: String,
    val condition: String,
    val approvalRequired: Boolean,
    val action: String,
    val enabled: Boolean,
    val lastRun: String
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val key: String,
    val savedInformation: String,
    val source: String,
    val createdAt: String,
    val usedByAgent: String,
    val isProtected: Boolean = true
)

@Entity(tableName = "tools")
data class ToolEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val connectedStatus: Boolean,
    val permissions: List<String>,
    val availableActions: List<String>,
    val lastPing: String,
    val rateLimitStatus: String = "10,000 req/day"
) {
    val isEnabled: Boolean get() = connectedStatus
    val description: String get() = category
}

@Entity(tableName = "approvals")
data class ApprovalEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val agentName: String,
    val action: String,
    val recipient: String,
    val contentPreview: String,
    val reason: String,
    val severity: SecurityLevel,
    val status: ApprovalStatus,
    val requestedAt: String
) {
    val actionDescription: String get() = action
    val reasoning: String get() = reason
    val payloadPreview: String get() = contentPreview
    val securityLevel: SecurityLevel get() = severity
}

@Entity(tableName = "activity_logs")
data class ActivityEntity(
    @PrimaryKey val id: String,
    val timestamp: String,
    val agentName: String,
    val message: String,
    val category: String,
    val status: String = "INFO"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val timestamp: String,
    val actor: String,
    val ipAddress: String,
    val action: String,
    val target: String,
    val status: String
)

data class ChatMessage(
    val id: String,
    val sender: String, // "user", "agent", "system"
    val text: String,
    val timestamp: String,
    val toolUsed: String? = null,
    val executionMs: Long? = null
) {
    val isUser: Boolean get() = sender == "user"
    val content: String get() = text
    val toolInvoked: String? get() = toolUsed
}

data class AnalyticsData(
    val totalTasks: Int,
    val successRatePercent: Float,
    val avgExecutionTimeSec: Float,
    val totalCostFormatted: String,
    val toolUsageMap: Map<String, Int>,
    val timeSeriesTasks: List<TimeSeriesPoint>
)

data class TimeSeriesPoint(
    val timeLabel: String,
    val taskCount: Int,
    val latencyMs: Int
)
