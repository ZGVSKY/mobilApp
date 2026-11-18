package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Lap
import com.example.myapplication.data.LapDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Timer tab: a pure display + button handler.
 * All timer logic (ticking, persistence, notification) lives in [MainApp],
 * so the buttons react on the very first press and work across tab switches.
 *
 * Buttons: Start / Pause / Continue (one of them) + Reset (always visible).
 */
@Composable
fun TimerTabContent(
    isDarkTheme: Boolean,
    timerState: TimerState,
    onTimerStateChange: (TimerState) -> Unit,
    laps: List<Lap>,
    onLapsChange: (List<Lap>) -> Unit,
    lapDao: LapDao,
    username: String
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedLavaLampBackground(isDark = isDarkTheme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                // Keep content clear of the floating navigation bar.
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---- timer panel ----
            LiquidGlassBox(isDark = isDarkTheme) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = formatTimerTime(timerState.minutes, timerState.seconds, timerState.millis),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Exactly one control button at a time.
                        when {
                            timerState.running -> Button(
                                onClick = { onTimerStateChange(timerState.copy(running = false, paused = true)) }
                            ) {
                                ShadowText(text = stringResource(R.string.pause), color = textColor)
                            }
                            timerState.paused -> Button(
                                onClick = { onTimerStateChange(timerState.copy(running = true, paused = false)) }
                            ) {
                                ShadowText(text = stringResource(R.string.continue_timer), color = textColor)
                            }
                            else -> Button(
                                onClick = { onTimerStateChange(timerState.copy(running = true, paused = false)) }
                            ) {
                                ShadowText(text = stringResource(R.string.start), color = textColor)
                            }
                        }
                        // Reset is always visible and always works.
                        Button(onClick = { onTimerStateChange(TimerState()) }) {
                            ShadowText(text = stringResource(R.string.reset), color = textColor)
                        }
                    }

                    if (timerState.running || timerState.paused) {
                        Button(
                            onClick = {
                                val time = formatTimerTime(timerState.minutes, timerState.seconds, timerState.millis)
                                scope.launch {
                                    val updated = withContext(Dispatchers.IO) {
                                        lapDao.insert(Lap(username = username, lapTime = time))
                                        lapDao.getLapsForUser(username)
                                    }
                                    onLapsChange(updated)
                                }
                            }
                        ) {
                            ShadowText(text = stringResource(R.string.lap), color = textColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- laps block: scrollable, with header + clear button ----
            LiquidGlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                isDark = isDarkTheme
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.laps),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        TextButton(
                            onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        lapDao.clearLapsForUser(username)
                                    }
                                    onLapsChange(emptyList())
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.clear_laps),
                                color = textColor.copy(alpha = 0.85f)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    ) {
                        itemsIndexed(laps) { index, lap ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .shadow(4.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.lap_row, index + 1, lap.lapTime),
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
