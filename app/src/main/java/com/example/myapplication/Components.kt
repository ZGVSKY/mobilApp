package com.example.myapplication

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Semi-transparent "liquid glass" panel: blurred-looking background layer,
 * gradient highlights and a thin border. Only the background is painted here,
 * the content stays crisp on top.
 */
@Composable
fun LiquidGlassBox(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val bgColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.15f)
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.3f)
    val gradientColors = if (isDark) {
        listOf(
            Color.White.copy(alpha = 0.2f),
            Color.White.copy(alpha = 0.08f),
            Color.Transparent,
            Color.White.copy(alpha = 0.05f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.9f),
            Color.White.copy(alpha = 0.5f),
            Color.Transparent,
            Color.White.copy(alpha = 0.3f)
        )
    }

    Box(
        modifier = modifier
            .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = shadowColor, spotColor = shadowColor)
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(brush = Brush.verticalGradient(colors = gradientColors))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isDark) {
                            listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                        } else {
                            listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
                        },
                        center = Offset(0.3f, 0.2f),
                        radius = 800f
                    )
                )
        )
        content()
    }
}

/** Animated lava-lamp gradient background (never blocks clicks). */
@Composable
fun AnimatedLavaLampBackground(isDark: Boolean = true) {
    val infiniteTransition = rememberInfiniteTransition(label = "lava")
    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim1"
    )
    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim2"
    )
    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim3"
    )

    val colors = if (isDark) {
        listOf(Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460))
    } else {
        listOf(Color(0xFFe8f4f8), Color(0xFFd0e8f0), Color(0xFFb8dce8))
    }

    val blobColors = if (isDark) {
        listOf(Color(0x88e94560), Color(0x44e94560))
    } else {
        listOf(Color(0x88ff8a80), Color(0x44ff8a80))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLavaLamp(size.width, size.height, anim1, anim2, anim3, colors, blobColors)
    }
}

fun DrawScope.drawLavaLamp(
    w: Float,
    h: Float,
    a1: Float,
    a2: Float,
    a3: Float,
    colors: List<Color>,
    blobColors: List<Color>
) {
    drawRect(brush = Brush.verticalGradient(colors = colors))

    val blobs = listOf(
        Triple(0.3f + a1 * 0.4f, 0.2f + a2 * 0.3f, 0.15f),
        Triple(0.7f - a2 * 0.3f, 0.5f + a3 * 0.2f, 0.12f),
        Triple(0.5f + a3 * 0.2f, 0.8f - a1 * 0.3f, 0.18f),
        Triple(0.2f + a2 * 0.5f, 0.6f + a1 * 0.2f, 0.1f),
        Triple(0.8f - a3 * 0.4f, 0.3f + a2 * 0.4f, 0.14f)
    )

    blobs.forEach { (x, y, r) ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(blobColors[0], blobColors[1], Color.Transparent),
                center = Offset(w * x, h * y),
                radius = w * r
            ),
            radius = w * r,
            center = Offset(w * x, h * y)
        )
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isDarkTheme: Boolean = true
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = textColor.copy(alpha = 0.7f)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
            unfocusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = textColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun ShadowText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    color: Color = Color.White,
    fontWeight: FontWeight? = null,
    textDecoration: TextDecoration? = null,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        textDecoration = textDecoration,
        style = androidx.compose.ui.text.TextStyle(
            shadow = Shadow(
                offset = Offset(2f, 2f),
                blurRadius = 4f,
                color = Color.Black.copy(alpha = 0.3f)
            )
        ),
        modifier = modifier
    )
}

/** Globe button with an animated language dropdown shown exactly under it. */
@Composable
fun LanguageMenuButton(
    currentLocale: String,
    onLocaleChange: (String) -> Unit,
    showLanguageMenu: Boolean,
    onShowLanguageMenu: (Boolean) -> Unit
) {
    Box {
        Button(
            onClick = { onShowLanguageMenu(true) },
            modifier = Modifier.size(40.dp)
        ) {
            Text("\uD83C\uDF10", fontSize = 16.sp)
        }
        DropdownMenu(
            expanded = showLanguageMenu,
            onDismissRequest = { onShowLanguageMenu(false) }
        ) {
            DropdownMenuItem(
                onClick = {
                    onLocaleChange("en")
                    onShowLanguageMenu(false)
                },
                text = { Text("English") }
            )
            DropdownMenuItem(
                onClick = {
                    onLocaleChange("uk")
                    onShowLanguageMenu(false)
                },
                text = { Text("\u0423\u043A\u0440\u0430\u0457\u043D\u0441\u044C\u043A\u0430") }
            )
            DropdownMenuItem(
                onClick = {
                    onLocaleChange("es")
                    onShowLanguageMenu(false)
                },
                text = { Text("Espa\u00F1ol") }
            )
        }
    }
}
