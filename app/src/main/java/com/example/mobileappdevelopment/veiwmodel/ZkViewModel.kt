package com.example.mobileappdevelopment.veiwmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappdevelopment.api.MerkleRegisterRequest
import com.example.mobileappdevelopment.api.RetrofitClient
import com.example.mobileappdevelopment.util.HexUtils
import com.example.mobileappdevelopment.util.PoseidonHash
import com.example.mobileappdevelopment.util.ZkKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger

class ZkViewModel(application: Application) : AndroidViewModel(application) {

    private val _registrationStatus = MutableStateFlow<String?>(null)
    val registrationStatus: StateFlow<String?> = _registrationStatus.asStateFlow()
    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering.asStateFlow()

    fun registerZkKeys(customNullifier: String, secret: String) {
        viewModelScope.launch {
            _isRegistering.value = true
            try {
                val leaf = withContext(Dispatchers.Default) {
                    try {
                        PoseidonHash.poseidon2(customNullifier, secret)
                    } catch (e: IllegalArgumentException) {
                        _registrationStatus.value = e.message
                        null
                    }
                }

                if (leaf == null) return@launch

                val request = MerkleRegisterRequest(leaf)
                val response = RetrofitClient.apiService.registerMerkle(request)

                if (response.isSuccessful) {
                    ZkKeyManager.saveKeys(
                        getApplication(),
                        HexUtils.requireHex(customNullifier, "custom nullifier"),
                        HexUtils.requireHex(secret, "secret")
                    )
                    _registrationStatus.value = "Anonymous ID registered successfully."
                } else {
                    _registrationStatus.value = "Registration failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _registrationStatus.value = "An error occurred: ${e.message}"
            } finally {
                _isRegistering.value = false
            }
        }
    }
}
