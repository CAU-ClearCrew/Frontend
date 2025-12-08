package com.example.mobileappdevelopment.blockchain

import android.content.Context
import com.example.ma_front.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

class BlockchainService(context: Context) {

    private val rpcUrl = context.getString(R.string.RPC_URL)
    private val contractAddress = context.getString(R.string.CONTRACT_ADDRESS)

    // WARNING: Do not store private keys in source code in a real application.
    private val privateKey = context.getString(R.string.DEV_PRIVATE_KEY)

    private val web3j: Web3j = Web3j.build(HttpService(rpcUrl))
    private val credentials = Credentials.create(privateKey)

    suspend fun submitWhistleblow(proof: ByteArray, ipfsCid: String, root: ByteArray): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure the root is 32 bytes, left-padding with zeros if necessary.
                val rootPadded = ByteArray(32).apply {
                    val offset = 32 - root.size
                    if (offset >= 0) {
                        System.arraycopy(root, 0, this, offset, root.size)
                    }
                }

                val function = org.web3j.abi.datatypes.Function(
                    "submitWhistleblow",
                    listOf(
                        DynamicBytes(proof),
                        Utf8String(ipfsCid),
                        Bytes32(rootPadded)
                    ),
                    emptyList()
                )

                val encodedFunction = FunctionEncoder.encode(function)

                val chainId = web3j.ethChainId().send().chainId
                val transactionManager: TransactionManager = RawTransactionManager(web3j, credentials, chainId.toLong())

                val response = transactionManager.sendTransaction(
                    DefaultGasProvider.GAS_PRICE,
                    DefaultGasProvider.GAS_LIMIT,
                    contractAddress,
                    encodedFunction,
                    BigInteger.ZERO
                )

                response.transactionHash
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}