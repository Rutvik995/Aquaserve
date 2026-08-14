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

class AnnouncementViewModel : ViewModel() {

    private val _announcements = MutableStateFlow<List<AnnouncementResponse>>(emptyList())
    val announcements: StateFlow<List<AnnouncementResponse>> = _announcements.asStateFlow()

    private val _newAnnouncementCount = MutableStateFlow(0)
    val newAnnouncementCount: StateFlow<Int> = _newAnnouncementCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchAnnouncements(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _announcements.value = RetrofitInstance.getAuthenticatedApi(context).getAnnouncements()
            } catch (e: Exception) {
                Log.e("AnnouncementViewModel", "Failed to fetch announcements: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkNewAnnouncements(context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.getAuthenticatedApi(context).getNewAnnouncementCount()
                _newAnnouncementCount.value = response.newCount
            } catch (e: Exception) {
                Log.e("AnnouncementViewModel", "Failed to check new count: ${e.message}")
            }
        }
    }

    fun markAsRead(context: Context) {
        viewModelScope.launch {
            try {
                RetrofitInstance.getAuthenticatedApi(context).markAnnouncementsAsRead()
                _newAnnouncementCount.value = 0 // Reset count locally
            } catch (e: Exception) {
                Log.e("AnnouncementViewModel", "Failed to mark as read: ${e.message}")
            }
        }
    }
}