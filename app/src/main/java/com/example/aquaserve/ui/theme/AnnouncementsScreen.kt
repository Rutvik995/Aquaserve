package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.AnnouncementResponse
import com.example.aquaserve.network.AnnouncementViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AnnouncementsScreen(
    viewModel: AnnouncementViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val announcements by viewModel.announcements.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // When this screen launches, fetch announcements and mark them as read
    LaunchedEffect(Unit) {
        viewModel.fetchAnnouncements(context)
        viewModel.markAsRead(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcements", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.height(70.dp),
                backgroundColor = Color.Blue,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (announcements.isEmpty()) {
                Text(
                    "No announcements at this time.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(announcements) { announcement ->
                        AnnouncementItemCard(announcement = announcement)
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementItemCard(announcement: AnnouncementResponse) {
    val (date, time) = formatDateTime(announcement.createdAt)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // This satisfies the "date and time" requirement
                Text(text = date, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = time, color = Color.Gray, fontSize = 14.sp)
            }
            Divider(modifier = Modifier.padding(vertical = 10.dp))
            Text(
                text = announcement.message,
                style = MaterialTheme.typography.body1,
                lineHeight = 22.sp
            )
        }
    }
}

private fun formatDateTime(isoString: String): Pair<String, String> {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateObj = inputFormat.parse(isoString) ?: return Pair("Invalid Date", "")
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        Pair(dateFormat.format(dateObj), timeFormat.format(dateObj))
    } catch (e: Exception) {
        e.printStackTrace()
        Pair("Invalid Date", "Invalid Time")
    }
}