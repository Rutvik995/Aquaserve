package com.example.aquaserve.network


data class DailyOrder(
    val _id: String,
    val productName: String?,
    val user: User?,
    val quantity: Int,
    val emptyBottles: Int,
    val deliveryDate: String,
    val billAmount: Int,
    val status: String,
    val orderType: String? = "Normal"

)

data class MonthlySummary(
    val grandTotalAmount: Double,
    val userSummaries: List<UserSummary>
)



data class UserSummary(
    val user: SummaryUser,

    val normalOrdered: Int,
    val normalReturned: Int,
    val coolerOrdered: Int,
    val coolerReturned: Int,
    val box1LOrdered: Int,
    val box250mlOrdered: Int,

    val subscriptionBill: Double = 0.0,
    val subStartDate: String? = null,
    val subEndDate: String? = null,

    val totalAmount: Double,

    )

data class SummaryUser(
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val address: String?
)
