package com.pokerku.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * PokerKu — tema Material 3.
 * Meja felt hijau gelap #0B3D2E, krim #F7F4EC, aksen emas #C08A1A
 * (selari jenama KebunKu). Aplikasi dikunci kepada tema gelap —
 * sesuai dengan meja poker — tiada mod cerah.
 */
val Hijau = Color(0xFF1E4D2B)
val HijauGelap = Color(0xFF163A22)
val HijauMuda = Color(0xFF2E6B3E)
val HijauPudar = Color(0xFFE4EEE6)
val Felt = Color(0xFF0B3D2E)
val FeltGelap = Color(0xFF072A20)
val FeltTerang = Color(0xFF14663F)
val Krim = Color(0xFFF7F4EC)
val KrimGelap = Color(0xFFEAE6D8)
val Emas = Color(0xFFC08A1A)
val EmasTerang = Color(0xFFE7C46A)
val TeksGelap = Color(0xFF1C1C1A)
val TeksPudar = Color(0xFF6B6B64)
val Merah = Color(0xFFB3261E)
val MerahPudar = Color(0xFFFBEAEA)
val KadMerah = Color(0xFFC62828)
val KadHitam = Color(0xFF1C1C1A)

private val SkemaGelap = darkColorScheme(
    primary = Emas,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6B4E05),
    onPrimaryContainer = EmasTerang,
    secondary = EmasTerang,
    onSecondary = TeksGelap,
    secondaryContainer = Color(0xFF3A2E05),
    onSecondaryContainer = EmasTerang,
    tertiary = HijauMuda,
    background = FeltGelap,
    onBackground = Krim,
    surface = Felt,
    onSurface = Krim,
    surfaceVariant = Color(0xFF0E4A37),
    onSurfaceVariant = Color(0xFFB9C9BC),
    inverseSurface = Krim,
    inverseOnSurface = TeksGelap,
    inversePrimary = Hijau,
    error = Color(0xFFFF8A80),
    onError = TeksGelap
)

private val Tipografi = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Krim),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Krim),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Krim),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Krim),
    bodyLarge = TextStyle(fontSize = 16.sp, color = Krim),
    bodyMedium = TextStyle(fontSize = 14.sp, color = Krim),
    bodySmall = TextStyle(fontSize = 12.sp, color = Color(0xFFB9C9BC)),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB9C9BC))
)

@Composable
fun PokerKuTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            // Meja gelap — ikon bar status & navigasi berwarna cerah.
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = SkemaGelap,
        typography = Tipografi,
        content = content
    )
}
