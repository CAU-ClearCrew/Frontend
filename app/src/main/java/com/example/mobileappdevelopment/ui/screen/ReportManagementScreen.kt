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
import androidx.compose.ui.unit.dp
import com.example.mobileappdevelopment.data.Report
import com.example.mobileappdevelopment.data.ReportPriority
import com.example.mobileappdevelopment.data.ReportStatus
import com.example.mobileappdevelopment.veiwmodel.ReportViewModel

@Composable
fun ReportManagementScreen(
    viewModel: ReportViewModel
) {
    val reports by viewModel.reports.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    var selectedReport by remember { mutableStateOf<Report?>(null) }

    val filteredReports = remember(reports, filterStatus) {
        if (filterStatus == null) {
            reports
        } else {
            reports.filter { it.status == filterStatus }
        }
    }

    val reportsByStatus = remember(reports) {
        mapOf(
            null to reports.size,
            ReportStatus.PENDING to reports.count { it.status == ReportStatus.PENDING },
            ReportStatus.INVESTIGATING to reports.count { it.status == ReportStatus.INVESTIGATING },
            ReportStatus.RESOLVED to reports.count { it.status == ReportStatus.RESOLVED },
            ReportStatus.CLOSED to reports.count { it.status == ReportStatus.CLOSED }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "신고 현황",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "접수된 모든 익명 신고를 관리합니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusCard(
                        label = "전체",
                        count = reportsByStatus[null] ?: 0,
                        onClick = { viewModel.setFilterStatus(null) },
                        modifier = Modifier.weight(1f)
                    )
                    StatusCard(
                        label = "접수",
                        count = reportsByStatus[ReportStatus.PENDING] ?: 0,
                        onClick = { viewModel.setFilterStatus(ReportStatus.PENDING) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusCard(
                        label = "조사중",
                        count = reportsByStatus[ReportStatus.INVESTIGATING] ?: 0,
                        onClick = { viewModel.setFilterStatus(ReportStatus.INVESTIGATING) },
                        modifier = Modifier.weight(1f)
                    )
                    StatusCard(
                        label = "해결",
                        count = reportsByStatus[ReportStatus.RESOLVED] ?: 0,
                        onClick = { viewModel.setFilterStatus(ReportStatus.RESOLVED) },
                        modifier = Modifier.weight(1f)
                    )
                    StatusCard(
                        label = "종료",
                        count = reportsByStatus[ReportStatus.CLOSED] ?: 0,
                        onClick = { viewModel.setFilterStatus(ReportStatus.CLOSED) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredReports.forEach { report ->
                        ReportCard(
                            report = report,
                            onClick = { selectedReport = report }
                        )
                    }

                    if (filteredReports.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "해당하는 신고가 없습니다",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    selectedReport?.let { report ->
        ReportDetailDialog(
            report = report,
            onDismiss = { selectedReport = null },
            onUpdateStatus = { status -> viewModel.updateReportStatus(report.id, status) },
            onUpdatePriority = { priority -> viewModel.updateReportPriority(report.id, priority) },
            onUpdateNotes = { notes -> viewModel.updateReportNotes(report.id, notes) }
        )
    }
}

@Composable
fun StatusCard(
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun ReportCard(
    report: Report,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(report.status.label, style = MaterialTheme.typography.labelSmall) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(report.priority.label, style = MaterialTheme.typography.labelSmall) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(report.category.label, style = MaterialTheme.typography.labelSmall) }
                )
            }

            Text(
                text = report.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (report.department.isNotBlank()) {
                    Text(
                        text = report.department,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    Icons.Default.DateRange, //CalanderToday인가에서 바꿈.
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = report.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailDialog(
    report: Report,
    onDismiss: () -> Unit,
    onUpdateStatus: (ReportStatus) -> Unit,
    onUpdatePriority: (ReportPriority) -> Unit,
    onUpdateNotes: (String) -> Unit
) {
    var notes by remember { mutableStateOf(report.notes) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedPriority by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("신고 상세 정보") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "신고 ID: ${report.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("제목", style = MaterialTheme.typography.labelMedium)
                    Text(report.title, style = MaterialTheme.typography.bodyMedium)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("신고 유형", style = MaterialTheme.typography.labelMedium)
                    AssistChip(
                        onClick = {},
                        label = { Text(report.category.label) }
                    )
                }

                if (report.department.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("관련 부서", style = MaterialTheme.typography.labelMedium)
                        Text(report.department, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (report.date.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("발생 일시", style = MaterialTheme.typography.labelMedium)
                        Text(report.date, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("상세 내용", style = MaterialTheme.typography.labelMedium)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = report.description,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("처리 상태", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = expandedStatus,
                            onExpandedChange = { expandedStatus = it }
                        ) {
                            OutlinedTextField(
                                value = report.status.label,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedStatus,
                                onDismissRequest = { expandedStatus = false }
                            ) {
                                ReportStatus.values().forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.label) },
                                        onClick = {
                                            onUpdateStatus(status)
                                            expandedStatus = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("우선순위", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = expandedPriority,
                            onExpandedChange = { expandedPriority = it }
                        ) {
                            OutlinedTextField(
                                value = report.priority.label,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedPriority,
                                onDismissRequest = { expandedPriority = false }
                            ) {
                                ReportPriority.values().forEach { priority ->
                                    DropdownMenuItem(
                                        text = { Text(priority.label) },
                                        onClick = {
                                            onUpdatePriority(priority)
                                            expandedPriority = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("처리 메모 (내부용)", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("조사 진행 상황이나 처리 내역을 기록하세요") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 5
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column {
                            Text(
                                text = "기밀 유지 안내",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "신고자의 익명성은 절대적으로 보호되어야 합니다. 조사 과정에서 신고자를 특정할 수 있는 정보가 유출되지 않도록 주의하세요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateNotes(notes)
                    onDismiss()
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}
