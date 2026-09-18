package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ActivityEntity
import com.example.data.model.AgentEntity
import com.example.data.model.AgentStatus
import com.example.data.model.ApprovalEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.SecurityLevel
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.data.model.ToolEntity
import com.example.data.model.WorkflowEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AgentEntity::class,
        TaskEntity::class,
        WorkflowEntity::class,
        MemoryEntity::class,
        ToolEntity::class,
        ApprovalEntity::class,
        ActivityEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AgentOsDatabase : RoomDatabase() {
    abstract fun agentOsDao(): AgentOsDao

    companion object {
        @Volatile
        private var INSTANCE: AgentOsDatabase? = null

        fun getInstance(context: Context): AgentOsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgentOsDatabase::class.java,
                    "agentos_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedDatabase(database.agentOsDao())
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDatabase(dao: AgentOsDao) {
            // Seed 8 Canonical Agents
            val agents = listOf(
                AgentEntity(
                    id = "agent-research",
                    name = "Research Agent",
                    role = "Deep web analysis & autonomous synthesis",
                    status = AgentStatus.ACTIVE,
                    lastRun = "3 mins ago",
                    tasksCompleted = 412,
                    connectedTools = listOf("GitHub", "Notion", "Google Drive"),
                    successRate = 99.1f,
                    instructions = "You are an autonomous intelligence researcher. Gather technical citations, cross-reference academic journals, verify CVE disclosures, and synthesize findings into concise executive briefs.",
                    goals = "1. Discover verified source documents\n2. Extract vulnerability metrics\n3. Deliver structured markdown reports",
                    memoryScope = "Full Workspace",
                    permissions = "Autonomous Read / Supervised Write",
                    triggers = "Scheduled Cron & Webhooks",
                    outputFormat = "Executive Markdown Dossier",
                    model = "gemini-3.5-flash"
                ),
                AgentEntity(
                    id = "agent-coding",
                    name = "Coding Agent",
                    role = "Software engineering & patch automation",
                    status = AgentStatus.ACTIVE,
                    lastRun = "12 mins ago",
                    tasksCompleted = 845,
                    connectedTools = listOf("GitHub", "Slack"),
                    successRate = 97.8f,
                    instructions = "Analyze AST codebases, generate type-safe Kotlin/TypeScript pull requests, run static security audits, and resolve zero-day vulnerability advisories.",
                    goals = "1. Zero regressions\n2. Maintain 100% test pass rate\n3. Automate dependency upgrades",
                    memoryScope = "Repository Scoped",
                    permissions = "Branch Write / PR Creation (Requires approval for Prod merge)",
                    triggers = "Git Push & Sentry Webhook",
                    outputFormat = "Git Diff & Verified Patch",
                    model = "gemini-3.1-pro-preview"
                ),
                AgentEntity(
                    id = "agent-travel",
                    name = "Travel Agent",
                    role = "Executive itinerary & logistics optimization",
                    status = AgentStatus.IDLE,
                    lastRun = "1 hour ago",
                    tasksCompleted = 128,
                    connectedTools = listOf("Google Calendar", "Maps", "Email"),
                    successRate = 98.4f,
                    instructions = "Coordinate executive travel logistics, calculate optimal buffer intervals between meetings, verify visa constraints, and sync flight bookings with calendar.",
                    goals = "1. Optimize travel times\n2. Prevent overlapping meetings\n3. Ensure VIP security protocols",
                    memoryScope = "Team Shared",
                    permissions = "Draft Bookings / User Approval Required",
                    triggers = "Calendar Invite Update",
                    outputFormat = "Interactive Route Map & Chrono Plan",
                    model = "gemini-3.5-flash"
                ),
                AgentEntity(
                    id = "agent-study",
                    name = "Study Agent",
                    role = "Curriculum ingestion & knowledge mastery",
                    status = AgentStatus.IDLE,
                    lastRun = "4 hours ago",
                    tasksCompleted = 264,
                    connectedTools = listOf("Notion", "YouTube", "Google Drive"),
                    successRate = 99.5f,
                    instructions = "Digest technical RFCs, security whitepapers, and cloud architectures into spaced-repetition flashcards and conceptual mind maps.",
                    goals = "1. Extract core foundational theorems\n2. Generate progressive recall challenges\n3. Track mastery rate",
                    memoryScope = "Personal Knowledge Base",
                    permissions = "Full Local Read/Write",
                    triggers = "Manual & Document Upload",
                    outputFormat = "Active Recall Matrix",
                    model = "gemini-3.5-flash"
                ),
                AgentEntity(
                    id = "agent-finance",
                    name = "Finance Agent",
                    role = "Capital allocation & cloud cost governance",
                    status = AgentStatus.BUSY,
                    lastRun = "Just now",
                    tasksCompleted = 531,
                    connectedTools = listOf("Slack", "Google Drive"),
                    successRate = 99.9f,
                    instructions = "Audit cloud infrastructure spending across AWS and GCP, detect anomalous billing spikes, and enforce monthly security budget thresholds.",
                    goals = "1. Zero undetected billing anomalies\n2. Real-time cost trend forecasting\n3. Automated FinOps alerts",
                    memoryScope = "Enterprise Financial Sandbox",
                    permissions = "Strict Read-Only Telemetry / High-Impact Transfer Approval",
                    triggers = "Billing Metric Exceeded",
                    outputFormat = "Tabular FinOps Report",
                    model = "gemini-3.1-pro-preview"
                ),
                AgentEntity(
                    id = "agent-email",
                    name = "Email Agent",
                    role = "Inbox triage & prioritized correspondence",
                    status = AgentStatus.ACTIVE,
                    lastRun = "1 min ago",
                    tasksCompleted = 972,
                    connectedTools = listOf("Email", "Google Calendar", "Slack"),
                    successRate = 99.3f,
                    instructions = "Categorize incoming correspondence, identify critical security incident escalations, draft polite contextual responses, and flag phishing attempts.",
                    goals = "1. Zero inbox clutter\n2. Instant escalation of P0 security threats\n3. High-touch communication tone",
                    memoryScope = "Executive Persona Memory",
                    permissions = "Draft Generation Only (Sending requires approval)",
                    triggers = "IMAP/OAuth New Email Ingest",
                    outputFormat = "Email Draft Preview",
                    model = "gemini-3.5-flash"
                ),
                AgentEntity(
                    id = "agent-career",
                    name = "Career Agent",
                    role = "Talent sourcing & skill architecture",
                    status = AgentStatus.PAUSED,
                    lastRun = "Yesterday",
                    tasksCompleted = 95,
                    connectedTools = listOf("GitHub", "Notion"),
                    successRate = 96.2f,
                    instructions = "Evaluate engineering candidates, parse open-source portfolio repositories, benchmark technical skills against company rubrics.",
                    goals = "1. Objective candidate evaluation\n2. Alignment with engineering core competencies\n3. Gap analysis report",
                    memoryScope = "HR & Recruiting Vault",
                    permissions = "Supervised Read",
                    triggers = "Applicant Portal Webhook",
                    outputFormat = "Candidate Competency Radar",
                    model = "gemini-3.5-flash"
                ),
                AgentEntity(
                    id = "agent-personal",
                    name = "Personal Assistant",
                    role = "Cross-tool workflow orchestration & life sync",
                    status = AgentStatus.ACTIVE,
                    lastRun = "15 mins ago",
                    tasksCompleted = 1430,
                    connectedTools = listOf("Google Calendar", "Slack", "Spotify", "Email", "Notion"),
                    successRate = 99.7f,
                    instructions = "Coordinate daily schedule, play focus soundtracks during deep work blocks, sync reminders, and oversee multi-agent delegations.",
                    goals = "1. Minimize context switching\n2. Ensure focus blocks are preserved\n3. Deliver daily morning briefing",
                    memoryScope = "Global User Context",
                    permissions = "Full Agent Autonomy with Guardrails",
                    triggers = "Continuous Daemon / Ambient Voice / Touch",
                    outputFormat = "Interactive Daily Briefing Card",
                    model = "gemini-3.5-flash"
                )
            )
            dao.insertAgents(agents)

            // Seed Tool Marketplace Integrations
            val tools = listOf(
                ToolEntity(
                    id = "tool-github",
                    name = "GitHub",
                    category = "Developer",
                    connectedStatus = true,
                    permissions = listOf("repo:write", "pull_requests:create", "security_events:read"),
                    availableActions = listOf("Create PR", "Review Diff", "List Issues", "Trigger Action"),
                    lastPing = "42ms (Nominal)"
                ),
                ToolEntity(
                    id = "tool-calendar",
                    name = "Google Calendar",
                    category = "Workspace",
                    connectedStatus = true,
                    permissions = listOf("calendar.events:read_write", "freebusy:read"),
                    availableActions = listOf("Schedule Event", "Find Common Free Slot", "Reschedule"),
                    lastPing = "31ms (Nominal)"
                ),
                ToolEntity(
                    id = "tool-drive",
                    name = "Google Drive",
                    category = "Workspace",
                    connectedStatus = true,
                    permissions = listOf("drive.file:read_write", "drive.metadata:read"),
                    availableActions = listOf("Upload Report", "Search Documents", "Generate PDF"),
                    lastPing = "48ms (Nominal)"
                ),
                ToolEntity(
                    id = "tool-email",
                    name = "Email",
                    category = "Communication",
                    connectedStatus = true,
                    permissions = listOf("mail.send:guarded", "mail.read:inbox", "mail.draft:write"),
                    availableActions = listOf("Draft Reply", "Send Approved Email", "Parse Headers"),
                    lastPing = "24ms (Nominal)"
                ),
                ToolEntity(
                    id = "tool-slack",
                    name = "Slack",
                    category = "Communication",
                    connectedStatus = true,
                    permissions = listOf("chat:write", "channels:read", "commands:listen"),
                    availableActions = listOf("Post Incident Alert", "Send Direct Message", "Create Channel"),
                    lastPing = "19ms (Nominal)"
                ),
                ToolEntity(
                    id = "tool-notion",
                    name = "Notion",
                    category = "Workspace",
                    connectedStatus = true,
                    permissions = listOf("pages:read_write", "databases:query"),
                    availableActions = listOf("Append Knowledge Page", "Update Task Item", "Query Roadmap"),
                    lastPing = "55ms (Nominal)"
                ),
                ToolEntity(
                    id = "tool-maps",
                    name = "Maps",
                    category = "Geolocation",
                    connectedStatus = true,
                    permissions = listOf("places:search", "directions:compute"),
                    availableActions = listOf("Compute Multi-leg ETA", "Find Meeting Venues", "Geofence Check"),
                    lastPing = "60ms (Nominal)"
                ),
                ToolEntity(
                    id = "tool-spotify",
                    name = "Spotify",
                    category = "Media",
                    connectedStatus = false,
                    permissions = listOf("user-modify-playback-state", "user-read-currently-playing"),
                    availableActions = listOf("Trigger Focus Playlist", "Pause Music on Meeting", "Resume Flow"),
                    lastPing = "Disconnected"
                ),
                ToolEntity(
                    id = "tool-youtube",
                    name = "YouTube",
                    category = "Media",
                    connectedStatus = true,
                    permissions = listOf("captions:read", "search:execute"),
                    availableActions = listOf("Fetch Tech Keynote Transcript", "Summarize Lecture", "Bookmark"),
                    lastPing = "72ms (Nominal)"
                )
            )
            dao.insertTools(tools)

            // Seed Tasks with real action summaries (Respecting privacy: no raw chain-of-thought)
            val tasks = listOf(
                TaskEntity(
                    id = "TSK-9401",
                    title = "Patch CVE-2026-4819 in Auth Gateway",
                    agentId = "agent-coding",
                    agentName = "Coding Agent",
                    status = TaskStatus.RUNNING,
                    input = "Inspect gateway/auth.kt, patch token nullability check, run unit tests, and prepare PR.",
                    reasoningSummary = "Parsed static AST, verified regression vector in OAuth handler, synthesized patch diff, executing test suite.",
                    toolActions = listOf(
                        "Cloned repository git@github.com:acme/auth-gw.git",
                        "Ran dependency scan against vulnerability DB",
                        "Generated patch in branch fix/cve-token-nullability",
                        "Running Gradle testDebugUnitTest..."
                    ),
                    outputs = "Branch created: fix/cve-token-nullability\nDiff: +14 -3 lines in AuthTokenValidator.kt\nLocal test coverage: 98.4%",
                    duration = "45s",
                    timestamp = System.currentTimeMillis() - 45000L,
                    costEstimate = "$0.0038"
                ),
                TaskEntity(
                    id = "TSK-9402",
                    title = "Autonomous Weekly Security Briefing & Threat Matrix",
                    agentId = "agent-research",
                    agentName = "Research Agent",
                    status = TaskStatus.COMPLETED,
                    input = "Aggregate high-severity CVEs from CISA, cross-reference active cloud nodes, summarize impact.",
                    reasoningSummary = "Harvested advisory feeds, correlated IP addresses 10.14.2.80 and 10.14.2.84, confirmed ingress mitigation.",
                    toolActions = listOf(
                        "Queried CISA Known Exploited Vulnerabilities catalog",
                        "Fetched cloud topology from internal CMDB",
                        "Synthesized markdown dossier into Notion Knowledge Base"
                    ),
                    outputs = "Document published: 'SecOps Executive Briefing Week 38'\nSeverity Breakdown: 0 Critical, 2 High (Mitigated), 5 Medium.",
                    duration = "1m 14s",
                    timestamp = System.currentTimeMillis() - 180000L,
                    costEstimate = "$0.0062"
                ),
                TaskEntity(
                    id = "TSK-9403",
                    title = "Quarterly Infrastructure Cloud Spend Re-allocation",
                    agentId = "agent-finance",
                    agentName = "Finance Agent",
                    status = TaskStatus.WAITING,
                    input = "Analyze idling GPU instances in us-central1, propose $14,200 reservation downsizing.",
                    reasoningSummary = "Identified 4 unallocated A100 clusters, drafted reservation termination, awaiting human approval before executing API call.",
                    toolActions = listOf(
                        "Polled GCP Cloud Billing API metrics",
                        "Calculated amortized monthly savings ($14,200/mo)",
                        "Constructed High-Impact Approval ticket #APP-1092"
                    ),
                    outputs = "Pending operator sign-off in Approvals center.\nTarget: GCP Instance Group us-central1-gpu-cluster-a",
                    duration = "22s",
                    timestamp = System.currentTimeMillis() - 600000L,
                    costEstimate = "$0.0019"
                ),
                TaskEntity(
                    id = "TSK-9404",
                    title = "Stale Branch Auto-Prune & Sentry Alert Sync",
                    agentId = "agent-coding",
                    agentName = "Coding Agent",
                    status = TaskStatus.FAILED,
                    input = "Delete stale remote branches inactive for >90 days.",
                    reasoningSummary = "Checked branch protection rules; encountered branch protection constraint on release/v2.4 requiring override.",
                    toolActions = listOf(
                        "Listed 32 remote branches",
                        "Attempted pruning protected branch 'release/v2.4'",
                        "Encountered HTTP 403 Protected Branch Rule from GitHub"
                    ),
                    outputs = "Execution terminated safely: Protected branch rule prevented automated deletion. Operator intervention requested.",
                    duration = "8s",
                    timestamp = System.currentTimeMillis() - 1200000L,
                    costEstimate = "$0.0008"
                )
            )
            dao.insertTasks(tasks)

            // Seed Approvals (Human-in-the-loop guardrails)
            val approvals = listOf(
                ApprovalEntity(
                    id = "APP-1091",
                    taskId = "TSK-9388",
                    agentName = "Email Agent",
                    action = "Agent wants to send client disclosure email",
                    recipient = "security-board@enterprise-client.com",
                    contentPreview = "Subject: Status Update: CVE-2026-4819 Remediation Complete\n\nDear Security Board,\nAll edge gateways have been successfully updated with zero downtime...",
                    reason = "External correspondence with tier-1 enterprise customer regarding security advisory status.",
                    severity = SecurityLevel.HIGH,
                    status = ApprovalStatus.PENDING,
                    requestedAt = "5 mins ago"
                ),
                ApprovalEntity(
                    id = "APP-1092",
                    taskId = "TSK-9403",
                    agentName = "Finance Agent",
                    action = "Terminate 4 idling A100 GPU compute instances",
                    recipient = "gcp://us-central1-gpu-cluster-a",
                    contentPreview = "Command: gcloud compute instances delete a100-worker-01..04 --zone=us-central1-a\nImpact: Saves $14,200/mo, terminates unattached ephemeral caches.",
                    reason = "Cost optimization action exceeding $5,000 threshold requires SecOps/FinOps authorization.",
                    severity = SecurityLevel.CRITICAL,
                    status = ApprovalStatus.PENDING,
                    requestedAt = "10 mins ago"
                ),
                ApprovalEntity(
                    id = "APP-1090",
                    taskId = "TSK-9372",
                    agentName = "Coding Agent",
                    action = "Merge patch branch to main and deploy to staging",
                    recipient = "github://acme/auth-gw/pull/482",
                    contentPreview = "Automated PR #482 passed 142/142 unit tests. Signed by AGENTOS-Bot GPG Key.",
                    reason = "Code change affects core authentication gateway routing.",
                    severity = SecurityLevel.MEDIUM,
                    status = ApprovalStatus.APPROVED,
                    requestedAt = "45 mins ago"
                )
            )
            dao.insertApprovals(approvals)

            // Seed Workflows
            val workflows = listOf(
                WorkflowEntity(
                    id = "WF-01",
                    name = "Autonomous CVE Alert to PR Pipeline",
                    description = "Detects vulnerability notices, assigns Coding Agent, runs AST patch, requests SecOps approval, merges.",
                    trigger = "Snyk / GitHub Security Webhook",
                    agentId = "agent-coding",
                    agentName = "Coding Agent",
                    tool = "GitHub + Snyk CLI",
                    condition = "CVSS >= 7.0 && Patch Available",
                    approvalRequired = true,
                    action = "Create Pull Request & Notify Slack #secops",
                    enabled = true,
                    lastRun = "12 mins ago"
                ),
                WorkflowEntity(
                    id = "WF-02",
                    name = "Executive Daily Schedule & Inbox Sync",
                    description = "Scans calendar clashes, drafts prioritized replies, plays morning focus soundtrack.",
                    trigger = "Schedule: Every weekday at 08:30 AM",
                    agentId = "agent-personal",
                    agentName = "Personal Assistant",
                    tool = "Google Calendar + Email + Spotify",
                    condition = "Unread VIP Emails > 0",
                    approvalRequired = false,
                    action = "Generate Daily Action Card & Open Spotify",
                    enabled = true,
                    lastRun = "Today 08:30 AM"
                ),
                WorkflowEntity(
                    id = "WF-03",
                    name = "Cloud Billing Spike Auto-Mitigator",
                    description = "Monitors GCP/AWS hourly burn rate, quarantines runaway compute loops, halts rogue jobs.",
                    trigger = "CloudWatch / GCP Billing Alert",
                    agentId = "agent-finance",
                    agentName = "Finance Agent",
                    tool = "GCP Cloud Console API + Slack",
                    condition = "Hourly Burn > $500/hr",
                    approvalRequired = true,
                    action = "Downscale ReplicaSet to Minimum Safe Threshold",
                    enabled = true,
                    lastRun = "Yesterday 23:10"
                )
            )
            dao.insertWorkflows(workflows)

            // Seed User-Controlled Memory
            val memories = listOf(
                MemoryEntity(
                    id = "MEM-01",
                    key = "primary_code_style_preference",
                    savedInformation = "Kotlin 2.2+ with Jetpack Compose M3, strict nullability, sealed interface UI states, Room repository pattern.",
                    source = "Repository code inspection & user instructions",
                    createdAt = "2026-09-10 14:22",
                    usedByAgent = "Coding Agent",
                    isProtected = true
                ),
                MemoryEntity(
                    id = "MEM-02",
                    key = "executive_time_zone_calendar",
                    savedInformation = "Home base: San Francisco (PST/PDT), preferred meeting windows: 10:00 AM - 3:30 PM, no meetings on Friday afternoons.",
                    source = "Google Calendar sync",
                    createdAt = "2026-09-12 09:15",
                    usedByAgent = "Personal Assistant, Travel Agent",
                    isProtected = true
                ),
                MemoryEntity(
                    id = "MEM-03",
                    key = "corporate_security_policy_cvss",
                    savedInformation = "All vulnerabilities with CVSS >= 7.0 must be patched within 24 hours. Automated PRs require two-factor approval for production.",
                    source = "SecOps Handbook PDF ingest",
                    createdAt = "2026-09-14 11:40",
                    usedByAgent = "Research Agent, Coding Agent",
                    isProtected = true
                ),
                MemoryEntity(
                    id = "MEM-04",
                    key = "preferred_focus_track",
                    savedInformation = "Ambient synth, 60-80 BPM, zero lyrics during deep programming blocks.",
                    source = "Spotify listening analysis",
                    createdAt = "2026-09-15 16:04",
                    usedByAgent = "Personal Assistant",
                    isProtected = false
                )
            )
            dao.insertMemories(memories)

            // Seed Real-time Activity Stream
            val activities = listOf(
                ActivityEntity(id = "ACT-101", timestamp = "09:32", agentName = "Research Agent", message = "Started vulnerability impact study task", category = "Execution", status = "INFO"),
                ActivityEntity(id = "ACT-102", timestamp = "09:33", agentName = "Research Agent", message = "Searched connected sources: GitHub Advisories, CISA", category = "Tool", status = "INFO"),
                ActivityEntity(id = "ACT-103", timestamp = "09:35", agentName = "Research Agent", message = "Synthesized and published report 'SecOps Briefing Week 38'", category = "Execution", status = "SUCCESS"),
                ActivityEntity(id = "ACT-104", timestamp = "09:41", agentName = "Coding Agent", message = "Detected vulnerability CVE-2026-4819 in Auth Gateway", category = "Security", status = "WARNING"),
                ActivityEntity(id = "ACT-105", timestamp = "09:42", agentName = "Coding Agent", message = "Applied patch diff (+14/-3 lines) & verified test pass", category = "Tool", status = "SUCCESS"),
                ActivityEntity(id = "ACT-106", timestamp = "09:45", agentName = "Email Agent", message = "Drafted client status email; held for human approval", category = "Approval", status = "WARNING")
            )
            dao.insertActivities(activities)

            // Seed Audit Logs
            val auditLogs = listOf(
                AuditLogEntity(id = "AUD-901", timestamp = "2026-09-16 09:45:12", actor = "agent:Email", ipAddress = "10.14.2.80", action = "POLICY_GUARD_INTERCEPT", target = "mail.send:external", status = "HELD_FOR_APPROVAL"),
                AuditLogEntity(id = "AUD-902", timestamp = "2026-09-16 09:42:04", actor = "agent:Coding", ipAddress = "10.14.2.84", action = "BRANCH_CREATE", target = "github://acme/auth-gw", status = "ALLOW"),
                AuditLogEntity(id = "AUD-903", timestamp = "2026-09-16 09:35:10", actor = "agent:Research", ipAddress = "10.14.2.77", action = "NOTION_PAGE_WRITE", target = "notion://page/secops-w38", status = "COMPLETED"),
                AuditLogEntity(id = "AUD-904", timestamp = "2026-09-16 09:12:00", actor = "user:security_admin", ipAddress = "192.168.1.104", action = "MFA_AUTH_VERIFY", target = "agentos://session", status = "SUCCESS")
            )
            dao.insertAuditLogs(auditLogs)
        }
    }
}
