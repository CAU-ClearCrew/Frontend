package com.example.mobileappdevelopment.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PoseidonHashTest {

    @Test
    fun poseidon1_matchesKnownPoseidonLiteVector() {
        assertEquals(
            "29176100eaa962bdc1fe6c654d6a3c130e96a4d1168b33848b897dc502820133",
            PoseidonHash.poseidon1("0x1")
        )
    }

    @Test
    fun poseidon2_matchesKnownPoseidonLiteVector() {
        assertEquals(
            "115cc0f5e7d690413df64c6b9662e9cf2a3617f2743245519e19607a4417189a",
            PoseidonHash.poseidon2("0x1", "0x2")
        )
    }
}
