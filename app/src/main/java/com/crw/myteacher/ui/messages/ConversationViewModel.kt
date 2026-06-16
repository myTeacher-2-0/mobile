package com.crw.myteacher.ui.messages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.dto.ChatMessageResponse
import com.crw.myteacher.data.repository.ChatRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class ConversationUiState(
    val isLoading: Boolean = true,
    val messages: List<ChatMessageResponse> = emptyList(),
    val currentUserId: String? = null,
    val chatRoomName: String = "",
    val errorMessage: String? = null,
    val isSending: Boolean = false
)

class ConversationViewModel(
    private val chatRepository: ChatRepository,
    private val chatRoomId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    /** Signals when WebSocket connection is ready for sending. */
    private val connectionReady = CompletableDeferred<Unit>()

    init {
        loadCurrentUser()
        loadMessages()
        connectAndSubscribe()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getCurrentUser()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        _uiState.value = _uiState.value.copy(currentUserId = user.accountId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load current user: ${e.message}", e)
            }
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            chatRepository.getMessages(chatRoomId)
                .onSuccess { messages ->
                    Log.d(TAG, "Loaded ${messages.size} messages")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = messages.reversed() // oldest first
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load messages: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Nie udało się pobrać wiadomości."
                    )
                }
        }
    }

    private fun connectAndSubscribe() {
        viewModelScope.launch {
            var retries = 0
            val maxRetries = 3
            while (retries < maxRetries) {
                try {
                    chatRepository.connectWebSocket()
                    connectionReady.complete(Unit)
                    Log.d(TAG, "WebSocket connected, subscribing to messages")
                    chatRepository.subscribeToMessages(chatRoomId).collect { newMessage ->
                        Log.d(TAG, "New realtime message: ${newMessage.id}")
                        val currentMessages = _uiState.value.messages
                        _uiState.value = _uiState.value.copy(
                            messages = currentMessages + newMessage
                        )
                    }
                    break // normal end of flow
                } catch (e: Exception) {
                    retries++
                    Log.e(TAG, "WebSocket error (attempt $retries/$maxRetries): ${e.message}", e)
                    if (retries >= maxRetries) {
                        Log.e(TAG, "WebSocket max retries reached, giving up")
                        if (!connectionReady.isCompleted) {
                            connectionReady.complete(Unit) // unblock senders
                        }
                    } else {
                        delay((2000L * retries).milliseconds) // exponential backoff
                    }
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            try {
                // Wait for WebSocket connection to be established
                connectionReady.await()
                chatRepository.sendMessage(chatRoomId, content)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message: ${e.message}", e)
            } finally {
                _uiState.value = _uiState.value.copy(isSending = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            chatRepository.disconnectWebSocket()
        }
    }

    companion object {
        private const val TAG = "ConversationViewModel"

        fun factory(chatRoomId: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val chatRepository = ChatRepository(
                        api = ApiClient.api,
                        stompClient = ApiClient.chatStompClient
                    )
                    return ConversationViewModel(chatRepository, chatRoomId) as T
                }
            }
        }
    }
}
