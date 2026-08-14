package com.example.aquaserve.network

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquaserve.util.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class OrderViewModel : ViewModel() {
    private val _orderResult = MutableStateFlow<OrderResult>(OrderResult.Idle)
    val orderResult: StateFlow<OrderResult> get() = _orderResult

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> get() = _orders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun getOrderHistory(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val orderList = RetrofitInstance.getAuthenticatedApi(context).getOrders()
                _orders.value = orderList
            } catch (e: Exception) {
                _orders.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun placeOrder(context: Context, productName: String, quantity: Int, emptyBottle: Int, date: String, bill: Int, returnDate: String? = null) {
        if (date.isBlank()) {
            _orderResult.value = OrderResult.Error("Please fill all required fields.")
            return
        }
        if (quantity <= 0) {
            _orderResult.value = OrderResult.Error("Quantity must be valid.")
            return
        }

        _orderResult.value = OrderResult.Loading
        viewModelScope.launch {
            try {
                val request = OrderRequest(productName, quantity, emptyBottle, date, bill, returnDate)
                val response = RetrofitInstance.getAuthenticatedApi(context).placeOrder(request)

                if (response.message.equals("Order placed successfully", ignoreCase = true)) {
                    _orderResult.value = OrderResult.Success(response.message)
                } else {
                    _orderResult.value = OrderResult.Error(response.message)
                }
            } catch (e: Exception) {
                val errorMessage = if (e is HttpException) {
                    val errorJsonString = e.response()?.errorBody()?.string()
                    try {
                        val jsonObject = JSONObject(errorJsonString!!)
                        jsonObject.getString("message")
                    } catch (_: Exception) {
                        "An unexpected error occurred."
                    }
                } else {
                    "Order failed: ${e.localizedMessage}"
                }
                _orderResult.value = OrderResult.Error(errorMessage)
            }
        }
    }

    fun reset() {
        _orderResult.value = OrderResult.Idle
    }
}