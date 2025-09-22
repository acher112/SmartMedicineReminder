package com.example.smartmedicinereminder

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartmedicinereminder.ui.theme.SmartMedicineReminderTheme
import com.example.smartmedicinereminder.data.db.ReminderDatabase
import com.example.smartmedicinereminder.data.repository.ReminderRepository
import com.example.smartmedicinereminder.viewmodel.ReminderListViewModel
import com.example.smartmedicinereminder.viewmodel.ReminderListViewModelFactory
import com.example.smartmedicinereminder.viewmodel.ReminderViewModel
import com.example.smartmedicinereminder.viewmodel.ReminderViewModelFactory
import com.example.smartmedicinereminder.ui.screens.ReminderListScreen
import com.example.smartmedicinereminder.ui.screens.AddReminderScreen
import com.example.smartmedicinereminder.ui.screens.LanguageScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.smartmedicinereminder.data.model.ReminderWithDoses
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Ask for Exact Alarm permission on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(AlarmManager::class.java)
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }


        // ✅ Load saved language before UI
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("LANG", "en") ?: "en"
        setLocale(this, lang)

        val database = ReminderDatabase.getInstance(applicationContext)
        val dao = database.reminderDao()
        val repository = ReminderRepository(dao)

        val listFactory = ReminderListViewModelFactory(repository)
        val reminderListViewModel =
            ViewModelProvider(this, listFactory)[ReminderListViewModel::class.java]

        val addFactory = ReminderViewModelFactory(repository)
        val reminderViewModel =
            ViewModelProvider(this, addFactory)[ReminderViewModel::class.java]

        setContent {
            SmartMedicineReminderTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "list") {

                    // ✅ Reminder List Screen
                    composable("list") {
                        val remindersWithDoses by reminderListViewModel
                            .reminders
                            .collectAsState(initial = emptyList())

                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(stringResource(R.string.app_name)) },
                                    actions = {
                                        // 🌐 Language change button
                                        IconButton(onClick = { navController.navigate("language") }) {
                                            Icon(
                                                Icons.Default.Translate,
                                                contentDescription = "Change Language"
                                            )
                                        }
                                    }
                                )
                            },
                            floatingActionButton = {
                                FloatingActionButton(onClick = { navController.navigate("add") }) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Reminder"
                                    )
                                }
                            }
                        ) { padding ->
                            Box(Modifier.padding(padding)) {
                                ReminderListScreen(
                                    reminders = remindersWithDoses,
                                    onDeleteDose = { dose ->
                                        reminderListViewModel.deleteDose(dose)
                                    },
                                    onDeleteReminder = { reminder ->
                                        // call ReminderViewModel to delete full reminder
                                        reminderViewModel.deleteReminder(reminder.reminder)
                                    },
                                    onEditReminder = { reminder ->
                                        // navigate to edit route with id
                                        navController.navigate("edit/${reminder.reminder.id}")
                                    }
                                )
                            }
                        }
                    }

                    // ✅ Add Reminder Screen
                    composable("add") {
                        AddReminderScreen(
                            viewModel = reminderViewModel,
                            navController = navController
                        )
                    }

                    // ✅ Edit Reminder Screen (fetch the ReminderWithDoses by id as Flow)
                    composable("edit/{reminderId}") { backStackEntry: NavBackStackEntry ->
                        val reminderId =
                            backStackEntry.arguments?.getString("reminderId")?.toIntOrNull()
                        if (reminderId != null) {
                            val reminderFlow = reminderViewModel.getReminderWithDosesById(reminderId)
                            val reminderToEdit by reminderFlow.collectAsState(initial = null as ReminderWithDoses?)
                            reminderToEdit?.let { rem ->
                                AddReminderScreen(
                                    viewModel = reminderViewModel,
                                    navController = navController,
                                    initialReminder = rem
                                )
                            } ?: Box(Modifier.fillMaxSize()) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // ✅ Language Screen
                    composable("language") {
                        LanguageScreen(
                            context = this@MainActivity,
                            onLanguageSelected = { langCode ->
                                setLocale(this@MainActivity, langCode)
                                saveLanguage(this@MainActivity, langCode)
                                recreate() // 🔄 reload UI
                            }
                        )
                    }
                }
            }
        }
    }
}

// ✅ Save language with English digits enforced
fun saveLanguage(context: Context, lang: String) {
    val locale = Locale.Builder()
        .setLanguage(lang)
        .setRegion("PK") // default region
        .setUnicodeLocaleKeyword("nu", "latn") // enforce English numbers
        .build()

    context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        .edit()
        .putString("LANG", locale.toLanguageTag()) // e.g. "sd-PK-u-nu-latn"
        .apply()
}

// ✅ Apply locale (Sindhi/Urdu text + English digits for calendar/time picker)
fun setLocale(context: Context, lang: String) {
    val locale = Locale.forLanguageTag(lang)
    Locale.setDefault(locale)

    val resources = context.resources
    val config = resources.configuration

    config.setLocale(locale)
    config.setLayoutDirection(locale)

    resources.updateConfiguration(config, resources.displayMetrics)
}
