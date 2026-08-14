package com.example.aquaserve.network
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @POST("api/login")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): LoginResponse

    @POST("api/signup")
    suspend fun signupUser(@Body request: SignupRequest): LoginResponse

    @GET("api/profile")
    suspend fun getProfile(@Header("Authorization") token: String): User

    @GET("api/profile")
    suspend fun getProfile(): UserProfile

    @PUT("api/profile")
    suspend fun updateUserProfile(@Body request: ProfileUpdateRequest): LoginResponse

    @POST("api/orders")
    suspend fun placeOrder(@Body request: OrderRequest): OrderResponse

    @GET("api/orders")
    suspend fun getOrders(): List<Order>

    @GET("api/users")
    suspend fun getUsers(): List<User>

    @GET("api/orders/by-date")
    suspend fun getOrdersByDate(@Query("date") date: String): List<DailyOrder>

    @GET("api/summary/monthly")
    suspend fun getMonthlySummary(@Query("year") year: Int, @Query("month") month: Int): MonthlySummary

    @GET("api/owner/subscriptions")
    suspend fun getAllSubscriptionStats(): List<OwnerSubscriptionSummary>

    @GET("api/subscription/preview")
    suspend fun getSubscriptionPreview(): SubscriptionPreviewResponse

    @GET("api/deliveries/by-date")
    suspend fun getDeliveriesByDate(@Query("date") date: String): List<Delivery>

    @GET("api/wallet/balance")
    suspend fun getWalletBalance(): WalletBalanceResponse

    @POST("api/wallet/add")
    suspend fun addMoneyToWallet(
        @Body request: AddMoneyRequest
    ): AddMoneyResponse

    @GET("api/wallet/history")
    suspend fun getWalletTransactions(): List<Transaction>

    @POST("api/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): GenericResponse

    @POST("api/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): GenericResponse

    @POST("api/announcements")
    suspend fun postAnnouncement(
        @Body request: AnnouncementRequest
    ): GenericResponse

    @GET("api/announcements")
    suspend fun getAnnouncements(): List<AnnouncementResponse>

    @GET("api/announcements/new-count")
    suspend fun getNewAnnouncementCount(): NewCountResponse

    @POST("api/announcements/read")
    suspend fun markAnnouncementsAsRead(): GenericResponse


    @POST("api/subscription/create")
    suspend fun buySubscription(@Body request: SubscriptionRequest): GenericResponse

    @GET("api/subscription/status")
    suspend fun getSubscriptionStatus(): SubscriptionResponse?

    @GET("api/subscription/details")
    suspend fun getSubscriptionDetails(): SubscriptionStatsResponse

    @PUT("api/orders/update-status")
    suspend fun updateOrderStatus(@Body request: UpdateStatusRequest): GenericResponse
}