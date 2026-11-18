package com.example.myapplication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

/** Login step 1: username only. */
@Composable
fun LoginStep1Screen(
    username: String,
    onUsernameChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegisterClick: () -> Unit,
    onGuestLogin: () -> Unit,
    error: String,
    currentLocale: String,
    onLocaleChange: (String) -> Unit,
    showLanguageMenu: Boolean,
    onShowLanguageMenu: (Boolean) -> Unit,
    isDarkTheme: Boolean
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedLavaLampBackground(isDark = isDarkTheme)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            LanguageMenuButton(
                currentLocale = currentLocale,
                onLocaleChange = onLocaleChange,
                showLanguageMenu = showLanguageMenu,
                onShowLanguageMenu = onShowLanguageMenu
            )
        }
        LiquidGlassBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            isDark = isDarkTheme
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShadowText(
                    text = stringResource(R.string.app_name),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                StyledTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = stringResource(R.string.username),
                    isDarkTheme = isDarkTheme
                )

                Button(onClick = onLogin) {
                    ShadowText(text = stringResource(R.string.login), color = textColor)
                }

                if (error.isNotEmpty()) {
                    ShadowText(text = error, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadowText(text = stringResource(R.string.if_no_account) + " ", color = textColor)
                    ShadowText(
                        text = stringResource(R.string.register),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onRegisterClick() },
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                ShadowText(text = stringResource(R.string.or), color = textColor)

                Spacer(modifier = Modifier.height(2.dp))

                ShadowText(
                    text = stringResource(R.string.login_as_guest),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onGuestLogin() },
                    color = textColor
                )
            }
        }
    }
}

/** Login step 2: password for the username entered in step 1. */
@Composable
fun LoginStep2Screen(
    username: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    error: String,
    currentLocale: String,
    onLocaleChange: (String) -> Unit,
    showLanguageMenu: Boolean,
    onShowLanguageMenu: (Boolean) -> Unit,
    isDarkTheme: Boolean
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedLavaLampBackground(isDark = isDarkTheme)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            LanguageMenuButton(
                currentLocale = currentLocale,
                onLocaleChange = onLocaleChange,
                showLanguageMenu = showLanguageMenu,
                onShowLanguageMenu = onShowLanguageMenu
            )
        }
        LiquidGlassBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            isDark = isDarkTheme
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShadowText(
                    text = stringResource(R.string.welcome) + " $username!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                ShadowText(
                    text = stringResource(R.string.please_enter_password),
                    fontSize = 16.sp,
                    color = textColor
                )

                StyledTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.password),
                    isPassword = true,
                    isDarkTheme = isDarkTheme
                )

                Button(onClick = onLogin) {
                    ShadowText(text = stringResource(R.string.login), color = textColor)
                }

                if (error.isNotEmpty()) {
                    ShadowText(text = error, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onBack) {
                    ShadowText(text = stringResource(R.string.back), color = textColor)
                }
            }
        }
    }
}

/** Registration screen. */
@Composable
fun RegisterScreen(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onBackToLogin: () -> Unit,
    error: String,
    currentLocale: String,
    onLocaleChange: (String) -> Unit,
    showLanguageMenu: Boolean,
    onShowLanguageMenu: (Boolean) -> Unit,
    isDarkTheme: Boolean
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedLavaLampBackground(isDark = isDarkTheme)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            LanguageMenuButton(
                currentLocale = currentLocale,
                onLocaleChange = onLocaleChange,
                showLanguageMenu = showLanguageMenu,
                onShowLanguageMenu = onShowLanguageMenu
            )
        }
        LiquidGlassBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            isDark = isDarkTheme
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShadowText(
                    text = stringResource(R.string.register),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                StyledTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = stringResource(R.string.username),
                    isDarkTheme = isDarkTheme
                )

                StyledTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.password),
                    isPassword = true,
                    isDarkTheme = isDarkTheme
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(onClick = onRegister) {
                        ShadowText(text = stringResource(R.string.register), color = textColor)
                    }
                    Button(onClick = onBackToLogin) {
                        ShadowText(text = stringResource(R.string.back_to_login), color = textColor)
                    }
                }

                if (error.isNotEmpty()) {
                    ShadowText(text = error, color = Color.Red)
                }
            }
        }
    }
}
