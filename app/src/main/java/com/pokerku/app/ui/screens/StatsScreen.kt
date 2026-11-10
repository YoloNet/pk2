package com.pokerku.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokerku.app.ui.PokerViewModel
import com.pokerku.app.ui.theme.Emas
import com.pokerku.app.ui.theme.EmasTerang
import com.pokerku.app.ui.theme.FeltGelap
import com.pokerku.app.ui.theme.Krim

/** PokerKu — skrin Statistik. */
@Composable
fun StatsScreen(vm: PokerViewModel) {
    val settings by vm.settings.collectAsState()
    val showReset by vm.showResetStats.collectAsState()
    val winRate = if (settings.tanganMain > 0) {
        (settings.tanganMenang * 100 / settings.tanganMain)
    } else 0
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FeltGelap)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.keMenu() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = vm.t("common.kembali"), tint = Krim)
            }
            Text(
                text = vm.t("stats.title"),
                color = Krim,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        StatGrid(
            listOf(
                vm.t("stats.cipSemasa") to settings.chips.toString(),
                vm.t("stats.tanganMain") to settings.tanganMain.toString(),
                vm.t("stats.tanganMenang") to settings.tanganMenang.toString(),
                vm.t("stats.kadarMenang") to "$winRate%",
                vm.t("stats.potTerbesar") to settings.potTerbesar.toString(),
                vm.t("stats.cipBersih") to settings.cipMenangBersih.toString()
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0B3D2E))
                .padding(16.dp)
        ) {
            Text(
                text = vm.t("stats.tanganTerbaik"),
                color = Color(0xFFB9C9BC),
                fontSize = 12.sp
            )
            Text(
                text = if (settings.tanganTerbaik.isBlank()) vm.t("stats.tiada")
                else vm.t(settings.tanganTerbaik),
                color = EmasTerang,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = { vm.showResetStats.value = true },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text(vm.t("stats.reset"), color = Color(0xFFFF8A80)) }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { vm.showResetStats.value = false },
            title = { Text(vm.t("stats.reset")) },
            text = { Text(vm.t("stats.resetSah")) },
            confirmButton = {
                TextButton(onClick = { vm.resetStatistik() }) {
                    Text(vm.t("common.ya"), color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showResetStats.value = false }) {
                    Text(vm.t("common.tidak"))
                }
            }
        )
    }
}

@Composable
private fun StatGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, value) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0B3D2E))
                            .padding(14.dp)
                    ) {
                        Text(text = label, color = Color(0xFFB9C9BC), fontSize = 12.sp)
                        Text(text = value, color = Krim, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
