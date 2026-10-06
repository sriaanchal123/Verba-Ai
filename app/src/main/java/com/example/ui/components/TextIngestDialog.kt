package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.IngestionOptions
import com.example.ui.theme.PrimaryCyan

@Composable
fun TextIngestDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, text: String, options: IngestionOptions) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var extractActionItems by remember { mutableStateOf(true) }
    var summaryLength by remember { mutableStateOf("Balanced") }

    val wordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    val charCount = text.length

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("text_ingest_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ingest Raw Text",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "AI Extraction",
                        tint = PrimaryCyan
                    )
                }

                Text(
                    text = "Paste meeting minutes, interview notes, or technical specs for multimodal decomposition.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Quick sample buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            title = "Executive Architecture Review"
                            text = """
                                Project Titan Architecture Sync Notes:
                                Attendees: Maya (Platform), Dev (Database), Chloe (Security)
                                
                                Key Discussion:
                                1. We decided to adopt Kafka partitioning for multi-tenant event streaming. Dev will lead the benchmarking to test 20,000 events/sec.
                                2. Security audit raised concern over cleartext vector metadata. Chloe must enforce AES-256 field encryption before next sprint release.
                                3. Maya confirmed that the Kubernetes cluster upgrade to 1.31 is scheduled for Sunday 2 AM UTC with 10-minute anticipated downtime.
                                
                                Action Items:
                                - Benchmark Kafka event throughput (Owner: Dev, Due: Thursday)
                                - Implement vector payload encryption (Owner: Chloe, Due: Friday)
                                - Notify operations team regarding Sunday maintenance window (Owner: Maya, Due: Wednesday)
                            """.trimIndent()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sample Notes", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            title = "AI Model Evaluation Report"
                            text = """
                                Benchmark summary on hybrid reasoning models:
                                Context retrieval accuracy increased by 31% when combining dense vector search with sparse BM25 keyword matching. 
                                Hallucination rates dropped below 2.4% with cross-encoder re-ranking.
                                Next step is integrating continuous RAG evaluation using Ragas framework.
                            """.trimIndent()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sample Report", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("text_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Paste Text Here") },
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 130.dp)
                        .testTag("text_content_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$wordCount words • $charCount characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Summary Depth",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Short", "Balanced", "Comprehensive").forEach { mode ->
                        FilterChip(
                            selected = summaryLength == mode,
                            onClick = { summaryLength = mode },
                            label = { Text(mode, fontSize = 12.sp) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = extractActionItems,
                        onCheckedChange = { extractActionItems = it }
                    )
                    Text("Auto-extract actionable tasks and owners", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onSubmit(
                                    title,
                                    text,
                                    IngestionOptions(
                                        extractActionItems = extractActionItems,
                                        summaryLength = summaryLength
                                    )
                                )
                            }
                        },
                        enabled = text.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        modifier = Modifier.testTag("submit_text_button")
                    ) {
                        Text("Extract Knowledge")
                    }
                }
            }
        }
    }
}
