package com.example.aquaserve.network

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquaserve.util.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OwnerScreen {
    Menu,
    RegisteredUsers,
    DailyOrders,
    UsersSummary,
    Announcements,
    SubscriptionInfo
}

sealed class SendResult {
    object Idle : SendResult()
    object Loading : SendResult()
    data class Success(val message: String) : SendResult()
    data class Error(val error: String) : SendResult()
}

class OwnerViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow(OwnerScreen.Menu)
    val currentScreen: StateFlow<OwnerScreen> = _currentScreen

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> get() = _users
    private val _usersLoading = MutableStateFlow(false)
    val usersLoading: StateFlow<Boolean> get() = _usersLoading

    private val _dailyOrders = MutableStateFlow<List<DailyOrder>>(emptyList())
    val dailyOrders: StateFlow<List<DailyOrder>> get() = _dailyOrders
    private val _dailyOrdersLoading = MutableStateFlow(false)
    val dailyOrdersLoading: StateFlow<Boolean> get() = _dailyOrdersLoading

    private val _monthlySummary = MutableStateFlow<MonthlySummary?>(null)
    val monthlySummary: StateFlow<MonthlySummary?> get() = _monthlySummary
    private val _summaryLoading = MutableStateFlow(false)
    val summaryLoading: StateFlow<Boolean> get() = _summaryLoading

    private val _sendResult = MutableStateFlow<SendResult>(SendResult.Idle)
    val sendResult: StateFlow<SendResult> = _sendResult.asStateFlow()

    private val _subscriptionStats = MutableStateFlow<List<OwnerSubscriptionSummary>>(emptyList())
    val subscriptionStats: StateFlow<List<OwnerSubscriptionSummary>> get() = _subscriptionStats

    private val _subStatsLoading = MutableStateFlow(false)
    val subStatsLoading: StateFlow<Boolean> get() = _subStatsLoading

    fun selectScreen(screen: OwnerScreen) {
        _currentScreen.value = screen
    }

    fun fetchUsers(context: Context) {
        viewModelScope.launch {
            _usersLoading.value = true
            try {
                val userList = RetrofitInstance.getAuthenticatedApi(context).getUsers()
                _users.value = userList
            } catch (e: Exception) {
                Log.e("OwnerViewModel", "Failed to fetch users: ${e.message}")
                _users.value = emptyList()
            } finally {
                _usersLoading.value = false
            }
        }
    }

    fun fetchDailyOrders(context: Context, date: String) {
        viewModelScope.launch {
            _dailyOrdersLoading.value = true
            _dailyOrders.value = emptyList()
            try {
                val ordersList = RetrofitInstance.getAuthenticatedApi(context).getOrdersByDate(date)
                _dailyOrders.value = ordersList
            } catch (e: Exception) {
                Log.e("OwnerViewModel", "Failed to fetch daily orders: ${e.message}")
                _dailyOrders.value = emptyList()
            } finally {
                _dailyOrdersLoading.value = false
            }
        }
    }

    fun fetchMonthlySummary(context: Context, year: Int, month: Int) {
        viewModelScope.launch {
            _summaryLoading.value = true
            _monthlySummary.value = null
            try {
                val summary = RetrofitInstance.getAuthenticatedApi(context).getMonthlySummary(year, month)
                _monthlySummary.value = summary
            } catch (e: Exception) {
                Log.e("OwnerViewModel", "Failed to fetch monthly summary: ${e.message}")
                _monthlySummary.value = null
            } finally {
                _summaryLoading.value = false
            }
        }
    }

    fun fetchAllSubscriptionStats(context: Context) {
        viewModelScope.launch {
            _subStatsLoading.value = true
            try {
                val stats = RetrofitInstance.getAuthenticatedApi(context).getAllSubscriptionStats()
                _subscriptionStats.value = stats
            } catch (e: Exception) {
                Log.e("OwnerViewModel", "Failed to fetch sub stats: ${e.message}")
                _subscriptionStats.value = emptyList()
            } finally {
                _subStatsLoading.value = false
            }
        }
    }

    fun sendAnnouncement(context: Context, message: String) {
        _sendResult.value = SendResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.getAuthenticatedApi(context)
                    .postAnnouncement(AnnouncementRequest(message))
                _sendResult.value = SendResult.Success(response.message)
            } catch (e: Exception) {
                Log.e("OwnerViewModel", "Failed to send announcement: ${e.message}")
                _sendResult.value = SendResult.Error("Failed to send announcement.")
            }
        }
    }

    fun resetSendResult() {
        _sendResult.value = SendResult.Idle
    }
}