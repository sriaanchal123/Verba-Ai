package com.example.ui.dashboard

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.KnowledgeSession
import com.example.domain.model.MediaType
import com.example.domain.model.StatusState
import com.example.ui.components.AudioRecorderDialog
import com.example.ui.components.MediaTypeBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.TextIngestDialog
import com.example.ui.components.UrlIngestDialog
import com.example.ui.components.VerbaLogo
import com.example.ui.components.VerbaTopBar
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepIndigo
import com.example.ui.theme.PaperBg
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToHub: (sessionId: String) -> Unit,
    onNavigateToChat: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val recorderState by viewModel.recorderState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher for mic permission
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.openAudioRecorder()
        } else {
            Toast.makeText(context, "Microphone permission is required to record meetings", Toast.LENGTH_SHORT).show()
        }
    }

    // Document file picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = copyUriToCacheFile(context, it, "imported_doc.pdf")
            if (file != null) {
                viewModel.submitPickedFile(file, MediaType.DOCUMENT, file.nameWithoutExtension)
            }
        }
    }

    // Video file picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = copyUriToCacheFile(context, it, "imported_video.mp4")
            if (file != null) {
                viewModel.submitPickedFile(file, MediaType.VIDEO, file.nameWithoutExtension)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            VerbaTopBar(
                onOpenDrawer = onOpenDrawer,
                onResetSamples = { viewModel.seedSamples() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PaperBg,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_screen")
        ) {
            // 1. Human-Crafted Hero Ingestion Banner
            item {
                HeroIngestionBanner(
                    onRecordMeeting = {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onPasteNotes = { viewModel.openTextIngest() }
                )
            }

            // Ingestion Progress Indicator
            if (uiState.isIngesting) {
                item {
                    IngestionProgressCard(progress = uiState.currentProgress)
                }
            }

            // 2. Multimodal Input Grid Cards (Section Title)
            item {
                Text(
                    text = "Multimodal Input Sources",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Transform any content into structured knowledge and conversational RAG",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // 5 Multimodal Cards in horizontal row with tactile light styling
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        InputMethodCard(
                            title = "Documents",
                            subtitle = "PDFs, slides, papers",
                            icon = Icons.Default.Description,
                            badge = "PDF / OCR",
                            iconBg = Color(0xFFEFF6FF),
                            iconTint = DeepIndigo,
                            testTag = "input_card_documents",
                            onClick = { documentPickerLauncher.launch("application/pdf") }
                        )
                    }
                    item {
                        InputMethodCard(
                            title = "Live Audio",
                            subtitle = "Record meetings, calls",
                            icon = Icons.Default.Mic,
                            badge = "Whisper STT",
                            iconBg = Color(0xFFEEF2FF),
                            iconTint = Color(0xFF4F46E5),
                            testTag = "input_card_audio",
                            onClick = {
                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                    }
                    item {
                        InputMethodCard(
                            title = "Raw Text",
                            subtitle = "Paste notes, transcripts",
                            icon = Icons.Default.Notes,
                            badge = "Fast Tokenizer",
                            iconBg = Color(0xFFF0FDF4),
                            iconTint = SageGreen,
                            testTag = "input_card_text",
                            onClick = { viewModel.openTextIngest() }
                        )
                    }
                    item {
                        InputMethodCard(
                            title = "Web URLs",
                            subtitle = "Articles, ArXiv, blogs",
                            icon = Icons.Default.Language,
                            badge = "Web Crawler",
                            iconBg = Color(0xFFFFFBEB),
                            iconTint = WarmAmber,
                            testTag = "input_card_urls",
                            onClick = { viewModel.openUrlIngest() }
                        )
                    }
                    item {
                        InputMethodCard(
                            title = "Videos",
                            subtitle = "MP4 uploads & lectures",
                            icon = Icons.Default.Videocam,
                            badge = "Video A/V",
                            iconBg = Color(0xFFFFF1F2),
                            iconTint = Color(0xFFE11D48),
                            testTag = "input_card_videos",
                            onClick = { videoPickerLauncher.launch("video/*") }
                        )
                    }
                }
            }

            // 3. Filter Row for History Feed
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Knowledge Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${sessions.size} indexed",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepIndigo,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedFilter == null,
                            onClick = { viewModel.setFilter(null) },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEFF6FF),
                                selectedLabelColor = DeepIndigo
                            )
                        )
                    }
                    MediaType.values().forEach { type ->
                        item {
                            FilterChip(
                                selected = uiState.selectedFilter == type,
                                onClick = {
                                    viewModel.setFilter(if (uiState.selectedFilter == type) null else type)
                                },
                                label = { Text(type.name.capitalize(Locale.ROOT), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEFF6FF),
                                    selectedLabelColor = DeepIndigo
                                )
                            )
                        }
                    }
                }
            }

            // 4. Session History Feed
            val filteredSessions = if (uiState.selectedFilter != null) {
                sessions.filter { it.mediaType == uiState.selectedFilter }
            } else {
                sessions
            }

            if (filteredSessions.isEmpty()) {
                item {
                    EmptyHistoryCard(
                        onSeed = { viewModel.seedSamples() }
                    )
                }
            } else {
                items(filteredSessions, key = { it.id }) { session ->
                    SessionFeedCard(
                        session = session,
                        onOpenHub = { onNavigateToHub(session.id) },
                        onOpenChat = { onNavigateToChat(session.id) },
                        onDelete = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (uiState.showAudioRecorder) {
        AudioRecorderDialog(
            recorderState = recorderState,
            onStartRecording = { viewModel.startRecording() },
            onStopRecording = { viewModel.stopRecording() },
            onCancel = { viewModel.closeAudioRecorder() },
            onSubmitRecording = { title, file ->
                viewModel.submitAudioRecording(title, file)
            }
        )
    }

    if (uiState.showTextIngest) {
        TextIngestDialog(
            onDismiss = { viewModel.closeTextIngest() },
            onSubmit = { title, text, options ->
                viewModel.submitTextIngest(title, text, options)
            }
        )
    }

    if (uiState.showUrlIngest) {
        UrlIngestDialog(
            onDismiss = { viewModel.closeUrlIngest() },
            onSubmit = { title, url, mode ->
                viewModel.submitUrlIngest(title, url, mode)
            }
        )
    }
}

@Composable
private fun HeroIngestionBanner(
    onRecordMeeting: () -> Unit,
    onPasteNotes: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_ingestion_banner")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MULTIMODAL CLARITY ENGINE",
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("v2.4", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Deconstruct documents, meetings & web streams",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 25.sp
            )

            Text(
                text = "Speaker diarization, executive TL;DRs, action items with owners, and grounded conversational Q&A.",
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onRecordMeeting)
                        .testTag("hero_record_cta")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Record", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Meeting", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onPasteNotes)
                        .testTag("hero_paste_cta")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Notes, contentDescription = "Paste", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paste Text", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun IngestionProgressCard(progress: com.example.domain.model.ProcessingProgress) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ingestion_progress_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = DeepIndigo
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = progress.currentStep.ifBlank { "Processing Multimodal Input..." },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }
                Text(
                    text = "${progress.progressPercent}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = DeepIndigo,
                trackColor = Color(0xFFDBEAFE)
            )
        }
    }
}

@Composable
private fun InputMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    iconBg: Color,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .width(144.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate100)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SessionFeedCard(
    session: KnowledgeSession,
    onOpenHub: () -> Unit,
    onOpenChat: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenHub)
            .testTag("session_card_${session.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Badges and delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MediaTypeBadge(mediaType = session.mediaType)
                    StatusBadge(state = session.status)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Session",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            session.summary?.tlDr?.let { tldr ->
                Text(
                    text = tldr,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer meta & actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (session.actionItems.isNotEmpty()) {
                        val pending = session.actionItems.count { !it.isCompleted }
                        Text(
                            text = "$pending pending actions",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepIndigo
                        )
                        Text(
                            text = " • ",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Text(
                        text = formatTimestamp(session.createdAt),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Chat button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEF2FF))
                            .clickable(onClick = onOpenChat)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("chat_button_${session.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = "Chat",
                                tint = DeepIndigo,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Q&A", color = DeepIndigo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Open hub button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable(onClick = onOpenHub)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View Hub", color = Slate700, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "View",
                                tint = Slate700,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard(onSeed: () -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = "Empty",
                modifier = Modifier.size(44.dp),
                tint = DeepIndigo
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Knowledge Sessions Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Record a meeting, upload a PDF, paste text, or reload sample multimodal data to get started.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                modifier = Modifier.clickable(onClick = onSeed)
            ) {
                Text(
                    text = "Load Sample Knowledge Bases",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun copyUriToCacheFile(context: Context, uri: Uri, fallbackName: String): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val targetFile = File(context.cacheDir, "${System.currentTimeMillis()}_$fallbackName")
        FileOutputStream(targetFile).use { output ->
            inputStream.copyTo(output)
        }
        targetFile
    } catch (e: Exception) {
        null
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(timestamp))
}
