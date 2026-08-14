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

class DriverViewModel : ViewModel() {

    private val _deliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val deliveries: StateFlow<List<Delivery>> = _deliveries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val totalToDeliver: StateFlow<Int> = MutableStateFlow(0)
    val totalToPickup: StateFlow<Int> = MutableStateFlow(0)

    fun fetchDeliveries(context: Context, date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _deliveries.value = emptyList()
            try {
                val deliveryList =
                    RetrofitInstance.getAuthenticatedApi(context).getDeliveriesByDate(date)
                _deliveries.value = deliveryList

                (totalToDeliver as MutableStateFlow).value = deliveryList.filter { it.type == "Delivery" }.sumOf { it.quantity }
                (totalToPickup as MutableStateFlow).value = deliveryList.sumOf { if (it.type == "Pickup") it.quantity else it.emptyBottles }

            } catch (e: Exception) {
                Log.e("DriverViewModel", "Failed to fetch deliveries: ${e.message}")
                _deliveries.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun markOrderDelivered(context: Context, orderId: String, currentSelectedDate: String) {
        viewModelScope.launch {
            try {
                val request = UpdateStatusRequest(orderId, "Delivered")
                RetrofitInstance.getAuthenticatedApi(context).updateOrderStatus(request)

                fetchDeliveries(context, currentSelectedDate)
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Failed to update status: ${e.message}")
            }
        }
    }
}
