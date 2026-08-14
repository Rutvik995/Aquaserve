package com.example.aquaserve.network

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquaserve.util.RetrofitInstance
import com.example.aquaserve.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(val response: LoginResponse) : AuthResult()
    data class Error(val error: String) : AuthResult()
}

sealed class ForgotPasswordResult {
    object Idle : ForgotPasswordResult()
    object Loading : ForgotPasswordResult()
    data class Success(val message: String) : ForgotPasswordResult()
    data class Error(val error: String) : ForgotPasswordResult()
}

sealed class ResetPasswordResult {
    object Idle : ResetPasswordResult()
    object Loading : ResetPasswordResult()
    data class Success(val message: String) : ResetPasswordResult()
    data class Error(val error: String) : ResetPasswordResult()
}

class AuthViewModel : ViewModel() {
    private val _authResult = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authResult: StateFlow<AuthResult> get() = _authResult

    private val _forgotPasswordResult = MutableStateFlow<ForgotPasswordResult>(ForgotPasswordResult.Idle)
    val forgotPasswordResult: StateFlow<ForgotPasswordResult> get() = _forgotPasswordResult

    private val _resetPasswordResult = MutableStateFlow<ResetPasswordResult>(ResetPasswordResult.Idle)
    val resetPasswordResult: StateFlow<ResetPasswordResult> get() = _resetPasswordResult

    fun login(context: Context, email: String, password: String) {
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                Log.d("LOGIN_ATTEMPT", "Logging in with email: $email")

                val response: LoginResponse = RetrofitInstance.api.loginUser(
                    LoginRequest(email.trim(), password)
                )

                Log.d("LOGIN_ATTEMPT", "Server Response: $response")

                if (response.message.contains("successful", ignoreCase = true) && response.token != null) {
                    Log.d("AuthViewModel", "Login successful. Saving token.")
                    val sessionManager = SessionManager(context)
                    sessionManager.saveAuthToken(response.token)
                    sessionManager.saveUserRole(response.role ?: "user")
                    _authResult.value = AuthResult.Success(response)
                } else {
                    _authResult.value = AuthResult.Error(response.message ?: "An unknown error occurred.")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login exception: ${e.message}", e)
                _authResult.value = AuthResult.Error("Login failed: ${e.localizedMessage}")
            }
        }
    }


    fun loginWithGoogle(context: Context, idToken: String) {
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                Log.d("GOOGLE_LOGIN_ATTEMPT", "Logging in with Google ID token.")
                val response: LoginResponse = RetrofitInstance.api.loginWithGoogle(
                    GoogleLoginRequest(idToken)
                )
                Log.d("GOOGLE_LOGIN_ATTEMPT", "Server Response: $response")

                if (response.message.contains("successful", ignoreCase = true) && response.token != null) {
                    Log.d("AuthViewModel", "Google login successful. Saving token.")
                    val sessionManager = SessionManager(context)
                    sessionManager.saveAuthToken(response.token)
                    sessionManager.saveUserRole(response.role ?: "user")
                    _authResult.value = AuthResult.Success(response)
                } else {
                    _authResult.value = AuthResult.Error(response.message ?: "An unknown error occurred.")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google login exception: ${e.message}", e)
                _authResult.value = AuthResult.Error("Google login failed: ${e.localizedMessage}")
            }
        }
    }


    fun signup(context: Context, name: String, email: String, password: String) {
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response: LoginResponse = RetrofitInstance.api.signupUser(
                    SignupRequest(name.trim(), email.trim(), password)
                )

                if (response.message.equals("Signup successful", ignoreCase = true) && response.token != null) {
                    Log.d("AuthViewModel", "Signup successful. Saving token.")
                    val sessionManager = SessionManager(context)
                    sessionManager.saveAuthToken(response.token)
                    sessionManager.saveUserRole(response.role ?: "user")
                    _authResult.value = AuthResult.Success(response)
                } else {
                    _authResult.value = AuthResult.Error(response.message ?: "An unknown error occurred.")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Signup exception: ${e.message}", e)
                _authResult.value = AuthResult.Error("Signup failed: ${e.localizedMessage}")
            }
        }
    }
    fun forgotPassword(context: Context, email: String) {
        _forgotPasswordResult.value = ForgotPasswordResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.forgotPassword(ForgotPasswordRequest(email.trim()))
                _forgotPasswordResult.value = ForgotPasswordResult.Success(response.message)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Forgot Password exception: ${e.message}", e)
                _forgotPasswordResult.value = ForgotPasswordResult.Error("Request failed: ${e.localizedMessage}")
            }
        }
    }

    fun resetPassword(context: Context, email: String, token: String, password: String) {
        _resetPasswordResult.value = ResetPasswordResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.resetPassword(
                    ResetPasswordRequest(email.trim(), token.trim(), password)
                )
                _resetPasswordResult.value = ResetPasswordResult.Success(response.message)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Reset Password exception: ${e.message}", e)
                _resetPasswordResult.value = ResetPasswordResult.Error("Reset failed: ${e.localizedMessage}")
            }
        }
    }

    fun reset() {
        _authResult.value = AuthResult.Idle
    }

    fun resetPasswordFlows() {
        _forgotPasswordResult.value = ForgotPasswordResult.Idle
        _resetPasswordResult.value = ResetPasswordResult.Idle
    }
}