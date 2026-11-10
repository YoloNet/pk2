package com.pokerku.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokerku.app.ui.PokerViewModel
import com.pokerku.app.ui.theme.EmasTerang
import com.pokerku.app.ui.theme.FeltGelap
import com.pokerku.app.ui.theme.Krim

/** PokerKu — skrin Cara Main (peraturan + ranking tangan). */
@Composable
fun HowToScreen(vm: PokerViewModel) {
    val ranks = listOf(
        "hand.royalflush" to "howto.ex.royalflush",
        "hand.straightflush" to "howto.ex.straightflush",
        "hand.quads" to "howto.ex.quads",
        "hand.fullhouse" to "howto.ex.fullhouse",
        "hand.flush" to "howto.ex.flush",
        "hand.straight" to "howto.ex.straight",
        "hand.trips" to "howto.ex.trips",
        "hand.twopair" to "howto.ex.twopair",
        "hand.pair" to "howto.ex.pair",
        "hand.highcard" to "howto.ex.highcard"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FeltGelap)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.keMenu() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = vm.t("common.kembali"), tint = Krim)
            }
            Text(
                text = vm.t("howto.title"),
                color = Krim,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Section(vm.t("howto.objektifTitle"), vm.t("howto.objektifBody"))
        Section(vm.t("howto.aliranTitle"), vm.t("howto.aliranBody"))
        Section(vm.t("howto.aksiTitle"), vm.t("howto.aksiBody"))

        Text(
            text = vm.t("howto.rankingTitle"),
            color = EmasTerang,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        ranks.forEachIndexed { index, (nameKey, exKey) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0B3D2E))
                    .padding(12.dp)
            ) {
                Row {
                    Text(
                        text = "${index + 1}. ",
                        color = EmasTerang,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = vm.t(nameKey),
                        color = Krim,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = vm.t(exKey),
                    color = Color(0xFFB9C9BC),
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Section(vm.t("howto.sidepotTitle"), vm.t("howto.sidepotBody"))
        Section(vm.t("howto.blindsTitle"), vm.t("howto.blindsBody"))
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String, body: String) {
    Text(
        text = title,
        color = EmasTerang,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
    Text(
        text = body,
        color = Krim,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}
