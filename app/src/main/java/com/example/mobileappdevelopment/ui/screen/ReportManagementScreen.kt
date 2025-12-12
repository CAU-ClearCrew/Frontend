package com.example.mobileappdevelopment.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
        Text(
            text = "신고 현황",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier
            .padding(4.dp)
        )
        Card {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

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
                    text = report.title ?: "제목 없음",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 수정 후
                AssistChip(
                    onClick = {},
                    label = { Text(report.status?.label ?: "상태 미지정", style = MaterialTheme.typography.labelSmall) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(report.priority?.label ?: "우선순위 미지정", style = MaterialTheme.typography.labelSmall) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(report.category?.label ?: "유형 미지정", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Text(
                text = report.description ?: "내용 없음",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically // 아이콘과 텍스트 정렬을 위해 추가하면 좋습니다.
            ) {
                // 1. report.department가 null이 아니고 비어있지도 않을 때만 Text를 표시합니다.
                if (!report.department.isNullOrBlank()) {
                    Text(
                        text = report.department,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(" • ", color = MaterialTheme.colorScheme.onSurfaceVariant) // 양쪽에 공백을 추가하면 더 보기 좋습니다.
                }
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Date",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 2. report.date가 null일 경우 빈 문자열("")을 표시하도록 합니다.
                Text(
                    text = report.date ?: "",
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
    // 1. report.notes가 null일 경우를 대비해 빈 문자열로 초기화
    var notes by remember { mutableStateOf(report.notes ?: "") }
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
                    // 2. report.title이 null일 경우를 대비
                    Text(report.title ?: "제목 없음", style = MaterialTheme.typography.bodyMedium)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("신고 유형", style = MaterialTheme.typography.labelMedium)
                    AssistChip(
                        onClick = {},
                        // 3. report.category가 null일 경우를 대비
                        label = { Text(report.category?.label ?: "유형 미지정") }
                    )
                }

                // 4. isNullOrBlank()를 사용하여 department가 null이어도 안전하게 처리
                if (!report.department.isNullOrBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("관련 부서", style = MaterialTheme.typography.labelMedium)
                        Text(report.department, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // 5. isNullOrBlank()를 사용하여 date가 null이어도 안전하게 처리
                if (!report.date.isNullOrBlank()) {
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
                            // 6. report.description이 null일 경우를 대비
                            text = report.description ?: "상세 내용 없음",
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
                                // 7. report.status가 null일 경우를 대비
                                value = report.status?.label ?: "상태 미지정",
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
                                // 8. report.priority가 null일 경우를 대비
                                value = report.priority?.label ?: "우선순위 미지정",
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("관리자 메모") },
                    modifier = Modifier.fillMaxWidth()
                )
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
                Text("취소")
            }
        }
    )
}
