package com.example.aquaserve.network

data class ProfileUpdateRequest(
    val phone_number: String,
    val address: String,
    val area: String,
    val postalCode: String,
    val extra: String
)

sealed class ProfileUpdateResult {
    object Idle : ProfileUpdateResult()
    object Loading : ProfileUpdateResult()
    data class Success(val message: String) : ProfileUpdateResult()
    data class Error(val error: String) : ProfileUpdateResult()
}
