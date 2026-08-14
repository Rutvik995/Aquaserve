package com.example.aquaserve.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.OwnerViewModel
import com.example.aquaserve.network.UserSummary
import java.text.DateFormatSymbols
import java.util.Calendar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MonthlySummaryScreen(ownerViewModel: OwnerViewModel) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonthIndex = calendar.get(Calendar.MONTH)

    val months = remember { DateFormatSymbols().months.toList().filter { it.isNotEmpty() } }
    var isMonthExpanded by remember { mutableStateOf(false) }
    var selectedMonthName by remember { mutableStateOf(months[currentMonthIndex]) }

    var year by remember { mutableStateOf(currentYear.toString()) }
    var month by remember { mutableStateOf((currentMonthIndex + 1).toString()) }

    val summary by ownerViewModel.monthlySummary.collectAsState()
    val isLoading by ownerViewModel.summaryLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = isMonthExpanded,
                onExpandedChange = { isMonthExpanded = !isMonthExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedMonthName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Month") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Month") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isMonthExpanded,
                    onDismissRequest = { isMonthExpanded = false }
                ) {
                    months.forEachIndexed { index, monthName ->
                        DropdownMenuItem(
                            onClick = {
                                selectedMonthName = monthName
                                month = (index + 1).toString()
                                isMonthExpanded = false
                            }
                        ) {
                            Text(monthName)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = year,
                onValueChange = { if (it.length <= 4) year = it },
                label = { Text("Year") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val yearInt = year.toIntOrNull()
                val monthInt = month.toIntOrNull()
                if (yearInt != null && monthInt != null) {
                    ownerViewModel.fetchMonthlySummary(context, yearInt, monthInt)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
        ) {
            Text("Get Summary", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val localSummary = summary

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        } else if (localSummary != null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    backgroundColor = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Grand Total Revenue: ₹ ${"%.2f".format(localSummary.grandTotalAmount)}",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(localSummary.userSummaries) { userSummary ->
                        UserDetailedSummaryCard(userSummary)
                    }
                }
            }
        } else {
            Text("Select a month and year to see the summary.", color = Color.Gray)
        }
    }
}

@Composable
fun UserDetailedSummaryCard(summary: UserSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = summary.user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Blue
                )

                if (summary.subStartDate != null) {
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

            if (!summary.user.phoneNumber.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = summary.user.phoneNumber, fontSize = 14.sp, color = Color.DarkGray)
                }
            }

            if (!summary.user.address.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = summary.user.address, fontSize = 14.sp, color = Color.DarkGray)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Product", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(2f))
                Text("Ordered", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("Returned", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 20L Bottle
            if (summary.normalOrdered > 0 || summary.normalReturned > 0) {
                ProductRow("20L Bottle", summary.normalOrdered, summary.normalReturned)
            }

            // Cooler Bottle
            if (summary.coolerOrdered > 0 || summary.coolerReturned > 0) {
                ProductRow("Cooler Bottle", summary.coolerOrdered, summary.coolerReturned)
            }

            // 1 Litre Box
            if (summary.box1LOrdered > 0) {
                ProductRow("1 Litre Box", summary.box1LOrdered, 0)
            }

            // 250ml Box
            if (summary.box250mlOrdered > 0) {
                ProductRow("250ml Box", summary.box250mlOrdered, 0)
            }

            if (summary.subscriptionBill > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Subscription Details:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF6A1B9A))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Active: ${summary.subStartDate} to ${summary.subEndDate}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        "₹ ${summary.subscriptionBill.toInt()}",
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6A1B9A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Monthly Bill:", fontWeight = FontWeight.Bold)
                Text(
                    "₹ ${"%.2f".format(summary.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun ProductRow(name: String, qty: Int, ret: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontSize = 14.sp, modifier = Modifier.weight(2f), color = Color.DarkGray)
        Text("$qty", fontSize = 14.sp, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Text(if (ret > 0) "$ret" else "-", fontSize = 14.sp, modifier = Modifier.weight(1f), color = if(ret > 0) Color.Red else Color.Gray)
    }
}