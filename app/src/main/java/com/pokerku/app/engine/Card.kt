package com.pokerku.app.engine

import kotlin.random.Random

/**
 * PokerKu — model kad & dek (pure Kotlin, tiada kebergantungan Android).
 * Rank: 2..14 (11 = J, 12 = Q, 13 = K, 14 = A).
 */

enum class Suit(val symbol: String, val isRed: Boolean) {
    SPADES("♠", false),
    HEARTS("♥", true),
    DIAMONDS("♦", true),
    CLUBS("♣", false)
}

data class Card(val rank: Int, val suit: Suit) {
    init {
        require(rank in 2..14) { "Rank kad mesti 2..14, dapat $rank" }
    }

    val label: String
        get() = when (rank) {
            14 -> "A"
            13 -> "K"
            12 -> "Q"
            11 -> "J"
            else -> rank.toString()
        }

    val isRed: Boolean get() = suit.isRed

    override fun toString(): String = label + suit.symbol
}

/** Dek 52 kad standard. Boleh diberi seed untuk ujian deterministik. */
class Deck(seed: Long? = null) {
    private val rng: Random = if (seed != null) Random(seed) else Random.Default
    private val cards: ArrayDeque<Card> = ArrayDeque(
        Suit.entries.flatMap { suit -> (2..14).map { rank -> Card(rank, suit) } }.shuffled(rng)
    )

    fun deal(): Card = cards.removeFirst()

    fun remaining(): Int = cards.size
}
