package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.AuthViewModel
import com.example.aquaserve.network.ForgotPasswordResult
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onCodeSent: (email: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val forgotPasswordResult by authViewModel.forgotPasswordResult.collectAsState()
    val isLoading = forgotPasswordResult is ForgotPasswordResult.Loading

    LaunchedEffect(Unit) {
        authViewModel.resetPasswordFlows()
    }

    LaunchedEffect(forgotPasswordResult) {
        when (val result = forgotPasswordResult) {
            is ForgotPasswordResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                authViewModel.resetPasswordFlows()
                onCodeSent(email.trim())
            }
            is ForgotPasswordResult.Error -> {
                Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                authViewModel.resetPasswordFlows()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
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
            Text(
                "Enter your account email",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "We will send a 6-digit code to your email to reset your password.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        authViewModel.forgotPassword(context, email)
                    } else {
                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Blue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Send Code")
                }
            }
        }
    }
}
