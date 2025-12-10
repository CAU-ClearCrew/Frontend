package com.example.mobileappdevelopment

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileappdevelopment.navigation.MainScreen
import com.example.mobileappdevelopment.ui.screen.LoginScreen
import com.example.mobileappdevelopment.veiwmodel.AuthViewModel
import com.example.mobileappdevelopment.veiwmodel.ReportViewModel
import com.example.mobileappdevelopment.veiwmodel.ZkViewModel

@Composable
fun CompanyManagementApp() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
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
        val reportViewModel: ReportViewModel = viewModel(
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )
        val zkViewModel: ZkViewModel = viewModel(
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )
        MainScreen(
            currentUser = currentUser!!,
            onLogout = { authViewModel.logout() },
            reportViewModel = reportViewModel,
            zkViewModel = zkViewModel
        )
    }
}
