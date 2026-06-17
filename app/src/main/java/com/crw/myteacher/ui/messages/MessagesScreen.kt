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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crw.myteacher.data.remote.dto.ChatRoomDto
import com.crw.myteacher.ui.components.AppBottomBar
import com.crw.myteacher.ui.components.BottomTab
import com.crw.myteacher.ui.theme.BrandBlue
import com.crw.myteacher.ui.theme.DarkText
import com.crw.myteacher.ui.theme.MutedText
import com.crw.myteacher.ui.theme.ScreenBackground

@Composable
fun MessagesRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onConversationClick: (String) -> Unit,
    viewModel: MessagesViewModel = viewModel(factory = MessagesViewModel.factory())
) {
    val uiState by viewModel.uiState.collectAsState()

    MessagesScreen(
        uiState = uiState,
        onNavigateToHome = onNavigateToHome,
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToProfile = onNavigateToProfile,
        onConversationClick = onConversationClick
    )
}

@Composable
fun MessagesScreen(
    uiState: MessagesUiState,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onConversationClick: (String) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        bottomBar = {
            AppBottomBar(
                selected = BottomTab.MESSAGES,
                onStartClick = onNavigateToHome,
                onCalendarClick = onNavigateToCalendar,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Text(
                    "Wiadomości",
                    color = BrandBlue,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SearchBar() }
            item { Spacer(modifier = Modifier.height(4.dp)) }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandBlue)
                        }
                    }
                }
                uiState.errorMessage != null -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                uiState.errorMessage,
                                color = MutedText,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                uiState.chatRooms.isEmpty() -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE6E9EF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                "BRAK WIADOMOŚCI",
                                color = MutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                else -> {
                    items(uiState.chatRooms) { room ->
                        ConversationRow(
                            room = room,
                            onClick = { onConversationClick(room.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 66.dp),
                            color = Color(0xFFEEF0F3),
                            thickness = 1.dp
                        )
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE6E9EF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                "KONIEC WIADOMOŚCI",
                                color = MutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(18.dp)
            )
            Text("Szukaj konwersacji...", color = MutedText, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ConversationRow(room: ChatRoomDto, onClick: () -> Unit) {
    val initials = room.name
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    val avatarColor = generateAvatarColor(room.id)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    room.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DarkText
                )
            }
        }
    }
}

private fun generateAvatarColor(id: String): Color {
    val colors = listOf(
        Color(0xFF3A5A8C),
        Color(0xFF5A3A8C),
        Color(0xFF8C3A3A),
        Color(0xFF3A6C4A),
        Color(0xFF8C6A3A),
        Color(0xFF3A8C8C),
    )
    val index = id.hashCode().let { if (it < 0) -it else it } % colors.size
    return colors[index]
}

