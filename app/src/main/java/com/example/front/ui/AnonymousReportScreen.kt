package com.example.front.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class User(
    val email: String,
    val name: String,
    val role: String,
    val department: String? = null
)

data class ReportFormData(
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val department: String = "",
    val date: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnonymousReportScreen(currentUser: com.example.front.data.User) {
    var submitted by remember { mutableStateOf(false) }
    var formData by remember { mutableStateOf(ReportFormData()) }
    var expandedCategory by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val categories = mapOf(
        "harassment" to "괴롭힘/따돌림",
        "discrimination" to "차별",
        "corruption" to "부정/비리",
        "safety" to "안전 위반",
        "ethics" to "윤리 위반",
        "other" to "기타"
    )

    fun handleSubmit() {
        println("Report submitted: $formData")
        submitted = true
        scope.launch {
            delay(3000)
            submitted = false
            formData = ReportFormData()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFDCEFFE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "익명 고발 시스템",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "부당한 대우나 부정행위를 안전하게 신고할 수 있습니다",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Alert
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEAB308),
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "완전한 익명성 보장",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF713F12)
                    )
                    Text(
                        text = "모든 신고는 완전히 익명으로 처리되며, 신고자의 신원은 절대 공개되지 않습니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF854D0E)
                    )
                }
            }
        }

        // Success Message or Form
        if (submitted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "신고가 접수되었습니다",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF14532D)
                    )
                    Text(
                        text = "신고 내용은 관리자가 검토할 예정입니다. 협조해 주셔서 감사합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF15803D)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Card Header
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "신고서 작성",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "가능한 자세히 작성해 주시면 더 신속한 처리가 가능합니다",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Category Select
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "신고 유형",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        ExposedDropdownMenuBox(
                            expanded = expandedCategory,
                            onExpandedChange = { expandedCategory = it }
                        ) {
                            OutlinedTextField(
                                value = categories[formData.category] ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("신고 유형을 선택하세요") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCategory,
                                onDismissRequest = { expandedCategory = false }
                            ) {
                                categories.forEach { (key, value) ->
                                    DropdownMenuItem(
                                        text = { Text(value) },
                                        onClick = {
                                            formData = formData.copy(category = key)
                                            expandedCategory = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Department Input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "관련 부서 (선택사항)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = formData.department,
                            onValueChange = { formData = formData.copy(department = it) },
                            placeholder = { Text("예: 마케팅팀") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Date Input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "발생 일시 (선택사항)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = formData.date,
                            onValueChange = { formData = formData.copy(date = it) },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Title Input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "제목",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = formData.title,
                            onValueChange = { formData = formData.copy(title = it) },
                            placeholder = { Text("간단한 제목을 입력하세요") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Description Input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "상세 내용",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = formData.description,
                            onValueChange = { formData = formData.copy(description = it) },
                            placeholder = { Text("발생한 상황을 자세히 설명해 주세요. 증거나 목격자 정보가 있다면 함께 기재해 주세요.") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 10
                        )
                    }

                    // Privacy Notice
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "개인정보 보호 안내",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "• 신고서에는 신고자를 특정할 수 있는 정보를 포함하지 마세요",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "• 모든 신고는 암호화되어 저장됩니다",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "• 조사는 독립적인 윤리위원회에서 진행합니다",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = { handleSubmit() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = formData.category.isNotEmpty() &&
                                formData.title.isNotEmpty() &&
                                formData.description.isNotEmpty()
                    ) {
                        Text("익명으로 신고하기")
                    }
                }
            }
        }
    }
}





/*
@Preview(showBackground = true, name = "Default Form View")
@Composable
fun AnonymousReportScreenPreview() {
    MaterialTheme {
        AnonymousReportScreen(
            currentUser = User(
                email = "test@example.com",
                name = "홍길동",
                role = "user",
                department = "개발팀"
            )
        )
    }
}
*/

@Preview(showBackground = true, name = "Submitted View")
@Composable
fun AnonymousReportScreenSubmittedPreview() {
    MaterialTheme {
        // 'submitted' 상태를 직접 제어하기 위해 상태를 여기서 선언합니다.
        var submitted by remember { mutableStateOf(true) }
        var formData by remember { mutableStateOf(ReportFormData()) }
        val scope = rememberCoroutineScope()

        // 실제 화면의 로직을 재사용하여 제출된 화면을 렌더링합니다.
        // submitted 상태를 true로 설정하여 제출 완료 UI를 표시합니다.
        if (submitted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "신고가 접수되었습니다",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF14532D)
                    )
                    Text(
                        text = "신고 내용은 관리자가 검토할 예정입니다. 협조해 주셔서 감사합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF15803D),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        // submitted 상태가 false일 때는 아무것도 그리지 않아,
        // 제출된 화면만 미리보기에 나타나도록 합니다.
    }
}

