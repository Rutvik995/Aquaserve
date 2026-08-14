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
import java.lang.Exception

class ProfileViewModel : ViewModel() {
    private val _updateResult = MutableStateFlow<ProfileUpdateResult>(ProfileUpdateResult.Idle)
    val updateResult: StateFlow<ProfileUpdateResult> get() = _updateResult

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getProfile(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userProfile = RetrofitInstance.getAuthenticatedApi(context).getProfile()
                _profile.value = userProfile
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to fetch profile: ${e.message}", e)
                _profile.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(context: Context, phone_number: String, address: String, area: String, postalCode: String, extra: String) {

        _updateResult.value = ProfileUpdateResult.Loading
        viewModelScope.launch {
            try {
                val request = ProfileUpdateRequest(phone_number, address, area, postalCode, extra)

                val response = RetrofitInstance.getAuthenticatedApi(context).updateUserProfile(request)

                if (response.message.equals("Profile updated successfully", ignoreCase = true)) {
                    _updateResult.value = ProfileUpdateResult.Success(response.message)
                    getProfile(context)
                } else {
                    _updateResult.value = ProfileUpdateResult.Error(response.message)
                }
            } catch (e: Exception) {
                _updateResult.value = ProfileUpdateResult.Error("Update failed: ${e.localizedMessage}")
            }
        }
    }

    fun reset() {
        _updateResult.value = ProfileUpdateResult.Idle
    }
}