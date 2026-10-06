package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingestion_sessions")
data class IngestionSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val mediaType: String,
    val status: String,
    val originalSource: String,
    val createdAt: Long,
    val tlDr: String? = null,
    val bulletPointsJson: String? = null,
    val executiveOverview: String? = null,
    val wordCount: Int = 0,
    val readingTimeMinutes: Int = 1,
    val actionItemsJson: String? = null,
    val transcriptJson: String? = null,
    val sourcesJson: String? = null
)
