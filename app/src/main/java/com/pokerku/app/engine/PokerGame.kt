package com.pokerku.app.engine

/**
 * PokerKu — enjin Texas Hold'em 4 pemain (pure Kotlin).
 *
 * Peraturan: blinds 10/20, stack mula 1,000, dealer button berpusing,
 * pusingan pre-flop / flop / turn / river, aksi check / call / bet /
 * raise / fold / all-in, side pot bila ada all-in, showdown 7 kad.
 *
 * Rekaan: kelas ini memegang keadaan boleh-ubah (mutable) permainan.
 * UI (ViewModel) membaca snapshot melalui [snapshot] dan menghantar
 * aksi melalui [applyAction]. Semua peraturan disahkan di sini, bukan di UI.
 */

enum class Street { PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN, HAND_OVER }

sealed class PlayerAction {
    object Fold : PlayerAction()
    object Check : PlayerAction()
    /** Panggil jumlah semasa (amaun dikira oleh enjin). */
    object Call : PlayerAction()
    /** Naikkan supaya jumlah pertaruhan pusingan ini menjadi [totalBet]. */
    data class RaiseTo(val totalBet: Int) : PlayerAction()
    /** All-in dengan semua baki chip. */
    object AllIn : PlayerAction()
}

data class PlayerState(
    val id: Int,
    val name: String,
    val isBot: Boolean,
    var stack: Int,
    var betInHand: Int = 0,
    var betInRound: Int = 0,
    var holeCards: List<Card> = emptyList(),
    var folded: Boolean = false,
    var allIn: Boolean = false,
    var isDealer: Boolean = false,
    var lastAction: String? = null
)

/** Satu pot (utama atau side) beserta pemain yang layak memenanginya. */
data class Pot(val amount: Int, val eligible: List<Int>)

/** Keputusan showdown — digunakan oleh UI untuk banner pemenang. */
data class ShowdownOutcome(
    val pots: List<Pot>,
    /** id pemain -> nilai tangan terbaik (hanya yang tidak fold). */
    val handValues: Map<Int, HandValue>,
    /** id pemain -> jumlah chip yang dimenangi. */
    val winnings: Map<Int, Int>,
    /** Pemenang setiap pot (id pemain). */
    val potWinners: List<List<Int>>
)

class PokerGame(
    val playerNames: List<String> = listOf("Anda", "vic", "Masta", "Ayam"),
    val botFlags: List<Boolean> = listOf(false, true, true, true),
    val smallBlind: Int = 10,
    val bigBlind: Int = 20,
    val startingStack: Int = 1000,
    private val deckSeed: Long? = null
) {
    val players: List<PlayerState> = playerNames.indices.map { i ->
        PlayerState(id = i, name = playerNames[i], isBot = botFlags[i], stack = startingStack)
    }

    var dealerIndex: Int = 0
        private set
    var street: Street = Street.PRE_FLOP
        private set
    var communityCards: List<Card> = emptyList()
        private set
    var currentBet: Int = 0
        private set
    /** Saiz kenaikan terakhir (min raise untuk kenaikan seterusnya). */
    var minRaise: Int = bigBlind
        private set
    var actionIndex: Int = 0
        private set
    var handNumber: Int = 0
        private set
    var lastShowdown: ShowdownOutcome? = null
        private set
    /** Saiz pot tangan terakhir yang tamat (untuk statistik & banner). */
    var lastPot: Int = 0
        private set
    /** Pemenang tangan yang tamat kerana semua lawan fold (null jika showdown). */
    var foldWinnerId: Int? = null
        private set

    private var deck: Deck = Deck(deckSeed)
    /** Pemain yang sudah bertindak sejak kenaikan terakhir / mula pusingan. */
    private val acted: MutableSet<Int> = mutableSetOf()
    val log: MutableList<String> = mutableListOf()

    fun totalPot(): Int = players.sumOf { it.betInHand }

    fun currentPlayer(): PlayerState = players[actionIndex]

    /** Jumlah yang perlu dibayar untuk call (0 jika boleh check). */
    fun callAmount(player: PlayerState): Int =
        (currentBet - player.betInRound).coerceIn(0, player.stack)

    fun canCheck(player: PlayerState): Boolean = player.betInRound == currentBet

    /**
     * Julat jumlah pertaruhan (total betInRound) yang sah untuk raise.
     * null = tidak boleh raise (hanya boleh call/check/fold/all-in).
     */
    fun legalRaiseRange(player: PlayerState): IntRange? {
        val call = callAmount(player)
        val afterCall = player.stack - call
        if (afterCall <= 0) return null
        val minTotal = currentBet + minRaise
        val maxTotal = player.betInRound + player.stack
        if (minTotal > maxTotal) {
            // Hanya all-in sahaja yang mungkin (kurang dari min raise).
            return null
        }
        return minTotal..maxTotal
    }

    /** Jumlah pertaruhan untuk pot-sized raise (jumlah total betInRound). */
    fun potSizeRaiseTotal(player: PlayerState): Int {
        val call = callAmount(player)
        val potAfterCall = totalPot() + call
        val total = currentBet + potAfterCall
        val maxTotal = player.betInRound + player.stack
        return total.coerceIn(currentBet + minRaise, maxTotal)
    }

    // ------------------------------------------------------------------
    // Mula tangan
    // ------------------------------------------------------------------

    /** Mulakan tangan baharu. Dealer berpusing (tangan pertama: pemain 0). */
    fun startHand() {
        // Reset keadaan tangan DAHULU — dealer mesti berpusing kepada pemain
        // seterusnya tanpa mengira siapa fold tangan lepas.
        for (p in players) {
            p.betInHand = 0
            p.betInRound = 0
            p.holeCards = emptyList()
            p.folded = false
            p.allIn = false
            p.isDealer = false
            p.lastAction = null
        }
        if (handNumber > 0) {
            dealerIndex = nextActiveIndex(dealerIndex, includeAllIn = true)
        }
        handNumber++
        deck = Deck(deckSeed?.let { it + handNumber })
        street = Street.PRE_FLOP
        communityCards = emptyList()
        currentBet = 0
        minRaise = bigBlind
        lastShowdown = null
        foldWinnerId = null
        acted.clear()
        log.clear()

        players[dealerIndex].isDealer = true

        // Deal: 2 kad setiap pemain, satu demi satu (seperti dealer sebenar).
        repeat(2) {
            for (p in players) p.holeCards = p.holeCards + deck.deal()
        }

        // Blinds — pemain selepas dealer (langkau pemain yang bust, stack 0).
        val sbIndex = nextActiveIndex(dealerIndex, includeAllIn = true)
        val bbIndex = nextActiveIndex(sbIndex, includeAllIn = true)
        postBlind(players[sbIndex], smallBlind)
        postBlind(players[bbIndex], bigBlind)
        currentBet = bigBlind

        // UTG = pemain selepas BB.
        actionIndex = nextActiveIndex(bbIndex, includeAllIn = false)
        log.add("Tangan #$handNumber — dealer: ${players[dealerIndex].name}")
    }

    private fun postBlind(player: PlayerState, amount: Int) {
        val paid = minOf(amount, player.stack)
        player.stack -= paid
        player.betInHand += paid
        player.betInRound += paid
        if (player.stack == 0) player.allIn = true
    }

    /** Indeks pemain aktif seterusnya selepas [from] (tidak fold, stack > 0). */
    private fun nextActiveIndex(from: Int, includeAllIn: Boolean): Int {
        for (i in 1..players.size) {
            val idx = (from + i) % players.size
            val p = players[idx]
            if (!p.folded && p.stack > 0 && (includeAllIn || !p.allIn)) return idx
        }
        return (from + 1) % players.size
    }

    /** Pemain yang masih boleh bertindak (tidak fold, tidak all-in, ada chip). */
    fun canActPlayers(): List<PlayerState> =
        players.filter { !it.folded && !it.allIn && it.stack > 0 }

    // ------------------------------------------------------------------
    // Aksi
    // ------------------------------------------------------------------

    /** Hantar aksi pemain semasa. Kembalikan false jika aksi tidak sah. */
    fun applyAction(playerId: Int, action: PlayerAction): Boolean {
        if (street == Street.SHOWDOWN || street == Street.HAND_OVER) return false
        val player = players[playerId]
        if (playerId != actionIndex) return false
        if (player.folded || player.allIn || player.stack <= 0) return false

        when (action) {
            is PlayerAction.Fold -> {
                player.folded = true
                player.lastAction = "fold"
                log.add("${player.name} fold")
            }
            is PlayerAction.Check -> {
                if (!canCheck(player)) return false
                player.lastAction = "check"
                log.add("${player.name} check")
            }
            is PlayerAction.Call -> {
                val amount = callAmount(player)
                if (amount <= 0) return false
                payBet(player, amount)
                player.lastAction = if (player.allIn) "allin" else "call"
                log.add("${player.name} call $amount")
            }
            is PlayerAction.RaiseTo -> {
                val range = legalRaiseRange(player)
                val target = action.totalBet.coerceIn(
                    range?.first ?: (player.betInRound + player.stack),
                    player.betInRound + player.stack
                )
                if (range != null && target < range.first) return false
                val pay = target - player.betInRound
                if (pay <= 0 || pay > player.stack) return false
                // Kenaikan sebenar (all-in bawah min raise tidak ubah minRaise).
                val raiseSize = target - currentBet
                payBet(player, pay)
                if (raiseSize >= minRaise) {
                    minRaise = raiseSize
                    acted.clear()
                }
                currentBet = target
                player.lastAction = if (player.allIn) "allin" else "raise"
                log.add("${player.name} raise ke $target")
            }
            is PlayerAction.AllIn -> {
                val pay = player.stack
                if (pay <= 0) return false
                val target = player.betInRound + pay
                val raiseSize = target - currentBet
                payBet(player, pay)
                if (target > currentBet) {
                    if (raiseSize >= minRaise) {
                        minRaise = raiseSize
                        acted.clear()
                    }
                    currentBet = target
                }
                player.lastAction = "allin"
                log.add("${player.name} ALL-IN $target")
            }
        }

        acted.add(playerId)

        // Hanya seorang tinggal (semua lawan fold) — tangan tamat.
        val remaining = players.filter { !it.folded }
        if (remaining.size == 1) {
            endByFold(remaining[0])
            return true
        }

        if (bettingRoundComplete()) {
            advanceStreet()
        } else {
            actionIndex = nextToAct(actionIndex)
        }
        return true
    }

    private fun payBet(player: PlayerState, amount: Int) {
        player.stack -= amount
        player.betInHand += amount
        player.betInRound += amount
        if (player.stack == 0) player.allIn = true
    }

    private fun nextToAct(from: Int): Int {
        for (i in 1..players.size) {
            val idx = (from + i) % players.size
            val p = players[idx]
            if (!p.folded && !p.allIn && p.stack > 0) return idx
        }
        return from
    }

    /** Pusingan pertaruhan tamat bila semua yang boleh bertindak sudah bertindak dan menyamai bet semasa. */
    private fun bettingRoundComplete(): Boolean {
        val canAct = canActPlayers()
        if (canAct.isEmpty()) return true
        return canAct.all { it.id in acted && it.betInRound == currentBet }
    }

    // ------------------------------------------------------------------
    // Pusingan jalan (street)
    // ------------------------------------------------------------------

    private fun advanceStreet() {
        for (p in players) p.betInRound = 0
        currentBet = 0
        minRaise = bigBlind
        acted.clear()

        when (street) {
            Street.PRE_FLOP -> {
                street = Street.FLOP
                repeat(3) { communityCards = communityCards + deck.deal() }
                log.add("Flop: ${communityCards.joinToString(" ")}")
            }
            Street.FLOP -> {
                street = Street.TURN
                communityCards = communityCards + deck.deal()
                log.add("Turn: ${communityCards.last()}")
            }
            Street.TURN -> {
                street = Street.RIVER
                communityCards = communityCards + deck.deal()
                log.add("River: ${communityCards.last()}")
            }
            Street.RIVER -> {
                doShowdown()
                return
            }
            else -> return
        }

        // Jika kurang 2 pemain boleh bertindak, deal baki jalan automatik.
        if (canActPlayers().size < 2) {
            dealOutAndShowdown()
            return
        }
        actionIndex = firstToActAfterDealer()
    }

    /** Pemain pertama yang boleh bertindak selepas dealer. */
    private fun firstToActAfterDealer(): Int {
        for (i in 1..players.size) {
            val idx = (dealerIndex + i) % players.size
            val p = players[idx]
            if (!p.folded && !p.allIn && p.stack > 0) return idx
        }
        return dealerIndex
    }

    /** Deal semua community card yang tinggal, kemudian showdown. */
    private fun dealOutAndShowdown() {
        while (communityCards.size < 5) {
            communityCards = communityCards + deck.deal()
        }
        street = Street.RIVER
        doShowdown()
    }

    // ------------------------------------------------------------------
    // Penamat tangan
    // ------------------------------------------------------------------

    private fun endByFold(winner: PlayerState) {
        val pot = totalPot()
        lastPot = pot
        winner.stack += pot
        for (p in players) p.betInHand = 0
        foldWinnerId = winner.id
        street = Street.HAND_OVER
        log.add("${winner.name} menang $pot (semua lawan fold)")
    }

    private fun doShowdown() {
        street = Street.SHOWDOWN
        val active = players.filter { !it.folded }
        val values = active.associate { p ->
            p.id to HandEvaluator.evaluateBest(p.holeCards + communityCards)
        }
        val pots = buildSidePots()
        val winnings = mutableMapOf<Int, Int>()
        val potWinners = mutableListOf<List<Int>>()
        for (pot in pots) {
            val contenders = pot.eligible.mapNotNull { id -> values[id]?.let { id to it } }
            if (contenders.isEmpty()) continue
            val best = contenders.maxOf { it.second }
            val winners = contenders.filter { it.second == best }.map { it.first }
            // Bahagi sama rata; baki chip kepada pemenang paling hampir kiri dealer.
            val share = pot.amount / winners.size
            var remainder = pot.amount % winners.size
            val ordered = winners.sortedBy { (it - dealerIndex + players.size) % players.size }
            for (id in ordered) {
                var won = share
                if (remainder > 0) {
                    won += 1
                    remainder--
                }
                winnings[id] = (winnings[id] ?: 0) + won
            }
            potWinners.add(winners)
        }
        for ((id, won) in winnings) {
            players[id].stack += won
        }
        lastPot = pots.sumOf { it.amount }
        for (p in players) p.betInHand = 0
        lastShowdown = ShowdownOutcome(
            pots = pots,
            handValues = values,
            winnings = winnings,
            potWinners = potWinners
        )
        street = Street.HAND_OVER
        val names = winnings.entries.joinToString(", ") { (id, won) ->
            "${players[id].name} +$won"
        }
        log.add("Showdown — $names")
    }

    /**
     * Bina side pot daripada jumlah pertaruhan setiap pemain.
     * Kaedah lapisan (level): ambil pertaruhan terkecil yang masih berbaki,
     * bentuk satu pot daripada lapisan itu, ulang sehingga habis.
     */
    fun buildSidePots(): List<Pot> {
        val remaining = players.map { it.betInHand }.toMutableList()
        val pots = mutableListOf<Pot>()
        while (remaining.any { it > 0 }) {
            val contributors = remaining.indices.filter { remaining[it] > 0 }
            val level = contributors.minOf { remaining[it] }
            val amount = level * contributors.size
            val eligible = contributors.filter { !players[it].folded }
            if (eligible.isNotEmpty()) {
                pots.add(Pot(amount, eligible))
            }
            for (i in contributors) remaining[i] -= level
        }
        return pots
    }

    // ------------------------------------------------------------------
    // Snapshot untuk UI
    // ------------------------------------------------------------------

    data class PlayerSnapshot(
        val id: Int,
        val name: String,
        val isBot: Boolean,
        val stack: Int,
        val betInRound: Int,
        val holeCards: List<Card>,
        val cardsRevealed: Boolean,
        val folded: Boolean,
        val allIn: Boolean,
        val isDealer: Boolean,
        val lastAction: String?,
        val isTurn: Boolean,
        val handValue: HandValue?
    )

    data class Snapshot(
        val street: Street,
        val communityCards: List<Card>,
        val totalPot: Int,
        val currentBet: Int,
        val actionIndex: Int,
        val handNumber: Int,
        val players: List<PlayerSnapshot>,
        val showdown: ShowdownOutcome?,
        val foldWinnerId: Int?,
        val log: List<String>
    )

    /** Salinan keadaan semasa untuk UI. Kad bot disembunyikan sehingga showdown. */
    fun snapshot(): Snapshot {
        val revealed = street == Street.HAND_OVER || street == Street.SHOWDOWN
        return Snapshot(
            street = street,
            communityCards = communityCards,
            totalPot = totalPot(),
            currentBet = currentBet,
            actionIndex = actionIndex,
            handNumber = handNumber,
            players = players.map { p ->
                PlayerSnapshot(
                    id = p.id,
                    name = p.name,
                    isBot = p.isBot,
                    stack = p.stack,
                    betInRound = p.betInRound,
                    holeCards = if (p.isBot && !revealed) emptyList() else p.holeCards,
                    cardsRevealed = !p.isBot || revealed,
                    folded = p.folded,
                    allIn = p.allIn,
                    isDealer = p.isDealer,
                    lastAction = p.lastAction,
                    isTurn = p.id == actionIndex &&
                        street != Street.HAND_OVER && street != Street.SHOWDOWN,
                    handValue = lastShowdown?.handValues?.get(p.id)
                )
            },
            showdown = lastShowdown,
            foldWinnerId = foldWinnerId,
            log = log.toList()
        )
    }
}
