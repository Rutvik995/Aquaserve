package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.AnnouncementResponse
import com.example.aquaserve.network.AnnouncementViewModel
import com.example.aquaserve.network.OwnerScreen
import com.example.aquaserve.network.OwnerViewModel
import com.example.aquaserve.network.SendResult
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.example.aquaserve.ui.theme.OwnerSubscriptionScreen

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun OwnerHomeScreen(
    ownerViewModel: OwnerViewModel,
    announcementViewModel: AnnouncementViewModel,
    onLogout: () -> Unit
) {
    val currentScreen by ownerViewModel.currentScreen.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            OwnerScreen.Menu -> "Owner Panel"
                            OwnerScreen.RegisteredUsers -> "Registered Users"
                            OwnerScreen.DailyOrders -> "Daily Orders"
                            OwnerScreen.UsersSummary -> "Monthly Summary"
                            OwnerScreen.Announcements -> "Announcements"
                            OwnerScreen.SubscriptionInfo -> "Subscription Info"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (currentScreen != OwnerScreen.Menu) {
                        IconButton(onClick = { ownerViewModel.selectScreen(OwnerScreen.Menu) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                modifier = Modifier.height(70.dp),
                backgroundColor = Color.Blue,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                OwnerScreen.Menu -> OwnerMenuContent(
                    onNavigate = { screen -> ownerViewModel.selectScreen(screen) }
                )
                OwnerScreen.RegisteredUsers -> RegisteredUsersScreen(ownerViewModel)
                OwnerScreen.DailyOrders -> DailyOrdersScreen(ownerViewModel)
                OwnerScreen.UsersSummary -> MonthlySummaryScreen(ownerViewModel)
                OwnerScreen.Announcements -> OwnerAnnouncementsScreen(ownerViewModel, announcementViewModel)
                OwnerScreen.SubscriptionInfo -> OwnerSubscriptionScreen(ownerViewModel)
            }
        }
    }
}

@Composable
fun OwnerMenuContent(
    onNavigate: (OwnerScreen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Manage App",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )

        OwnerMenuCard(
            title = "Registered Users",
            icon = Icons.Default.Group,
            onClick = { onNavigate(OwnerScreen.RegisteredUsers) }
        )
        OwnerMenuCard(
            title = "Daily Orders",
            icon = Icons.Default.Today,
            onClick = { onNavigate(OwnerScreen.DailyOrders) }
        )
        OwnerMenuCard(
            title = "Monthly Summary",
            icon = Icons.Default.Assessment,
            onClick = { onNavigate(OwnerScreen.UsersSummary) }
        )
        OwnerMenuCard(
            title = "Announcements",
            icon = Icons.Default.Campaign,
            onClick = { onNavigate(OwnerScreen.Announcements) }
        )
        OwnerMenuCard(
            title = "Subscription Info",
            icon = Icons.Default.Info,
            onClick = { onNavigate(OwnerScreen.SubscriptionInfo) }
        )
    }
}

@Composable
fun OwnerAnnouncementsScreen(
    ownerViewModel: OwnerViewModel,
    announcementViewModel: AnnouncementViewModel
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    val sendResult by ownerViewModel.sendResult.collectAsState()

    val historyList by announcementViewModel.announcements.collectAsState()
    val isHistoryLoading by announcementViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        announcementViewModel.fetchAnnouncements(context)
    }

    LaunchedEffect(sendResult) {
        when (val result = sendResult) {
            is SendResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                message = ""
                ownerViewModel.resetSendResult()
                announcementViewModel.fetchAnnouncements(context)
            }
            is SendResult.Error -> {
                Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                ownerViewModel.resetSendResult()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            backgroundColor = Color(0xFFF3E5F5)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Create New Announcement", fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Type your message...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    enabled = sendResult !is SendResult.Loading,
                    colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            ownerViewModel.sendAnnouncement(context, message)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                    enabled = message.isNotBlank() && sendResult !is SendResult.Loading
                ) {
                    if (sendResult is SendResult.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Announcement", color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        Text("History", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (isHistoryLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (historyList.isEmpty()) {
            Text("No past announcements.", color = Color.Gray)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items = historyList) { announcement ->
                    OwnerAnnouncementHistoryCard(announcement)
                }
            }
        }
    }
}

@Composable
fun OwnerAnnouncementHistoryCard(announcement: AnnouncementResponse) {
    val date = try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        input.timeZone = TimeZone.getTimeZone("UTC")
        val d = input.parse(announcement.createdAt)
        val output = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        output.format(d!!)
    } catch (e: Exception) {
        "Unknown Date"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = announcement.message, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = date, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
fun OwnerMenuCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Color.Blue
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        }
    }
}