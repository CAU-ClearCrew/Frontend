package com.example.mobileappdevelopment.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mobileappdevelopment.data.Employee
import com.example.mobileappdevelopment.data.EmployeeStatus
import com.example.mobileappdevelopment.veiwmodel.EmployeeViewModel

@Composable
fun EmployeeManagementScreen(
    viewModel: EmployeeViewModel
) {
    val employees by viewModel.employees.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }

    val filteredEmployees = remember(employees, searchQuery) {
        if (searchQuery.isBlank()) {
            employees
        } else {
            employees.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true) ||
                        it.department.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "사원 관리",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )/*
                        Text(
                            text = "회사 사원 정보를 관리합니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )*/
                    }

                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("사원 추가")
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("이름, 이메일, 부서로 검색...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredEmployees.forEach { employee ->
                        EmployeeCard(
                            employee = employee,
                            onEdit = { editingEmployee = employee },
                            onDelete = { viewModel.deleteEmployee(employee.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        EmployeeDialog(
            employee = null,
            onDismiss = { showAddDialog = false },
            onSave = { employee ->
                viewModel.addEmployee(employee)
                showAddDialog = false
            }
        )
    }

    editingEmployee?.let { employee ->
        EmployeeDialog(
            employee = employee,
            onDismiss = { editingEmployee = null },
            onSave = { updatedEmployee ->
                viewModel.updateEmployee(updatedEmployee)
                editingEmployee = null
            }
        )
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = employee.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = if (employee.status == EmployeeStatus.ACTIVE) "재직중" else "퇴사",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
                Text(
                    text = employee.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${employee.department} • ${employee.position} • 입사일: ${employee.joinDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "수정")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("사원 삭제") },
            text = { Text("정말로 이 사원을 삭제하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDialog(
    employee: Employee?,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var email by remember { mutableStateOf(employee?.email ?: "") }
    var department by remember { mutableStateOf(employee?.department ?: "") }
    var position by remember { mutableStateOf(employee?.position ?: "") }
    var joinDate by remember { mutableStateOf(employee?.joinDate ?: "") }
    var status by remember { mutableStateOf(employee?.status ?: EmployeeStatus.ACTIVE) }
    var expandedStatus by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employee == null) "새 사원 추가" else "사원 정보 수정") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("이름") },
                    placeholder = { Text("홍길동") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("이메일") },
                    placeholder = { Text("hong@company.com") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("부서") },
                    placeholder = { Text("개발팀") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("직급") },
                    placeholder = { Text("주니어 개발자") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = joinDate,
                    onValueChange = { joinDate = it },
                    label = { Text("입사일") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = it }
                ) {
                    OutlinedTextField(
                        value = if (status == EmployeeStatus.ACTIVE) "재직중" else "퇴사",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("상태") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("재직중") },
                            onClick = {
                                status = EmployeeStatus.ACTIVE
                                expandedStatus = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("퇴사") },
                            onClick = {
                                status = EmployeeStatus.INACTIVE
                                expandedStatus = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newEmployee = Employee(
                        id = employee?.id ?: System.currentTimeMillis().toString(),
                        name = name,
                        email = email,
                        department = department,
                        position = position,
                        joinDate = joinDate,
                        status = status
                    )
                    onSave(newEmployee)
                },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text(if (employee == null) "추가" else "저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소.")
            }
        }
    )
}
