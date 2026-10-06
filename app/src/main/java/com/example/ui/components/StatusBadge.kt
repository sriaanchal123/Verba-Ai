package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MediaType
import com.example.domain.model.StatusState
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.SecondaryIndigo

@Composable
fun StatusBadge(
    state: StatusState,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = when (state) {
        StatusState.COMPLETED -> Quad(
            AccentEmerald.copy(alpha = 0.15f),
            AccentEmerald,
            "Completed",
            Icons.Default.CheckCircle
        )
        StatusState.UPLOADING,
        StatusState.PROCESSING,
        StatusState.TRANSCRIBING,
        StatusState.EXTRACTING_INSIGHTS -> Quad(
            PrimaryCyan.copy(alpha = 0.15f),
            PrimaryCyan,
            when (state) {
                StatusState.TRANSCRIBING -> "Transcribing"
                StatusState.EXTRACTING_INSIGHTS -> "Extracting"
                else -> "Processing"
            },
            Icons.Default.Sync
        )
        StatusState.FAILED -> Quad(
            AccentRose.copy(alpha = 0.15f),
            AccentRose,
            "Failed",
            Icons.Default.Error
        )
        StatusState.IDLE -> Quad(
            Color.Gray.copy(alpha = 0.15f),
            Color.Gray,
            "Ready",
            Icons.Default.CheckCircle
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state == StatusState.PROCESSING || state == StatusState.UPLOADING || state == StatusState.TRANSCRIBING || state == StatusState.EXTRACTING_INSIGHTS) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = textColor
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun MediaTypeBadge(
    mediaType: MediaType,
    modifier: Modifier = Modifier
) {
    val (icon, label, color) = when (mediaType) {
        MediaType.DOCUMENT -> Triple(Icons.Default.Description, "Document", PrimaryCyan)
        MediaType.AUDIO -> Triple(Icons.Default.Audiotrack, "Audio / Meeting", SecondaryIndigo)
        MediaType.VIDEO -> Triple(Icons.Default.Videocam, "Video", Color(0xFFE11D48))
        MediaType.TEXT -> Triple(Icons.Default.Notes, "Pasted Text", Color(0xFF0D9488))
        MediaType.URL -> Triple(Icons.Default.Language, "Web Link", Color(0xFFD97706))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
