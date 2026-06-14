package com.crw.myteacher.ui.messages

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crw.myteacher.ui.theme.BrandBlue
import com.crw.myteacher.ui.theme.DarkText
import com.crw.myteacher.ui.theme.MutedText

data class ChatMessage(
    val id: Int,
    val content: String,
    val isOwn: Boolean,
    val time: String,
    val attachment: MessageAttachment? = null
)

data class MessageAttachment(
    val fileName: String,
    val fileSize: String,
    val fileType: String
)

private val dummyMessages = listOf(
    ChatMessage(
        1,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam sagittis vulputate massa sed luctus. Nullam vel enim in quam interdum efficitur et sed ex. Nunc lobortis nulla et scelerisque blandit.",
        isOwn = false,
        time = "09:40 AM"
    ),
    ChatMessage(
        2,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam sagittis vulputate massa sed luctus. Nulla",
        isOwn = true,
        time = "09:55 AM"
    ),
    ChatMessage(
        3,
        content = "",
        isOwn = false,
        time = "09:58 AM",
        attachment = MessageAttachment("Visualizing Fourier.pdf", "1.2 MB", "Educational Resource")
    ),
    ChatMessage(
        4,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam vel enim in quam interdum efficitur et sed ex. Nunc lobortis nulla et scelerisque blandit. Vestibulum id eros id massa vehicula aliquet non et purus. In tempus hendrerit lorem, sit",
        isOwn = false,
        time = "09:48 AM"
    ),
)

@Composable
fun ConversationRoute(onNavigateBack: () -> Unit) {
    ConversationScreen(onNavigateBack = onNavigateBack)
}

@Composable
fun ConversationScreen(onNavigateBack: () -> Unit = {}) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = { ConversationTopBar(onNavigateBack = onNavigateBack) },
        bottomBar = { ConversationInputBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item { DateSeparator("TODAY, OCTOBER 24") }
            items(dummyMessages.size) { index ->
                val msg = dummyMessages[index]
                when {
                    msg.attachment != null -> AttachmentBubble(attachment = msg.attachment, time = msg.time)
                    msg.isOwn -> OwnMessageBubble(message = msg)
                    else -> ReceivedMessageBubble(message = msg)
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
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
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A5A8C)),
                contentAlignment = Alignment.Center
            ) {
                Text("ER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Dr. Elena Rossi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkText
                )
                Text(
                    "ADVANCED MATHEMATICS • ONLINE",
                    color = BrandBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DateSeparator(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            label,
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReceivedMessageBubble(message: ChatMessage) {
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
                message.time,
                color = MutedText,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun OwnMessageBubble(message: ChatMessage) {
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
                message.time,
                color = MutedText,
                fontSize = 10.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Composable
private fun AttachmentBubble(attachment: MessageAttachment, time: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F2F5)),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFDDE5F4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.InsertDriveFile,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            attachment.fileName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkText
                        )
                        Text(
                            "${attachment.fileSize} • ${attachment.fileType}",
                            color = MutedText,
                            fontSize = 11.sp
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pobierz", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = {},
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Zobacz", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Text(
            time,
            color = MutedText,
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun ConversationInputBar() {
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
                    Text(
                        "Napisz wiadomość...",
                        color = MutedText,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
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
                    .background(BrandBlue),
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
