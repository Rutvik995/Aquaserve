package com.example.aquaserve.network

data class OrderRequest(
    val productName: String,
    val quantity: Int,
    val emptyBottle: Int,
    val date: String,
    val bill: Int,
    val returnDate: String? = null
)

data class Order(
    val _id: String,
    val userId: String,
    val productName: String?,
    val quantity: Int,
    val emptyBottles: Int,
    val deliveryDate: String,
    val returnDate: String? = null,
    val billAmount: Int,
    val status: String,
    val createdAt: String,
    val orderType: String? = "Normal"
)

data class OrderResponse(
    val message: String
)

sealed class OrderResult {
    object Idle : OrderResult()
    object Loading : OrderResult()
    data class Success(val message: String) : OrderResult()
    data class Error(val error: String) : OrderResult()
}

data class SubscriptionRequest(
    val productName: String,
    val quantity: Int,
    val startDate: String,
    val pricePerUnit: Int
)

data class SubscriptionResponse(
    val _id: String,
    val productName: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean
)