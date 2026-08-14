package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.OrderResult
import com.example.aquaserve.network.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CoolerOrderScreen(
    orderViewModel: OrderViewModel,
    onPlaceOrder: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var deliveryDate by remember { mutableStateOf("") }
    var returnDate by remember { mutableStateOf("") }
    var quantityString by remember { mutableStateOf("") }

    val quantity = quantityString.toIntOrNull() ?: 0
    val bill = quantity * 50

    val orderResult by orderViewModel.orderResult.collectAsState()
    val isLoading = orderResult is OrderResult.Loading

    LaunchedEffect(orderResult) {
        when(val result = orderResult) {
            is OrderResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                orderViewModel.reset()
                onPlaceOrder()
            }
            is OrderResult.Error -> {
                Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                orderViewModel.reset()
            }
            else -> {}
        }
    }

    val deliveryDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            deliveryDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
            returnDate = ""
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }

    val returnDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            returnDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    ).apply {
        if (deliveryDate.isNotEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val deliveryCalendar = Calendar.getInstance().apply { time = sdf.parse(deliveryDate)!! }
            deliveryCalendar.add(Calendar.DAY_OF_YEAR, 1)
            datePicker.minDate = deliveryCalendar.timeInMillis
        } else {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Place Your Order", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home")
                    }
                },
                modifier = Modifier.height(70.dp),
                backgroundColor = Color.Blue,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = deliveryDate,
                    onValueChange = {},
                    label = { Text("Delivery Date") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.DateRange, "Select Delivery Date") },
                    readOnly = true,
                    enabled = false,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colors.onSurface,
                        disabledBorderColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                        disabledLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium),
                        disabledTrailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                    )
                )
                Spacer(modifier = Modifier.matchParentSize().clickable(enabled = !isLoading) { deliveryDatePickerDialog.show() })
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = quantityString,
                onValueChange = { quantityString = it.filter { char -> char.isDigit() } },
                label = { Text("Quantity") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = returnDate,
                    onValueChange = {},
                    label = { Text("Return Date") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.DateRange, "Select Return Date") },
                    readOnly = true,
                    enabled = false,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colors.onSurface,
                        disabledBorderColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                        disabledLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium),
                        disabledTrailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                    )
                )
                Spacer(modifier = Modifier.matchParentSize().clickable(enabled = !isLoading) {
                    if (deliveryDate.isNotEmpty()) {
                        returnDatePickerDialog.show()
                    } else {
                        Toast.makeText(context, "Please select a delivery date first", Toast.LENGTH_SHORT).show()
                    }
                })
            }


            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Total Bill Amount: ₹ $bill",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(56.dp))
            } else {
                SlideToConfirmButton(
                    onConfirmed = {
                        if (deliveryDate.isNotBlank() && quantity > 0 && returnDate.isNotBlank()) {
                            orderViewModel.placeOrder(context, "Cooler Bottle", quantity, 0, deliveryDate, bill, returnDate)
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}