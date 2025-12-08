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
            onValueChange = { customNullifier = it },
            label = { Text("익명 ID (Custom Nullifier)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = secret,
            onValueChange = { secret = it },
            label = { Text("비밀 값 (Secret)") },
            modifier = Modifier.fillMaxWidth()
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
