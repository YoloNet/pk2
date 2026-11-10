import com.pokerku.app.engine.BotAi
import com.pokerku.app.engine.BotStyle
import com.pokerku.app.engine.Card
import com.pokerku.app.engine.Deck
import com.pokerku.app.engine.HandCategory
import com.pokerku.app.engine.HandEvaluator
import com.pokerku.app.engine.PlayerAction
import com.pokerku.app.engine.PokerGame
import com.pokerku.app.engine.Street
import com.pokerku.app.engine.Suit
import kotlin.random.Random

var passed = 0
var failed = 0

fun check(cond: Boolean, msg: String) {
    if (cond) {
        passed++
    } else {
        failed++
        println("GAGAL: $msg")
    }
}

fun c(rank: Int, suit: Suit) = Card(rank, suit)

fun testEvaluator() {
    println("== HandEvaluator ==")
    // Royal flush
    val royal = HandEvaluator.evaluateBest(
        listOf(c(14, Suit.SPADES), c(13, Suit.SPADES), c(12, Suit.SPADES), c(11, Suit.SPADES), c(10, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS))
    )
    check(royal.category == HandCategory.ROYAL_FLUSH, "royal flush dikesan")
    // Straight flush lawan quads — straight flush menang
    val sf = HandEvaluator.evaluateBest(
        listOf(c(9, Suit.HEARTS), c(8, Suit.HEARTS), c(7, Suit.HEARTS), c(6, Suit.HEARTS), c(5, Suit.HEARTS), c(2, Suit.CLUBS), c(3, Suit.DIAMONDS))
    )
    val quads = HandEvaluator.evaluateBest(
        listOf(c(13, Suit.SPADES), c(13, Suit.HEARTS), c(13, Suit.DIAMONDS), c(13, Suit.CLUBS), c(14, Suit.HEARTS), c(2, Suit.CLUBS), c(3, Suit.DIAMONDS))
    )
    check(sf > quads, "straight flush > quads")
    check(sf.category == HandCategory.STRAIGHT_FLUSH && sf.tiebreak == listOf(9), "straight flush 9 tinggi")
    check(quads.category == HandCategory.FOUR_OF_A_KIND && quads.tiebreak[0] == 13, "quads K")
    // Wheel straight A-2-3-4-5
    val wheel = HandEvaluator.evaluateBest(
        listOf(c(14, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS), c(4, Suit.CLUBS), c(5, Suit.SPADES), c(13, Suit.HEARTS), c(12, Suit.DIAMONDS))
    )
    check(wheel.category == HandCategory.STRAIGHT && wheel.tiebreak == listOf(5), "wheel straight high=5")
    // 6-high straight kalahkan wheel
    val sixHigh = HandEvaluator.evaluateBest(
        listOf(c(6, Suit.SPADES), c(5, Suit.HEARTS), c(4, Suit.DIAMONDS), c(3, Suit.CLUBS), c(2, Suit.SPADES), c(13, Suit.HEARTS), c(12, Suit.DIAMONDS))
    )
    check(sixHigh > wheel, "6-high straight > wheel")
    // Full house tiebreak: QQQ88 > QQQ77
    val fh1 = HandEvaluator.evaluateBest(
        listOf(c(12, Suit.SPADES), c(12, Suit.HEARTS), c(12, Suit.DIAMONDS), c(8, Suit.CLUBS), c(8, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS))
    )
    val fh2 = HandEvaluator.evaluateBest(
        listOf(c(12, Suit.SPADES), c(12, Suit.HEARTS), c(12, Suit.DIAMONDS), c(7, Suit.CLUBS), c(7, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS))
    )
    check(fh1.category == HandCategory.FULL_HOUSE && fh1 > fh2, "full house pair 8 > pair 7")
    // Two pair kicker
    val tp1 = HandEvaluator.evaluateBest(
        listOf(c(11, Suit.SPADES), c(11, Suit.HEARTS), c(5, Suit.DIAMONDS), c(5, Suit.CLUBS), c(9, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS))
    )
    val tp2 = HandEvaluator.evaluateBest(
        listOf(c(11, Suit.SPADES), c(11, Suit.HEARTS), c(5, Suit.DIAMONDS), c(5, Suit.CLUBS), c(8, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS))
    )
    check(tp1.category == HandCategory.TWO_PAIR && tp1 > tp2, "two pair kicker 9 > 8")
    // Flush tiebreak
    val fl1 = HandEvaluator.evaluateBest(
        listOf(c(14, Suit.DIAMONDS), c(11, Suit.DIAMONDS), c(8, Suit.DIAMONDS), c(5, Suit.DIAMONDS), c(2, Suit.DIAMONDS), c(13, Suit.HEARTS), c(12, Suit.CLUBS))
    )
    val fl2 = HandEvaluator.evaluateBest(
        listOf(c(13, Suit.DIAMONDS), c(11, Suit.DIAMONDS), c(8, Suit.DIAMONDS), c(5, Suit.DIAMONDS), c(2, Suit.DIAMONDS), c(14, Suit.HEARTS), c(12, Suit.CLUBS))
    )
    check(fl1.category == HandCategory.FLUSH && fl1 > fl2, "flush A tinggi > K tinggi")
    // Pair kicker
    val pr1 = HandEvaluator.evaluateBest(
        listOf(c(14, Suit.SPADES), c(14, Suit.HEARTS), c(13, Suit.DIAMONDS), c(12, Suit.CLUBS), c(11, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS))
    )
    val pr2 = HandEvaluator.evaluateBest(
        listOf(c(14, Suit.SPADES), c(14, Suit.HEARTS), c(13, Suit.DIAMONDS), c(12, Suit.CLUBS), c(10, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS))
    )
    check(pr1 > pr2, "pair AA kicker J > 10")
    // Trips
    val trips = HandEvaluator.evaluateBest(
        listOf(c(8, Suit.SPADES), c(8, Suit.HEARTS), c(8, Suit.DIAMONDS), c(14, Suit.CLUBS), c(4, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS))
    )
    check(trips.category == HandCategory.THREE_OF_A_KIND, "trips dikesan")
    // High card
    val hc = HandEvaluator.evaluateBest(
        listOf(c(14, Suit.DIAMONDS), c(12, Suit.CLUBS), c(9, Suit.HEARTS), c(6, Suit.SPADES), c(3, Suit.DIAMONDS), c(2, Suit.HEARTS), c(4, Suit.CLUBS))
    )
    check(hc.category == HandCategory.HIGH_CARD, "high card dikesan")
    // 7 kad terbaik: royal dari board spade
    val best7 = HandEvaluator.evaluateBest(
        listOf(c(14, Suit.SPADES), c(13, Suit.SPADES), c(12, Suit.SPADES), c(11, Suit.SPADES), c(10, Suit.SPADES), c(9, Suit.SPADES), c(2, Suit.DIAMONDS))
    )
    check(best7.category == HandCategory.ROYAL_FLUSH, "7 kad: royal dipilih")
    check(best7.bestCards.size == 5, "bestCards tepat 5 kad")
    // Seri: board royal flush — kedua-dua pemain sama nilai
    val split1 = HandEvaluator.evaluateBest(
        listOf(c(14, Suit.SPADES), c(13, Suit.SPADES), c(12, Suit.SPADES), c(11, Suit.SPADES), c(10, Suit.SPADES), c(2, Suit.HEARTS), c(3, Suit.DIAMONDS))
    )
    val split2 = HandEvaluator.evaluateBest(
        listOf(c(14, Suit.SPADES), c(13, Suit.SPADES), c(12, Suit.SPADES), c(11, Suit.SPADES), c(10, Suit.SPADES), c(9, Suit.CLUBS), c(8, Suit.DIAMONDS))
    )
    check(split1.compareTo(split2) == 0, "board royal = seri")
    // Straight biasa (bukan flush)
    val st = HandEvaluator.evaluateBest(
        listOf(c(10, Suit.SPADES), c(9, Suit.HEARTS), c(8, Suit.DIAMONDS), c(7, Suit.CLUBS), c(6, Suit.HEARTS), c(2, Suit.SPADES), c(3, Suit.DIAMONDS))
    )
    check(st.category == HandCategory.STRAIGHT && st.tiebreak == listOf(10), "straight 10 tinggi")
}

fun testDeck() {
    println("== Deck ==")
    val deck = Deck(42L)
    val seen = mutableSetOf<Card>()
    repeat(52) { seen.add(deck.deal()) }
    check(seen.size == 52, "dek 52 kad unik")
    check(deck.remaining() == 0, "dek habis selepas 52 deal")
    val d1 = Deck(7L)
    val d2 = Deck(7L)
    check(d1.deal() == d2.deal() && d1.deal() == d2.deal(), "seed sama = susunan sama")
}

fun testSidePots() {
    println("== Side pots ==")
    val g = PokerGame()
    // Sumbangan: P0 fold 100, P1 300, P2 300, P3 100
    g.players[0].betInHand = 100; g.players[0].folded = true
    g.players[1].betInHand = 300
    g.players[2].betInHand = 300
    g.players[3].betInHand = 100
    val pots = g.buildSidePots()
    check(pots.size == 2, "2 pot dibina, dapat ${pots.size}")
    check(pots[0].amount == 400 && pots[0].eligible == listOf(1, 2, 3), "main pot 400, layak 1,2,3 — dapat ${pots[0]}")
    check(pots[1].amount == 400 && pots[1].eligible == listOf(1, 2), "side pot 400, layak 1,2 — dapat ${pots[1]}")
    check(pots.sumOf { it.amount } == 800, "jumlah pot = jumlah sumbangan")
    // Semua sama: satu pot sahaja
    val g2 = PokerGame()
    g2.players.forEach { it.betInHand = 50 }
    val pots2 = g2.buildSidePots()
    check(pots2.size == 1 && pots2[0].amount == 200, "sumbangan sama = 1 pot 200")
}

fun testPreflopScore() {
    println("== Preflop score ==")
    val aa = BotAi.preflopScore(listOf(c(14, Suit.SPADES), c(14, Suit.HEARTS)))
    val kk = BotAi.preflopScore(listOf(c(13, Suit.SPADES), c(13, Suit.HEARTS)))
    val low = BotAi.preflopScore(listOf(c(7, Suit.CLUBS), c(2, Suit.DIAMONDS)))
    check(aa == 1.0, "AA = 1.0, dapat $aa")
    check(aa > kk && kk > low, "AA > KK > 72o")
    val aks = BotAi.preflopScore(listOf(c(14, Suit.SPADES), c(13, Suit.SPADES)))
    val ako = BotAi.preflopScore(listOf(c(14, Suit.SPADES), c(13, Suit.HEARTS)))
    check(aks > ako, "AK suited > AK offsuit")
}

fun humanPolicy(g: PokerGame): PlayerAction {
    val p = g.currentPlayer()
    val call = g.callAmount(p)
    val strength = BotAi.handStrength(g, p)
    val range = g.legalRaiseRange(p)
    return when {
        strength >= 0.75 && range != null ->
            PlayerAction.RaiseTo(g.potSizeRaiseTotal(p).coerceIn(range))
        g.canCheck(p) -> PlayerAction.Check
        call > p.stack / 2 -> PlayerAction.Fold
        else -> PlayerAction.Call
    }
}

fun assertLegal(g: PokerGame, action: PlayerAction, who: String) {
    val p = g.currentPlayer()
    when (action) {
        is PlayerAction.Fold -> {}
        is PlayerAction.Check -> check(g.canCheck(p), "$who: check hanya bila boleh check")
        is PlayerAction.Call -> check(g.callAmount(p) > 0, "$who: call mesti ada amaun")
        is PlayerAction.RaiseTo -> {
            val range = g.legalRaiseRange(p)
            val max = p.betInRound + p.stack
            check(
                (range != null && action.totalBet in range) || action.totalBet == max,
                "$who: raise ${action.totalBet} di luar julat sah $range / max $max"
            )
        }
        is PlayerAction.AllIn -> check(p.stack > 0, "$who: all-in perlukan chip")
    }
}

fun testSimulation() {
    println("== Simulasi 300 tangan penuh ==")
    var showdowns = 0
    var foldWins = 0
    var raises = 0
    var allIns = 0
    var maxActions = 0
    val rng = Random(1234L)
    repeat(300) { handIdx ->
        val g = PokerGame(deckSeed = 1000L + handIdx)
        // Top-up pemain bust (seperti ViewModel buat).
        for (p in g.players) if (p.stack <= 0) p.stack = 1000
        g.startHand()
        check(g.players.all { it.holeCards.size == 2 }, "setiap pemain dapat 2 kad")
        check(g.communityCards.isEmpty(), "community kosong pre-flop")
        check(g.currentBet == 20, "currentBet = BB 20")
        var actions = 0
        while (g.street != Street.HAND_OVER) {
            val current = g.currentPlayer()
            val action = if (current.isBot) {
                val style = when (current.id) {
                    1 -> BotStyle.KETAT
                    2 -> BotStyle.LONGGAR
                    else -> BotStyle.AGRESIF
                }
                BotAi.decide(g, current, style, rng)
            } else {
                humanPolicy(g)
            }
            assertLegal(g, action, "tangan $handIdx pemain ${current.id}")
            if (action is PlayerAction.RaiseTo) raises++
            if (action is PlayerAction.AllIn) allIns++
            val ok = g.applyAction(current.id, action)
            check(ok, "tangan $handIdx: aksi ${current.name} diterima")
            actions++
            if (actions > 500) {
                check(false, "tangan $handIdx: gelung aksi melebihi 500 — kemungkinan gelung tak berpenghujung")
                break
            }
        }
        if (actions > maxActions) maxActions = actions
        // Konservasi chip: jumlah semua stack = 4000 (pot sudah diagih).
        val total = g.players.sumOf { it.stack }
        check(total == 4000, "tangan $handIdx: jumlah chip kekal 4000, dapat $total")
        check(g.players.all { it.stack >= 0 }, "tangan $handIdx: tiada stack negatif")
        check(g.totalPot() == 0, "tangan $handIdx: pot dikosongkan selepas tamat")
        if (g.lastShowdown != null) {
            showdowns++
            val won = g.lastShowdown!!.winnings.values.sum()
            check(won == g.lastPot, "tangan $handIdx: jumlah kemenangan = pot (${g.lastPot})")
            check(g.communityCards.size == 5, "tangan $handIdx: 5 community di showdown")
        } else {
            foldWins++
            check(g.foldWinnerId != null, "tangan $handIdx: fold win ada pemenang")
        }
        // Dealer berpusing.
        if (handIdx > 0) {
            // (disemak longgar — dealer berubah dari tangan sebelumnya disahkan melalui log)
        }
        // Top-up untuk tangan seterusnya (simulasi berbilang tangan pada instance sama).
        // (Instance baharu setiap lelaran; top-up di atas sudah cukup.)
    }
    println("Showdowns: $showdowns, Fold wins: $foldWins, Raises: $raises, All-ins: $allIns, Max aksi/tangan: $maxActions")
    check(showdowns > 50, "cukup showdown berlaku ($showdowns)")
    check(foldWins > 0, "sekurang-kurangnya satu fold-win berlaku ($foldWins)")
    check(raises > 50, "cukup raise berlaku ($raises)")
    check(allIns > 0, "sekurang-kurangnya satu all-in berlaku ($allIns)")
}

fun testDealerRotation() {
    println("== Dealer rotation ==")
    val g = PokerGame(deckSeed = 5L)
    val dealers = mutableListOf<Int>()
    repeat(8) { i ->
        for (p in g.players) if (p.stack <= 0) p.stack = 1000
        g.startHand()
        dealers.add(g.dealerIndex)
        // Mainkan tangan sampai habis dengan dasar mudah.
        var guard = 0
        while (g.street != Street.HAND_OVER && guard < 500) {
            val cur = g.currentPlayer()
            val a = if (cur.isBot) BotAi.decide(g, cur, BotStyle.KETAT, Random(i.toLong())) else humanPolicy(g)
            g.applyAction(cur.id, a)
            guard++
        }
    }
    check(dealers == listOf(0, 1, 2, 3, 0, 1, 2, 3), "dealer berpusing 0,1,2,3,0,1,2,3 — dapat $dealers")
}

fun testFoldWin() {
    println("== Fold win (deterministik) ==")
    val g = PokerGame(deckSeed = 99L)
    g.startHand()
    // Dealer 0, SB 1 (10), BB 2 (20), UTG 3 bertindak dahulu.
    check(g.actionIndex == 3, "UTG ialah pemain 3, dapat ${g.actionIndex}")
    val okRaise = g.applyAction(3, PlayerAction.RaiseTo(60))
    check(okRaise, "UTG raise ke 60 diterima")
    check(g.currentBet == 60, "currentBet 60 selepas raise")
    // Semua lawan fold satu demi satu: 0, 1, 2.
    check(g.applyAction(0, PlayerAction.Fold), "pemain 0 fold")
    check(g.applyAction(1, PlayerAction.Fold), "pemain 1 fold")
    check(g.applyAction(2, PlayerAction.Fold), "pemain 2 (BB) fold")
    check(g.street == Street.HAND_OVER, "tangan tamat selepas semua fold")
    check(g.foldWinnerId == 3, "pemenang fold ialah pemain 3")
    // Pot = 60 (UTG) + 10 (SB) + 20 (BB) = 90; UTG: 1000-60+90 = 1030.
    check(g.players[3].stack == 1030, "UTG 1030 selepas menang pot 90, dapat ${g.players[3].stack}")
    check(g.players[0].stack == 1000, "pemain 0 kekal 1000")
    check(g.players[1].stack == 990, "SB 990 selepas blind 10")
    check(g.players[2].stack == 980, "BB 980 selepas blind 20")
    check(g.totalPot() == 0, "pot dikosongkan")
    check(g.lastPot == 90, "lastPot 90")
    // Aksi selepas tangan tamat ditolak.
    check(!g.applyAction(3, PlayerAction.Check), "aksi selepas HAND_OVER ditolak")
}

fun main() {
    testDeck()
    testEvaluator()
    testSidePots()
    testPreflopScore()
    testDealerRotation()
    testFoldWin()
    testSimulation()
    println()
    println("LULUS: $passed, GAGAL: $failed")
    if (failed > 0) {
        kotlin.system.exitProcess(1)
    }
}
