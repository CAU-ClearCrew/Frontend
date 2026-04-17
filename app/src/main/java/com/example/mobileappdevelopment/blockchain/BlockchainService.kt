package com.example.mobileappdevelopment.blockchain

import android.content.Context
import android.util.Log
import com.example.ma_front.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String as AbiUtf8String
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import java.io.File
import java.math.BigInteger
import java.util.Locale

class BlockchainService(context: Context) {
    private companion object {
        const val TAG = "BlockchainService"
        const val HONK_PROOF_BYTES = 508 * 32
    }

    private val rpcUrl = context.getString(R.string.RPC_URL)
    private val contractAddress = context.getString(R.string.CONTRACT_ADDRESS)
    private val privateKey = context.getString(R.string.DEV_PRIVATE_KEY)
    private val appContext = context.applicationContext

    private val web3j: Web3j = Web3j.build(HttpService(rpcUrl))

    suspend fun getCurrentMerkleRoot(): String? {
        return withContext(Dispatchers.IO) {
            try {
                require(contractAddress.isNotBlank()) { "CONTRACT_ADDRESS is missing in local.properties." }
                require(rpcUrl.isNotBlank()) { "RPC_URL is missing in local.properties." }

                val function = Function(
                    "currentMerkleRoot",
                    emptyList(),
                    listOf(object : TypeReference<Bytes32>() {})
                )
                val encodedFunction = FunctionEncoder.encode(function)
                val response = web3j.ethCall(
                    Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST
                ).send()

                if (response.hasError()) {
                    Log.e(TAG, "Failed to read currentMerkleRoot: ${response.error.message}")
                    return@withContext null
                }

                val decoded: List<Type<*>> = FunctionReturnDecoder.decode(
                    response.value,
                    function.outputParameters
                )
                val root = (decoded.firstOrNull()?.value as? ByteArray)
                    ?.joinToString("") { "%02x".format(it) }
                    ?.lowercase(Locale.US)
                    ?.let { "0x$it" }

                Log.d(TAG, "On-chain currentMerkleRoot: $root")
                root
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read currentMerkleRoot", e)
                null
            }
        }
    }

    suspend fun submitWhistleblow(
        proof: ByteArray,
        ipfsCid: String,
        root: ByteArray,
        nullifierHash: ByteArray
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                require(privateKey.isNotBlank()) { "DEV_PRIVATE_KEY is missing in local.properties." }
                require(contractAddress.isNotBlank()) { "CONTRACT_ADDRESS is missing in local.properties." }
                require(rpcUrl.isNotBlank()) { "RPC_URL is missing in local.properties." }
                require(ipfsCid.isNotBlank()) { "IPFS CID is missing." }

                Log.d(TAG, "Preparing blockchain submission")
                Log.d(TAG, "RPC URL: $rpcUrl")
                Log.d(TAG, "Contract Address: $contractAddress")
                Log.d(TAG, "IPFS CID: $ipfsCid")
                val rootPaddedFromBackend = leftPad32(root)
                val nullifierPadded = leftPad32(nullifierHash)
                val normalizedProof = normalizeProofForSolidity(proof, rootPaddedFromBackend, nullifierPadded)
                val proofPublicInputs = extractPublicInputsFromProof(proof)
                val proofPublicRoot = proofPublicInputs?.first
                proofPublicRoot?.let {
                    require(it.contentEquals(rootPaddedFromBackend)) {
                        "Proof public root does not match backend root."
                    }
                }
                proofPublicInputs?.second?.let {
                    require(it.contentEquals(nullifierPadded)) {
                        "Proof public nullifier does not match submitted nullifier."
                    }
                }
                Log.d(TAG, "Proof bytes: ${proof.size}")
                Log.d(TAG, "Normalized proof bytes: ${normalizedProof.size}")
                logProofLayout(proof, normalizedProof)
                Log.d(TAG, "Root bytes: ${root.size}")
                Log.d(TAG, "Nullifier bytes: ${nullifierHash.size}")
                writeProofDebugFiles(proof, normalizedProof, rootPaddedFromBackend, nullifierPadded)

                val credentials = Credentials.create(privateKey)

                // Ensure values are 32 bytes, left-padding with zeros if necessary.
                val rootPadded = proofPublicRoot ?: rootPaddedFromBackend
                if (proofPublicRoot != null) {
                    Log.d(TAG, "Using proof public input root: ${rootPadded.toHex()}")
                }
                Log.d(TAG, "Using nullifier hash: ${nullifierPadded.toHex()}")

                val verifierAddress = readVerifierAddress()
                if (verifierAddress != null) {
                    Log.d(TAG, "Contract verifier address: $verifierAddress")
                    logDirectVerifierResult(verifierAddress, normalizedProof, rootPadded, nullifierPadded)
                }

                val function = org.web3j.abi.datatypes.Function(
                    "submitWhistleblow",
                    listOf(
                        DynamicBytes(normalizedProof),
                        Utf8String(ipfsCid),
                        Bytes32(rootPadded),
                        Bytes32(nullifierPadded)
                    ),
                    emptyList()
                )

                val encodedFunction = FunctionEncoder.encode(function)
                Log.d(TAG, "Encoded function length: ${encodedFunction.length}")

                val chainId = web3j.ethChainId().send().chainId
                Log.d(TAG, "Chain ID: $chainId")
                val fromAddress = credentials.address
                Log.d(TAG, "From Address: $fromAddress")

                val simulationTx = Transaction.createEthCallTransaction(
                    fromAddress,
                    contractAddress,
                    encodedFunction
                )
                val callResult = web3j.ethCall(simulationTx, DefaultBlockParameterName.LATEST).send()
                if (callResult.hasError()) {
                    Log.e(TAG, "eth_call simulation failed: ${callResult.error.message}")
                    Log.e(TAG, "eth_call raw value: ${callResult.value}")
                    decodeRevertReason(callResult.value)?.let {
                        Log.e(TAG, "eth_call revert reason: $it")
                    }
                    return@withContext null
                }
                Log.d(TAG, "eth_call simulation succeeded")

                val gasEstimate = web3j.ethEstimateGas(
                    Transaction.createFunctionCallTransaction(
                        fromAddress,
                        null,
                        DefaultGasProvider.GAS_PRICE,
                        DefaultGasProvider.GAS_LIMIT,
                        contractAddress,
                        encodedFunction
                    )
                ).send()
                if (gasEstimate.hasError()) {
                    Log.e(TAG, "eth_estimateGas failed: ${gasEstimate.error.message}")
                    gasEstimate.error.data?.let {
                        Log.e(TAG, "eth_estimateGas error data: $it")
                        decodeRevertReason(it)?.let { reason ->
                            Log.e(TAG, "eth_estimateGas revert reason: $reason")
                        }
                    }
                    return@withContext null
                }
                Log.d(TAG, "Estimated gas: ${gasEstimate.amountUsed}")

                val transactionManager: TransactionManager = RawTransactionManager(web3j, credentials, chainId.toLong())

                val response = transactionManager.sendTransaction(
                    DefaultGasProvider.GAS_PRICE,
                    DefaultGasProvider.GAS_LIMIT,
                    contractAddress,
                    encodedFunction,
                    BigInteger.ZERO
                )

                if (response.hasError()) {
                    Log.e(TAG, "Blockchain transaction failed: ${response.error.message}")
                } else {
                    Log.d(TAG, "Blockchain transaction sent: ${response.transactionHash}")
                }

                response.transactionHash
            } catch (e: Exception) {
                Log.e(TAG, "Blockchain submission failed", e)
                null
            }
        }
    }

    private fun readVerifierAddress(): String? {
        val function = Function(
            "verifier",
            emptyList(),
            listOf(object : TypeReference<Address>() {})
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val response = web3j.ethCall(
            Transaction.createEthCallTransaction(null, contractAddress, encodedFunction),
            DefaultBlockParameterName.LATEST
        ).send()

        if (response.hasError()) {
            Log.e(TAG, "Failed to read verifier address: ${response.error.message}")
            return null
        }

        val decoded = FunctionReturnDecoder.decode(response.value, function.outputParameters)
        return decoded.firstOrNull()?.value as? String
    }

    private fun logDirectVerifierResult(
        verifierAddress: String,
        proof: ByteArray,
        root: ByteArray,
        nullifierHash: ByteArray
    ) {
        val function = Function(
            "verify",
            listOf(
                DynamicBytes(proof),
                DynamicArray(
                    Bytes32::class.java,
                    listOf(Bytes32(root), Bytes32(nullifierHash))
                )
            ),
            listOf(object : TypeReference<org.web3j.abi.datatypes.Bool>() {})
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val response = web3j.ethCall(
            Transaction.createEthCallTransaction(null, verifierAddress, encodedFunction),
            DefaultBlockParameterName.LATEST
        ).send()

        if (response.hasError()) {
            Log.e(TAG, "Direct verifier.verify failed: ${response.error.message}")
            Log.e(TAG, "Direct verifier.verify raw value: ${response.value}")
            decodeRevertReason(response.value)?.let {
                Log.e(TAG, "Direct verifier.verify revert reason: $it")
            }
            return
        }

        val decoded = FunctionReturnDecoder.decode(response.value, function.outputParameters)
        Log.d(TAG, "Direct verifier.verify result: ${decoded.firstOrNull()?.value}")
    }

    private fun leftPad32(value: ByteArray): ByteArray {
        return ByteArray(32).apply {
            val offset = 32 - value.size
            require(offset >= 0) { "Expected at most 32 bytes but got ${value.size}." }
            System.arraycopy(value, 0, this, offset, value.size)
        }
    }

    private fun normalizeProofForSolidity(
        proof: ByteArray,
        expectedRoot: ByteArray,
        expectedNullifier: ByteArray
    ): ByteArray {
        if (proof.size == HONK_PROOF_BYTES) {
            return proof
        }

        if (
            proof.size >= 64 + HONK_PROOF_BYTES &&
            proof.copyOfRange(0, 32).contentEquals(expectedRoot) &&
            proof.copyOfRange(32, 64).contentEquals(expectedNullifier)
        ) {
            val offset = 64
            Log.w(
                TAG,
                "Proof includes public root/nullifier prefix; slicing Solidity proof ${HONK_PROOF_BYTES} bytes from offset $offset"
            )
            return proof.copyOfRange(offset, offset + HONK_PROOF_BYTES)
        }

        if (proof.size >= 32 + HONK_PROOF_BYTES && proof.copyOfRange(0, 32).contentEquals(expectedRoot)) {
            val offset = 32
            Log.w(
                TAG,
                "Proof includes public root prefix only; slicing Solidity proof ${HONK_PROOF_BYTES} bytes from offset $offset"
            )
            return proof.copyOfRange(offset, offset + HONK_PROOF_BYTES)
        }

        if (proof.size > HONK_PROOF_BYTES) {
            val offset = proof.size - HONK_PROOF_BYTES
            Log.w(
                TAG,
                "Proof larger than Solidity verifier expectation; falling back to tail ${HONK_PROOF_BYTES} bytes from offset $offset"
            )
            return proof.copyOfRange(offset, proof.size)
        }

        throw IllegalArgumentException(
            "Proof shorter than Solidity verifier expectation: ${proof.size} < $HONK_PROOF_BYTES"
        )
    }

    private fun extractPublicInputsFromProof(proof: ByteArray): Pair<ByteArray, ByteArray>? {
        if (proof.size <= HONK_PROOF_BYTES || proof.size < 64) {
            return null
        }

        return Pair(
            proof.copyOfRange(0, 32),
            proof.copyOfRange(32, 64)
        )
    }

    private fun logProofLayout(proof: ByteArray, normalizedProof: ByteArray) {
        Log.d(TAG, "Proof first32: ${proof.copyOfRange(0, minOf(32, proof.size)).toHex()}")
        if (proof.size >= 64) {
            Log.d(TAG, "Proof second32: ${proof.copyOfRange(32, 64).toHex()}")
        }
        Log.d(TAG, "Normalized proof first32: ${normalizedProof.copyOfRange(0, minOf(32, normalizedProof.size)).toHex()}")
        if (normalizedProof.size >= 64) {
            Log.d(TAG, "Normalized proof second32: ${normalizedProof.copyOfRange(32, 64).toHex()}")
        }
    }

    private fun writeProofDebugFiles(
        rawProof: ByteArray,
        proof: ByteArray,
        root: ByteArray,
        nullifierHash: ByteArray
    ) {
        val dir = File(appContext.filesDir, "zk-proof-debug").apply { mkdirs() }
        val rawProofFile = File(dir, "raw_proof")
        val proofFile = File(dir, "proof")
        val publicInputsFile = File(dir, "public_inputs")

        rawProofFile.writeBytes(rawProof)
        proofFile.writeBytes(proof)
        publicInputsFile.writeBytes(root + nullifierHash)

        Log.d(TAG, "Wrote debug raw proof file: ${rawProofFile.absolutePath}")
        Log.d(TAG, "Wrote debug proof file: ${proofFile.absolutePath}")
        Log.d(TAG, "Wrote debug public inputs file: ${publicInputsFile.absolutePath}")
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "", prefix = "0x") { "%02x".format(it) }

    private fun decodeRevertReason(data: String?): String? {
        if (data.isNullOrBlank()) return null
        val normalized = data.removePrefix("0x")
        if (!normalized.startsWith("08c379a0") || normalized.length <= 8) return null

        return try {
            val encodedReason = "0x" + normalized.substring(8)
            val revertFunction = Function(
                "Error",
                emptyList(),
                listOf(object : TypeReference<AbiUtf8String>() {})
            )
            val decoded = FunctionReturnDecoder.decode(encodedReason, revertFunction.outputParameters)
            (decoded.firstOrNull()?.value as? String)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode revert reason", e)
            null
        }
    }
}
