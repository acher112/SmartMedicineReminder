package com.example.smartmedicinereminder.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartmedicinereminder.viewmodel.ReminderViewModel
import com.example.smartmedicinereminder.data.model.Option
import com.example.smartmedicinereminder.R
import com.example.smartmedicinereminder.data.model.ReminderWithDoses
import com.example.smartmedicinereminder.viewmodel.FontSizeViewModel
import com.example.smartmedicinereminder.viewmodel.FontSizeViewModelFactory
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: ReminderViewModel,
    navController: NavController,
    initialReminder: ReminderWithDoses? = null // pass this when editing
) {
    val isEditMode = initialReminder != null
    val context = LocalContext.current

    // Font size ViewModel
    val fontSizeViewModel: FontSizeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = FontSizeViewModelFactory(context)
    )
    val fontLevel by fontSizeViewModel.fontSize.collectAsState()

    var name by remember { mutableStateOf(initialReminder?.reminder?.name ?: "") }
    var startDate by remember { mutableStateOf(initialReminder?.reminder?.startDate ?: "") }
    var endDate by remember { mutableStateOf(initialReminder?.reminder?.endDate ?: "") }

    var fontDropdownExpanded by remember { mutableStateOf(false) }
    val fontOptions = listOf("Small", "Medium", "Large")

    // ✅ Medicine type
    val medicineOptions = listOf(
        Option("Tablet", R.string.tablet),
        Option("Syrup", R.string.syrup)
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember {
        mutableStateOf(
            medicineOptions.find { it.key == initialReminder?.reminder?.category }
                ?: medicineOptions[0]
        )
    }

    // ✅ Tablet images
    var frontImageUri by remember {
        mutableStateOf<Uri?>(initialReminder?.reminder?.frontImageUri?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) })
    }
    var backImageUri by remember {
        mutableStateOf<Uri?>(initialReminder?.reminder?.backImageUri?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) })
    }

    // ✅ Syrup image
    var syrupImageUri by remember {
        mutableStateOf<Uri?>(initialReminder?.reminder?.syrupImageUri?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) })
    }

    // ✅ Separate doses
    val tabletDoses = remember { mutableStateListOf<Pair<String, Int>>() }
    val syrupDoses = remember { mutableStateListOf<Pair<String, Int>>() }

    // Pre-fill doses if editing
    LaunchedEffect(initialReminder) {
        tabletDoses.clear()
        syrupDoses.clear()
        initialReminder?.doses?.forEach { dose ->
            if (selectedOption.key == "Tablet") {
                tabletDoses.add(dose.time to dose.quantity)
            } else {
                syrupDoses.add(dose.time to dose.quantity)
            }
        }
    }

    val calendar = Calendar.getInstance()
    val startDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> startDate = "$day/${month + 1}/$year" },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    val endDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> endDate = "$day/${month + 1}/$year" },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // ✅ launchers
    val frontImagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            frontImageUri = uri
        }
    val backImagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            backImageUri = uri
        }
    val syrupImagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            syrupImageUri = uri
        }

    // ✅ scroll state
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // -------------------
        // Font Size Dropdown
        // -------------------
        Box(modifier = Modifier.fillMaxWidth(0.9f)) {
            OutlinedButton(onClick = { fontDropdownExpanded = true }) {
                Text(
                    when (fontLevel) {
                        1 -> "Small"
                        2 -> "Medium"
                        3 -> "Large"
                        else -> "Small"
                    },
                    fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp
                )
            }
            DropdownMenu(
                expanded = fontDropdownExpanded,
                onDismissRequest = { fontDropdownExpanded = false }
            ) {
                fontOptions.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            fontSizeViewModel.saveFontSize(index + 1)
                            fontDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // -------------------
        // Medicine Type
        // -------------------
        Text(
            text = stringResource(R.string.choose_type),
            style = TextStyle(fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 6.dp)
        )

        Box(modifier = Modifier.fillMaxWidth(0.9f)) {
            Button(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    stringResource(id = selectedOption.label),
                    fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                medicineOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(id = option.label),
                                fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp
                            )
                        },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // -------------------
        // Medicine Name
        // -------------------
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text(
                    stringResource(R.string.medicine_name),
                    fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp
                )
            },
            modifier = Modifier.fillMaxWidth(0.9f),
            textStyle = TextStyle(fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp)
        )

        Spacer(Modifier.height(16.dp))

        // Start Date
        Button(
            onClick = { startDatePickerDialog.show() },
            modifier = Modifier.fillMaxWidth(0.9f).height(48.dp)
        ) {
            Text(
                startDate.ifEmpty { stringResource(R.string.select_start_date) },
                fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // End Date
        Button(
            onClick = { endDatePickerDialog.show() },
            modifier = Modifier.fillMaxWidth(0.9f).height(48.dp)
        ) {
            Text(
                endDate.ifEmpty { stringResource(R.string.select_end_date) },
                fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // Add dose time
        Button(
            onClick = {
                val now = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hour: Int, minute: Int ->
                        val time = String.format("%02d:%02d", hour, minute)
                        if (selectedOption.key == "Tablet") {
                            tabletDoses.add(time to 1)
                        } else if (selectedOption.key == "Syrup") {
                            syrupDoses.add(time to 1)
                        }
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
                ).show()
            },
            modifier = Modifier.fillMaxWidth(0.9f).height(48.dp)
        ) {
            Text(
                stringResource(R.string.add_dose_time),
                fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // Show doses
        val dosesToShow = if (selectedOption.key == "Tablet") tabletDoses else syrupDoses
        if (dosesToShow.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(0.9f).height(180.dp)
            ) {
                itemsIndexed(dosesToShow) { index, (time, qty) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "• $time",
                            style = TextStyle(fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp)
                        )

                        var expandedQty by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expandedQty = true }) {
                                Text(
                                    if (selectedOption.key == "Tablet") "Qty: $qty"
                                    else "Spoon(s): $qty",
                                    fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp
                                )
                            }
                            DropdownMenu(
                                expanded = expandedQty,
                                onDismissRequest = { expandedQty = false }
                            ) {
                                if (selectedOption.key == "Tablet") {
                                    (1..5).forEach { q ->
                                        DropdownMenuItem(
                                            text = { Text("$q") },
                                            onClick = {
                                                tabletDoses[index] = time to q
                                                expandedQty = false
                                            }
                                        )
                                    }
                                } else {
                                    (1..5).forEach { spoon ->
                                        DropdownMenuItem(
                                            text = { Text("$spoon spoon(s)") },
                                            onClick = {
                                                syrupDoses[index] = time to spoon
                                                expandedQty = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                if (selectedOption.key == "Tablet") tabletDoses.removeAt(index)
                                else syrupDoses.removeAt(index)
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Time")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tablet or Syrup image upload
        if (selectedOption.key == "Tablet") {
            Button(
                onClick = { frontImagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(0.9f).height(48.dp)
            ) { Text(stringResource(R.string.upload_front_image), fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp) }

            frontImageUri?.let {
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Front Image",
                    modifier = Modifier.fillMaxWidth(0.9f).height(160.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { backImagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(0.9f).height(48.dp)
            ) { Text(stringResource(R.string.upload_back_image), fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp) }

            backImageUri?.let {
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Back Image",
                    modifier = Modifier.fillMaxWidth(0.9f).height(160.dp)
                )
            }
        } else if (selectedOption.key == "Syrup") {
            Button(
                onClick = { syrupImagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(0.9f).height(48.dp)
            ) { Text(stringResource(R.string.upload_syrup_image), fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp) }

            syrupImageUri?.let {
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Syrup Image",
                    modifier = Modifier.fillMaxWidth(0.9f).height(160.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Save or Update Reminder
        Button(
            onClick = {
                val finalDoses =
                    if (selectedOption.key == "Tablet") tabletDoses.toList()
                    else syrupDoses.toList()

                if (name.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty() && finalDoses.isNotEmpty()) {

                    // ✅ Save images to internal storage before inserting
                    val savedFront = if (selectedOption.key == "Tablet" && frontImageUri != null) {
                        com.example.smartmedicinereminder.utils.ImageUtils.saveImageToInternalStorage(context, frontImageUri!!)
                    } else ""

                    val savedBack = if (selectedOption.key == "Tablet" && backImageUri != null) {
                        com.example.smartmedicinereminder.utils.ImageUtils.saveImageToInternalStorage(context, backImageUri!!)
                    } else ""

                    val savedSyrup = if (selectedOption.key == "Syrup" && syrupImageUri != null) {
                        com.example.smartmedicinereminder.utils.ImageUtils.saveImageToInternalStorage(context, syrupImageUri!!)
                    } else ""

                    // ✅ Insert reminder (schedules alarms automatically)
                    viewModel.insertReminder(
                        context = context,
                        name = name,
                        startDate = startDate,
                        endDate = endDate,
                        category = selectedOption.key,
                        frontImageUri = savedFront ?: "",
                        backImageUri = savedBack ?: "",
                        syrupImageUri = savedSyrup ?: "",
                        teaspoonCount = if (selectedOption.key == "Syrup") 1 else 0,
                        doses = finalDoses
                    )

                    // 🚫 Removed manual AlarmReceiver.setAlarm() call

                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(0.9f).height(48.dp)
        ) {
            Text(
                if (isEditMode) stringResource(R.string.update_reminder)
                else stringResource(R.string.save_reminder),
                fontSize = fontSizeViewModel.getFontSizeSp(fontLevel).sp
            )
        }

    }
}
