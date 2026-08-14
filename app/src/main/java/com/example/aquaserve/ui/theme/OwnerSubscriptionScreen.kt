package com.example.aquaserve.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.OwnerSubscriptionSummary
import com.example.aquaserve.network.OwnerViewModel

@Composable
fun OwnerSubscriptionScreen(ownerViewModel: OwnerViewModel) {
    val context = LocalContext.current
    val subList by ownerViewModel.subscriptionStats.collectAsState()
    val isLoading by ownerViewModel.subStatsLoading.collectAsState()

    LaunchedEffect(Unit) {
        ownerViewModel.fetchAllSubscriptionStats(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (subList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active subscriptions found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subList) { sub ->
                    OwnerSubItemCard(sub)
                }
            }
        }
    }
}

@Composable
fun OwnerSubItemCard(sub: OwnerSubscriptionSummary) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User & Product
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(sub.userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
                Surface(
                    color = Color(0xFFF3E5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(sub.productName, fontSize = 12.sp, color = Color(0xFF7B1FA2), modifier = Modifier.padding(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${sub.startDate}  ➔  ${sub.endDate}", fontSize = 14.sp, color = Color.DarkGray)
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SubStatColumn("Total", sub.totalOrders.toString(), Color.Black)
                SubStatColumn("Delivered", sub.delivered.toString(), Color(0xFF388E3C))
                SubStatColumn("Pending", sub.pending.toString(), Color(0xFFFF9800))
            }
        }
    }
}

@Composable
fun SubStatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}