package com.example

import com.example.domain.model.ActionItem
import com.example.domain.model.ActionPriority
import com.example.domain.model.KnowledgeSession
import com.example.domain.model.MediaType
import com.example.domain.model.SourceChunk
import com.example.domain.model.StatusState
import com.example.domain.model.SummaryResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerbaAiTest {

    @Test
    fun testActionItemCreationAndToggle() {
        val item = ActionItem(
            id = "test-1",
            task = "Benchmark vector search",
            assignee = "Dev Lead",
            priority = ActionPriority.HIGH,
            dueDate = "Friday",
            isCompleted = false
        )

        assertFalse(item.isCompleted)
        val completed = item.copy(isCompleted = true)
        assertTrue(completed.isCompleted)
        assertEquals("Benchmark vector search", completed.task)
    }

    @Test
    fun testSummaryResultProperties() {
        val summary = SummaryResult(
            id = "sum-1",
            sessionId = "sess-1",
            title = "Transformer Architecture",
            mediaType = MediaType.DOCUMENT,
            tlDr = "Attention mechanism replaces recurrence.",
            bulletPoints = listOf("Self-attention", "Parallel training"),
            executiveOverview = "Detailed overview of Transformer.",
            wordCount = 2500,
            readingTimeMinutes = 5
        )

        assertEquals(MediaType.DOCUMENT, summary.mediaType)
        assertEquals(2, summary.bulletPoints.size)
        assertEquals(5, summary.readingTimeMinutes)
    }

    @Test
    fun testSourceChunkConfidence() {
        val chunk = SourceChunk(
            id = "chunk-1",
            documentName = "system_architecture.pdf",
            chunkIndex = 2,
            snippet = "Low-latency streaming tokens",
            confidenceScore = 0.95f,
            pageOrTime = "Page 4"
        )

        assertTrue(chunk.confidenceScore > 0.9f)
        assertEquals("Page 4", chunk.pageOrTime)
    }

    @Test
    fun testSessionChronologicalGrouping() {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600000L
        val twoDaysAgo = now - 172800000L

        val s1 = KnowledgeSession(
            id = "s1",
            title = "Morning Meeting",
            mediaType = MediaType.AUDIO,
            status = StatusState.COMPLETED,
            originalSource = "rec.m4a",
            createdAt = oneHourAgo
        )
        val s2 = KnowledgeSession(
            id = "s2",
            title = "Older Research Paper",
            mediaType = MediaType.DOCUMENT,
            status = StatusState.COMPLETED,
            originalSource = "doc.pdf",
            createdAt = twoDaysAgo
        )

        assertTrue(now - s1.createdAt < 86400000L)
        assertTrue(now - s2.createdAt >= 86400000L)
    }
}
