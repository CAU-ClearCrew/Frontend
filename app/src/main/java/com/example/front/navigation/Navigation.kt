package com.example.front.navigation

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.front.data.User
import com.example.front.data.UserRole
import com.example.front.ui.AdminDashboardScreen
import com.example.front.ui.AnonymousReportScreen


// Main App with Navigation
@Composable
fun CompanyManagementApp(modifier : Modifier = Modifier) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Test accounts
    val testAccounts = mapOf(
        "admin@company.com" to Pair("admin123", User("admin@company.com", "관리자", UserRole.ADMIN)),
        "minsu.kim@company.com" to Pair("password123", User("minsu.kim@company.com", "김민수", UserRole.EMPLOYEE, "개발팀"))
    )

    fun handleLogin(email: String, password: String, role: UserRole) {
        val account = testAccounts[email]
        if (account != null && account.first == password && account.second.role == role) {
            currentUser = account.second
            loginError = null
        } else {
            loginError = "이메일, 비밀번호 또는 로그인 유형이 올바르지 않습니다"
        }
    }

    when {
        currentUser == null -> {
            LoginScreen(
                onLogin = { email, password, role -> handleLogin(email, password, role) },
                error = loginError
            )
        }
        currentUser?.role == UserRole.EMPLOYEE -> {
            AnonymousReportScreen(currentUser = currentUser!!)
        }
        currentUser?.role == UserRole.ADMIN -> {
            AdminDashboardScreen(currentUser = currentUser!!)
        }
    }
}

