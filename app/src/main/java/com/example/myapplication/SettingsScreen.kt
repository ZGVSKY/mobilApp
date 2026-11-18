package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Settings tab: account (avatar from gallery, username + role, logout),
 * language dropdown and theme dropdown — both persisted per user.
 * Bottom padding keeps everything above the floating navigation bar.
 */
@Composable
fun SettingsTabContent(
    username: String,
    userRole: String,
    profileImageUri: String?,
    onProfileImageChange: (String) -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    currentLocale: String,
    onLocaleChange: (String) -> Unit
) {
    var langExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onProfileImageChange(it.toString()) }
    }

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val languageLabel = when (currentLocale) {
        "uk" -> stringResource(R.string.language_ukrainian)
        "es" -> stringResource(R.string.language_spanish)
        else -> stringResource(R.string.language_english)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedLavaLampBackground(isDark = isDarkTheme)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account section
            LiquidGlassBox(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDarkTheme
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri != null) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = stringResource(R.string.tap_to_change_photo),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = username.take(1).uppercase(),
                                fontSize = 32.sp,
                                color = Color.White
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = username,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = userRole,
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }

                    Button(onClick = onLogout) {
                        Text(stringResource(R.string.logout))
                    }
                }
            }

            // Language section
            LiquidGlassBox(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDarkTheme
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.language),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Button(onClick = { langExpanded = true }) {
                        Text(languageLabel)
                    }
                    DropdownMenu(
                        expanded = langExpanded,
                        onDismissRequest = { langExpanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onLocaleChange("en")
                                langExpanded = false
                            },
                            text = { Text(stringResource(R.string.language_english)) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onLocaleChange("uk")
                                langExpanded = false
                            },
                            text = { Text(stringResource(R.string.language_ukrainian)) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onLocaleChange("es")
                                langExpanded = false
                            },
                            text = { Text(stringResource(R.string.language_spanish)) }
                        )
                    }
                }
            }

            // Theme section
            LiquidGlassBox(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDarkTheme
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.theme),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Button(onClick = { themeExpanded = true }) {
                        Text(
                            stringResource(
                                if (isDarkTheme) R.string.dark_theme else R.string.light_theme
                            )
                        )
                    }
                    DropdownMenu(
                        expanded = themeExpanded,
                        onDismissRequest = { themeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onThemeChange(false)
                                themeExpanded = false
                            },
                            text = { Text(stringResource(R.string.light_theme)) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onThemeChange(true)
                                themeExpanded = false
                            },
                            text = { Text(stringResource(R.string.dark_theme)) }
                        )
                    }
                }
            }
        }
    }
}
