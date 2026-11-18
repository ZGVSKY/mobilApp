package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

// ---------- app preferences (theme, locale, logged-in user) ----------

fun loadSavedLocale(context: Context): String {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getString("locale", "en") ?: "en"
}

fun saveLocale(context: Context, locale: String) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("locale", locale).apply()
}

fun loadSavedTheme(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("dark_theme", false)
}

fun saveTheme(context: Context, isDark: Boolean) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("dark_theme", isDark).apply()
}

fun loadSavedUser(context: Context): String? {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getString("logged_in_user", null)
}

fun saveUser(context: Context, username: String, role: String) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit()
        .putString("logged_in_user", username)
        .putString("user_role", role)
        .apply()
}

fun clearUser(context: Context) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit()
        .remove("logged_in_user")
        .remove("user_role")
        .apply()
}

fun setAppLocale(activity: Activity, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(activity.resources.configuration)
    config.setLocale(locale)
    activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
}

// ---------- timer ----------

data class TimerState(
    val minutes: Int = 0,
    val seconds: Int = 0,
    val millis: Int = 0,
    val running: Boolean = false,
    val paused: Boolean = false
)

/** Formats timer value as mm:ss:SSS with locale-independent digits. */
fun formatTimerTime(minutes: Int, seconds: Int, millis: Int): String =
    String.format(Locale.ROOT, "%02d:%02d:%03d", minutes, seconds, millis)

private fun timerPrefs(context: Context) =
    context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

fun saveTimerState(context: Context, state: TimerState) {
    timerPrefs(context).edit()
        .putInt("timer_minutes", state.minutes)
        .putInt("timer_seconds", state.seconds)
        .putInt("timer_millis", state.millis)
        .putBoolean("timer_running", state.running)
        .putBoolean("timer_paused", state.paused)
        .apply()
}

/**
 * Restores the timer after process restart. A timer that was running when the
 * process died is restored as *paused* (the exact elapsed time is unknown).
 */
fun loadTimerState(context: Context): TimerState {
    val prefs = timerPrefs(context)
    val wasRunning = prefs.getBoolean("timer_running", false)
    return TimerState(
        minutes = prefs.getInt("timer_minutes", 0),
        seconds = prefs.getInt("timer_seconds", 0),
        millis = prefs.getInt("timer_millis", 0),
        running = false,
        paused = wasRunning || prefs.getBoolean("timer_paused", false)
    )
}

/** Clears only the time/running keys so a pending notification command survives. */
fun clearTimerState(context: Context) {
    timerPrefs(context).edit()
        .remove("timer_minutes")
        .remove("timer_seconds")
        .remove("timer_millis")
        .remove("timer_running")
        .remove("timer_paused")
        .apply()
}

/**
 * Commands sent from the notification action buttons to the app.
 * The notification receiver writes them, MainApp polls and applies them.
 */
object TimerCommand {
    const val KEY = "timer_command"
    const val NONE = ""
    const val PAUSE = "pause"
    const val RESUME = "resume"
    const val RESET = "reset"

    fun get(context: Context): String =
        timerPrefs(context).getString(KEY, NONE) ?: NONE

    fun set(context: Context, command: String) {
        timerPrefs(context).edit().putString(KEY, command).apply()
    }
}
