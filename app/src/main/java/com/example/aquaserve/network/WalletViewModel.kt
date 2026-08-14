package com.example.aquaserve.network


import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquaserve.util.RetrofitInstance
import com.razorpay.Checkout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class WalletResult {
    object Idle : WalletResult()
    object Loading : WalletResult()
    data class Success(val message: String) : WalletResult()
    data class Error(val error: String) : WalletResult()
}

class WalletViewModel : ViewModel() {

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _walletResult = MutableStateFlow<WalletResult>(WalletResult.Idle)
    val walletResult: StateFlow<WalletResult> = _walletResult.asStateFlow()

    private var amountToAdd: Double = 0.0


    fun loadWalletData(context: Context) {
        getWalletBalance(context)
        getTransactionHistory(context)
    }

    fun getWalletBalance(context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.getAuthenticatedApi(context).getWalletBalance()
                _balance.value = response.balance
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Failed to fetch balance: ${e.message}")
            }
        }
    }

    fun getTransactionHistory(context: Context) {
        viewModelScope.launch {
            try {
                val history = RetrofitInstance.getAuthenticatedApi(context).getWalletTransactions()
                _transactions.value = history
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Failed to fetch history: ${e.message}")
            }
        }
    }

    fun addMoneyToWallet(context: Context, amount: Double) {
        _walletResult.value = WalletResult.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.getAuthenticatedApi(context)
                    .addMoneyToWallet(AddMoneyRequest(amount))
                _balance.value = response.newBalance
                _walletResult.value = WalletResult.Success(response.message)
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Failed to add money: ${e.message}")
                _walletResult.value = WalletResult.Error("Failed to update wallet.")
            }
        }
    }

    fun startPayment(activity: Activity, amount: Double) {
        this.amountToAdd = amount
        val checkout = Checkout()

        //Razorpay Key ID
        checkout.setKeyID("rzp_test_RU2M8p060wVu6f")

        try {
            val options = JSONObject()
            options.put("name", "AquaServe")
            options.put("description", "Add to Wallet")
            options.put("theme.color", "#0000FF")
            options.put("currency", "INR")
            options.put("amount", (amount * 100).toInt())

            val prefill = JSONObject()
            prefill.put("email", "")
            prefill.put("contact", "")
            options.put("prefill", prefill)

            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.e("WalletViewModel", "Error in starting Razorpay Checkout", e)
        }
    }

    fun confirmPaymentSuccess(context: Context) {
        if (amountToAdd > 0) {
            addMoneyToWallet(context, amountToAdd)
            amountToAdd = 0.0
        }
    }

    fun resetResult() {
        _walletResult.value = WalletResult.Idle
    }
}