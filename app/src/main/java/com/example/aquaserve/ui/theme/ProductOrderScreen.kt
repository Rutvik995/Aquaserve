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
import java.util.Calendar

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ProductOrderScreen(
    orderViewModel: OrderViewModel,
    onPlaceOrder: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var quantityString by remember { mutableStateOf("") }
    var emptyBottleString by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    val quantity = quantityString.toIntOrNull() ?: 0
    val emptyBottle = emptyBottleString.toIntOrNull() ?: 0
    val bill = quantity * 20

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

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

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.Start
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { },
                        label = { Text("Delivery Date") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.DateRange, "Select Date") },
                        readOnly = true,
                        enabled = false,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = MaterialTheme.colors.onSurface,
                            disabledBorderColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                            disabledLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium),
                            disabledTrailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                        )
                    )
                    Spacer(modifier = Modifier.matchParentSize().clickable(enabled = !isLoading) { datePickerDialog.show() })
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantityString,
                    onValueChange = { quantityString = it.filter { char -> char.isDigit() } },
                    label = { Text("Quantity") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = emptyBottleString,
                    onValueChange = { emptyBottleString = it.filter { char -> char.isDigit() } },
                    label = { Text("Empty Bottles to Return") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                Text("Bill Amount: ₹ $bill", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(56.dp))
                } else {
                    SlideToConfirmButton(
                        onConfirmed = {
                            if (date.isNotBlank() && quantity > 0) {
                                orderViewModel.placeOrder(context, "Normal Bottle", quantity, emptyBottle, date, bill)
                            } else {
                                Toast.makeText(context, "Please select a date and enter a valid quantity.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}