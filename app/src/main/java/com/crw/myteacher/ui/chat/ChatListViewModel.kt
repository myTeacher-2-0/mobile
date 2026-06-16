package com.crw.myteacher.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.dto.ChatRoomDto
import com.crw.myteacher.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatListUiState(
    val isLoading: Boolean = true,
    val chatRooms: List<ChatRoomDto> = emptyList(),
    val errorMessage: String? = null
)

class ChatListViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadChatRooms()
    }

    fun loadChatRooms() {
        viewModelScope.launch {
            Log.d(TAG, "loadChatRooms() — token present: ${ApiClient.getTokenManager().isLoggedIn}")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            chatRepository.getMyChatRooms()
                .onSuccess { rooms ->
                    Log.d(TAG, "loadChatRooms() — loaded ${rooms.size} rooms")
                    _uiState.value = ChatListUiState(
                        isLoading = false,
                        chatRooms = rooms
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "loadChatRooms() — FAILED: ${e.message}", e)
                    _uiState.value = ChatListUiState(
                        isLoading = false,
                        errorMessage = "Nie udało się pobrać czatów."
                    )
                }
        }
    }

    companion object {
        private const val TAG = "ChatListViewModel"

        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val chatRepository = ChatRepository(
                        api = ApiClient.api,
                        stompClient = ApiClient.chatStompClient
                    )
                    return ChatListViewModel(chatRepository) as T
                }
            }
        }
    }
}

