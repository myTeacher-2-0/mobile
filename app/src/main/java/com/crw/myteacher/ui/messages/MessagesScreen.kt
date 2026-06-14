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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crw.myteacher.ui.components.AppBottomBar
import com.crw.myteacher.ui.components.BottomTab
import com.crw.myteacher.ui.theme.BrandBlue
import com.crw.myteacher.ui.theme.DarkText
import com.crw.myteacher.ui.theme.MutedText
import com.crw.myteacher.ui.theme.ScreenBackground

data class ConversationItem(
    val id: Int,
    val teacherName: String,
    val subject: String,
    val lastMessage: String,
    val timeLabel: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val initials: String,
    val avatarColor: Color = Color(0xFF3F4858)
)

private val dummyConversations = listOf(
    ConversationItem(1, "Dr. Elena Rossi", "ADVANCED MATHEMATICS", "Lorem ipsum dolor sit amet,", "10:42 AM", unreadCount = 2, isOnline = true, initials = "ER", avatarColor = Color(0xFF3A5A8C)),
    ConversationItem(2, "Mgr Marek Nowak", "PHYSICS", "Lorem ipsum, stabat matter...", "YESTERDAY", initials = "MN", avatarColor = Color(0xFF5A3A8C)),
    ConversationItem(3, "Sara Jenkins", "ENGLISH LITERATURE", "Lorem ipsum dolor sit amet,", "MON", isOnline = true, initials = "SJ", avatarColor = Color(0xFF8C3A3A)),
    ConversationItem(4, "Prof. David Klein", "CHEMISTRY", "Lorem ipsum dolor sit amet,", "23 OCT", initials = "DK", avatarColor = Color(0xFF3A6C4A)),
)

@Composable
fun MessagesRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onConversationClick: () -> Unit
) {
    MessagesScreen(
        onNavigateToHome = onNavigateToHome,
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToProfile = onNavigateToProfile,
        onConversationClick = onConversationClick
    )
}

@Composable
fun MessagesScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onConversationClick: () -> Unit = {}
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
            items(dummyConversations) { conversation ->
                ConversationRow(item = conversation, onClick = onConversationClick)
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
            Text("Search conversations...", color = MutedText, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ConversationRow(item: ConversationItem, onClick: () -> Unit) {
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
                    .background(item.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            if (item.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }
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
                    item.teacherName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DarkText
                )
                Text(
                    item.timeLabel,
                    color = MutedText,
                    fontSize = 11.sp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.subject,
                    color = BrandBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                if (item.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(BrandBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                item.lastMessage,
                color = MutedText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
