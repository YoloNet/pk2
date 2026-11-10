package com.pokerku.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokerku.app.ui.PokerViewModel
import com.pokerku.app.ui.Screen
import com.pokerku.app.ui.screens.HowToScreen
import com.pokerku.app.ui.screens.MenuScreen
import com.pokerku.app.ui.screens.SettingsScreen
import com.pokerku.app.ui.screens.StatsScreen
import com.pokerku.app.ui.screens.TableScreen
import com.pokerku.app.ui.theme.PokerKuTheme

/**
 * PokerKu native v1.0.0 — Activity utama.
 *
 * - 5 skrin: Menu, Meja, Cara Main, Statistik, Tetapan.
 * - Edge-to-edge dengan bar status/navigasi gelap (meja felt gelap).
 * - 100% offline: tiada keizinan, tiada login, tiada iklan, tiada rangkaian.
 * - TIADA paparan web terbenam — sepenuhnya native (Jetpack Compose).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            PokerKuTheme {
                val vm: PokerViewModel = viewModel()
                val screen by vm.screen.collectAsState()
                when (screen) {
                    Screen.MENU -> MenuScreen(vm)
                    Screen.TABLE -> TableScreen(vm)
                    Screen.HOWTO -> HowToScreen(vm)
                    Screen.STATS -> StatsScreen(vm)
                    Screen.SETTINGS -> SettingsScreen(vm)
                }
            }
        }
    }
}
