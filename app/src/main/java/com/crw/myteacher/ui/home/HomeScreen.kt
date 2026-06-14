package com.crw.myteacher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crw.myteacher.data.model.DashboardData
import com.crw.myteacher.data.model.Lesson
import com.crw.myteacher.data.model.LessonStatus
import com.crw.myteacher.data.model.Subject

private val ScreenBackground = Color(0xFFF2F4F7)
private val BrandBlue = Color(0xFF0D4CCC)
private val LightBlue = Color(0xFF4A7DF3)
private val CardGrey = Color(0xFFE6E8EC)
private val MutedText = Color(0xFF6F7684)

@Composable
fun HomeRoute(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onNavigateToProposeLesson: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToChat: () -> Unit = {}
) {
    HomeScreen(
        uiState = uiState,
        modifier = modifier,
        onRetry = onRetry,
        onNavigateToProposeLesson = onNavigateToProposeLesson,
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToChat = onNavigateToChat
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onNavigateToProposeLesson: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToChat: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        bottomBar = {
            BottomBar(
                onCalendarClick = onNavigateToCalendar,
                onChatClick = onNavigateToChat
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandBlue)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }
            item { Text("myTeacher", color = BrandBlue, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold) }
            item {
                if (uiState.errorMessage != null) {
                    ErrorBanner(message = uiState.errorMessage, onRetry = onRetry)
                }
            }
            item { HeaderSection(data = uiState.data) }
            item { ProgressCard(data = uiState.data) }
            item { QuickActionsSection(
                data = uiState.data,
                onProposeClick = onNavigateToProposeLesson,
                onCalendarClick = onNavigateToCalendar,
                onChatClick = onNavigateToChat
            ) }
            item { LessonsSection(data = uiState.data) }
            item {
                SubjectSection(subjects = uiState.data.subjects)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection(data: DashboardData) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = data.dateLabel, color = MutedText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(
            text = "Dzien dobry, ${data.greetingName}!",
            color = Color(0xFF1E2530),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 42.sp,
            lineHeight = 46.sp
        )
    }
}

@Composable
private fun ProgressCard(data: DashboardData) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(BrandBlue, LightBlue)))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Twoj postep w tym tygodniu", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${data.progressPercent}%",
                        color = Color.White,
                        fontSize = 66.sp,
                        lineHeight = 66.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = data.completedLabel, color = Color(0xFFE7EEFF), fontSize = 34.sp, fontWeight = FontWeight.Medium)
                }
                LinearProgressIndicator(
                    progress = { data.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color.White,
                    trackColor = Color(0x3DFFFFFF)
                )
                Text(text = data.remainingMeetingsLabel, color = Color(0xFFD7E3FF), fontSize = 24.sp, lineHeight = 30.sp)
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    data: DashboardData,
    onProposeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Szybkie akcje", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF222833))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            data.quickActions.forEach { action ->
                val actionClick = when (action.id) {
                    "propose" -> onProposeClick
                    "calendar" -> onCalendarClick
                    "messages" -> onChatClick
                    else -> onProposeClick
                }
                Card(
                    onClick = actionClick,
                    colors = CardDefaults.cardColors(containerColor = CardGrey),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFD5DFF0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(action.iconText, color = BrandBlue, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                        }
                        Text(action.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonsSection(data: DashboardData) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("Dzisiejsze zajecia", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("Zobacz wszystkie", color = BrandBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            data.todaysLessons.forEach { lesson ->
                LessonCard(lesson = lesson)
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: Lesson) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6F8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = lesson.subjectLabel,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFE2E7F1))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = BrandBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("o", color = MutedText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(lesson.timeRange, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF303643))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3F4858)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("N", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(lesson.teacherTitle, color = MutedText, fontSize = 12.sp)
                    Text(lesson.teacherName, color = Color(0xFF262D39), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            val enabled = lesson.status == LessonStatus.JOINABLE
            Button(
                onClick = {},
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enabled) BrandBlue else Color(0xFFD6D9DE),
                    disabledContainerColor = Color(0xFFD6D9DE),
                    disabledContentColor = Color(0xFF727782)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(lesson.actionLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubjectSection(subjects: List<Subject>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Twoje przedmioty", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            subjects.forEach { subject ->
                val backgroundColor = if (subject.isSelected) BrandBlue else Color(0xFFD7DAE0)
                val textColor = if (subject.isSelected) Color.White else Color(0xFF474E5D)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(subject.name, color = textColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun BottomBar(onCalendarClick: () -> Unit = {}, onChatClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomBarItem("START", "H", selected = true, onClick = {})
            BottomBarItem("KALENDARZ", "K", selected = false, onClick = onCalendarClick)
            BottomBarItem("WIADOMOSCI", "W", selected = false, onClick = onChatClick)
            BottomBarItem("PROFIL", "P", selected = false, onClick = {})
        }
    }
}

@Composable
private fun BottomBarItem(label: String, iconLabel: String, selected: Boolean, onClick: () -> Unit = {}) {
    val textColor = if (selected) BrandBlue else Color(0xFF8B98B4)
    val background = if (selected) Color(0xFFE9EDF4) else Color.Transparent
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(iconLabel, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBD2))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                color = Color(0xFF6D4800),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) {
                Text("Ponow")
            }
        }
    }
}
