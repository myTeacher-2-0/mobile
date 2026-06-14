package com.crw.myteacher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crw.myteacher.ui.theme.BrandBlue

enum class BottomTab { START, CALENDAR, MESSAGES, PROFILE }

@Composable
fun AppBottomBar(
    selected: BottomTab,
    onStartClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
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
            BottomBarItem("START", "H", selected = selected == BottomTab.START, onClick = onStartClick)
            BottomBarItem("KALENDARZ", "K", selected = selected == BottomTab.CALENDAR, onClick = onCalendarClick)
            BottomBarItem("WIADOMOSCI", "W", selected = selected == BottomTab.MESSAGES, onClick = onMessagesClick)
            BottomBarItem("PROFIL", "P", selected = selected == BottomTab.PROFILE, onClick = onProfileClick)
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
        shape = RoundedCornerShape(18.dp)
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