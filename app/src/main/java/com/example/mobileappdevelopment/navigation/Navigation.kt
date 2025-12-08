package com.example.mobileappdevelopment.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobileappdevelopment.data.User
import com.example.mobileappdevelopment.data.UserRole
import com.example.mobileappdevelopment.ui.screen.AnonymousReportScreen
import com.example.mobileappdevelopment.ui.screen.EmployeeManagementScreen
import com.example.mobileappdevelopment.ui.screen.ReportManagementScreen
import com.example.mobileappdevelopment.ui.screen.ZkSettingsScreen
import com.example.mobileappdevelopment.veiwmodel.EmployeeViewModel
import com.example.mobileappdevelopment.veiwmodel.ReportViewModel
import com.example.mobileappdevelopment.veiwmodel.ZkViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object AnonymousReport : Screen("anonymous_report", "익명 고발", Icons.Default.Report)
    object ZkSettings : Screen("zk_settings", "익명 ID 설정", Icons.Default.VpnKey)
    object EmployeeManagement : Screen("employee_management", "사원 관리", Icons.Default.People)
    object ReportManagement : Screen("report_management", "신고 관리", Icons.Default.Assignment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentUser: User,
    onLogout: () -> Unit,
    reportViewModel: ReportViewModel,
    zkViewModel: ZkViewModel
) {
    val navController = rememberNavController()
    val employeeViewModel: EmployeeViewModel = viewModel()

    val screens = if (currentUser.role == UserRole.ADMIN) {
        listOf(
            Screen.AnonymousReport,
            Screen.ZkSettings,
            Screen.EmployeeManagement,
            Screen.ReportManagement
        )
    } else {
        listOf(Screen.AnonymousReport, Screen.ZkSettings)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회사 관리 시스템") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "로그아웃")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.AnonymousReport.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.AnonymousReport.route) {
                AnonymousReportScreen(
                    currentUser = currentUser,
                    onSubmit = { category, title, description, department, date ->
                        reportViewModel.submitReport(category, title, description, department, date)
                    }
                )
            }

            composable(Screen.ZkSettings.route) {
                ZkSettingsScreen(viewModel = zkViewModel)
            }

            composable(Screen.EmployeeManagement.route) {
                if (currentUser.role == UserRole.ADMIN) {
                    EmployeeManagementScreen(viewModel = employeeViewModel)
                }
            }

            composable(Screen.ReportManagement.route) {
                if (currentUser.role == UserRole.ADMIN) {
                    ReportManagementScreen(viewModel = reportViewModel)
                }
            }
        }
    }
}
