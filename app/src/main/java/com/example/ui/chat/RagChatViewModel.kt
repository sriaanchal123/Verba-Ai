package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.VerbaRepository
import com.example.domain.model.ChatMessage
import com.example.domain.model.ChatRole
import com.example.domain.model.KnowledgeSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class RagChatUiState(
    val activeSessionId: String? = null,
    val isStreaming: Boolean = false,
    val streamingChunk: String = "",
    val errorMessage: String? = null
)

class RagChatViewModel(
    private val repository: VerbaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RagChatUiState())
    val uiState: StateFlow<RagChatUiState> = _uiState.asStateFlow()

    val sessions: StateFlow<List<KnowledgeSession>> = repository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val messages: StateFlow<List<ChatMessage>> = _uiState.flatMapLatest { state ->
        val id = state.activeSessionId
        if (id != null) {
            repository.getChatMessages(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setActiveSession(sessionId: String) {
        _uiState.value = _uiState.value.copy(activeSessionId = sessionId)
    }

    fun sendMessage(query: String) {
        val sessionId = _uiState.value.activeSessionId ?: return
        if (query.isBlank() || _uiState.value.isStreaming) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isStreaming = true,
                streamingChunk = "",
                errorMessage = null
            )

            try {
                repository.askQuestionStream(sessionId, query).collect { chunk ->
                    _uiState.value = _uiState.value.copy(
                        streamingChunk = _uiState.value.streamingChunk + chunk
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error querying RAG engine: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    streamingChunk = ""
                )
            }
        }
    }
}
