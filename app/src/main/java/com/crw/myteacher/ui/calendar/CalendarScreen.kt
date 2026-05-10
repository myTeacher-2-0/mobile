package com.crw.myteacher.ui.calendar

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crw.myteacher.R
import com.crw.myteacher.data.model.CalendarMeeting
import com.crw.myteacher.data.model.LessonActionType
import com.crw.myteacher.ui.theme.BrandBlue
import com.crw.myteacher.ui.theme.DarkText
import com.crw.myteacher.ui.theme.LightCardBg
import com.crw.myteacher.ui.theme.MutedText
import com.crw.myteacher.ui.theme.ScreenBackground
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

fun Int.lessonCountLabel(): String = when (this) {
    1 -> "1 lekcja"
    in 2..4 -> "$this lekcje"
    else -> "$this lekcji"
}

@Composable
fun CalendarRoute(
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onNavigateBack: () -> Unit,
    onProposeLessonClick: () -> Unit,
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {}
) {
    CalendarScreenContent(
        uiState = uiState,
        onDateSelected = onDateSelected,
        onNavigateBack = onNavigateBack,
        onProposeLessonClick = onProposeLessonClick,
        onPreviousMonth = onPreviousMonth,
        onNextMonth = onNextMonth
    )
}

@Composable
fun CalendarScreenContent(
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onNavigateBack: () -> Unit,
    onProposeLessonClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onProposeLessonClick,
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            BottomBarPlaceholder(onNavigateBack)
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
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Text(
                    text = "Kalendarz",
                    color = BrandBlue,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Error message
            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Calendar grid
            item {
                CalendarGrid(
                    currentMonth = uiState.currentMonth,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = onDateSelected,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Lekcje", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = uiState.meetings.size.lessonCountLabel(),
                            color = BrandBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                if (uiState.meetings.isEmpty()) {
                    Text(
                        text = "Brak spotkań",
                        color = MutedText,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        uiState.meetings.forEach { meeting ->
                            MeetingCard(meeting)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Month header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentMonth.let {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("LLLL yyyy", java.util.Locale("pl"))
                    it.format(formatter).replaceFirstChar { c -> c.titlecase(java.util.Locale("pl")) }
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "<",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    modifier = Modifier.clickable { onPreviousMonth() }
                )
                Text(
                    ">",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    modifier = Modifier.clickable { onNextMonth() }
                )
            }
        }

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("PON", "WT", "ŚR", "CZW", "PT", "SOB", "NDZ").forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Calculate days in the grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        // Monday=1 .. Sunday=7 => offset 0..6
        val startOffset = (firstDayOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value)
        val totalCells = startOffset + daysInMonth
        val weeks = (totalCells + 6) / 7
        val today = LocalDate.now()

        for (week in 0 until weeks) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (d in 0..6) {
                    val cellIndex = week * 7 + d
                    val dayNum = cellIndex - startOffset + 1
                    val isValidDay = dayNum in 1..daysInMonth
                    val date = if (isValidDay) currentMonth.atDay(dayNum) else null
                    val isSelected = date == selectedDate
                    val isToday = date == today

                    val bgColor = when {
                        isSelected -> BrandBlue
                        isToday -> BrandBlue.copy(alpha = 0.15f)
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isSelected -> Color.White
                        isToday -> BrandBlue
                        else -> DarkText
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(bgColor)
                            .clickable(enabled = isValidDay) {
                                date?.let { onDateSelected(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isValidDay) {
                            Text(
                                text = dayNum.toString(),
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingCard(meeting: CalendarMeeting) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)) {
            Box(modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(BrandBlue))

            Column(
                modifier = Modifier.padding(start = 14.dp, end = 18.dp, top = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top section (Avatar + Subject + Title)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_avatar_placeholder),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = meeting.subject.uppercase(),
                            color = BrandBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            meeting.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    }
                }

                // Middle section (Time)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🕐", fontSize = 16.sp)
                    Text(
                        "${meeting.timeRange} (${meeting.durationMin} min)",
                        fontSize = 14.sp,
                        color = MutedText
                    )
                }

                // Bottom section (Teacher + Action button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            meeting.status.lowercase()
                                .replaceFirstChar { it.titlecase(java.util.Locale("pl")) },
                            color = BrandBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            meeting.teacherName,
                            color = DarkText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val isJoin = meeting.actionType == LessonActionType.JOIN
                    val btnColor = if (isJoin) BrandBlue else Color.Transparent
                    val textColor = if (isJoin) Color.White else BrandBlue

                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                        border = if (!isJoin) BorderStroke(1.dp, BrandBlue) else null,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            if (isJoin) "DOŁĄCZ" else "SZCZEGÓŁY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBarPlaceholder(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigateBack() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("H", color = Color(0xFF8B98B4), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("START", color = Color(0xFF8B98B4), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFE9EDF4))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("K", color = BrandBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("KALENDARZ", color = BrandBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        Toast.makeText(context, "Wkrótce dostępne", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("W", color = Color(0xFF8B98B4), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("WIADOMOŚCI", color = Color(0xFF8B98B4), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("P", color = Color(0xFF8B98B4), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("PROFIL", color = Color(0xFF8B98B4), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}