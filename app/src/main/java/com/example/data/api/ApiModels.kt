package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TextIngestRequestDto(
    @Json(name = "title") val title: String,
    @Json(name = "text") val text: String,
    @Json(name = "language") val language: String? = "en",
    @Json(name = "extract_action_items") val extractActionItems: Boolean = true,
    @Json(name = "summary_length") val summaryLength: String = "Balanced"
)

@JsonClass(generateAdapter = true)
data class UrlIngestRequestDto(
    @Json(name = "title") val title: String?,
    @Json(name = "url") val url: String,
    @Json(name = "extract_mode") val extractMode: String? = "article"
)

@JsonClass(generateAdapter = true)
data class ChatHistoryItemDto(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class RagChatRequestDto(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "query") val query: String,
    @Json(name = "conversation_history") val conversationHistory: List<ChatHistoryItemDto>? = null,
    @Json(name = "top_k") val topK: Int = 4
)

@JsonClass(generateAdapter = true)
data class IngestResponseDto(
    @Json(name = "task_id") val taskId: String,
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class StatusResponseDto(
    @Json(name = "task_id") val taskId: String,
    @Json(name = "state") val state: String,
    @Json(name = "progress_percent") val progressPercent: Int,
    @Json(name = "current_step") val currentStep: String,
    @Json(name = "error_message") val errorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class SummaryResponseDto(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "title") val title: String,
    @Json(name = "media_type") val mediaType: String,
    @Json(name = "tl_dr") val tlDr: String,
    @Json(name = "bullet_points") val bulletPoints: List<String>,
    @Json(name = "executive_overview") val executiveOverview: String,
    @Json(name = "word_count") val wordCount: Int = 0,
    @Json(name = "reading_time_minutes") val readingTimeMinutes: Int = 1
)

@JsonClass(generateAdapter = true)
data class ActionItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "task") val task: String,
    @Json(name = "assignee") val assignee: String? = "Unassigned",
    @Json(name = "priority") val priority: String? = "MEDIUM",
    @Json(name = "due_date") val dueDate: String? = "No due date",
    @Json(name = "is_completed") val isCompleted: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class TranscriptEntryDto(
    @Json(name = "id") val id: String,
    @Json(name = "speaker") val speaker: String,
    @Json(name = "timestamp_seconds") val timestampSeconds: Int,
    @Json(name = "timestamp_formatted") val timestampFormatted: String,
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class SourceChunkDto(
    @Json(name = "id") val id: String,
    @Json(name = "document_name") val documentName: String,
    @Json(name = "chunk_index") val chunkIndex: Int,
    @Json(name = "snippet") val snippet: String,
    @Json(name = "confidence_score") val confidenceScore: Float,
    @Json(name = "page_or_time") val pageOrTime: String
)

@JsonClass(generateAdapter = true)
data class KnowledgeSessionDto(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "title") val title: String,
    @Json(name = "media_type") val mediaType: String,
    @Json(name = "status") val status: String,
    @Json(name = "summary") val summary: SummaryResponseDto?,
    @Json(name = "action_items") val actionItems: List<ActionItemDto>?,
    @Json(name = "transcript") val transcript: List<TranscriptEntryDto>?,
    @Json(name = "sources") val sources: List<SourceChunkDto>?
)

@JsonClass(generateAdapter = true)
data class RagChatResponseDto(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "answer") val answer: String,
    @Json(name = "grounded_sources") val groundedSources: List<SourceChunkDto>? = null
)
