package com.example.myapplication

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Lap
import com.example.myapplication.data.LapDao

/**
 * Logged-in shell: animated tab content + floating rounded popup navigation
 * bar (transparent, only its background layer is blurred).
 * Content is padded at the bottom so it never hides behind the nav bar.
 */
@Composable
fun MainScreenWithNav(
    username: String,
    userRole: String,
    profileImageUri: String?,
    onProfileImageChange: (String) -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    currentLocale: String,
    onLocaleChange: (String) -> Unit,
    timerState: TimerState,
    onTimerStateChange: (TimerState) -> Unit,
    laps: List<Lap>,
    onLapsChange: (List<Lap>) -> Unit,
    lapDao: LapDao
) {
    var selectedTab by remember { mutableStateOf(0) }

    val textColor = if (isDarkTheme) Color.White else Color.Black

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedLavaLampBackground(isDark = isDarkTheme)

        // Tab switch: slide + fade, the previous tab stays visible during the transition.
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "tabTransition"
        ) { tab ->
            when (tab) {
                0 -> MainTabContent(username, userRole, isDarkTheme)
                1 -> TimerTabContent(
                    isDarkTheme = isDarkTheme,
                    timerState = timerState,
                    onTimerStateChange = onTimerStateChange,
                    laps = laps,
                    onLapsChange = onLapsChange,
                    lapDao = lapDao,
                    username = username
                )
                2 -> SettingsTabContent(
                    username = username,
                    userRole = userRole,
                    profileImageUri = profileImageUri,
                    onProfileImageChange = onProfileImageChange,
                    onLogout = onLogout,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    currentLocale = currentLocale,
                    onLocaleChange = onLocaleChange
                )
            }
        }

        // Floating bottom navigation bar (popup style with blur).
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.12f))
                    .blur(16.dp)
            )
            NavigationBar(
                containerColor = Color.Transparent,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .width(300.dp)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.main), tint = textColor) },
                    label = { Text(stringResource(R.string.main), color = textColor) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = navItemColors(textColor)
                )
                NavigationBarItem(
                    icon = { Text("\u23F1", color = textColor) },
                    label = { Text(stringResource(R.string.timer_tab), color = textColor) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = navItemColors(textColor)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings), tint = textColor) },
                    label = { Text(stringResource(R.string.settings), color = textColor) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = navItemColors(textColor)
                )
            }
        }
    }
}

@Composable
private fun navItemColors(textColor: Color) = NavigationBarItemDefaults.colors(
    selectedIconColor = textColor,
    unselectedIconColor = textColor.copy(alpha = 0.6f),
    selectedTextColor = textColor,
    unselectedTextColor = textColor.copy(alpha = 0.6f),
    indicatorColor = textColor.copy(alpha = 0.2f)
)

/** First tab: greeting. */
@Composable
fun MainTabContent(
    username: String,
    userRole: String,
    isDarkTheme: Boolean
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedLavaLampBackground(isDark = isDarkTheme)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShadowText(
                text = stringResource(R.string.hello_world_text),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            ShadowText(
                text = stringResource(R.string.welcome_user, username, userRole),
                color = textColor
            )
        }
    }
}
