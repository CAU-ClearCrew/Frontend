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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobileappdevelopment.data.ReportCategory
import com.example.mobileappdevelopment.data.User
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnonymousReportScreen(
    currentUser: User,
    onSubmit: (ReportCategory, String, String, String, String) -> Unit
) {
    var submitted by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf<ReportCategory?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }

    LaunchedEffect(submitted) {
        if (submitted) {
            delay(3000)
            submitted = false
            category = null
            title = ""
            description = ""
            department = ""
            date = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Anonymous Report",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "부당한 대우나 부정행위를 안전하게 신고할 수 있습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Column {
                    Text(
                        text = "완전한 익명성 보장",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "모든 신고는 완전히 익명으로 처리되며, 신고자의 신원은 절대 공개되지 않습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        if (submitted) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "신고가 접수되었습니다",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "신고 내용은 관리자가 검토할 예정입니다. 협조해 주셔서 감사합니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = "신고서 작성",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "가능한 자세히 작성해 주시면 더 신속한 처리가 가능합니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = it }
                    ) {
                        OutlinedTextField(
                            value = category?.label ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("신고 유형") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            ReportCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.label) },
                                    onClick = {
                                        category = cat
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("관련 부서 (선택사항)") },
                        placeholder = { Text("예: 마케팅팀") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("발생 일시 (선택사항)") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("제목") },
                        placeholder = { Text("간단한 제목을 입력하세요") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("상세 내용") },
                        placeholder = { Text("발생한 상황을 자세히 설명해 주세요. 증거나 목격자 정보가 있다면 함께 기재해 주세요.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "개인정보 보호 안내",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("• 신고서에는 신고자를 특정할 수 있는 정보를 포함하지 마세요", style = MaterialTheme.typography.bodySmall)
                            Text("• 모든 신고는 암호화되어 저장됩니다", style = MaterialTheme.typography.bodySmall)
                            Text("• 조사는 독립적인 윤리위원회에서 진행합니다", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Button(
                        onClick = {
                            category?.let { cat ->
                                onSubmit(cat, title, description, department, date)
                                submitted = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = category != null && title.isNotBlank() && description.isNotBlank()
                    ) {
                        Text("익명으로 신고하기")
                    }
                }
            }
        }
    }
}
