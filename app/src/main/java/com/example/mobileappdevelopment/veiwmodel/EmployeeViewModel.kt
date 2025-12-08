package com.example.mobileappdevelopment.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappdevelopment.api.CreateEmployeeRequest
import com.example.mobileappdevelopment.api.RetrofitClient
import com.example.mobileappdevelopment.api.StatusUpdateRequest
import com.example.mobileappdevelopment.api.UpdateEmployeeRequest
import com.example.mobileappdevelopment.data.Employee
import com.example.mobileappdevelopment.data.EmployeeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmployeeViewModel : ViewModel() {
    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadEmployees()
    }

    fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getEmployees()
                if (response.isSuccessful) {
                    _employees.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }


    fun addEmployee(employee: Employee) {
        viewModelScope.launch {
            try {
                val request = CreateEmployeeRequest(
                    email = employee.email,
                    password = "default123", // 기본 비밀번호
                    name = employee.name,
                    position = employee.position,
                    departmentId = null, // 부서 ID 매핑 필요
                    joinedAt = employee.joinDate,
                    status = if (employee.status == EmployeeStatus.ACTIVE) "ACTIVE" else "RESIGNED"
                )
                val response = RetrofitClient.apiService.createEmployee(request)
                if (response.isSuccessful) {
                    loadEmployees() // 목록 새로고침
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            try {
                val request = UpdateEmployeeRequest(
                    name = employee.name,
                    position = employee.position,
                    departmentId = null,
                    joinedAt = employee.joinDate
                )
                val response = RetrofitClient.apiService.updateEmployee(employee.id, request)
                if (response.isSuccessful) {
                    // 상태도 변경되었다면 추가로 상태 업데이트
                    val statusRequest = StatusUpdateRequest(
                        status = if (employee.status == EmployeeStatus.ACTIVE) "ACTIVE" else "RESIGNED"
                    )
                    RetrofitClient.apiService.updateEmployeeStatus(employee.id, statusRequest)
                    loadEmployees()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    fun deleteEmployee(id: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteEmployee(id)
                if (response.isSuccessful) {
                    loadEmployees()
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}
