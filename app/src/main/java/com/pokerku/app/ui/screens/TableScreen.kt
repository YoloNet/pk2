package com.pokerku.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokerku.app.engine.HandEvaluator
import com.pokerku.app.engine.PlayerAction
import com.pokerku.app.engine.Street
import com.pokerku.app.i18n.Strings
import com.pokerku.app.ui.PokerViewModel
import com.pokerku.app.ui.components.CardRow
import com.pokerku.app.ui.components.ChipBadge
import com.pokerku.app.ui.components.PlayerSeat
import com.pokerku.app.ui.theme.Emas
import com.pokerku.app.ui.theme.EmasTerang
import com.pokerku.app.ui.theme.Felt
import com.pokerku.app.ui.theme.FeltGelap
import com.pokerku.app.ui.theme.Krim

/**
 * PokerKu — skrin meja permainan.
 *
 * Susun atur: vic di atas, Masta kiri, Ayam kanan, anda di bawah.
 * Community card + pot di tengah meja. Butang aksi di bawah kad anda.
 */
@Composable
fun TableScreen(vm: PokerViewModel) {
    val snap by vm.table.collectAsState()
    val settings by vm.settings.collectAsState()
    val showRaise by vm.showRaisePanel.collectAsState()
    val showRebuy by vm.showRebuy.collectAsState()
    val raiseAmount by vm.raiseAmount.collectAsState()
    val lang = settings.bahasa
    val s = snap

    if (s == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(FeltGelap),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Emas)
        }
        return
    }

    val you = s.players[0]
    val isYourTurn = s.street != Street.HAND_OVER && s.actionIndex == 0
    val range = vm.raiseRange()
    val call = vm.callAmount()

    // Kad pemenang di-highlight (showdown sahaja).
    val winningCards = remember(s.showdown) {
        s.showdown?.handValues
            ?.filter { (id, _) -> (s.showdown?.winnings?.get(id) ?: 0) > 0 }
            ?.values?.flatMap { it.bestCards }?.toSet() ?: emptySet()
    }
    // Hint kekuatan tangan anda (kad sendiri — bukan maklumat bocor).
    val myBest = remember(s.communityCards, you.holeCards, you.folded) {
        if (s.communityCards.size >= 3 && you.holeCards.size == 2 && !you.folded) {
            HandEvaluator.evaluateBest(you.holeCards + s.communityCards)
        } else null
    }

    Column(
        modifier = Modifier.fillMaxSize().background(FeltGelap).padding(12.dp)
    ) {
        // --- Bar atas ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.keMenu() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = vm.t("common.kembali"), tint = Krim)
            }
            Text(
                text = vm.t("table.tangan", "n" to s.handNumber.toString()),
                color = Krim,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = streetName(s.street, lang),
                color = EmasTerang,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // --- Meja ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Felt)
                .border(2.dp, Emas, RoundedCornerShape(24.dp))
        ) {
            // Community cards + pot di tengah.
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = vm.t("table.pot"),
                        color = Color(0xFFB9C9BC),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ChipBadge(s.totalPot)
                }
                Spacer(modifier = Modifier.height(8.dp))
                CardRow(
                    cards = (0 until 5).map { s.communityCards.getOrNull(it) },
                    width = 36.dp,
                    highlighted = winningCards,
                    spacing = 5.dp
                )
            }
            // vic — atas tengah.
            PlayerSeat(
                p = s.players[1],
                styleLabel = vm.t("bot.ketat"),
                t = { key -> vm.t(key) },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
            )
            // Masta — kiri tengah.
            PlayerSeat(
                p = s.players[2],
                styleLabel = vm.t("bot.longgar"),
                t = { key -> vm.t(key) },
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            )
            // Ayam — kanan tengah.
            PlayerSeat(
                p = s.players[3],
                styleLabel = vm.t("bot.agresif"),
                t = { key -> vm.t(key) },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
            )

            // Banner pemenang (tangan tamat).
            if (s.street == Street.HAND_OVER) {
                WinnerBanner(vm, s, Modifier.align(Alignment.Center))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Kawasan anda ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = vm.t("table.kadAnda"),
                    color = Color(0xFFB9C9BC),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                CardRow(
                    cards = if (you.holeCards.isEmpty()) listOf(null, null) else you.holeCards,
                    width = 52.dp,
                    highlighted = winningCards,
                    spacing = 8.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = vm.t("table.anda"),
                        color = Krim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = you.stack.toString(),
                        color = EmasTerang,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (you.allIn) {
                        Text(text = "ALL-IN", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (you.betInRound > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                ChipBadge(you.betInRound)
            }
            if (myBest != null && s.street != Street.HAND_OVER) {
                Text(
                    text = "${vm.t("table.kadAnda")}: ${vm.t(myBest.category.key)}",
                    color = Color(0xFFB9C9BC),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                s.street == Street.HAND_OVER -> {
                    // Butang tangan seterusnya ada dalam banner; tiada aksi di sini.
                }
                isYourTurn -> {
                    if (showRaise && range != null) {
                        RaisePanel(vm, range, raiseAmount)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { vm.aksiManusia(PlayerAction.Fold) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(vm.t("table.lipat"), color = Color(0xFFFF8A80))
                            }
                            Button(
                                onClick = { vm.aksiManusia(PlayerAction.Call) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B3E))
                            ) {
                                Text(
                                    if (vm.canCheck()) vm.t("table.semak")
                                    else vm.t("table.panggilJumlah", "jumlah" to call.toString())
                                )
                            }
                            Button(
                                onClick = {
                                    if (range != null) vm.showRaisePanel.value = true
                                    else vm.aksiManusia(PlayerAction.AllIn)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Emas)
                            ) {
                                Text(vm.t("table.naikkan"), color = Color.White)
                            }
                        }
                    }
                }
                else -> {
                    val current = s.players.getOrNull(s.actionIndex)
                    Text(
                        text = if (current != null) vm.t("table.menunggu", "nama" to current.name)
                        else vm.t("table.menungguBot"),
                        color = Color(0xFFB9C9BC),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showRebuy) {
        AlertDialog(
            onDismissRequest = { /* wajib pilih — tiada dismiss luar */ },
            title = { Text(vm.t("table.rebuyTitle")) },
            text = { Text(vm.t("table.rebuyDesc")) },
            confirmButton = {
                TextButton(onClick = { vm.sahkanRebuy() }) {
                    Text(vm.t("table.rebuyYa"), color = EmasTerang, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.tolakRebuy() }) {
                    Text(vm.t("table.rebuyTidak"))
                }
            }
        )
    }
}

@Composable
private fun RaisePanel(vm: PokerViewModel, range: IntRange, raiseAmount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0E4A37))
            .padding(12.dp)
    ) {
        Text(
            text = "${vm.t("table.jumlahNaik")}: $raiseAmount",
            color = Krim,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = raiseAmount.toFloat(),
            onValueChange = { vm.setRaiseAmount(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Emas,
                activeTrackColor = Emas,
                inactiveTrackColor = Color(0xFF3A5A48)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { vm.quickRaise("separuh") },
                modifier = Modifier.weight(1f)
            ) { Text(vm.t("table.setengahPot"), fontSize = 12.sp) }
            OutlinedButton(
                onClick = { vm.quickRaise("pot") },
                modifier = Modifier.weight(1f)
            ) { Text(vm.t("table.potPenuh"), fontSize = 12.sp) }
            OutlinedButton(
                onClick = { vm.quickRaise("allin") },
                modifier = Modifier.weight(1f)
            ) { Text(vm.t("table.allin"), fontSize = 12.sp) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { vm.aksiManusia(PlayerAction.RaiseTo(raiseAmount)) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Emas)
            ) { Text(vm.t("table.sahkan"), color = Color.White) }
            OutlinedButton(
                onClick = { vm.showRaisePanel.value = false },
                modifier = Modifier.weight(1f)
            ) { Text(vm.t("table.batal")) }
        }
    }
}

@Composable
private fun WinnerBanner(
    vm: PokerViewModel,
    s: com.pokerku.app.engine.PokerGame.Snapshot,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF072A20).copy(alpha = 0.96f))
            .border(2.dp, Emas, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val showdown = s.showdown
            if (showdown != null) {
                val winners = showdown.winnings.entries.sortedByDescending { it.value }
                if (winners.size > 1) {
                    Text(
                        text = vm.t("table.seri"),
                        color = EmasTerang,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                winners.forEach { (id, won) ->
                    val name = s.players[id].name
                    val handKey = showdown.handValues[id]?.category?.key
                    Text(
                        text = vm.t("table.menang", "nama" to name, "jumlah" to won.toString()),
                        color = Krim,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (handKey != null) {
                        Text(
                            text = vm.t("table.menangTangan", "tangan" to vm.t(handKey)),
                            color = Color(0xFFB9C9BC),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (s.foldWinnerId != null) {
                val name = s.players[s.foldWinnerId!!].name
                Text(
                    text = vm.t("table.semuaFold", "nama" to name),
                    color = Krim,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { vm.tanganSeterusnya() },
                colors = ButtonDefaults.buttonColors(containerColor = Emas)
            ) {
                Text(vm.t("table.tanganSeterusnya"), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun streetName(street: Street, lang: String): String {
    return when (street) {
        Street.PRE_FLOP -> Strings.t("street.preflop", lang)
        Street.FLOP -> Strings.t("street.flop", lang)
        Street.TURN -> Strings.t("street.turn", lang)
        Street.RIVER -> Strings.t("street.river", lang)
        Street.SHOWDOWN, Street.HAND_OVER -> Strings.t("street.showdown", lang)
    }
}
