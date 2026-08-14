package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
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
import com.example.aquaserve.network.SubscriptionViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SubscriptionDetailsScreen(
    subscriptionViewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val details by subscriptionViewModel.subscriptionDetails.collectAsState()

    LaunchedEffect(Unit) {
        subscriptionViewModel.fetchSubscriptionDetails(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Subscription Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color.Blue,
                contentColor = Color.White,
                modifier = Modifier.height(70.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (details == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val sub = details!!.subscription
                val stats = details!!.stats

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Card(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color(0xFFE3F2FD),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("ACTIVE PLAN", color = Color.Blue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(sub.productName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${sub.startDate}  —  ${sub.endDate}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Delivery Statistics", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Total Orders",
                            count = stats.totalGenerated.toString(),
                            icon = Icons.Default.LocalShipping,
                            color = Color(0xFF1976D2),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Delivered",
                            count = stats.delivered.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF388E3C),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Pending",
                            count = stats.pending.toString(),
                            icon = Icons.Default.Schedule,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        elevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 14.sp, color = Color.Gray)
        }
    }
}