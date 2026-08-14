package com.example.aquaserve.ui.theme

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.Transaction
import com.example.aquaserve.network.WalletResult
import com.example.aquaserve.network.WalletViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun WalletScreen(
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val balance by walletViewModel.balance.collectAsState()
    val walletResult by walletViewModel.walletResult.collectAsState()

    val transactions by walletViewModel.transactions.collectAsState()

    var amountToAdd by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        walletViewModel.getWalletBalance(context)
        walletViewModel.getTransactionHistory(context)
    }

    LaunchedEffect(walletResult) {
        when(val result = walletResult) {
            is WalletResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                amountToAdd = ""
                walletViewModel.resetResult()
                walletViewModel.getTransactionHistory(context)
            }
            is WalletResult.Error -> {
                Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                walletViewModel.resetResult()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Current Balance", style = MaterialTheme.typography.h6)
            Text(
                text = "₹ %.2f".format(balance),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = amountToAdd,
                onValueChange = { amountToAdd = it },
                label = { Text("Amount to Add") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("₹", fontSize = 18.sp) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amount = amountToAdd.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        walletViewModel.startPayment(context as Activity, amount)
                    } else {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                enabled = walletResult !is WalletResult.Loading
            ) {
                if (walletResult is WalletResult.Loading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Add Money", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transaction History", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                Text("No transactions yet.", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(transactions) { transaction ->
                        TransactionItemCard(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(transaction: Transaction) {
    val (date, time) = formatDateTime(transaction.createdAt)

    Card(
        elevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(transaction.description, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$date at $time", style = MaterialTheme.typography.caption, color = Color.Gray)
            }

            val amountColor = if (transaction.type == "Credit") Color(0xFF388E3C) else Color.Red
            val sign = if (transaction.type == "Credit") "+" else "-"

            Text(
                text = "$sign ₹${transaction.amount.toInt()}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
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