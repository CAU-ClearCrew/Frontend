package com.example.mobileappdevelopment.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileappdevelopment.api.LoginRequest
import com.example.mobileappdevelopment.api.RetrofitClient
import com.example.mobileappdevelopment.api.TokenManager
import com.example.mobileappdevelopment.data.User
import com.example.mobileappdevelopment.data.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    fun login(email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = RetrofitClient.apiService.login(request)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    loginResponse?.let {
                        // 토큰 저장
                        TokenManager.saveToken(it.token)

                        // User 객체 생성
                        val userRole = if (it.role == "ADMIN") UserRole.ADMIN else UserRole.EMPLOYEE
                        _currentUser.value = User(
                            email = email,
                            name = it.name,
                            role = userRole
                        )
                        _loginError.value = null
                    }
                } else {
                    _loginError.value = "로그인에 실패했습니다."
                }
            } catch (e: Exception) {
                _loginError.value = "네트워크 오류: ${e.message}"
            }
//
//            /*테스트용 아이디&비번*/
//            when {
//                //관리자
//                email == "admin@company.com" && password == "admin123" && role == UserRole.ADMIN -> {
//                    _currentUser.value = User(
//                        email = "admin@company.com",
//                        name = "관리자",
//                        role = UserRole.ADMIN,
//                        department = "시스템 관리" // department 필드가 있다면 추가
//                    )
//                    _loginError.value = null
//                }
//                //사원
//                email == "minsu.kim@company.com" && password == "password123" && role == UserRole.EMPLOYEE -> {
//                    _currentUser.value = User(
//                        email = "minsu.kim@company.com",
//                        name = "김민수",
//                        role = UserRole.EMPLOYEE,
//                        department = "개발팀" // department 필드가 있다면 추가
//                    )
//                    _loginError.value = null
//                }
//
//                else -> {
//                    _loginError.value = "이메일 또는 비밀번호가 올바르지 않습니다."
//                }
//            }
        }
    }

    fun logout() {
        TokenManager.clearToken()  // 토큰 삭제
        _currentUser.value = null
    }
}