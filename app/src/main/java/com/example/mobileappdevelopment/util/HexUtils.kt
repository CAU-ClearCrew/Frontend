package com.example.mobileappdevelopment.util

import java.math.BigInteger

object HexUtils {
    fun normalizeHex(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.startsWith("0x", ignoreCase = true)) {
            trimmed.substring(2)
        } else {
            trimmed
        }
    }

    fun requireHex(value: String, fieldName: String): String {
        val normalized = normalizeHex(value)
        require(normalized.isNotBlank()) { "$fieldName must not be empty." }
        require(normalized.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
            "$fieldName must be a hexadecimal value."
        }
        return normalized.lowercase()
    }

    fun toBigInteger(value: String, fieldName: String): BigInteger {
        return BigInteger(requireHex(value, fieldName), 16)
    }

    fun toByteArray(value: String): ByteArray {
        val normalized = normalizeHex(value).let {
            if (it.length % 2 == 0) it else "0$it"
        }
        return normalized.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
