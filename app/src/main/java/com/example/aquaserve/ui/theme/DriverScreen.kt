package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.AnnouncementViewModel
import com.example.aquaserve.network.Delivery
import com.example.aquaserve.network.DriverViewModel
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DriverHomeScreen(
    driverViewModel: DriverViewModel,
    announcementViewModel: AnnouncementViewModel,
    onNavigateToAnnouncements: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedDate by remember { mutableStateOf("") }
    var hasDateBeenSelected by remember { mutableStateOf(false) }

    val deliveries by driverViewModel.deliveries.collectAsState()
    val isLoading by driverViewModel.isLoading.collectAsState()

    val newAnnouncementCount by announcementViewModel.newAnnouncementCount.collectAsState()

    LaunchedEffect(Unit) {
        announcementViewModel.checkNewAnnouncements(context)
    }

    val totalToDeliver = deliveries.filter { it.type == "Delivery" }.sumOf { it.quantity }
    val totalToPickup = deliveries.sumOf { if (it.type == "Pickup") it.quantity else it.emptyBottles }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            selectedDate = sdf.format(cal.time)
            hasDateBeenSelected = true
            driverViewModel.fetchDeliveries(context, selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Panel") },
                backgroundColor = Color.Blue,
                contentColor = Color.White,
                modifier = Modifier.height(70.dp),
                actions = {
                    IconButton(onClick = onNavigateToAnnouncements) {
                        BadgedBox(
                            badge = {
                                if (newAnnouncementCount > 0) {
                                    Badge(backgroundColor = Color.Red) {
                                        Text("$newAnnouncementCount", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Announcements")
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = if (selectedDate.isEmpty()) "Select Date" else selectedDate,
                onValueChange = {},
                label = { Text("Task Date") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                trailingIcon = {
                    Icon(Icons.Default.DateRange, "Select Date", modifier = Modifier.clickable { datePickerDialog.show() })
                },
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
            } else if (deliveries.isEmpty() && hasDateBeenSelected) {
                Text("No tasks scheduled for this date.", modifier = Modifier.padding(top = 24.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(deliveries) { delivery ->
                        DeliveryItemCard(
                            delivery = delivery,
                            onMarkDone = {
                                driverViewModel.markOrderDelivered(context, delivery._id, selectedDate)
                            }
                        )
                    }
                }

                if (hasDateBeenSelected) {
                    SummaryCard(totalToDeliver, totalToPickup)
                }
            }
        }
    }
}

@Composable
fun DeliveryItemCard(delivery: Delivery, onMarkDone: () -> Unit) {
    val isPickup = delivery.type == "Pickup"
    val isSubscription = delivery.orderType == "Subscription"
    val isDelivered = delivery.status == "Delivered"

    val cardColor = if (isPickup) Color(0xFFFFEBEE) else Color.White
    val typeColor = if (isPickup) Color.Red else Color(0xFF388E3C)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = cardColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = delivery.type.uppercase(),
                        style = MaterialTheme.typography.subtitle2,
                        color = typeColor,
                        fontWeight = FontWeight.Bold
                    )

                    if (isSubscription) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF9C27B0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SUB",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isDelivered) {
                    Text(
                        text = "DONE",
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Button(
                        onClick = onMarkDone,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Delivered", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = delivery.productName ?: "Product",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(delivery.userId.name, fontSize = 18.sp)

            Text(
                text = delivery.userId.address ?: "No address",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = delivery.userId.phoneNumber ?: "No phone",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if (isPickup) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Collect Coolers", fontWeight = FontWeight.Medium)
                        Text("${delivery.quantity}", fontSize = 22.sp, color = Color.Red)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Deliver", fontWeight = FontWeight.Medium)
                        Text("${delivery.quantity}", fontSize = 22.sp, color = Color.Blue)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Collect Empty", fontWeight = FontWeight.Medium)
                        Text("${delivery.emptyBottles}", fontSize = 22.sp, color = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(totalToDeliver: Int, totalToPickup: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        elevation = 8.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total To Deliver", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$totalToDeliver", fontSize = 24.sp, color = Color.Blue)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total To Collect", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$totalToPickup", fontSize = 24.sp, color = Color(0xFFC62828))
            }
        }
    }
}