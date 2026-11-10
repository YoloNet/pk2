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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

/** PokerKu — skrin Tetapan. */
@Composable
fun SettingsScreen(vm: PokerViewModel) {
    val settings by vm.settings.collectAsState()
    val showResetChips by vm.showResetChips.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FeltGelap)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.keMenu() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = vm.t("common.kembali"), tint = Krim)
            }
            Text(
                text = vm.t("settings.title"),
                color = Krim,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = vm.t("settings.bahasa"),
            color = EmasTerang,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PilihanButton(
                label = "Bahasa Malaysia",
                dipilih = settings.bahasa == "ms",
                onClick = { vm.setBahasa("ms") },
                modifier = Modifier.weight(1f)
            )
            PilihanButton(
                label = "English",
                dipilih = settings.bahasa == "en",
                onClick = { vm.setBahasa("en") },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = vm.t("settings.kelajuan"),
            color = EmasTerang,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
        )
        Text(
            text = vm.t("settings.kelajuanHint"),
            color = Color(0xFFB9C9BC),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PilihanButton(
                label = vm.t("settings.perlahan"),
                dipilih = settings.kelajuan == "perlahan",
                onClick = { vm.setKelajuan("perlahan") },
                modifier = Modifier.weight(1f)
            )
            PilihanButton(
                label = vm.t("settings.sederhana"),
                dipilih = settings.kelajuan == "sederhana",
                onClick = { vm.setKelajuan("sederhana") },
                modifier = Modifier.weight(1f)
            )
            PilihanButton(
                label = vm.t("settings.laju"),
                dipilih = settings.kelajuan == "laju",
                onClick = { vm.setKelajuan("laju") },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = vm.t("settings.botTitle"),
            color = EmasTerang,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
        )
        Text(
            text = vm.t("settings.botDesc"),
            color = Krim,
            fontSize = 14.sp
        )

        Text(
            text = vm.t("settings.resetCip"),
            color = EmasTerang,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
        )
        Text(
            text = vm.t("settings.resetCipDesc"),
            color = Color(0xFFB9C9BC),
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedButton(
            onClick = { vm.showResetChips.value = true },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text(vm.t("settings.resetCip")) }

        Text(
            text = vm.t("settings.tentang"),
            color = EmasTerang,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
        )
        Text(
            text = vm.t("settings.tentangBody"),
            color = Krim,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showResetChips) {
        AlertDialog(
            onDismissRequest = { vm.showResetChips.value = false },
            title = { Text(vm.t("settings.resetCip")) },
            text = { Text(vm.t("settings.resetCipDesc")) },
            confirmButton = {
                TextButton(onClick = { vm.resetCip() }) {
                    Text(vm.t("common.ya"), color = EmasTerang, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showResetChips.value = false }) {
                    Text(vm.t("common.tidak"))
                }
            }
        )
    }
}

@Composable
private fun PilihanButton(
    label: String,
    dipilih: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (dipilih) {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emas)
        ) { Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp)
        ) { Text(label, fontSize = 13.sp) }
    }
}
