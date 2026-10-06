package com.example.ui.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.VerbaRepository
import com.example.domain.model.KnowledgeSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HubTab {
    SUMMARY,
    INSIGHTS,
    TRANSCRIPT,
    SOURCES
}

data class KnowledgeHubUiState(
    val selectedSessionId: String? = null,
    val selectedTab: HubTab = HubTab.SUMMARY,
    val highlightedTimestampSeconds: Int? = null
)

class KnowledgeHubViewModel(
    private val repository: VerbaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KnowledgeHubUiState())
    val uiState: StateFlow<KnowledgeHubUiState> = _uiState.asStateFlow()

    val allSessions: StateFlow<List<KnowledgeSession>> = repository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentSession: StateFlow<KnowledgeSession?> = _uiState.flatMapLatest { state ->
        val id = state.selectedSessionId
        if (id != null) {
            repository.getSessionById(id)
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun selectSession(sessionId: String) {
        _uiState.value = _uiState.value.copy(selectedSessionId = sessionId)
    }

    fun selectTab(tab: HubTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun highlightTimestamp(seconds: Int) {
        _uiState.value = _uiState.value.copy(
            highlightedTimestampSeconds = seconds,
            selectedTab = HubTab.TRANSCRIPT
        )
    }

    fun toggleActionItem(actionItemId: String) {
        val sessionId = _uiState.value.selectedSessionId ?: return
        viewModelScope.launch {
            repository.toggleActionItem(sessionId, actionItemId)
        }
    }
}
