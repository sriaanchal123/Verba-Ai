package com.example.domain.model

enum class MediaType {
    DOCUMENT,
    TEXT,
    VIDEO,
    AUDIO,
    URL
}

enum class StatusState {
    IDLE,
    UPLOADING,
    PROCESSING,
    TRANSCRIBING,
    EXTRACTING_INSIGHTS,
    COMPLETED,
    FAILED
}

data class ProcessingProgress(
    val state: StatusState = StatusState.IDLE,
    val progressPercent: Int = 0,
    val currentStep: String = "",
    val errorMessage: String? = null
)

data class IngestionOptions(
    val language: String = "en",
    val extractActionItems: Boolean = true,
    val detailedTranscript: Boolean = true,
    val summaryLength: String = "Balanced" // Short, Balanced, Comprehensive
)

data class IngestionRequest(
    val id: String,
    val mediaType: MediaType,
    val title: String,
    val sourceUriOrText: String,
    val options: IngestionOptions = IngestionOptions(),
    val createdAt: Long = System.currentTimeMillis()
)

data class SummaryResult(
    val id: String,
    val sessionId: String,
    val title: String,
    val mediaType: MediaType,
    val tlDr: String,
    val bulletPoints: List<String>,
    val executiveOverview: String,
    val wordCount: Int = 0,
    val readingTimeMinutes: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ActionPriority {
    HIGH,
    MEDIUM,
    LOW
}

data class ActionItem(
    val id: String,
    val task: String,
    val assignee: String = "Unassigned",
    val priority: ActionPriority = ActionPriority.MEDIUM,
    val dueDate: String = "No due date",
    val isCompleted: Boolean = false
)

data class TranscriptEntry(
    val id: String,
    val speaker: String,
    val timestampSeconds: Int,
    val timestampFormatted: String,
    val text: String
)

data class SourceChunk(
    val id: String,
    val documentName: String,
    val chunkIndex: Int,
    val snippet: String,
    val confidenceScore: Float,
    val pageOrTime: String
)

enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: ChatRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val groundedSources: List<SourceChunk> = emptyList(),
    val isStreaming: Boolean = false
)

data class KnowledgeSession(
    val id: String,
    val title: String,
    val mediaType: MediaType,
    val status: StatusState,
    val originalSource: String,
    val createdAt: Long = System.currentTimeMillis(),
    val summary: SummaryResult? = null,
    val actionItems: List<ActionItem> = emptyList(),
    val transcript: List<TranscriptEntry> = emptyList(),
    val sources: List<SourceChunk> = emptyList()
)
