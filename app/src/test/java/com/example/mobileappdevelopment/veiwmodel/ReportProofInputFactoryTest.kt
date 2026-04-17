package com.example.mobileappdevelopment.veiwmodel

import com.example.mobileappdevelopment.api.CircuitInputsResponse
import com.example.mobileappdevelopment.api.LeafItemResponse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportProofInputFactoryTest {

    @Test
    fun create_mapsBackendTreeInfoToProofRequest() {
        val circuitInputs = CircuitInputsResponse(
            root = "0x24ac8aadcda752d74c09987a80537abeab5b6247909a88fe603829d1d4bafcae",
            path_elements = listOf(
                "0xad105582e58b003ff82d191fcd1acdb2f3d7a33a755074790f8a686b085d409",
                "0x0"
            ),
            path_indices = listOf(1, 0),
            active_bits = listOf(1, 0),
            leaf_item = LeafItemResponse(
                key = "0x3ee",
                value = "0x158ddb74957f922121ad7886d2f8f6d9de9e54819c7d8c20e7362ded309b37b2",
                nextKey = "0x0",
                nextIdx = 0
            )
        )

        val request = ReportProofInputFactory.create(
            customNullifier = "0x1234",
            secret = "0xabcd",
            circuitInputs = circuitInputs,
            itemValue = "0xbeef",
            nullifierHash = "0xcafe"
        )

        assertEquals("0x1234", request.customNullifier)
        assertEquals("0xabcd", request.secret)
        assertEquals("0x3ee", request.itemKey)
        assertEquals("0", request.itemNextIdx)
        assertEquals("0x0", request.itemNextKey)
        assertEquals("0xbeef", request.itemValue)
        assertEquals(
            listOf(
                "0xad105582e58b003ff82d191fcd1acdb2f3d7a33a755074790f8a686b085d409",
                "0x0"
            ),
            request.pathElements
        )
        assertEquals(listOf("1", "0"), request.pathIndices)
        assertEquals(listOf("1", "0"), request.activeBits)
        assertEquals("0x24ac8aadcda752d74c09987a80537abeab5b6247909a88fe603829d1d4bafcae", request.root)
        assertEquals("0xcafe", request.nullifierHash)
    }

    @Test
    fun validateAgainstTreeInfo_rejectsLeafValueMismatchBeforeProving() {
        val circuitInputs = CircuitInputsResponse(
            root = "0x1",
            path_elements = listOf("0x0"),
            path_indices = listOf(0),
            active_bits = listOf(0),
            leaf_item = LeafItemResponse(
                key = "0x2",
                value = "0x9999",
                nextKey = "0x0",
                nextIdx = 0
            )
        )
        val request = ReportProofInputFactory.create(
            customNullifier = "0x1234",
            secret = "0xabcd",
            circuitInputs = circuitInputs,
            itemValue = "0xbeef",
            nullifierHash = "0xcafe"
        )

        val error = runCatching {
            ReportProofInputFactory.validateAgainstTreeInfo(request, circuitInputs)
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(error!!.message!!.contains("Local Poseidon output"))
    }
}
