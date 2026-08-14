package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.R
import com.example.aquaserve.network.AuthResult
import com.example.aquaserve.network.AuthViewModel
import com.example.aquaserve.network.LoginResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (LoginResponse) -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authResult by authViewModel.authResult.collectAsState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val idToken = account.idToken!!
            Log.d("GoogleSignIn", "Successfully received ID Token.")
            authViewModel.loginWithGoogle(context, idToken)
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Google sign in failed", e)
            Toast.makeText(context, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("679630101667-o8rgro8bmdjo23t8pgt88c27hps0qbkh.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    LaunchedEffect(authResult) {
        when (val result = authResult) {
            is AuthResult.Success -> {
                Toast.makeText(context, result.response.message, Toast.LENGTH_SHORT).show()
                onLoginSuccess(result.response)
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
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.aquaservelogo),
                    contentDescription = "aquaservelogo",
                    modifier = Modifier.size(170.dp).padding(bottom = 16.dp)
                )
                Text(
                    text = "Login",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, end = 4.dp)
                        .clickable(enabled = authResult != AuthResult.Loading) {
                            onNavigateToForgotPassword()
                        },
                    text = "Forgot Password?",
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        authViewModel.login(context, email.trim(), password)
                    },
                    enabled = authResult != AuthResult.Loading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Blue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Log In")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    enabled = authResult != AuthResult.Loading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    elevation = ButtonDefaults.elevation(defaultElevation = 2.dp)
                ) {
                    Text("Sign in with Google", modifier = Modifier.padding(6.dp))
                }

                if (authResult == AuthResult.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.height(200.dp))

                Text(
                    modifier = Modifier.clickable { onNavigateToSignup() },
                    text = buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Blue)) {
                            append("Sign Up")
                        }
                    }
                )
            }
        }
    }
}