package com.crw.myteacher.ui.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crw.myteacher.data.remote.dto.UserDto
import com.crw.myteacher.ui.components.AppBottomBar
import com.crw.myteacher.ui.components.BottomTab
import com.crw.myteacher.ui.theme.BrandBlue
import com.crw.myteacher.ui.theme.DarkText
import com.crw.myteacher.ui.theme.MutedText
import com.crw.myteacher.ui.theme.ScreenBackground

private val LogoutRed = Color(0xFFE53935)

@Composable
fun ProfileRoute(
    uiState: ProfileUiState,
    onNavigateToStart: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToMessages: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileScreen(
        uiState = uiState,
        onNavigateToStart = onNavigateToStart,
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToMessages = onNavigateToMessages,
        onLogout = onLogout,
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNavigateToStart: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        bottomBar = {
            AppBottomBar(
                selected = BottomTab.PROFILE,
                onStartClick = onNavigateToStart,
                onCalendarClick = onNavigateToCalendar,
                onMessagesClick = onNavigateToMessages
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Profil",
                color = BrandBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            AvatarSection(user = uiState.user)
            MenuSection()
            LogoutButton(onLogout = onLogout)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AvatarSection(user: UserDto?) {
    val initials = user?.let {
        "${it.firstName.firstOrNull() ?: ""}${it.lastName.firstOrNull() ?: ""}"
    } ?: "?"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3F4858)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(BrandBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(
            text = "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim(),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = DarkText
        )
    }
}

private data class ProfileMenuItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit = {}
)

@Composable
private fun MenuSection() {
    val items = listOf(
        ProfileMenuItem(Icons.Outlined.Person, "Moje dane"),
        ProfileMenuItem(Icons.Outlined.Notifications, "Ustawienia powiadomień"),
        ProfileMenuItem(Icons.Outlined.CreditCard, "Metody płatności"),
        ProfileMenuItem(Icons.Outlined.Help, "Centrum pomocy"),
        ProfileMenuItem(Icons.Outlined.Lock, "Zmień hasło"),
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            items.forEachIndexed { index, item ->
                MenuRow(item = item)
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF0F2F5),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuRow(item: ProfileMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEEF2FA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = item.label,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = DarkText
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MutedText
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, LogoutRed),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = LogoutRed)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Wyloguj się", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}