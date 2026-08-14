package com.example.aquaserve.network

data class Delivery(
    val _id: String,
    val type: String,
    val orderType: String? = "Normal",
    val status: String? = "Accepted",
    val productName: String?,
    val userId: DeliveryUser,
    val quantity: Int,
    val emptyBottles: Int
)

data class DeliveryUser(
    val name: String,
    val address: String?,
    val phoneNumber: String?
)