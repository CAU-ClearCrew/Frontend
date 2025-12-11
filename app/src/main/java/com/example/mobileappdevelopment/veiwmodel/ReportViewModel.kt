package com.example.mobileappdevelopment.veiwmodel

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ma_front.R
import com.example.mobileappdevelopment.api.RetrofitClient
import com.example.mobileappdevelopment.api.UpdateNotesRequest
import com.example.mobileappdevelopment.api.UpdatePriorityRequest
import com.example.mobileappdevelopment.api.UpdateStatusRequest
import com.example.mobileappdevelopment.blockchain.BlockchainService
import com.example.mobileappdevelopment.data.Report
import com.example.mobileappdevelopment.data.ReportCategory
import com.example.mobileappdevelopment.data.ReportPriority
import com.example.mobileappdevelopment.data.ReportStatus
import com.example.mobileappdevelopment.util.ZkKeyManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.Gson
import com.loopring.poseidon.PoseidonHash
import com.noirandroid.lib.Circuit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _filterStatus = MutableStateFlow<ReportStatus?>(null)
    val filterStatus: StateFlow<ReportStatus?> = _filterStatus.asStateFlow()

    private val gson = Gson()
    private val blockchainService = BlockchainService(application)

    init {
        loadReports()
    }

    private suspend fun poseidon1(input: String): String = withContext(Dispatchers.Default) {
        val hasher: PoseidonHash = PoseidonHash.Digest.newInstance(PoseidonHash.DefaultParams)
        (hasher as PoseidonHash.Digest).setStrict(false)
        hasher.add(BigInteger(input, 16))
        val hashResult = hasher.digest(false)
        hashResult[0].toString(16)
    }

    private suspend fun poseidon2(input1: String, input2: String): String = withContext(Dispatchers.Default) {
        val hasher: PoseidonHash = PoseidonHash.Digest.newInstance(PoseidonHash.DefaultParams)
        (hasher as PoseidonHash.Digest).setStrict(false)
        hasher.add(BigInteger(input1, 16))
        hasher.add(BigInteger(input2, 16))
        val hashResult = hasher.digest(false)
        hashResult[0].toString(16)
    }

    private fun loadCircuit(): Circuit {
        val application = getApplication<Application>()
        val json = application.assets.open("zkClearCrew.json")
            .bufferedReader()
            .use { it.readText() }

        val circuit = Circuit.fromJsonManifest(json)

        // Copy SRS from assets to internal storage if it doesn't exist
        val srsFile = File(application.filesDir, "srs")
        if (!srsFile.exists()) {
            application.assets.open("srs").use { input ->
                FileOutputStream(srsFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        circuit.setupSrs(srsFile.path)
        return circuit
    }

    private fun generateProof(
        customNullifier: String,
        secret: String,
        itemKey: String,
        itemNextIdx: String,
        itemNextKey: String,
        itemValue: String,
        pathElements: List<String>,
        pathIndices: List<String>,
        activeBits: List<String>,
        root: String,
        nullifierHash: String
    ): String {
        val circuit = loadCircuit()

        val inputs = hashMapOf<String, Any>().apply {
            this["custom_nullifier"] = customNullifier
            this["secret"] = secret
            this["item_key"] = itemKey
            this["item_nextIdx"] = itemNextIdx
            this["item_nextKey"] = itemNextKey
            this["item_value"] = itemValue
            this["path_elements"] = pathElements
            this["path_indices"] = pathIndices
            this["active_bits"] = activeBits
            this["root"] = root
            this["nullifier_hash"] = nullifierHash
        }

        return circuit.prove(inputs)
    }

    fun setFilterStatus(status: ReportStatus?) {
        _filterStatus.value = status
    }

    private suspend fun encryptData(data: String, publicKeyPem: String): ByteArray = withContext(Dispatchers.IO) {
        val keyBytes = Base64.decode(publicKeyPem.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replace("\n", ""), Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        cipher.doFinal(data.toByteArray())
    }

    private suspend fun uploadToIpfs(data: ByteArray): String = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("report", ".bin", getApplication<Application>().cacheDir)
        tempFile.writeBytes(data)
        
        val pinataJwt = getApplication<Application>().getString(R.string.PINATA_JWT)
        Log.d("PinataJWTCheck", "JWT: $pinataJwt")

        val base = "https://api.pinata.cloud"
        val response = Fuel.upload(base + "/pinning/pinFileToIPFS", Method.POST)
            .add(FileDataPart(tempFile, name = "file"))
            .header("Authorization", "Bearer $pinataJwt")
            .responseObject<IpfsResponse>(gson)

        tempFile.delete()
        response.third.get().ipfsHash
    }

    private fun loadReports() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getReports()
                if (response.isSuccessful) {
                    _reports.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _reports.value = emptyList()
            }
        }
    }

    fun submitReport(
        category: ReportCategory,
        title: String,
        description: String,
        department: String,
        date: String
    ) {
        viewModelScope.launch {
            try {
                val customNullifier = ZkKeyManager.getCustomNullifier(getApplication()) ?: return@launch
                val secret = ZkKeyManager.getSecret(getApplication()) ?: return@launch

                // 1. Create JSON from report data
                val reportJson = gson.toJson(mapOf(
                    "category" to category.name,
                    "title" to title,
                    "description" to description,
                    "department" to department,
                    "date" to date
                ))

                // 2. Get public key from the server
                val publicKeyResponse = RetrofitClient.apiService.getPublicKey()
                if (!publicKeyResponse.isSuccessful) {
                    // Handle error
                    return@launch
                }
                val publicKey = publicKeyResponse.body()!!.publicKey

                // 3. Encrypt data
                val encryptedData = encryptData(reportJson, publicKey)

                // 4. Upload to IPFS
                val cid = uploadToIpfs(encryptedData)

                // 5. Get Merkle Tree info (Circuit Inputs)
                val circuitInputsResponse = RetrofitClient.apiService.getMerkleTreeInfo()
                if (!circuitInputsResponse.isSuccessful) {
                    // Handle error
                    return@launch
                }
                val circuitInputs = circuitInputsResponse.body()!!

                // 6. Calculate hashes required for the proof
                val nullifierHash = poseidon1(customNullifier)
                val itemValue = poseidon2(customNullifier, secret)

                // 7. Generate ZK Proof using real data from the API
                val proofString = generateProof(
                    customNullifier,
                    secret,
                    circuitInputs.item_key,
                    circuitInputs.item_nextIdx,
                    circuitInputs.item_nextKey,
                    itemValue, // Use calculated itemValue
                    circuitInputs.path_elements,
                    circuitInputs.path_indices,
                    circuitInputs.active_bits,
                    circuitInputs.root,
                    nullifierHash
                )

                // 8. Submit to the smart contract
                val proofBytes = proofString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                val rootBytes = circuitInputs.root.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                val txHash = blockchainService.submitWhistleblow(proofBytes, cid, rootBytes)

                if (txHash != null) {
                    loadReports()
                }

            } catch (t: Throwable) {
                Log.e("ReportViewModel", "Failed to submit report", t)
            }
        }
    }

    fun updateReportStatus(reportId: String, status: ReportStatus) {
        viewModelScope.launch {
            try {
                val request = UpdateStatusRequest(status.name)
                val response = RetrofitClient.apiService.updateReportStatus(reportId, request)
                if (response.isSuccessful) {
                    loadReports()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    fun updateReportPriority(reportId: String, priority: ReportPriority) {
        viewModelScope.launch {
            try {
                val request = UpdatePriorityRequest(priority.name)
                val response = RetrofitClient.apiService.updateReportPriority(reportId, request)
                if (response.isSuccessful) {
                    loadReports()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    fun updateReportNotes(reportId: String, notes: String) {
        viewModelScope.launch {
            try {
                val request = UpdateNotesRequest(notes)
                val response = RetrofitClient.apiService.updateReportNotes(reportId, request)
                if (response.isSuccessful) {
                    loadReports()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}

data class IpfsResponse(val ipfsHash: String)
