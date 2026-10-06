package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VerbaDao {

    @Query("SELECT * FROM ingestion_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<IngestionSessionEntity>>

    @Query("SELECT * FROM ingestion_sessions WHERE id = :id LIMIT 1")
    fun getSessionById(id: String): Flow<IngestionSessionEntity?>

    @Query("SELECT * FROM ingestion_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionByIdSync(id: String): IngestionSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: IngestionSessionEntity)

    @Query("DELETE FROM ingestion_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("UPDATE ingestion_sessions SET actionItemsJson = :actionItemsJson WHERE id = :id")
    suspend fun updateActionItems(id: String, actionItemsJson: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearMessagesForSession(sessionId: String)

    @Query("SELECT COUNT(*) FROM ingestion_sessions")
    suspend fun getSessionCount(): Int
}
