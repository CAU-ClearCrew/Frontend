package com.example.mobileappdevelopment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileappdevelopment.navigation.MainScreen
import com.example.mobileappdevelopment.ui.screen.LoginScreen
import com.example.mobileappdevelopment.veiwmodel.AuthViewModel

@Composable
fun CompanyManagementApp() {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val loginError by authViewModel.loginError.collectAsState()

    if (currentUser == null) {
        LoginScreen(
            onLogin = { email, password, role ->
                authViewModel.login(email, password, role)
            },
            error = loginError
        )
    } else {
        MainScreen(
            currentUser = currentUser!!,
            onLogout = { authViewModel.logout() }
        )
    }
}