package com.example.mobileappdevelopment.data

data class User(
    val email: String,
    val name: String,
    val role: UserRole,
    val department: String? = null
)

