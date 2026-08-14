package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import com.example.aquaserve.network.Order
import com.example.aquaserve.network.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun OrderSummaryScreen(
    orderViewModel: OrderViewModel,
    onNavigateHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        orderViewModel.getOrderHistory(context)
    }

    val orders by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order History") },
                backgroundColor = Color.Blue,
                contentColor = Color.White,
                modifier = Modifier.height(70.dp)
            )
        },
        bottomBar = {
            BottomNavigation(backgroundColor = Color.Blue) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = onNavigateHome,
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Order History") },
                    label = { Text("History") },
                    selected = true,
                    onClick = { },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onNavigateToProfile,
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                orders.isEmpty() -> {
                    Text(
                        text = "You have no past orders.",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(orders) { order ->
                            OrderItemCard(order = order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(order: Order) {
    val (date, time) = formatDateTime(order.createdAt)
    val isSubscription = order.orderType == "Subscription"
    val cardColor = if (isSubscription) Color(0xFFE3F2FD) else Color.White
    val statusColor = when(order.status) {
        "Delivered" -> Color(0xFF388E3C)
        "Accepted" -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        backgroundColor = cardColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isSubscription) {
                Text(
                    text = "SUBSCRIPTION ORDER",
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(text = date, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = time, color = Color.Gray, fontSize = 14.sp)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(order.productName ?: "Product", fontWeight = FontWeight.Bold)
                    Text("Quantity: ${order.quantity}")
                    Text("Empty Bottles: ${order.emptyBottles}")
                }
                Text(
                    text = "₹ ${order.billAmount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

private fun formatDateTime(isoString: String): Pair<String, String> {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateObj = inputFormat.parse(isoString) ?: return Pair("", "")
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        Pair(dateFormat.format(dateObj), timeFormat.format(dateObj))
    } catch (e: Exception) {
        Pair("", "")
    }
}