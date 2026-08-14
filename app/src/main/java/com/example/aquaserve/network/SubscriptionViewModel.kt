package com.example.aquaserve.network

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquaserve.util.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil

sealed class SubResult {
    object Idle : SubResult()
    object Loading : SubResult()
    data class Success(val message: String) : SubResult()
    data class Error(val error: String) : SubResult()
}

class SubscriptionViewModel : ViewModel() {

    private val _activeSubscription = MutableStateFlow<SubscriptionResponse?>(null)
    val activeSubscription: StateFlow<SubscriptionResponse?> = _activeSubscription.asStateFlow()

    private val _subscriptionDetails = MutableStateFlow<SubscriptionStatsResponse?>(null)
    val subscriptionDetails: StateFlow<SubscriptionStatsResponse?> = _subscriptionDetails.asStateFlow()

    private val _previewData = MutableStateFlow<SubscriptionPreviewResponse?>(null)
    val previewData: StateFlow<SubscriptionPreviewResponse?> = _previewData.asStateFlow()

    private val _subResult = MutableStateFlow<SubResult>(SubResult.Idle)
    val subResult: StateFlow<SubResult> = _subResult.asStateFlow()

    fun fetchSubscriptionStatus(context: Context) {
        viewModelScope.launch {
            try {
                val sub = RetrofitInstance.getAuthenticatedApi(context).getSubscriptionStatus()
                _activeSubscription.value = sub
            } catch (e: Exception) {
                _activeSubscription.value = null
            }
        }
    }

    fun fetchSubscriptionDetails(context: Context) {
        viewModelScope.launch {
            try {
                val details = RetrofitInstance.getAuthenticatedApi(context).getSubscriptionDetails()
                _subscriptionDetails.value = details
            } catch (e: Exception) {
                _subscriptionDetails.value = null
            }
        }
    }

    fun fetchSubscriptionPreview(context: Context) {
        viewModelScope.launch {
            try {
                val preview = RetrofitInstance.getAuthenticatedApi(context).getSubscriptionPreview()
                _previewData.value = preview
            } catch (e: Exception) {
                _previewData.value = SubscriptionPreviewResponse(0)
            }
        }
    }

    fun buySubscription(context: Context, productName: String, quantity: Int, startDate: String, pricePerUnit: Int) {
        _subResult.value = SubResult.Loading
        viewModelScope.launch {
            try {
                val request = SubscriptionRequest(productName, quantity, startDate, pricePerUnit)
                val response = RetrofitInstance.getAuthenticatedApi(context).buySubscription(request)
                _subResult.value = SubResult.Success(response.message)
                fetchSubscriptionStatus(context)
            } catch (e: Exception) {
                _subResult.value = SubResult.Error("Failed: ${e.localizedMessage}")
            }
        }
    }

    fun resetResult() {
        _subResult.value = SubResult.Idle
    }
}