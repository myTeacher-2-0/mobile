package com.crw.myteacher.ui.proposelesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.crw.myteacher.data.model.MockTeacher
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.crw.myteacher.R
import com.crw.myteacher.ui.theme.BrandBlue
import com.crw.myteacher.ui.theme.CardGrey
import com.crw.myteacher.ui.theme.DarkText
import com.crw.myteacher.ui.theme.LightCardBg
import com.crw.myteacher.ui.theme.MutedText
import com.crw.myteacher.ui.theme.ScreenBackground
import com.crw.myteacher.ui.theme.TagBgDark

@Composable
fun ProposeLessonRoute(
    uiState: ProposeLessonUiState,
    onNavigateBack: () -> Unit,
    onDateSelected: (Int) -> Unit,
    onSlotSelected: (String) -> Unit,
    onSubmit: () -> Unit
) {
    ProposeLessonScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onDateSelected = onDateSelected,
        onSlotSelected = onSlotSelected,
        onSubmit = onSubmit
    )
}

@Composable
fun ProposeLessonScreenContent(
    uiState: ProposeLessonUiState,
    onNavigateBack: () -> Unit,
    onDateSelected: (Int) -> Unit,
    onSlotSelected: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        bottomBar = {
            if (!uiState.isLoading && uiState.teacher != null) {
                FooterSection(uiState.amountToPay, onSubmit)
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading || uiState.teacher == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clickable { onNavigateBack() }.padding(end = 8.dp)) {
                        Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkText)
                    }
                    Text("Zaproponuj lekcję", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DarkText)
                }
            }

            item { TeacherCard(uiState.teacher) }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Wybierz datę", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Wrzesień 2024", fontSize = 14.sp, color = MutedText, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Zobacz kalendarz", color = BrandBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                DateSelector(uiState.selectedDateIndex, onDateSelected)
            }

            item {
                Text("Dostępne terminy", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }

            val allSlots = uiState.availableSlots.flatMap { (section, slots) ->
                slots.map { slot -> section to slot }
            }

            allSlots.chunked(2).forEach { rowSlots ->
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        rowSlots.forEach { (sectionName, slot) ->
                            val isSelected = uiState.selectedSlot == slot
                            val bgColor = if (isSelected) BrandBlue else Color.Transparent
                            val textColor = if (isSelected) Color.White else DarkText
                            val subColor = if (isSelected) Color.White else MutedText
                            val modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, if (!isSelected) CardGrey else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { onSlotSelected(slot) }
                                .padding(vertical = 16.dp, horizontal = 12.dp)

                            Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(sectionName.uppercase(), color = subColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(slot, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        if (rowSlots.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun TeacherCard(teacher: MockTeacher) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(BrandBlue))

            Column(modifier = Modifier.padding(start = 16.dp, end = 20.dp, top = 20.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = teacher.role.uppercase(),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrandBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        color = BrandBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_avatar_placeholder),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(72.dp).clip(CircleShape)
                    )
                    Column {
                        Text(teacher.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DarkText)
                        Text(teacher.description, color = MutedText, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐ ${teacher.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            Text("|", color = MutedText, fontSize = 12.sp)
                            Text("Z", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold) // ikona zegara
                            Text("${teacher.lessonsCount} lekcji", fontSize = 12.sp, color = MutedText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelector(selectedIndex: Int, onSelected: (Int) -> Unit) {
    val days = listOf("MON" to "11", "TUE" to "12", "WED" to "13", "THU" to "14", "FRI" to "15", "SAT" to "16", "SUN" to "17")
    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(days) { index, (day, num) ->
            val isSelected = index == selectedIndex
            val bgColor = if (isSelected) BrandBlue else Color.Transparent
            val textColor = if (isSelected) Color.White else DarkText
            val subColor = if (isSelected) Color.White else MutedText

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(if (!isSelected) 1.dp else 0.dp, if (!isSelected) CardGrey else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable { onSelected(index) }
                    .padding(vertical = 12.dp)
            ) {
                Text(day, color = subColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(num, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun FooterSection(amountToPay: Double, onSubmit: () -> Unit) {
    Card(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.4f)) {
                Text("KWOTA DO ZAPŁATY", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(String.format(Locale.getDefault(), "%.2f zł", amountToPay), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkText)
            }
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(0.6f).height(56.dp)
            ) {
                Text("Zaproponuj", fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}
