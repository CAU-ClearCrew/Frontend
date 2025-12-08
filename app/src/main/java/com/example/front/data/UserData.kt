package com.example.front.data


// Data Classes
enum class UserRole {
    EMPLOYEE, ADMIN
}

data class User(
    val email: String,
    val name: String,
    val role: UserRole,
    val department: String? = null
)

data class ReportFormData(
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val department: String = "",
    val date: String = ""
)