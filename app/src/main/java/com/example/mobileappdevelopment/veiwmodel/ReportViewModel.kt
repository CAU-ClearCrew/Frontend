package com.example.mobileappdevelopment.veiwmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappdevelopment.api.RetrofitClient
import com.example.mobileappdevelopment.api.UpdateNotesRequest
import com.example.mobileappdevelopment.api.UpdatePriorityRequest
import com.example.mobileappdevelopment.api.UpdateStatusRequest
import com.example.mobileappdevelopment.data.Report
import com.example.mobileappdevelopment.data.ReportCategory
import com.example.mobileappdevelopment.data.ReportPriority
import com.example.mobileappdevelopment.data.ReportStatus
import com.example.mobileappdevelopment.util.ZkKeyManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.gson.responseObject
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.gson.Gson
import com.noirandroid.lib.Circuit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.GeneralSecurityException

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _filterStatus = MutableStateFlow<ReportStatus?>(null)
    val filterStatus: StateFlow<ReportStatus?> = _filterStatus.asStateFlow()

    private val gson = Gson()

    init {
        loadReports()
        try {
            AeadConfig.register()
        } catch (e: GeneralSecurityException) {
            // Handle the exception
        }
    }

    private fun loadCircuit(): Circuit {
        val json = getApplication<Application>().assets.open("zkClearCrew.json")
            .bufferedReader()
            .use { it.readText() }

        val circuit = Circuit.fromJsonManifest(json)

        val srs = File(getApplication<Application>().filesDir, "srs")
        if (srs.exists()) {
            circuit.setupSrs(srs.path)
        } else {
            circuit.setupSrs()
        }
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

    private suspend fun encryptData(data: String, publicKey: String): ByteArray = withContext(Dispatchers.IO) {
        val keysetHandle = com.google.crypto.tink.CleartextKeysetHandle.read(
            com.google.crypto.tink.JsonKeysetReader.withString(publicKey)
        )
        val aead = keysetHandle.getPrimitive(Aead::class.java)
        aead.encrypt(data.toByteArray(), null)
    }

    private suspend fun uploadToIpfs(data: ByteArray): String = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("report", ".bin", getApplication<Application>().cacheDir)
        tempFile.writeBytes(data)

        val response = Fuel.upload("/pinning/pinFileToIPFS", Method.POST)
            .add(FileDataPart(tempFile, name = "file"))
            .header("Authorization", "Bearer d35f9fdaa852c589d654")
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

                // 5. Get Merkle Tree info
                val merkleInfoResponse = RetrofitClient.apiService.getMerkleTreeInfo()
                if (!merkleInfoResponse.isSuccessful) {
                    // Handle error
                    return@launch
                }
                val merkleInfo = merkleInfoResponse.body()!!

                // For now, we will use dummy data for ZK proof generation
                val itemKey = "0"
                val itemNextIdx = "0"
                val itemNextKey = "0"
                val itemValue = "0"
                val pathElements = List(8) { "0" }
                val pathIndices = List(8) { "0" }
                val activeBits = List(8) { "0" }

                // 6. Generate ZK Proof
                val proof = generateProof(
                    customNullifier,
                    secret,
                    itemKey,
                    itemNextIdx,
                    itemNextKey,
                    itemValue,
                    pathElements,
                    pathIndices,
                    activeBits,
                    merkleInfo.root,
                    "0" // dummy nullifier hash for now
                )

                // 7. Submit to the smart contract (or server)
                val zkReportRequest = com.example.mobileappdevelopment.api.ZkReportRequest(
                    encryptedContent = cid,
                    zkProof = proof,
                    nullifierHash = "0", // dummy nullifier hash
                    root = merkleInfo.root
                )
                val zkReportResponse = RetrofitClient.apiService.submitZkReport(zkReportRequest)

                if (zkReportResponse.isSuccessful) {
                    loadReports()
                }

            } catch (e: Exception) {
                // Handle error
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
