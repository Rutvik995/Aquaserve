//package com.example.aquaserve.ui.theme
//
//import android.app.DatePickerDialog
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.aquaserve.network.DailyOrder
//import com.example.aquaserve.network.OwnerViewModel
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun DailyOrdersScreen(ownerViewModel: OwnerViewModel) {
//    val context = LocalContext.current
//    val calendar = Calendar.getInstance()
//
//    var selectedDate by remember { mutableStateOf("") }
//    var hasDateBeenSelected by remember { mutableStateOf(false) }
//
//    val dailyOrders by ownerViewModel.dailyOrders.collectAsState()
//    val isLoading by ownerViewModel.dailyOrdersLoading.collectAsState()
//
//    val datePickerDialog = DatePickerDialog(
//        context,
//        { _, year, month, dayOfMonth ->
//            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
//            val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
//            selectedDate = sdf.format(cal.time)
//            hasDateBeenSelected = true
//            ownerViewModel.fetchDailyOrders(context, selectedDate)
//        },
//        calendar.get(Calendar.YEAR),
//        calendar.get(Calendar.MONTH),
//        calendar.get(Calendar.DAY_OF_MONTH)
//    )
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        OutlinedTextField(
//            value = selectedDate.ifEmpty { "Select a date" },
//            onValueChange = {},
//            label = { Text("Order Date") },
//            readOnly = true,
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { datePickerDialog.show() },
//            trailingIcon = {
//                Icon(Icons.Default.DateRange, "Select Date", modifier = Modifier.clickable { datePickerDialog.show() })
//            },
//            shape = RoundedCornerShape(16.dp)
//        )
//        Spacer(modifier = Modifier.height(24.dp))
//
//        if (isLoading) {
//            CircularProgressIndicator()
//        } else if (dailyOrders.isEmpty() && hasDateBeenSelected) {
//            Text("No orders found for this date.")
//        } else {
//            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                items(dailyOrders) { order ->
//                    DailyOrderItemCard(order)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun DailyOrderItemCard(order: DailyOrder) {
//    val isSubscription = order.orderType == "Subscription"
//    val cardColor = if (isSubscription) Color(0xFFE3F2FD) else Color.White
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = 4.dp,
//        shape = MaterialTheme.shapes.medium,
//        backgroundColor = cardColor
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = order.productName ?: "Product",
//                    style = MaterialTheme.typography.body2,
//                    fontWeight = FontWeight.Bold
//                )
//
//                if (isSubscription) {
//                    Surface(
//                        color = Color(0xFF9C27B0),
//                        shape = RoundedCornerShape(4.dp)
//                    ) {
//                        Text(
//                            text = "SUB",
//                            color = Color.White,
//                            fontSize = 10.sp,
//                            fontWeight = FontWeight.Bold,
//                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
//                        )
//                    }
//                }
//            }
//
//            Text(text = order.user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
//            Text(text = order.user.email, color = Color.Gray, fontSize = 14.sp)
//            Divider(modifier = Modifier.padding(vertical = 8.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text("Ordered: ${order.quantity}")
//                Text("Empty: ${order.emptyBottles}")
//            }
//        }
//    }
//}



package com.example.aquaserve.ui.theme

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.DailyOrder
import com.example.aquaserve.network.OwnerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyOrdersScreen(ownerViewModel: OwnerViewModel) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedDate by remember { mutableStateOf("") }
    var hasDateBeenSelected by remember { mutableStateOf(false) }

    val dailyOrders by ownerViewModel.dailyOrders.collectAsState()
    val isLoading by ownerViewModel.dailyOrdersLoading.collectAsState()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            selectedDate = sdf.format(cal.time)
            hasDateBeenSelected = true
            ownerViewModel.fetchDailyOrders(context, selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = selectedDate.ifEmpty { "Select a date" },
            onValueChange = {},
            label = { Text("Order Date") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
            trailingIcon = {
                Icon(Icons.Default.DateRange, "Select Date", modifier = Modifier.clickable { datePickerDialog.show() })
            },
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (dailyOrders.isEmpty() && hasDateBeenSelected) {
            Text("No orders found for this date.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(dailyOrders) { order ->
                    DailyOrderItemCard(order)
                }
            }
        }
    }
}

@Composable
fun DailyOrderItemCard(order: DailyOrder) {
    val isSubscription = order.orderType == "Subscription"
    val cardColor = if (isSubscription) Color(0xFFE3F2FD) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        backgroundColor = cardColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.productName ?: "Product",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )

                if (isSubscription) {
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

            val userName = order.user?.name ?: "Deleted User"
            val userEmail = order.user?.email ?: "N/A"

            Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = userEmail, color = Color.Gray, fontSize = 14.sp)

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ordered: ${order.quantity}")
                Text("Empty: ${order.emptyBottles}")
            }
        }
    }
}