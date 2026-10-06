package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.AudioRecorderManager
import com.example.data.audio.RecorderState
import com.example.data.auth.AuthRepository
import com.example.data.auth.UserProfile
import com.example.data.notification.VerbaNotificationManager
import com.example.data.repository.VerbaRepository
import com.example.domain.model.IngestionOptions
import com.example.domain.model.KnowledgeSession
import com.example.domain.model.MediaType
import com.example.domain.model.ProcessingProgress
import com.example.domain.model.StatusState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

data class DashboardUiState(
    val isIngesting: Boolean = false,
    val currentProgress: ProcessingProgress = ProcessingProgress(),
    val showAudioRecorder: Boolean = false,
    val showTextIngest: Boolean = false,
    val showUrlIngest: Boolean = false,
    val selectedFilter: MediaType? = null,
    val drawerSearchQuery: String = ""
)

data class ChronologicalSessionGroup(
    val title: String,
    val sessions: List<KnowledgeSession>
)

class DashboardViewModel(
    private val repository: VerbaRepository,
    private val audioRecorder: AudioRecorderManager,
    private val authRepository: AuthRepository,
    private val notificationManager: VerbaNotificationManager
) : ViewModel() {

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser

    val sessions: StateFlow<List<KnowledgeSession>> = repository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recorderState: StateFlow<RecorderState> = audioRecorder.recorderState

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    fun setDrawerSearch(query: String) {
        _uiState.value = _uiState.value.copy(drawerSearchQuery = query)
    }

    fun getGroupedSessions(query: String = ""): List<ChronologicalSessionGroup> {
        val all = sessions.value.filter {
            query.isBlank() || it.title.contains(query, ignoreCase = true)
        }

        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val twoDaysMs = 2 * oneDayMs
        val sevenDaysMs = 7 * oneDayMs

        val todayList = mutableListOf<KnowledgeSession>()
        val yesterdayList = mutableListOf<KnowledgeSession>()
        val prev7DaysList = mutableListOf<KnowledgeSession>()
        val olderList = mutableListOf<KnowledgeSession>()

        all.forEach { session ->
            val diff = now - session.createdAt
            when {
                diff < oneDayMs -> todayList.add(session)
                diff < twoDaysMs -> yesterdayList.add(session)
                diff < sevenDaysMs -> prev7DaysList.add(session)
                else -> olderList.add(session)
            }
        }

        val groups = mutableListOf<ChronologicalSessionGroup>()
        if (todayList.isNotEmpty()) groups.add(ChronologicalSessionGroup("Today", todayList))
        if (yesterdayList.isNotEmpty()) groups.add(ChronologicalSessionGroup("Yesterday", yesterdayList))
        if (prev7DaysList.isNotEmpty()) groups.add(ChronologicalSessionGroup("Previous 7 Days", prev7DaysList))
        if (olderList.isNotEmpty()) groups.add(ChronologicalSessionGroup("Older", olderList))

        return groups
    }

    fun openAudioRecorder() {
        audioRecorder.reset()
        _uiState.value = _uiState.value.copy(showAudioRecorder = true)
    }

    fun closeAudioRecorder() {
        audioRecorder.reset()
        _uiState.value = _uiState.value.copy(showAudioRecorder = false)
    }

    fun startRecording(): Boolean {
        return audioRecorder.startRecording()
    }

    fun stopRecording(): File? {
        return audioRecorder.stopRecording()
    }

    fun submitAudioRecording(title: String, file: File) {
        _uiState.value = _uiState.value.copy(showAudioRecorder = false, isIngesting = true)
        notificationManager.showProgressNotification(1001, title, "Uploading audio & initializing Whisper...", 10)

        viewModelScope.launch {
            repository.ingestFile(
                file = file,
                mediaType = MediaType.AUDIO,
                title = title,
                options = IngestionOptions(detailedTranscript = true, extractActionItems = true)
            ).collect { progress ->
                _uiState.value = _uiState.value.copy(currentProgress = progress)
                notificationManager.showProgressNotification(1001, title, progress.currentStep, progress.progressPercent)

                if (progress.state == StatusState.COMPLETED) {
                    _uiState.value = _uiState.value.copy(isIngesting = false)
                    notificationManager.showCompletionNotification(
                        1002,
                        file.name,
                        title,
                        "Meeting recording transcribed with speaker diarization & action items."
                    )
                    _snackbarEvent.emit("Audio session '$title' ingested and transcribed!")
                }
            }
        }
    }

    fun openTextIngest() {
        _uiState.value = _uiState.value.copy(showTextIngest = true)
    }

    fun closeTextIngest() {
        _uiState.value = _uiState.value.copy(showTextIngest = false)
    }

    fun submitTextIngest(title: String, text: String, options: IngestionOptions) {
        _uiState.value = _uiState.value.copy(showTextIngest = false, isIngesting = true)
        notificationManager.showProgressNotification(1001, title.ifBlank { "Text Note" }, "Tokenizing text...", 20)

        viewModelScope.launch {
            repository.ingestText(title, text, options).collect { progress ->
                _uiState.value = _uiState.value.copy(currentProgress = progress)
                notificationManager.showProgressNotification(1001, title.ifBlank { "Text Note" }, progress.currentStep, progress.progressPercent)

                if (progress.state == StatusState.COMPLETED) {
                    _uiState.value = _uiState.value.copy(isIngesting = false)
                    notificationManager.showCompletionNotification(
                        1002,
                        "text-${System.currentTimeMillis()}",
                        title.ifBlank { "Text Note" },
                        "Structured executive summary & key takeaways generated."
                    )
                    _snackbarEvent.emit("Text document processed successfully!")
                }
            }
        }
    }

    fun openUrlIngest() {
        _uiState.value = _uiState.value.copy(showUrlIngest = true)
    }

    fun closeUrlIngest() {
        _uiState.value = _uiState.value.copy(showUrlIngest = false)
    }

    fun submitUrlIngest(title: String?, url: String, mode: String) {
        _uiState.value = _uiState.value.copy(showUrlIngest = false, isIngesting = true)
        notificationManager.showProgressNotification(1001, title ?: url, "Crawling page & parsing content...", 25)

        viewModelScope.launch {
            repository.ingestUrl(title, url, mode).collect { progress ->
                _uiState.value = _uiState.value.copy(currentProgress = progress)
                notificationManager.showProgressNotification(1001, title ?: url, progress.currentStep, progress.progressPercent)

                if (progress.state == StatusState.COMPLETED) {
                    _uiState.value = _uiState.value.copy(isIngesting = false)
                    notificationManager.showCompletionNotification(
                        1002,
                        "url-${System.currentTimeMillis()}",
                        title ?: "Web Article",
                        "Content indexed and grounded for conversational RAG queries."
                    )
                    _snackbarEvent.emit("Web resource parsed and indexed!")
                }
            }
        }
    }

    fun submitPickedFile(file: File, mediaType: MediaType, title: String) {
        _uiState.value = _uiState.value.copy(isIngesting = true)
        notificationManager.showProgressNotification(1001, title, "Uploading document...", 15)

        viewModelScope.launch {
            repository.ingestFile(file, mediaType, title).collect { progress ->
                _uiState.value = _uiState.value.copy(currentProgress = progress)
                notificationManager.showProgressNotification(1001, title, progress.currentStep, progress.progressPercent)

                if (progress.state == StatusState.COMPLETED) {
                    _uiState.value = _uiState.value.copy(isIngesting = false)
                    notificationManager.showCompletionNotification(
                        1002,
                        file.name,
                        title,
                        "Document decomposed into searchable vector chunks."
                    )
                    _snackbarEvent.emit("File '${file.name}' processed successfully!")
                }
            }
        }
    }

    fun setFilter(filter: MediaType?) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            _snackbarEvent.emit("Session deleted")
        }
    }

    fun seedSamples() {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
            _snackbarEvent.emit("Sample multimodal datasets reloaded")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
