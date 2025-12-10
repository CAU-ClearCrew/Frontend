package com.example.mobileappdevelopment.veiwmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappdevelopment.api.MerkleRegisterRequest
import com.example.mobileappdevelopment.api.RetrofitClient
import com.example.mobileappdevelopment.util.ZkKeyManager
import com.loopring.poseidon.PoseidonHash
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

    fun registerZkKeys(customNullifier: String, secret: String) {
        viewModelScope.launch {
            try {
                val leaf = withContext(Dispatchers.Default) {
                    try {
                        val hasher: PoseidonHash = PoseidonHash.Digest.newInstance(PoseidonHash.DefaultParams)
                        // The library is strict by default, so we disable it to prevent crashes if hex strings are not field elements.
                        (hasher as PoseidonHash.Digest).setStrict(false)

                        hasher.add(BigInteger(customNullifier, 16))
                        hasher.add(BigInteger(secret, 16))
                        
                        // digest(false) returns BigInteger[] where the first element is the hash
                        hasher.digest(false)[0].toString(16)
                    } catch (e: NumberFormatException) {
                        _registrationStatus.value = "입력 값은 16진수 문자(0-9, a-f)만 포함해야 합니다."
                        null
                    }
                }

                if (leaf == null) return@launch

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
