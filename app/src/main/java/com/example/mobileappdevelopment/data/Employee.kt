package com.example.mobileappdevelopment.data

data class Employee(
    val id: String,
    val name: String,
    val email: String,
    val department: String,
    val position: String,
    val joinDate: String,
    val status: EmployeeStatus
)

