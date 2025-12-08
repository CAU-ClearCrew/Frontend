package com.example.mobileappdevelopment.data

data class Report(
    val id: String,
    val category: ReportCategory,
    val title: String,
    val description: String,
    val department: String,
    val date: String,
    val submittedAt: String,
    val status: ReportStatus,
    val priority: ReportPriority,
    val notes: String
)