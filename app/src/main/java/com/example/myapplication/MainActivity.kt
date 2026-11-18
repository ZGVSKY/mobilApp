package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Entry point. Only hosts the theme/locale state and requests the
 * notification permission; all screens live in [MainApp].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        setContent {
            var isDarkTheme by remember { mutableStateOf(loadSavedTheme(this@MainActivity)) }
            var currentLocale by remember { mutableStateOf(loadSavedLocale(this@MainActivity)) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainApp(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { newTheme ->
                        isDarkTheme = newTheme
                        saveTheme(this@MainActivity, newTheme)
                    },
                    currentLocale = currentLocale,
                    onLocaleChange = { newLocale ->
                        saveLocale(this@MainActivity, newLocale)
                        currentLocale = newLocale
                        setAppLocale(this@MainActivity, newLocale)
                    }
                )
            }
        }
    }
}
