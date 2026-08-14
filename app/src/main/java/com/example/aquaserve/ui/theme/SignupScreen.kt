package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.R
import com.example.aquaserve.network.AuthResult
import com.example.aquaserve.network.AuthViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authResult by authViewModel.authResult.collectAsState()
    val context = LocalContext.current

    fun validatePassword(pass: String): String? {
        if (pass.length < 8) return "Password must be at least 8 characters"
        if (!pass.any { it.isUpperCase() }) return "Password must contain at least one Uppercase letter"
        if (!pass.any { it.isLowerCase() }) return "Password must contain at least one Lowercase letter"
        if (!pass.any { it.isDigit() }) return "Password must contain at least one Number"
        if (!pass.any { !it.isLetterOrDigit() }) return "Password must contain at least one Special Character (@, #, $, etc.)"
        return null
    }

    LaunchedEffect(authResult) {
        when (val result = authResult) {
            is AuthResult.Success -> {
                Toast.makeText(context, "Signup Successful! Please log in.", Toast.LENGTH_LONG).show()
                onSignupSuccess()
                authViewModel.reset()
            }
            is AuthResult.Error -> {
                Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                authViewModel.reset()
            }
            else -> {}
        }
    }

    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.aquaservelogo),
                    contentDescription = "aquaservelogo",
                    modifier = Modifier.size(170.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sign Up",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authResult != AuthResult.Loading
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authResult != AuthResult.Loading
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = authResult != AuthResult.Loading
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
//                    onClick = {
//                        authViewModel.signup(context, name.trim(), email.trim(), password)
//                    },
                    onClick = {
                        val passwordError = validatePassword(password)

                        if (name.isBlank() || email.isBlank()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else if (passwordError != null) {
                            Toast.makeText(context, passwordError, Toast.LENGTH_LONG).show()
                        } else {
                            authViewModel.signup(context, name.trim(), email.trim(), password)
                        }
                    },
                    enabled = authResult != AuthResult.Loading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Blue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Create Account")
                }

                if (authResult == AuthResult.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    modifier = Modifier.clickable { onNavigateToLogin() },
                    text = buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Blue)) {
                            append("Log In")
                        }
                    }
                )
            }
        }
    }
}

