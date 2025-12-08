package com.example.mobileappdevelopment.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappdevelopment.api.RetrofitClient
import com.example.mobileappdevelopment.api.SubmitReportRequest
import com.example.mobileappdevelopment.api.UpdateNotesRequest
import com.example.mobileappdevelopment.api.UpdatePriorityRequest
import com.example.mobileappdevelopment.api.UpdateStatusRequest
import com.example.mobileappdevelopment.data.Report
import com.example.mobileappdevelopment.data.ReportCategory
import com.example.mobileappdevelopment.data.ReportPriority
import com.example.mobileappdevelopment.data.ReportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _filterStatus = MutableStateFlow<ReportStatus?>(null)
    val filterStatus: StateFlow<ReportStatus?> = _filterStatus.asStateFlow()

    init {
        loadReports()
    }

    fun setFilterStatus(status: ReportStatus?) {
        _filterStatus.value = status
    }

    // loadReports()
    private fun loadReports() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getReports()
                if (response.isSuccessful) {
                    _reports.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _reports.value = emptyList()
            }
        }
    }

    // submitReport()
    fun submitReport(
        category: ReportCategory,
        title: String,
        description: String,
        department: String,
        date: String
    ) {
        viewModelScope.launch {
            try {
                val request = SubmitReportRequest(
                    category = category.name,
                    title = title,
                    description = description,
                    department = department,
                    date = date
                )
                val response = RetrofitClient.apiService.submitReport(request)
                if (response.isSuccessful) {
                    loadReports()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    // updateReportStatus()
    fun updateReportStatus(reportId: String, status: ReportStatus) {
        viewModelScope.launch {
            try {
                val request = UpdateStatusRequest(status.name)
                val response = RetrofitClient.apiService.updateReportStatus(reportId, request)
                if (response.isSuccessful) {
                    loadReports()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    // updateReportPriority()
    fun updateReportPriority(reportId: String, priority: ReportPriority) {
        viewModelScope.launch {
            try {
                val request = UpdatePriorityRequest(priority.name)
                val response = RetrofitClient.apiService.updateReportPriority(reportId, request)
                if (response.isSuccessful) {
                    loadReports()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    // updateReportNotes()
    fun updateReportNotes(reportId: String, notes: String) {
        viewModelScope.launch {
            try {
                val request = UpdateNotesRequest(notes)
                val response = RetrofitClient.apiService.updateReportNotes(reportId, request)
                if (response.isSuccessful) {
                    loadReports()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}