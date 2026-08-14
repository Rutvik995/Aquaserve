package com.example.aquaserve.network

data class LoginRequest(val email: String, val password: String)

data class SignupRequest(val name: String, val email: String, val password: String)


data class WalletBalanceResponse(val balance: Double)

data class AddMoneyRequest(val amount: Double)

data class AddMoneyResponse(val message: String, val newBalance: Double)

data class LoginResponse(
    val message: String,
    val user: User? = null,
    val token: String? = null,
    val role: String? = null
)

data class User(
    val _id: String? = null,
    val name: String,
    val email: String,
    val isSubscriber: Boolean = false
)

data class GoogleLoginRequest(val idToken: String)


data class GenericResponse(val message: String)

data class ForgotPasswordRequest(val email: String)

data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val password: String
)


data class UserProfile(
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val address: String?,
    val area: String?,
    val postalCode: String?,
    val extra: String?
)

data class AnnouncementRequest(val message: String)

data class AnnouncementResponse(
    val _id: String,
    val message: String,
    val createdAt: String
)

data class NewCountResponse(val newCount: Int)

data class Transaction(
    val _id: String,
    val amount: Double,
    val type: String,
    val description: String,
    val createdAt: String
)


data class SubscriptionStatsResponse(
    val subscription: SubscriptionResponse,
    val stats: SubscriptionStats
)

data class SubscriptionStats(
    val totalGenerated: Int,
    val delivered: Int,
    val pending: Int
)

data class OwnerSubscriptionSummary(
    val _id: String,
    val userName: String,
    val productName: String,
    val startDate: String,
    val endDate: String,
    val totalOrders: Int,
    val delivered: Int,
    val pending: Int
)

data class SubscriptionPreviewResponse(
    val missedBottles: Int
)


data class UpdateStatusRequest(
    val orderId: String,
    val status: String
)