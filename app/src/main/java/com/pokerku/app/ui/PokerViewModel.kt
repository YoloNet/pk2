package com.pokerku.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pokerku.app.data.PokerSettings
import com.pokerku.app.data.PokerStore
import com.pokerku.app.engine.BotAi
import com.pokerku.app.engine.BotStyle
import com.pokerku.app.engine.PlayerAction
import com.pokerku.app.engine.PokerGame
import com.pokerku.app.engine.Street
import com.pokerku.app.i18n.Strings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class Screen { MENU, TABLE, HOWTO, STATS, SETTINGS }

/**
 * PokerKu — ViewModel utama.
 *
 * Memegang satu instance [PokerGame], menjalankan giliran bot dengan jeda
 * (mengikut kelajuan dalam tetapan), dan menyimpan chip + statistik ke
 * DataStore. Semua peraturan kekal dalam enjin — di sini hanya orkestra.
 */
class PokerViewModel(application: Application) : AndroidViewModel(application) {

    private val store = PokerStore(application)

    val settings: StateFlow<PokerSettings> =
        store.settings.stateIn(viewModelScope, SharingStarted.Eagerly, PokerSettings())

    val screen = MutableStateFlow(Screen.MENU)
    val table = MutableStateFlow<PokerGame.Snapshot?>(null)
    val raiseAmount = MutableStateFlow(0)
    val showRaisePanel = MutableStateFlow(false)
    val showRebuy = MutableStateFlow(false)
    val showResetStats = MutableStateFlow(false)
    val showResetChips = MutableStateFlow(false)

    private var game: PokerGame? = null
    private var botJob: Job? = null
    private var humanStackAtHandStart = 1000
    private var handRecorded = false
    private val rng = Random.Default

    /** Personaliti bot mengikut id pemain: 1 = vic, 2 = Masta, 3 = Ayam. */
    private val gayaBotMengikutId: Map<Int, BotStyle> = mapOf(
        1 to BotStyle.KETAT,
        2 to BotStyle.LONGGAR,
        3 to BotStyle.AGRESIF
    )

    fun gayaBot(id: Int): BotStyle = gayaBotMengikutId[id] ?: BotStyle.KETAT

    fun t(key: String, vararg params: Pair<String, String>): String =
        Strings.t(key, settings.value.bahasa, *params)

    fun botDelayMs(): Long = when (settings.value.kelajuan) {
        "perlahan" -> 1400L
        "laju" -> 400L
        else -> 800L
    }

    fun keSkrin(s: Screen) {
        screen.value = s
    }

    fun keMenu() {
        botJob?.cancel()
        screen.value = Screen.MENU
    }

    // ------------------------------------------------------------------
    // Meja
    // ------------------------------------------------------------------

    fun mulaMeja() {
        if (game == null) {
            val g = PokerGame()
            g.players[0].stack = settings.value.chips.coerceAtLeast(0)
            game = g
        }
        screen.value = Screen.TABLE
        if (table.value == null) {
            tanganBaharu()
        } else {
            jalankanBot()
        }
    }

    /** Mulakan tangan baharu (atau tunjuk dialog rebuy jika chip habis). */
    fun tanganBaharu() {
        val g = game ?: return
        for (p in g.players) {
            if (p.isBot && p.stack <= 0) p.stack = 1000
        }
        if (g.players[0].stack <= 0) {
            showRebuy.value = true
            return
        }
        humanStackAtHandStart = g.players[0].stack
        handRecorded = false
        g.startHand()
        muatSemula()
        jalankanBot()
    }

    fun tanganSeterusnya() = tanganBaharu()

    fun sahkanRebuy() {
        val g = game ?: return
        g.players[0].stack = 1000
        showRebuy.value = false
        viewModelScope.launch { store.setChips(1000) }
        tanganBaharu()
    }

    fun tolakRebuy() {
        showRebuy.value = false
        screen.value = Screen.MENU
    }

    // ------------------------------------------------------------------
    // Aksi manusia
    // ------------------------------------------------------------------

    fun aksiManusia(action: PlayerAction) {
        val g = game ?: return
        if (g.street == Street.HAND_OVER || g.street == Street.SHOWDOWN) return
        if (g.currentPlayer().id != 0) return
        if (!g.applyAction(0, action)) return
        showRaisePanel.value = false
        muatSemula()
        semakTamatTangan()
        jalankanBot()
    }

    fun callAmount(): Int {
        val g = game ?: return 0
        return g.callAmount(g.players[0])
    }

    fun canCheck(): Boolean {
        val g = game ?: return false
        return g.canCheck(g.players[0])
    }

    fun raiseRange(): IntRange? {
        val g = game ?: return null
        return g.legalRaiseRange(g.players[0])
    }

    fun setRaiseAmount(v: Int) {
        raiseAmount.value = v
    }

    /** Butang pantas: separuh pot / pot penuh / all-in. */
    fun quickRaise(kind: String) {
        val g = game ?: return
        val player = g.players[0]
        val range = g.legalRaiseRange(player) ?: return
        val total = when (kind) {
            "separuh" -> g.currentBet + g.totalPot() / 2
            "pot" -> g.potSizeRaiseTotal(player)
            "allin" -> player.betInRound + player.stack
            else -> g.potSizeRaiseTotal(player)
        }
        raiseAmount.value = total.coerceIn(range)
    }

    // ------------------------------------------------------------------
    // Giliran bot
    // ------------------------------------------------------------------

    private fun jalankanBot() {
        botJob?.cancel()
        botJob = viewModelScope.launch {
            var g = game ?: return@launch
            while (g.street != Street.HAND_OVER && g.street != Street.SHOWDOWN) {
                val current = g.currentPlayer()
                if (!current.isBot) break
                delay(botDelayMs())
                g = game ?: return@launch
                if (g.street == Street.HAND_OVER || g.street == Street.SHOWDOWN) break
                if (g.currentPlayer().id != current.id) continue
                val decision = BotAi.decide(g, current, gayaBot(current.id), rng)
                if (!g.applyAction(current.id, decision)) break
                muatSemula()
            }
            semakTamatTangan()
        }
    }

    private fun muatSemula() {
        val g = game ?: return
        table.value = g.snapshot()
        if (g.street != Street.HAND_OVER && g.currentPlayer().id == 0) {
            val range = g.legalRaiseRange(g.players[0])
            if (range != null) {
                raiseAmount.value = g.potSizeRaiseTotal(g.players[0]).coerceIn(range)
            }
        }
    }

    // ------------------------------------------------------------------
    // Statistik & simpanan
    // ------------------------------------------------------------------

    private fun semakTamatTangan() {
        val g = game ?: return
        if (g.street != Street.HAND_OVER || handRecorded) return
        handRecorded = true
        val human = g.players[0]
        val won = if (g.lastShowdown != null) {
            (g.lastShowdown!!.winnings[0] ?: 0) > 0
        } else {
            g.foldWinnerId == 0
        }
        val net = human.stack - humanStackAtHandStart
        val bestHandKey = g.lastShowdown?.handValues?.get(0)?.category?.key
        viewModelScope.launch {
            store.rekodTangan(won, g.lastPot, bestHandKey, net)
            store.setChips(human.stack)
        }
        if (human.stack <= 0) showRebuy.value = true
    }

    // ------------------------------------------------------------------
    // Tetapan
    // ------------------------------------------------------------------

    fun setBahasa(code: String) {
        viewModelScope.launch { store.setBahasa(code) }
    }

    fun setKelajuan(v: String) {
        viewModelScope.launch { store.setKelajuan(v) }
    }

    fun resetStatistik() {
        viewModelScope.launch { store.resetStatistik() }
        showResetStats.value = false
    }

    fun resetCip() {
        viewModelScope.launch { store.resetCip() }
        game?.players?.get(0)?.let { it.stack = 1000 }
        muatSemula()
        showResetChips.value = false
    }
}
