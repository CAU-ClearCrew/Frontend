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
    val isRegistering by viewModel.isRegistering.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Anonymous ID Settings", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = customNullifier,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
                    customNullifier = newValue
                }
            },
            label = { Text("Anonymous ID (Custom Nullifier)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter a hexadecimal value") }
        )

        OutlinedTextField(
            value = secret,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
                    secret = newValue
                }
            },
            label = { Text("Secret Value (Secret)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter a hexadecimal value") }
        )

        Button(
            onClick = { viewModel.registerZkKeys(customNullifier, secret) },
            enabled = customNullifier.isNotBlank() && secret.isNotBlank() && !isRegistering,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegistering) "Registering..." else "Register")
        }

        registrationStatus?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}
