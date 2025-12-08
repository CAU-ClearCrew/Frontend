package com.example.mobileappdevelopment.veiwmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappdevelopment.api.MerkleRegisterRequest
import com.example.mobileappdevelopment.api.RetrofitClient
import com.example.mobileappdevelopment.util.ZkKeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ZkViewModel(application: Application) : AndroidViewModel(application) {

    private val _registrationStatus = MutableStateFlow<String?>(null)
    val registrationStatus: StateFlow<String?> = _registrationStatus.asStateFlow()

    fun registerZkKeys(customNullifier: String, secret: String) {
        viewModelScope.launch {
            try {
                // Hash the values (or prepare them as needed by the server)
                val leaf = (customNullifier + secret) // Replace with actual hashing logic

                val request = MerkleRegisterRequest(leaf)
                val response = RetrofitClient.apiService.registerMerkle(request)

                if (response.isSuccessful) {
                    ZkKeyManager.saveKeys(getApplication(), customNullifier, secret)
                    _registrationStatus.value = "성공적으로 등록되었습니다."
                } else {
                    _registrationStatus.value = "등록에 실패했습니다: ${response.message()}"
                }
            } catch (e: Exception) {
                _registrationStatus.value = "오류가 발생했습니다: ${e.message}"
            }
        }
    }
}
