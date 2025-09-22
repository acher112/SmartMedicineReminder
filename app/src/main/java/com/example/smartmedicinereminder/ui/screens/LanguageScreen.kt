// File: app/src/main/java/com/example/smartmedicinereminder/ui/screens/LanguageScreen.kt
package com.example.smartmedicinereminder.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.smartmedicinereminder.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    context: Context,
    onLanguageSelected: (String) -> Unit // ← IMPORTANT: takes a String
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.select_language)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onLanguageSelected("en") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.english))
            }
            Button(
                onClick = { onLanguageSelected("ur") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.urdu))
            }
            Button(
                onClick = { onLanguageSelected("sd") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sindhi))
            }
        }
    }
}
