package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.AuthViewModel
import com.example.aquaserve.network.ResetPasswordResult

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ResetPasswordScreen(
    email: String,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onResetSuccess: () -> Unit
) {
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val context = LocalContext.current
    val resetPasswordResult by authViewModel.resetPasswordResult.collectAsState()
    val isLoading = resetPasswordResult is ResetPasswordResult.Loading

    LaunchedEffect(resetPasswordResult) {
        when (val result = resetPasswordResult) {
            is ResetPasswordResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                authViewModel.resetPasswordFlows()
                onResetSuccess()
            }
            is ResetPasswordResult.Error -> {
                Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                authViewModel.resetPasswordFlows()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
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
                "Enter the code sent to $email",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("6-Digit Code") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (token.isBlank() || newPassword.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (newPassword.length < 8) {
                        Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    authViewModel.resetPassword(context, email, token, newPassword)
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
                    Text("Reset Password")
                }
            }
        }
    }
}
