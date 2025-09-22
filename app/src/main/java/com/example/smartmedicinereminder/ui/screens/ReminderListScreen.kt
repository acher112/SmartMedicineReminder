package com.example.smartmedicinereminder.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.smartmedicinereminder.R
import com.example.smartmedicinereminder.data.model.Dose
import com.example.smartmedicinereminder.data.model.ReminderWithDoses
import com.example.smartmedicinereminder.viewmodel.FontSizeViewModel
import com.example.smartmedicinereminder.viewmodel.FontSizeViewModelFactory

@Composable
fun ReminderListScreen(
    reminders: List<ReminderWithDoses>,
    onDeleteDose: (Dose) -> Unit,
    onDeleteReminder: (ReminderWithDoses) -> Unit,
    onEditReminder: (ReminderWithDoses) -> Unit
) {
    val context = LocalContext.current
    val fontSizeViewModel: FontSizeViewModel =
        viewModel(factory = FontSizeViewModelFactory(context))
    val fontLevel by fontSizeViewModel.fontSize.collectAsState()
    val fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp

    Column(modifier = Modifier.fillMaxSize()) {

        // 🔽 Dropdown at top
        FontSizeDropdown(fontSizeViewModel = fontSizeViewModel)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(reminders) { r ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // ✅ Reminder name
                        Text(
                            text = r.reminder.name,
                            fontSize = fontSize,
                            style = MaterialTheme.typography.titleLarge
                        )

                        // ✅ Category
                        Text(
                            text = stringResource(R.string.type_label, r.reminder.category),
                            fontSize = fontSize,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // ✅ Duration
                        if (r.reminder.startDate.isNotEmpty() && r.reminder.endDate.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.duration_label,
                                    r.reminder.startDate,
                                    r.reminder.endDate
                                ),
                                fontSize = fontSize,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // ✅ Front Image
                        r.reminder.frontImageUri?.takeIf { it.isNotEmpty() }?.let { uri ->
                            Text(
                                text = stringResource(R.string.front_side_label),
                                fontSize = fontSize,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = stringResource(R.string.front_side_label),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(vertical = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // ✅ Back Image
                        r.reminder.backImageUri?.takeIf { it.isNotEmpty() }?.let { uri ->
                            Text(
                                text = stringResource(R.string.back_side_label),
                                fontSize = fontSize,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = stringResource(R.string.back_side_label),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(vertical = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // ✅ Syrup Image
                        r.reminder.syrupImageUri?.takeIf { it.isNotEmpty() }?.let { uri ->
                            Text(
                                text = stringResource(R.string.syrup_bottle_label),
                                fontSize = fontSize,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = stringResource(R.string.syrup_bottle_label),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(vertical = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // ✅ Doses list
                        if (r.doses.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.doses_label),
                                fontSize = fontSize,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))

                            r.doses.forEach { dose ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // 🔑 Show dose depending on category
                                    val doseText = when (r.reminder.category) {
                                        "Tablet" -> stringResource(
                                            R.string.tablet_dose_format,
                                            dose.quantity
                                        )
                                        "Syrup" -> stringResource(
                                            R.string.syrup_dose_format,
                                            dose.quantity
                                        )
                                        else -> stringResource(
                                            R.string.default_dose_format,
                                            dose.quantity
                                        )
                                    }

                                    Text(
                                        text = stringResource(
                                            R.string.dose_item_format,
                                            dose.time,
                                            doseText
                                        ),
                                        fontSize = fontSize
                                    )

                                    IconButton(onClick = { onDeleteDose(dose) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete_dose)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.no_doses_scheduled),
                                fontSize = fontSize,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // 🔘 Action buttons (Edit + Delete reminder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onEditReminder(r) },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                                Spacer(Modifier.width(4.dp))
                                Text("Edit")
                            }

                            OutlinedButton(
                                onClick = { onDeleteReminder(r) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                                Spacer(Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FontSizeDropdown(fontSizeViewModel: FontSizeViewModel) {
    val fontLevel by fontSizeViewModel.fontSize.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(8.dp)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                text = when (fontLevel) {
                    1 -> "Small"
                    2 -> "Medium"
                    3 -> "Large"
                    else -> "Small"
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Small") },
                onClick = {
                    fontSizeViewModel.saveFontSize(1)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Medium") },
                onClick = {
                    fontSizeViewModel.saveFontSize(2)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Large") },
                onClick = {
                    fontSizeViewModel.saveFontSize(3)
                    expanded = false
                }
            )
        }
    }
}
