package com.pokerku.app.engine

import kotlin.math.max
import kotlin.random.Random

/**
 * PokerKu — AI bot (pure Kotlin).
 *
 * 3 personaliti (nama tepat seperti yang ditetapkan Jean):
 * - KETAT (vic): main tangan kuat sahaja, jarang bluff.
 * - LONGGAR (Masta): main banyak tangan, kerap call.
 * - AGRESIF (Ayam): kerap raise, kerap bluff.
 *
 * Strategi heuristik: kekuatan tangan (formula Chen pre-flop;
 * kategori tangan + draw post-flop) dibandingkan dengan pot odds,
 * dengan sedikit rawak supaya bot tidak mudah dibaca.
 */

enum class BotStyle { KETAT, LONGGAR, AGRESIF }

object BotAi {

    fun decide(
        game: PokerGame,
        player: PlayerState,
        style: BotStyle,
        rng: Random = Random.Default
    ): PlayerAction {
        val strength = handStrength(game, player)
        val call = game.callAmount(player)
        val pot = game.totalPot()
        val potOdds = if (call > 0) call.toDouble() / (pot + call) else 0.0
        val canCheck = game.canCheck(player)
        val raiseRange = game.legalRaiseRange(player)

        val (foldThreshold, raiseThreshold, bluffProb) = when (style) {
            BotStyle.KETAT -> Triple(0.45, 0.75, 0.03)
            BotStyle.LONGGAR -> Triple(0.22, 0.68, 0.08)
            BotStyle.AGRESIF -> Triple(0.32, 0.60, 0.15)
        }

        // Bluff rawak sekali-sekala (lebih kerap untuk Agresif).
        val bluff = rng.nextDouble() < bluffProb

        if (canCheck) {
            if (strength >= raiseThreshold || bluff) {
                return raiseOrNull(game, player, strength, raiseRange, rng)
                    ?: PlayerAction.Check
            }
            // Agresif kadang-kadang raise dengan tangan sederhana.
            if (style == BotStyle.AGRESIF && strength >= 0.5 && rng.nextDouble() < 0.35) {
                return raiseOrNull(game, player, strength, raiseRange, rng)
                    ?: PlayerAction.Check
            }
            return PlayerAction.Check
        }

        // Menghadapi pertaruhan.
        if (strength < foldThreshold && !bluff) {
            // Call murah kadang-kadang berbaloi (pot odds baik).
            if (potOdds <= 0.12 && strength > foldThreshold * 0.6) {
                return PlayerAction.Call
            }
            if (style == BotStyle.LONGGAR && rng.nextDouble() < 0.15) {
                return PlayerAction.Call
            }
            return PlayerAction.Fold
        }
        if (strength >= raiseThreshold || bluff) {
            // Tangan sangat kuat + rawak = all-in sekali-sekala.
            if (strength >= 0.95 && rng.nextDouble() < 0.25) {
                return PlayerAction.AllIn
            }
            return raiseOrNull(game, player, strength, raiseRange, rng)
                ?: PlayerAction.Call
        }
        return PlayerAction.Call
    }

    private fun raiseOrNull(
        game: PokerGame,
        player: PlayerState,
        strength: Double,
        raiseRange: IntRange?,
        rng: Random
    ): PlayerAction? {
        if (raiseRange == null) return null
        val pot = game.totalPot()
        val base = game.currentBet + (pot * (0.6 + 0.4 * strength)).toInt()
        // Sedikit variasi saiz supaya tidak mudah dibaca.
        val jitter = (rng.nextDouble() * 0.2 - 0.1) * pot
        val total = (base + jitter).toInt().coerceIn(raiseRange)
        return PlayerAction.RaiseTo(total)
    }

    /** Kekuatan tangan 0.0–1.0 untuk pemain ini dalam keadaan semasa. */
    fun handStrength(game: PokerGame, player: PlayerState): Double {
        if (game.street == Street.PRE_FLOP) {
            return preflopScore(player.holeCards)
        }
        val cards = player.holeCards + game.communityCards
        val value = HandEvaluator.evaluateBest(cards)
        var strength = value.category.rank / 10.0
        // Kualiti kicker / rank tertinggi sedikit sebanyak.
        strength += (value.tiebreak.firstOrNull() ?: 2) / 14.0 * 0.05
        // Draw: flush draw / straight draw.
        strength += drawBonus(cards)
        return strength.coerceIn(0.0, 0.99)
    }

    /**
     * Skor Chen pre-flop (0–20) dinormalkan ke 0–1.
     * AA = 20, KK = 16, 72o ≈ 2.5.
     */
    fun preflopScore(hole: List<Card>): Double {
        require(hole.size == 2) { "Pre-flop perlu tepat 2 kad hole" }
        val sorted = hole.sortedByDescending { it.rank }
        val a = sorted[0]
        val b = sorted[1]
        var score = when (a.rank) {
            14 -> 10.0
            13 -> 8.0
            12 -> 7.0
            11 -> 6.0
            else -> a.rank / 2.0
        }
        if (a.rank == b.rank) {
            // Pair: gandakan, minimum 5 (22 = 5, AA = 20).
            score = max(5.0, score * 2)
        } else {
            if (a.suit == b.suit) score += 2.0
            val gap = a.rank - b.rank - 1
            score += when (gap) {
                0 -> 0.0
                1 -> -1.0
                2 -> -2.0
                3 -> -4.0
                else -> -5.0
            }
            // Bonus straight (kad berurutan, bukan Ace tinggi).
            if (gap <= 1 && a.rank < 14) score += 1.0
        }
        return (score.coerceIn(0.0, 20.0)) / 20.0
    }

    /**
     * Bonus draw post-flop: flush draw +0.12, open-ended straight draw
     * +0.12, gutshot +0.06.
     */
    fun drawBonus(cards: List<Card>): Double {
        var bonus = 0.0
        // Flush draw: tepat 4 kad sut yang sama.
        val suitCounts = cards.groupingBy { it.suit }.eachCount()
        if (suitCounts.values.any { it == 4 }) bonus += 0.12
        // Straight draw: semak jendela 4 kad berturutan.
        val ranks = cards.map { it.rank }.toSet()
        val ranksWithAceLow = if (14 in ranks) ranks + 1 else ranks
        var openEnded = false
        var gutshot = false
        for (low in 2..11) {
            val window = (low..low + 3).toSet()
            val have = window.count { it in ranksWithAceLow }
            if (have == 4) {
                // Boleh lengkap di kedua-dua hujung jika kad luar tiada.
                val canLow = (low - 1) !in ranksWithAceLow && low - 1 >= 1
                val canHigh = (low + 4) !in ranksWithAceLow && low + 4 <= 14
                if (canLow && canHigh) openEnded = true else gutshot = true
            } else if (have == 3) {
                gutshot = true
            }
        }
        if (openEnded) bonus += 0.12 else if (gutshot) bonus += 0.06
        return bonus
    }
}
