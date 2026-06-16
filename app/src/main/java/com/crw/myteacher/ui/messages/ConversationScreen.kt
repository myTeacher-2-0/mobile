package com.crw.myteacher.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crw.myteacher.data.remote.dto.ChatMessageResponse
import com.crw.myteacher.ui.theme.BrandBlue
import com.crw.myteacher.ui.theme.DarkText
import com.crw.myteacher.ui.theme.MutedText

@Composable
fun ConversationRoute(
    chatRoomId: String,
    onNavigateBack: () -> Unit,
    viewModel: ConversationViewModel = viewModel(factory = ConversationViewModel.factory(chatRoomId))
) {
    val uiState by viewModel.uiState.collectAsState()

    ConversationScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSendMessage = { viewModel.sendMessage(it) }
    )
}

@Composable
fun ConversationScreen(
    uiState: ConversationUiState,
    onNavigateBack: () -> Unit = {},
    onSendMessage: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = { ConversationTopBar(onNavigateBack = onNavigateBack) },
        bottomBar = { ConversationInputBar(onSendMessage = onSendMessage, isSending = uiState.isSending) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.errorMessage, color = MutedText, fontSize = 14.sp)
                }
            }
            uiState.messages.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Rozpocznij konwersację...",
                        color = MutedText,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(uiState.messages, key = { it.id }) { message ->
                        val isOwn = message.senderId == uiState.currentUserId
                        if (isOwn) {
                            OwnMessageBubble(message = message)
                        } else {
                            ReceivedMessageBubble(message = message)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ConversationTopBar(onNavigateBack: () -> Unit) {
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Wróć",
                    tint = DarkText
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Konwersacja",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkText
                )
            }
        }
    }
}

@Composable
private fun ReceivedMessageBubble(message: ChatMessageResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F2F5))
            ) {
                Text(
                    message.content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize = 14.sp,
                    color = DarkText,
                    lineHeight = 20.sp
                )
            }
            Text(
                formatMessageTime(message.timestamp),
                color = MutedText,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun OwnMessageBubble(message: ChatMessageResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandBlue)
            ) {
                Text(
                    message.content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            }
            Text(
                formatMessageTime(message.timestamp),
                color = MutedText,
                fontSize = 10.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Composable
private fun ConversationInputBar(onSendMessage: (String) -> Unit, isSending: Boolean) {
    var messageText by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MutedText
                )
            }
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F5F7)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (messageText.isEmpty()) {
                            Text(
                                "Napisz wiadomość...",
                                color = MutedText,
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            textStyle = TextStyle(fontSize = 14.sp, color = DarkText),
                            cursorBrush = SolidColor(BrandBlue),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.SentimentSatisfied,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (messageText.isNotBlank() && !isSending) BrandBlue else Color(0xFFB0B8C4))
                    .clickable(enabled = messageText.isNotBlank() && !isSending) {
                        onSendMessage(messageText)
                        messageText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Wyślij",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatMessageTime(timestamp: String): String {
    return try {
        val parts = timestamp.split("T")
        if (parts.size >= 2) {
            parts[1].take(5) // HH:mm
        } else {
            timestamp
        }
    } catch (_: Exception) {
        timestamp
    }
}

