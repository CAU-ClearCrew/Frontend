package com.example.mobileappdevelopment.veiwmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoirServiceTest {

    @Test
    fun buildProofInputs_convertsDecimalAndHexValuesToWitnessHex() {
        val inputs = NoirService.buildProofInputs(
            customNullifier = "1234",
            secret = "0x10",
            itemKey = "0x3ee",
            itemNextIdx = "0",
            itemNextKey = "0x0",
            itemValue = "158ddb74957f922121ad7886d2f8f6d9de9e54819c7d8c20e7362ded309b37b2",
            pathElements = listOf("0xad105582e58b003ff82d191fcd1acdb2f3d7a33a755074790f8a686b085d409", "0x0"),
            pathIndices = listOf("1", "0"),
            activeBits = listOf("1", "0"),
            root = "24ac8aadcda752d74c09987a80537abeab5b6247909a88fe603829d1d4bafcae",
            nullifierHash = "0x01"
        )

        assertEquals("0x4d2", inputs["custom_nullifier"])
        assertEquals("0x10", inputs["secret"])
        assertEquals("0x3ee", inputs["item_key"])
        assertEquals("0x0", inputs["item_nextIdx"])
        assertEquals("0x0", inputs["item_nextKey"])
        assertEquals(
            "0x158ddb74957f922121ad7886d2f8f6d9de9e54819c7d8c20e7362ded309b37b2",
            inputs["item_value"]
        )
        assertEquals(
            listOf(
                "0xad105582e58b003ff82d191fcd1acdb2f3d7a33a755074790f8a686b085d409",
                "0x0"
            ),
            inputs["path_elements"]
        )
        assertEquals(listOf("0x1", "0x0"), inputs["path_indices"])
        assertEquals(listOf("0x1", "0x0"), inputs["active_bits"])
        assertEquals(
            "0x24ac8aadcda752d74c09987a80537abeab5b6247909a88fe603829d1d4bafcae",
            inputs["root"]
        )
        assertEquals("0x01", inputs["nullifier_hash"])
    }

    @Test
    fun buildProofInputs_rejectsInvalidWitnessValue() {
        val error = runCatching {
            NoirService.buildProofInputs(
                customNullifier = "not-a-number",
                secret = "1",
                itemKey = "2",
                itemNextIdx = "0",
                itemNextKey = "0",
                itemValue = "3",
                pathElements = listOf("0"),
                pathIndices = listOf("0"),
                activeBits = listOf("0"),
                root = "4",
                nullifierHash = "5"
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(error!!.message!!.contains("custom_nullifier"))
    }
}
