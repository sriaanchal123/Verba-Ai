package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.audio.RecorderState
import com.example.data.audio.RecordingStatus
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryCyan
import java.io.File

@Composable
fun AudioRecorderDialog(
    recorderState: RecorderState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> File?,
    onCancel: () -> Unit,
    onSubmitRecording: (title: String, file: File) -> Unit
) {
    var title by remember { mutableStateOf("Live Meeting Recording") }
    val isRecording = recorderState.status == RecordingStatus.RECORDING
    val isStopped = recorderState.status == RecordingStatus.STOPPED

    val minutes = recorderState.durationSeconds / 60
    val seconds = recorderState.durationSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Dialog(onDismissRequest = {
        if (!isRecording) onCancel()
    }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("audio_recorder_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Multimodal Audio Recorder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Record meetings, lectures, or interviews for speech-to-text diarization & insights",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recording_title_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Waveform visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    AudioWaveformView(
                        waveformPoints = recorderState.waveformPoints,
                        isRecording = isRecording
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer & Decibel display
                Text(
                    text = formattedTime,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) AccentRose else MaterialTheme.colorScheme.onSurface
                )

                if (isRecording) {
                    Text(
                        text = "Recording amplitude: ${recorderState.currentDecibels.toInt()} dB",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRecording && !isStopped) {
                        Button(
                            onClick = onStartRecording,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            modifier = Modifier.testTag("start_recording_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Start Recording")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Recording")
                        }
                    } else if (isRecording) {
                        IconButton(
                            onClick = { onStopRecording() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AccentRose)
                                .testTag("stop_recording_button")
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Stop Recording",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else if (isStopped && recorderState.outputFile != null) {
                        Button(
                            onClick = {
                                onSubmitRecording(title, recorderState.outputFile)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                            modifier = Modifier.testTag("submit_recording_button")
                        ) {
                            Text("Extract Knowledge & Insights")
                        }
                    }
                }

                recorderState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("cancel_recording_button")
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
