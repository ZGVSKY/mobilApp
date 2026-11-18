package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Lap
import com.example.myapplication.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Root composable: owns the authentication flow, the single source of truth
 * for the timer ([timerState]), its background ticking, the foreground
 * notification lifecycle and the per-account laps.
 *
 * Timer rules implemented here:
 *  - the tick loop lives at this level, so switching tabs never stops it;
 *  - while the app is hidden (ON_STOP) a silent notification with a live
 *    time is shown; it is removed as soon as the app is back (ON_START);
 *  - notification buttons write a [TimerCommand] into SharedPreferences which
 *    is polled and applied here (pause / resume / reset);
 *  - reset closes the notification and resets the timer, but keeps laps;
 *  - logging out resets the timer and stops the notification.
 */
@Composable
fun MainApp(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    currentLocale: String,
    onLocaleChange: (String) -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = remember { db.userDao() }
    val lapDao = remember { db.lapDao() }
    val scope = rememberCoroutineScope()

    var isLoggedIn by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("guest") }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var showRegister by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf("") }
    var loginStep by remember { mutableStateOf(1) }
    var foundUsername by remember { mutableStateOf("") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf<List<Lap>>(emptyList()) }

    // Single source of truth for the timer (persists across tab switches).
    var timerState by remember { mutableStateOf(loadTimerState(context)) }
    var isAppInBackground by remember { mutableStateOf(false) }
    var serviceRunning by remember { mutableStateOf(false) }

    // ---------- timer helpers ----------

    fun persistTimer(state: TimerState) {
        timerState = state
        if (state.running || state.paused) saveTimerState(context, state)
        else clearTimerState(context)
    }

    fun startTimerNotification(state: TimerState) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_TIME, formatTimerTime(state.minutes, state.seconds, state.millis))
            putExtra(TimerService.EXTRA_PAUSED, state.paused)
        }
        context.startForegroundService(intent)
        serviceRunning = true
    }

    fun stopTimerNotification() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        context.startService(intent)
        serviceRunning = false
    }

    fun updateTimerNotification(state: TimerState) {
        if (!serviceRunning) return
        // Never throw if the service was just stopped by a reset command.
        runCatching {
            context.startService(Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_UPDATE
                putExtra(TimerService.EXTRA_TIME, formatTimerTime(state.minutes, state.seconds, state.millis))
                putExtra(TimerService.EXTRA_PAUSED, state.paused)
            })
        }
    }

    // ---------- app lifecycle: show/hide the timer notification ----------

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // App is hidden: show the silent notification with live time.
                    isAppInBackground = true
                    if (timerState.running) {
                        // runCatching: never crash if the OS refuses a
                        // background FGS start (Android 14 restrictions).
                        runCatching { startTimerNotification(timerState) }
                    }
                }
                Lifecycle.Event.ON_START -> {
                    // App is visible again: the notification must disappear.
                    isAppInBackground = false
                    stopTimerNotification()
                }
                else -> {}
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
        onDispose {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
        }
    }

    // ---------- the tick loop: the only place the timer advances ----------

    LaunchedEffect(Unit) {
        var lastSavedAt = 0L
        var lastNotifiedAt = 0L
        while (true) {
            if (timerState.running) {
                delay(10L)
                // Guard: the timer may have been paused/reset during the delay.
                if (!timerState.running) continue

                val newMillis = timerState.millis + 10
                val next = if (newMillis >= 1000) {
                    val newSeconds = timerState.seconds + 1
                    if (newSeconds >= 60) {
                        timerState.copy(millis = 0, seconds = 0, minutes = timerState.minutes + 1, running = true, paused = false)
                    } else {
                        timerState.copy(millis = 0, seconds = newSeconds, running = true, paused = false)
                    }
                } else {
                    timerState.copy(millis = newMillis, running = true, paused = false)
                }
                timerState = next

                val now = SystemClock.elapsedRealtime()
                if (now - lastSavedAt >= 500L) {
                    saveTimerState(context, next)
                    lastSavedAt = now
                }
                // Live notification update, throttled to every ~100 ms and
                // guarded against a stopped service (crash fix).
                if (isAppInBackground && serviceRunning &&
                    now - lastNotifiedAt >= 100L &&
                    TimerCommand.get(context) == TimerCommand.NONE
                ) {
                    lastNotifiedAt = now
                    updateTimerNotification(next)
                }
            } else {
                delay(50L)
            }
        }
    }

    // ---------- commands from the notification buttons ----------

    LaunchedEffect(Unit) {
        while (true) {
            delay(200L)
            val command = TimerCommand.get(context)
            if (command.isNotEmpty()) {
                TimerCommand.set(context, TimerCommand.NONE)
                when (command) {
                    TimerCommand.PAUSE -> {
                        if (timerState.running) {
                            persistTimer(timerState.copy(running = false, paused = true))
                        }
                        // Refresh the notification so the button becomes "Continue".
                        updateTimerNotification(timerState)
                    }
                    TimerCommand.RESUME -> {
                        if (timerState.paused) {
                            persistTimer(timerState.copy(running = true, paused = false))
                        }
                        updateTimerNotification(timerState)
                    }
                    TimerCommand.RESET -> {
                        // Reset the timer only — laps belong to the account.
                        persistTimer(TimerState())
                        // The receiver already stopped the service (closed the notification).
                        serviceRunning = false
                    }
                }
            }
        }
    }

    // ---------- restore session / seed admin ----------

    LaunchedEffect(Unit) {
        scope.launch {
            val restored = withContext(Dispatchers.IO) {
                if (userDao.count() == 0) {
                    userDao.insert(User(username = "admin", password = "admin", role = "admin"))
                }
                val savedUser = loadSavedUser(context)
                savedUser?.let { userDao.findByUsername(it) }
            }
            if (restored != null) {
                isLoggedIn = true
                username = restored.username
                userRole = restored.role
                onThemeChange(restored.isDarkTheme)
                onLocaleChange(restored.locale)
                laps = withContext(Dispatchers.IO) { lapDao.getLapsForUser(restored.username) }
                setAppLocale(context as Activity, restored.locale)
            }
        }
    }

    // ---------- screens ----------

    AnimatedContent(
        targetState = isLoggedIn,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
        },
        label = "loginTransition"
    ) { loggedIn ->
        if (!loggedIn) {
            if (showRegister) {
                RegisterScreen(
                    username = username,
                    password = password,
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it },
                    onRegister = {
                        scope.launch {
                            val existing = withContext(Dispatchers.IO) { userDao.findByUsername(username) }
                            if (existing != null) {
                                authError = context.getString(R.string.user_already_exists)
                            } else {
                                withContext(Dispatchers.IO) {
                                    userDao.insert(
                                        User(
                                            username = username,
                                            password = password,
                                            role = "user",
                                            isDarkTheme = isDarkTheme,
                                            locale = currentLocale
                                        )
                                    )
                                }
                                isLoggedIn = true
                                userRole = "user"
                                saveUser(context, username, "user")
                                authError = ""
                            }
                        }
                    },
                    onBackToLogin = { showRegister = false },
                    error = authError,
                    currentLocale = currentLocale,
                    onLocaleChange = onLocaleChange,
                    showLanguageMenu = showLanguageMenu,
                    onShowLanguageMenu = { showLanguageMenu = it },
                    isDarkTheme = isDarkTheme
                )
            } else {
                if (loginStep == 1) {
                    LoginStep1Screen(
                        username = username,
                        onUsernameChange = { username = it },
                        onLogin = {
                            scope.launch {
                                val user = withContext(Dispatchers.IO) { userDao.findByUsername(username) }
                                if (user != null) {
                                    foundUsername = username
                                    loginStep = 2
                                    authError = ""
                                } else {
                                    authError = context.getString(R.string.invalid_username)
                                }
                            }
                        },
                        onRegisterClick = {
                            showRegister = true
                            authError = ""
                        },
                        onGuestLogin = {
                            scope.launch {
                                val guestName = "guest_${System.currentTimeMillis()}"
                                withContext(Dispatchers.IO) {
                                    userDao.insert(
                                        User(
                                            username = guestName,
                                            password = "",
                                            role = "guest",
                                            isDarkTheme = isDarkTheme,
                                            locale = currentLocale
                                        )
                                    )
                                }
                                isLoggedIn = true
                                userRole = "guest"
                                username = guestName
                                saveUser(context, guestName, "guest")
                            }
                        },
                        error = authError,
                        currentLocale = currentLocale,
                        onLocaleChange = onLocaleChange,
                        showLanguageMenu = showLanguageMenu,
                        onShowLanguageMenu = { showLanguageMenu = it },
                        isDarkTheme = isDarkTheme
                    )
                } else {
                    LoginStep2Screen(
                        username = foundUsername,
                        password = password,
                        onPasswordChange = { password = it },
                        onLogin = {
                            scope.launch {
                                val user = withContext(Dispatchers.IO) {
                                    userDao.login(foundUsername, password)
                                }
                                if (user != null) {
                                    isLoggedIn = true
                                    userRole = user.role
                                    username = foundUsername
                                    saveUser(context, foundUsername, user.role)
                                    onThemeChange(user.isDarkTheme)
                                    onLocaleChange(user.locale)
                                    laps = withContext(Dispatchers.IO) {
                                        lapDao.getLapsForUser(foundUsername)
                                    }
                                    authError = ""
                                } else {
                                    authError = context.getString(R.string.invalid_password)
                                }
                            }
                        },
                        onBack = {
                            loginStep = 1
                            password = ""
                            authError = ""
                        },
                        error = authError,
                        currentLocale = currentLocale,
                        onLocaleChange = onLocaleChange,
                        showLanguageMenu = showLanguageMenu,
                        onShowLanguageMenu = { showLanguageMenu = it },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        } else {
            MainScreenWithNav(
                username = username,
                userRole = userRole,
                profileImageUri = profileImageUri,
                onProfileImageChange = { profileImageUri = it },
                onLogout = {
                    // The timer MUST reset when the user logs out.
                    persistTimer(TimerState())
                    TimerCommand.set(context, TimerCommand.NONE)
                    stopTimerNotification()
                    // Laps stay in the DB for the account; only clear the screen.
                    laps = emptyList()
                    clearUser(context)
                    isLoggedIn = false
                    username = ""
                    password = ""
                    userRole = "guest"
                    profileImageUri = null
                    showRegister = false
                    loginStep = 1
                    foundUsername = ""
                    authError = ""
                },
                isDarkTheme = isDarkTheme,
                onThemeChange = { newTheme ->
                    onThemeChange(newTheme)
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val user = userDao.findByUsername(username)
                            user?.let {
                                userDao.update(it.copy(isDarkTheme = newTheme))
                            }
                        }
                    }
                },
                currentLocale = currentLocale,
                onLocaleChange = { newLocale ->
                    onLocaleChange(newLocale)
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val user = userDao.findByUsername(username)
                            user?.let {
                                userDao.update(it.copy(locale = newLocale))
                            }
                        }
                    }
                },
                timerState = timerState,
                onTimerStateChange = { persistTimer(it) },
                laps = laps,
                onLapsChange = { laps = it },
                lapDao = lapDao
            )
        }
    }
}
