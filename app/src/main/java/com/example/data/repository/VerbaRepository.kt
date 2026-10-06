package com.example.data.repository

import com.example.domain.model.ChatMessage
import com.example.domain.model.IngestionOptions
import com.example.domain.model.KnowledgeSession
import com.example.domain.model.MediaType
import com.example.domain.model.ProcessingProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

interface VerbaRepository {

    fun getAllSessions(): Flow<List<KnowledgeSession>>

    fun getSessionById(id: String): Flow<KnowledgeSession?>

    suspend fun getSessionSync(id: String): KnowledgeSession?

    fun ingestText(
        title: String,
        text: String,
        options: IngestionOptions = IngestionOptions()
    ): Flow<ProcessingProgress>

    fun ingestUrl(
        title: String?,
        url: String,
        extractMode: String = "article"
    ): Flow<ProcessingProgress>

    fun ingestFile(
        file: File,
        mediaType: MediaType,
        title: String,
        options: IngestionOptions = IngestionOptions()
    ): Flow<ProcessingProgress>

    suspend fun toggleActionItem(sessionId: String, actionItemId: String)

    suspend fun deleteSession(sessionId: String)

    fun getChatMessages(sessionId: String): Flow<List<ChatMessage>>

    fun askQuestionStream(
        sessionId: String,
        question: String
    ): Flow<String>

    suspend fun seedSampleDataIfEmpty()
}
