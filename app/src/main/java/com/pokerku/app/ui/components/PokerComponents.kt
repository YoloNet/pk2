package com.pokerku.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokerku.app.engine.Card
import com.pokerku.app.engine.PokerGame
import com.pokerku.app.ui.theme.Emas
import com.pokerku.app.ui.theme.EmasTerang
import com.pokerku.app.ui.theme.Felt
import com.pokerku.app.ui.theme.FeltTerang
import com.pokerku.app.ui.theme.KadHitam
import com.pokerku.app.ui.theme.KadMerah
import com.pokerku.app.ui.theme.Krim

/**
 * PokerKu — komponen meja (pure Compose, tiada imej).
 * Kad dilukis dengan teks + bentuk sahaja (pengajaran dari kuv2:
 * jangan rujuk drawable yang tidak wujud dalam repo).
 */

/** Satu kad permainan. [card] null = slot kosong. */
@Composable
fun PlayingCard(
    card: Card?,
    width: Dp,
    faceDown: Boolean = false,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val height = width * 1.42f
    val rankSize = (width.value * 0.34f).sp
    val centerSize = (width.value * 0.52f).sp
    Box(
        modifier = modifier
            .size(width, height)
            .clip(RoundedCornerShape(6.dp))
            .background(
                when {
                    card == null -> Color(0xFF0E4A37)
                    faceDown -> FeltTerang
                    else -> Color.White
                }
            )
            .border(
                width = if (highlighted) 2.5.dp else 1.dp,
                color = when {
                    highlighted -> Emas
                    card == null -> Color(0xFF1E6B4A)
                    faceDown -> Emas
                    else -> Color(0xFFD8D8D8)
                },
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (card == null) {
            // Slot kosong — garis putus-putus tidak disokong mudah; biar kosong.
        } else if (faceDown) {
            Text(
                text = "♠",
                color = EmasTerang,
                fontSize = centerSize,
                fontWeight = FontWeight.Bold
            )
        } else {
            val color = if (card.isRed) KadMerah else KadHitam
            if (width < 34.dp) {
                // Kad kecil (tempat duduk bot): rank + sut sahaja, ditengah.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = card.label,
                        color = color,
                        fontSize = rankSize,
                        fontWeight = FontWeight.Bold,
                        lineHeight = rankSize
                    )
                    Text(
                        text = card.suit.symbol,
                        color = color,
                        fontSize = rankSize,
                        lineHeight = rankSize
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().padding(3.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.TopStart),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = card.label,
                            color = color,
                            fontSize = rankSize,
                            fontWeight = FontWeight.Bold,
                            lineHeight = rankSize
                        )
                        Text(
                            text = card.suit.symbol,
                            color = color,
                            fontSize = rankSize,
                            lineHeight = rankSize
                        )
                    }
                    Text(
                        text = card.suit.symbol,
                        color = color,
                        fontSize = centerSize,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/** Baris kad (2 hole / 5 community) dengan slot kosong untuk kad belum dibuka. */
@Composable
fun CardRow(
    cards: List<Card?>,
    width: Dp,
    faceDown: Boolean = false,
    highlighted: Set<Card> = emptySet(),
    spacing: Dp = 6.dp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        cards.forEachIndexed { i, card ->
            if (i > 0) Spacer(modifier = Modifier.width(spacing))
            PlayingCard(
                card = card,
                width = width,
                faceDown = faceDown,
                highlighted = card != null && card in highlighted
            )
        }
    }
}

/** Lencana cip emas dengan jumlah. */
@Composable
fun ChipBadge(amount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3A2E05))
            .border(1.dp, Emas, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Emas)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = amount.toString(),
            color = EmasTerang,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Tempat duduk seorang pemain di meja. */
@Composable
fun PlayerSeat(
    p: PokerGame.PlayerSnapshot,
    styleLabel: String?,
    t: (String) -> String,
    modifier: Modifier = Modifier
) {
    val foldedAlpha = if (p.folded) 0.45f else 1f
    Column(
        modifier = modifier.alpha(foldedAlpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Kad hole (2 kad kecil; tertutup untuk bot sehingga showdown).
                CardRow(
                    cards = if (p.holeCards.isEmpty()) listOf(null, null) else p.holeCards,
                    width = 26.dp,
                    faceDown = !p.cardsRevealed,
                    spacing = 3.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (p.isBot) FeltTerang else Emas)
                        .border(
                            width = if (p.isTurn) 2.5.dp else 1.dp,
                            color = if (p.isTurn) EmasTerang else Color(0xFF1E6B4A),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = p.name.firstOrNull()?.uppercase() ?: "?",
                        color = if (p.isBot) Krim else Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = p.name,
                    color = Krim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (styleLabel != null) {
                    Text(
                        text = styleLabel,
                        color = Color(0xFFB9C9BC),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = p.stack.toString(),
                    color = EmasTerang,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (p.allIn) {
                    Text(
                        text = "ALL-IN",
                        color = Color(0xFFFF8A80),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (p.lastAction != null) {
                    Text(
                        text = t("action.${p.lastAction}"),
                        color = Color(0xFFB9C9BC),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (p.isDealer) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Emas),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (p.betInRound > 0) {
            Spacer(modifier = Modifier.height(3.dp))
            ChipBadge(p.betInRound)
        }
    }
}
