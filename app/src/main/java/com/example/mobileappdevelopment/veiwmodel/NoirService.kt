package com.example.mobileappdevelopment.veiwmodel

import android.content.Context
import com.noirandroid.lib.Circuit
import java.io.File

class NoirService(private val context: Context) {

    private fun loadCircuit(context: Context): Circuit {
        val json = context.assets.open("zkClearCrew.json")
            .bufferedReader()
            .use { it.readText() }

        val circuit = Circuit.fromJsonManifest(json)
        
        val srs = File(context.filesDir, "srs")
        if (srs.exists()) {
            circuit.setupSrs(srs.path)
        } else {
            circuit.setupSrs()
            // Consider saving the SRS to the file for future use
        }
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
        val circuit = loadCircuit(context)

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
}
