package com.example.data.repository

import com.example.data.db.AgentOsDao
import com.example.data.model.ActivityEntity
import com.example.data.model.AgentEntity
import com.example.data.model.ApprovalEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.data.model.ToolEntity
import com.example.data.model.WorkflowEntity
import kotlinx.coroutines.flow.Flow

class AgentOsRepository(private val dao: AgentOsDao) {
    val agents: Flow<List<AgentEntity>> = dao.getAllAgents()
    val tasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val workflows: Flow<List<WorkflowEntity>> = dao.getAllWorkflows()
    val memories: Flow<List<MemoryEntity>> = dao.getAllMemories()
    val tools: Flow<List<ToolEntity>> = dao.getAllTools()
    val approvals: Flow<List<ApprovalEntity>> = dao.getAllApprovals()
    val activities: Flow<List<ActivityEntity>> = dao.getAllActivities()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()

    suspend fun getAgentById(id: String): AgentEntity? = dao.getAgentById(id)
    suspend fun insertAgent(agent: AgentEntity) = dao.insertAgent(agent)
    suspend fun updateAgent(agent: AgentEntity) = dao.updateAgent(agent)
    suspend fun deleteAgent(agent: AgentEntity) = dao.deleteAgent(agent)

    suspend fun insertTask(task: TaskEntity) = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(taskId: String) = dao.deleteTaskById(taskId)

    suspend fun insertWorkflow(workflow: WorkflowEntity) = dao.insertWorkflow(workflow)
    suspend fun updateWorkflow(workflow: WorkflowEntity) = dao.updateWorkflow(workflow)
    suspend fun deleteWorkflow(workflowId: String) = dao.deleteWorkflowById(workflowId)

    suspend fun insertMemory(memory: MemoryEntity) = dao.insertMemory(memory)
    suspend fun deleteMemory(memory: MemoryEntity) = dao.deleteMemory(memory)

    suspend fun updateTool(tool: ToolEntity) = dao.updateTool(tool)

    suspend fun resolveApproval(approval: ApprovalEntity, approved: Boolean, reason: String? = null) {
        val newStatus = if (approved) ApprovalStatus.APPROVED else ApprovalStatus.REJECTED
        dao.updateApproval(approval.copy(status = newStatus))

        // Log to Activity
        dao.insertActivity(
            ActivityEntity(
                id = "ACT-" + System.currentTimeMillis().toString().takeLast(4),
                timestamp = "Just now",
                agentName = approval.agentName,
                message = "${if (approved) "Approved" else "Rejected"} action: ${approval.action}",
                category = "Approval",
                status = if (approved) "SUCCESS" else "CRITICAL"
            )
        )

        // Log to Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                id = "AUD-" + System.currentTimeMillis().toString().takeLast(4),
                timestamp = "2026-09-16 " + java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date()),
                actor = "user:operator",
                ipAddress = "192.168.1.104",
                action = if (approved) "MANUAL_APPROVAL_GRANTED" else "MANUAL_APPROVAL_DENIED",
                target = approval.recipient,
                status = if (approved) "ALLOW" else "BLOCKED"
            )
        )
    }

    suspend fun addActivity(agentName: String, message: String, category: String, status: String = "INFO") {
        dao.insertActivity(
            ActivityEntity(
                id = "ACT-" + System.currentTimeMillis().toString().takeLast(5),
                timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date()),
                agentName = agentName,
                message = message,
                category = category,
                status = status
            )
        )
    }

    suspend fun addAuditLog(actor: String, action: String, target: String, status: String) {
        dao.insertAuditLog(
            AuditLogEntity(
                id = "AUD-" + System.currentTimeMillis().toString().takeLast(5),
                timestamp = "2026-09-16 " + java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date()),
                actor = actor,
                ipAddress = "10.14.2.88",
                action = action,
                target = target,
                status = status
            )
        )
    }
}
