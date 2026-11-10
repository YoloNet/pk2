package com.pokerku.app.engine

/**
 * PokerKu — penilai tangan Texas Hold'em (pure Kotlin).
 *
 * Menilai tangan terbaik 5 kad daripada 5–7 kad (hole + community).
 * Kaedah: nilai kesemua kombinasi C(n,5) dan ambil yang tertinggi —
 * mudah dibuktikan betul dan cukup pantas (maks 21 kombinasi).
 */

enum class HandCategory(val rank: Int, val key: String) {
    HIGH_CARD(1, "hand.highcard"),
    PAIR(2, "hand.pair"),
    TWO_PAIR(3, "hand.twopair"),
    THREE_OF_A_KIND(4, "hand.trips"),
    STRAIGHT(5, "hand.straight"),
    FLUSH(6, "hand.flush"),
    FULL_HOUSE(7, "hand.fullhouse"),
    FOUR_OF_A_KIND(8, "hand.quads"),
    STRAIGHT_FLUSH(9, "hand.straightflush"),
    ROYAL_FLUSH(10, "hand.royalflush")
}

/**
 * Nilai tangan yang boleh dibandingkan.
 * [tiebreak] ialah senarai rank mengikut tertib kepentingan
 * (cth. untuk two pair: [pair tinggi, pair rendah, kicker]).
 * [bestCards] ialah 5 kad yang membentuk tangan terbaik
 * (digunakan untuk highlight kad pemenang di UI).
 */
data class HandValue(
    val category: HandCategory,
    val tiebreak: List<Int>,
    val bestCards: List<Card>
) : Comparable<HandValue> {
    override fun compareTo(other: HandValue): Int {
        val c = category.rank.compareTo(other.category.rank)
        if (c != 0) return c
        for (i in tiebreak.indices) {
            val d = tiebreak[i].compareTo(other.tiebreak.getOrElse(i) { 0 })
            if (d != 0) return d
        }
        return 0
    }
}

object HandEvaluator {

    /** Nilai tangan terbaik daripada 5–7 kad. */
    fun evaluateBest(cards: List<Card>): HandValue {
        require(cards.size in 5..7) { "Perlu 5–7 kad, dapat ${cards.size}" }
        if (cards.size == 5) return evaluate5(cards)
        var best: HandValue? = null
        for (combo in combinations(cards, 5)) {
            val value = evaluate5(combo)
            if (best == null || value > best) best = value
        }
        return best!!
    }

    /** Nilai tepat 5 kad. */
    fun evaluate5(cards: List<Card>): HandValue {
        require(cards.size == 5) { "evaluate5 perlu tepat 5 kad" }
        val ranksDesc = cards.map { it.rank }.sortedDescending()
        val groups = cards.groupBy { it.rank }
            .map { (rank, cs) -> rank to cs.size }
            .sortedWith(compareByDescending<Pair<Int, Int>> { it.second }.thenByDescending { it.first })
        val counts = groups.map { it.second }
        val isFlush = cards.all { it.suit == cards[0].suit }
        val straightHigh = straightHigh(ranksDesc)

        // Kad terbaik diisih mengikut kumpulan (bilangan sama dahulu,
        // kemudian rank) — susunan ini digunakan untuk highlight.
        val bestCards = groups.flatMap { (rank, _) ->
            cards.filter { it.rank == rank }
        }

        if (isFlush && straightHigh != null) {
            return if (straightHigh == 14) {
                HandValue(HandCategory.ROYAL_FLUSH, listOf(14), bestCards)
            } else {
                HandValue(HandCategory.STRAIGHT_FLUSH, listOf(straightHigh), bestCards)
            }
        }
        return when (counts) {
            listOf(4, 1) -> HandValue(
                HandCategory.FOUR_OF_A_KIND,
                listOf(groups[0].first, groups[1].first),
                bestCards
            )
            listOf(3, 2) -> HandValue(
                HandCategory.FULL_HOUSE,
                listOf(groups[0].first, groups[1].first),
                bestCards
            )
            listOf(3, 1, 1) -> HandValue(
                HandCategory.THREE_OF_A_KIND,
                listOf(groups[0].first, groups[1].first, groups[2].first),
                bestCards
            )
            listOf(2, 2, 1) -> HandValue(
                HandCategory.TWO_PAIR,
                listOf(groups[0].first, groups[1].first, groups[2].first),
                bestCards
            )
            listOf(2, 1, 1, 1) -> HandValue(
                HandCategory.PAIR,
                listOf(groups[0].first, groups[1].first, groups[2].first, groups[3].first),
                bestCards
            )
            else -> {
                if (isFlush) {
                    HandValue(HandCategory.FLUSH, ranksDesc, bestCards)
                } else if (straightHigh != null) {
                    HandValue(HandCategory.STRAIGHT, listOf(straightHigh), bestCards)
                } else {
                    HandValue(HandCategory.HIGH_CARD, ranksDesc, bestCards)
                }
            }
        }
    }

    /**
     * Kembalikan rank tertinggi straight, atau null jika tiada.
     * Mengendalikan roda (wheel) A-2-3-4-5 — high = 5.
     */
    fun straightHigh(ranksDesc: List<Int>): Int? {
        val distinct = ranksDesc.distinct()
        if (distinct.size != 5) return null
        if (distinct[0] - distinct[4] == 4) return distinct[0]
        // Roda: A,5,4,3,2
        if (distinct == listOf(14, 5, 4, 3, 2)) return 5
        return null
    }

    /** Semua kombinasi k unsur daripada senarai (tertib asal dikekalkan). */
    private fun <T> combinations(list: List<T>, k: Int): List<List<T>> {
        val out = mutableListOf<List<T>>()
        fun rec(start: Int, current: MutableList<T>) {
            if (current.size == k) {
                out.add(current.toList())
                return
            }
            for (i in start until list.size) {
                current.add(list[i])
                rec(i + 1, current)
                current.removeAt(current.lastIndex)
            }
        }
        rec(0, mutableListOf())
        return out
    }
}
