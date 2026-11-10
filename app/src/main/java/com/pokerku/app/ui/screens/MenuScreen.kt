package com.pokerku.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokerku.app.R
import com.pokerku.app.ui.PokerViewModel
import com.pokerku.app.ui.Screen
import com.pokerku.app.ui.theme.Emas
import com.pokerku.app.ui.theme.EmasTerang
import com.pokerku.app.ui.theme.FeltGelap
import com.pokerku.app.ui.theme.Krim

/** PokerKu — menu utama. */
@Composable
fun MenuScreen(vm: PokerViewModel) {
    val settings by vm.settings.collectAsState()
    val lang = settings.bahasa
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FeltGelap)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "PokerKu",
            modifier = Modifier.size(110.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "PokerKu",
            color = EmasTerang,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = vm.t("app.tagline"),
            color = Krim,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0B3D2E))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = vm.t("menu.cipAnda"),
                color = Color(0xFFB9C9BC),
                fontSize = 12.sp
            )
            Text(
                text = settings.chips.toString(),
                color = EmasTerang,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            if (settings.tanganTerbaik.isNotBlank()) {
                Text(
                    text = "${vm.t("menu.tanganTerbaik")}: ${vm.t(settings.tanganTerbaik)}",
                    color = Color(0xFFB9C9BC),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { vm.mulaMeja() },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emas)
        ) {
            Text(vm.t("menu.main"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = { vm.keSkrin(Screen.HOWTO) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text(vm.t("menu.caramain")) }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = { vm.keSkrin(Screen.STATS) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text(vm.t("menu.statistik")) }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = { vm.keSkrin(Screen.SETTINGS) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text(vm.t("menu.tetapan")) }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = vm.t("menu.lawan"),
            color = Color(0xFFB9C9BC),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = vm.t("app.playmoney"),
            color = Color(0xFFB9C9BC),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = vm.t("menu.versi"),
            color = Color(0xFFB9C9BC),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
