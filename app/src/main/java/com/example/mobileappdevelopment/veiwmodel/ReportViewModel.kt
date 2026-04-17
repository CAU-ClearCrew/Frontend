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
import com.example.mobileappdevelopment.util.HexUtils
import com.example.mobileappdevelopment.util.PoseidonHash
import com.example.mobileappdevelopment.util.ZkKeyManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _filterStatus = MutableStateFlow<ReportStatus?>(null)
    val filterStatus: StateFlow<ReportStatus?> = _filterStatus.asStateFlow()
    private val _submissionStatus = MutableStateFlow<String?>(null)
    val submissionStatus: StateFlow<String?> = _submissionStatus.asStateFlow()
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val gson = Gson()
    private val blockchainService = BlockchainService(application)
    private val noirService = NoirService(application)

    init {
        loadReports()
    }

    private suspend fun poseidon1(input: String): String = withContext(Dispatchers.Default) {
        PoseidonHash.poseidon1(input)
    }

    private suspend fun poseidon2(input1: String, input2: String): String = withContext(Dispatchers.Default) {
        PoseidonHash.poseidon2(input1, input2)
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

        val base = "https://api.pinata.cloud"
        val response = Fuel.upload(base + "/pinning/pinFileToIPFS", Method.POST)
            .add(FileDataPart(tempFile, name = "file"))
            .header("Authorization", "Bearer $pinataJwt")
            .responseObject<IpfsResponse>(gson)

        tempFile.delete()
        response.third.get().ipfsHash
            ?: throw IllegalStateException("Pinata response did not include an IPFS CID.")
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
            _isSubmitting.value = true
            _submissionStatus.value = null
            try {
                val customNullifier = ZkKeyManager.getCustomNullifier(getApplication())
                    ?: throw IllegalStateException("Register your Anonymous ID before submitting a report.")
                val secret = ZkKeyManager.getSecret(getApplication())
                    ?: throw IllegalStateException("Register your Anonymous ID before submitting a report.")

                val reportJson = gson.toJson(mapOf(
                    "category" to category.name,
                    "title" to title,
                    "description" to description,
                    "department" to department,
                    "date" to date
                ))

                val publicKeyResponse = RetrofitClient.apiService.getPublicKey()
                if (!publicKeyResponse.isSuccessful) {
                    throw IllegalStateException("Failed to fetch the server public key.")
                }
                val publicKey = publicKeyResponse.body()!!.publicKey

                val encryptedData = encryptData(reportJson, publicKey)
                val cid = uploadToIpfs(encryptedData)

                val circuitInputsResponse = RetrofitClient.apiService.getMerkleTreeInfo()
                if (!circuitInputsResponse.isSuccessful) {
                    throw IllegalStateException("Failed to fetch Merkle proof inputs. Register your Anonymous ID first.")
                }
                val circuitInputs = circuitInputsResponse.body()!!

                val nullifierHash = poseidon1(customNullifier)
                val itemValue = poseidon2(customNullifier, secret)
                val proofRequest = ReportProofInputFactory.create(
                    customNullifier = customNullifier,
                    secret = secret,
                    circuitInputs = circuitInputs,
                    itemValue = itemValue,
                    nullifierHash = nullifierHash
                )
                ReportProofInputFactory.validateAgainstTreeInfo(proofRequest, circuitInputs)

                val proofString = noirService.generateProof(
                    proofRequest.customNullifier,
                    proofRequest.secret,
                    proofRequest.itemKey,
                    proofRequest.itemNextIdx,
                    proofRequest.itemNextKey,
                    proofRequest.itemValue,
                    proofRequest.pathElements,
                    proofRequest.pathIndices,
                    proofRequest.activeBits,
                    proofRequest.root,
                    proofRequest.nullifierHash
                )

                val proofBytes = HexUtils.toByteArray(proofString)
                val rootBytes = HexUtils.toByteArray(circuitInputs.root)
                val nullifierHashBytes = HexUtils.toByteArray(proofRequest.nullifierHash)
                val onChainRoot = blockchainService.getCurrentMerkleRoot()
                val proofHex = "0x" + proofBytes.joinToString("") { "%02x".format(it) }
                logProofHexChunks(proofHex)
                Log.d("ReportViewModel", "Backend root: ${circuitInputs.root}")
                Log.d("ReportViewModel", "On-chain root: $onChainRoot")
                Log.d("ReportViewModel", "Nullifier hash: ${proofRequest.nullifierHash}")
                val txHash = blockchainService.submitWhistleblow(
                    proofBytes,
                    cid,
                    rootBytes,
                    nullifierHashBytes
                )

                if (txHash != null) {
                    loadReports()
                    _submissionStatus.value = "Anonymous report submitted successfully."
                } else {
                    _submissionStatus.value = "The proof was generated, but blockchain submission failed."
                }

            } catch (t: Throwable) {
                Log.e("ReportViewModel", "Failed to submit report", t)
                _submissionStatus.value = t.message ?: "Failed to submit report."
            } finally {
                _isSubmitting.value = false
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

    private fun logProofHexChunks(proofHex: String) {
        val chunkSize = 1800
        val parts = proofHex.chunked(chunkSize)
        parts.forEachIndexed { index, part ->
            Log.d("ReportViewModel", "Proof hex [${index + 1}/${parts.size}]: $part")
        }
    }
}

data class IpfsResponse(
    @com.google.gson.annotations.SerializedName("IpfsHash")
    val ipfsHash: String?
)

internal data class NoirProofRequest(
    val customNullifier: String,
    val secret: String,
    val itemKey: String,
    val itemNextIdx: String,
    val itemNextKey: String,
    val itemValue: String,
    val pathElements: List<String>,
    val pathIndices: List<String>,
    val activeBits: List<String>,
    val root: String,
    val nullifierHash: String
)

internal object ReportProofInputFactory {
    fun create(
        customNullifier: String,
        secret: String,
        circuitInputs: com.example.mobileappdevelopment.api.CircuitInputsResponse,
        itemValue: String,
        nullifierHash: String
    ): NoirProofRequest {
        return NoirProofRequest(
            customNullifier = customNullifier,
            secret = secret,
            itemKey = circuitInputs.leaf_item.key,
            itemNextIdx = circuitInputs.leaf_item.nextIdx.toString(),
            itemNextKey = circuitInputs.leaf_item.nextKey,
            itemValue = itemValue,
            pathElements = circuitInputs.path_elements,
            pathIndices = circuitInputs.path_indices.map { it.toString() },
            activeBits = circuitInputs.active_bits.map { it.toString() },
            root = circuitInputs.root,
            nullifierHash = nullifierHash
        )
    }

    fun validateAgainstTreeInfo(
        proofRequest: NoirProofRequest,
        circuitInputs: com.example.mobileappdevelopment.api.CircuitInputsResponse
    ) {
        val expectedLeafValue = HexUtils.requireHex(circuitInputs.leaf_item.value, "leaf_item.value")
        val actualLeafValue = HexUtils.requireHex(proofRequest.itemValue, "item_value")
        require(expectedLeafValue == actualLeafValue) {
            "Local Poseidon output does not match the registered Merkle leaf value."
        }
    }
}
