package com.example.mobileappdevelopment.veiwmodel

import android.content.Context
import com.noirandroid.lib.Circuit
import com.example.mobileappdevelopment.util.HexUtils
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger

class NoirService(private val context: Context) {

    private fun loadCircuit(): Circuit {
        val json = context.assets.open("zkClearCrew.json")
            .bufferedReader()
            .use { it.readText() }

        val circuit = Circuit.fromJsonManifest(json)

        val srs = File(context.filesDir, "srs")
        if (!srs.exists()) {
            context.assets.open("srs").use { input ->
                FileOutputStream(srs).use { output ->
                    input.copyTo(output)
                }
            }
        }
        circuit.setupSrs(srs.path)
        return circuit
    }

    fun generateProof(
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
        val verificationKey = circuit.getVerificationKey(EVM_PROOF_TYPE)
        writeDebugText("verification_key_hex", verificationKey)
        return circuit.prove(
            buildProofInputs(
                customNullifier = customNullifier,
                secret = secret,
                itemKey = itemKey,
                itemNextIdx = itemNextIdx,
                itemNextKey = itemNextKey,
                itemValue = itemValue,
                pathElements = pathElements,
                pathIndices = pathIndices,
                activeBits = activeBits,
                root = root,
                nullifierHash = nullifierHash
            ),
            verificationKey,
            EVM_PROOF_TYPE
        )
    }

    private fun writeDebugText(fileName: String, contents: String) {
        val debugDir = File(context.filesDir, "zk-proof-debug")
        if (!debugDir.exists()) {
            debugDir.mkdirs()
        }
        File(debugDir, fileName).writeText(contents)
    }

    internal companion object InputBuilder {
        private const val EVM_PROOF_TYPE = "ultra_honk_keccak"

        fun buildProofInputs(
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
        ): HashMap<String, Any> {
            return hashMapOf<String, Any>().apply {
                this["custom_nullifier"] = toWitnessHex(customNullifier, "custom_nullifier")
                this["secret"] = toWitnessHex(secret, "secret")
                this["item_key"] = toWitnessHex(itemKey, "item_key")
                this["item_nextIdx"] = toWitnessHex(itemNextIdx, "item_nextIdx")
                this["item_nextKey"] = toWitnessHex(itemNextKey, "item_nextKey")
                this["item_value"] = toWitnessHex(itemValue, "item_value")
                this["path_elements"] = pathElements.mapIndexed { index, value ->
                    toWitnessHex(value, "path_elements[$index]")
                }
                this["path_indices"] = pathIndices.mapIndexed { index, value ->
                    toWitnessHex(value, "path_indices[$index]")
                }
                this["active_bits"] = activeBits.mapIndexed { index, value ->
                    toWitnessHex(value, "active_bits[$index]")
                }
                this["root"] = toWitnessHex(root, "root")
                this["nullifier_hash"] = toWitnessHex(nullifierHash, "nullifier_hash")
            }
        }

        private fun toWitnessHex(value: String, fieldName: String): String {
            val trimmed = value.trim()
            return try {
                "0x" + BigInteger(trimmed).toString(16)
            } catch (_: NumberFormatException) {
                "0x" + HexUtils.requireHex(trimmed, fieldName)
            }
        }
    }
}
