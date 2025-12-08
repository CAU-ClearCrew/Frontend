package com.example.mobileappdevelopment.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileappdevelopment.veiwmodel.ZkViewModel

@Composable
fun ZkSettingsScreen(viewModel: ZkViewModel = viewModel()) {
    var customNullifier by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    val registrationStatus by viewModel.registrationStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("익명 ID 설정", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = customNullifier,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
                    customNullifier = newValue
                }
            },
            label = { Text("익명 ID (Custom Nullifier)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("16진수 값을 입력하세요") }
        )

        OutlinedTextField(
            value = secret,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
                    secret = newValue
                }
            },
            label = { Text("비밀 값 (Secret)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("16진수 값을 입력하세요") }
        )

        Button(
            onClick = { viewModel.registerZkKeys(customNullifier, secret) },
            enabled = customNullifier.isNotBlank() && secret.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("등록하기")
        }

        registrationStatus?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}
