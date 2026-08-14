package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.SubResult
import com.example.aquaserve.network.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SubscriptionScreen(
    subscriptionViewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf("") }
    var quantityString by remember { mutableStateOf("1") }

    val productName = "Normal Bottle"
    val pricePerUnit = 20

    val quantity = quantityString.toIntOrNull() ?: 1
    val totalCost = 30 * quantity * pricePerUnit

    val subResult by subscriptionViewModel.subResult.collectAsState()
    val previewData by subscriptionViewModel.previewData.collectAsState()
    val isLoading = subResult is SubResult.Loading

    LaunchedEffect(Unit) {
        subscriptionViewModel.fetchSubscriptionPreview(context)
    }

    LaunchedEffect(subResult) {
        when(val result = subResult) {
            is SubResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                subscriptionViewModel.resetResult()
                onNavigateBack()
            }
            is SubResult.Error -> {
                Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                subscriptionViewModel.resetResult()
            }
            else -> {}
        }
    }

    val missedBottles = previewData?.missedBottles ?: 0
    val extraDays = if (quantity > 0) ceil(missedBottles.toFloat() / quantity).toInt() else 0
    val totalDuration = 30 + extraDays

    var calculatedEndDate = "Select Start Date"
    if (startDate.isNotBlank()) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(startDate)!!
            cal.add(Calendar.DAY_OF_YEAR, totalDuration - 1)
            calculatedEndDate = sdf.format(cal.time)
        } catch (e: Exception) { /* ignore */ }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            startDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buy Subscription") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Subscribe & Forget!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
            Text("Get water delivered automatically.", color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Product: $productName", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = quantityString,
                onValueChange = { quantityString = it.filter { char -> char.isDigit() } },
                label = { Text("Daily Quantity") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    label = { Text("Start Date") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.DateRange, "Date") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.matchParentSize().clickable(enabled = !isLoading) { datePickerDialog.show() })
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            if (missedBottles > 0) {
                Card(
                    backgroundColor = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Carry Forward Alert!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "You have $missedBottles pending bottles from previous plan. We added $extraDays extra days for free.",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Standard Duration:", fontWeight = FontWeight.Medium)
                Text("30 Days")
            }

            if (extraDays > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Carry Forward:", fontWeight = FontWeight.Medium, color = Color(0xFFE65100))
                    Text("+ $extraDays Days", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Divider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("End Date:", fontWeight = FontWeight.Bold)
                Text(calculatedEndDate, fontWeight = FontWeight.Bold, color = Color.Blue)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Cost:", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("₹ $totalCost", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF388E3C))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (startDate.isNotBlank() && quantity > 0) {
                        subscriptionViewModel.buySubscription(context, productName, quantity, startDate, pricePerUnit)
                    } else {
                        Toast.makeText(context, "Please select date and quantity", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White) else Text("Pay & Subscribe", color = Color.White)
            }
        }
    }
}