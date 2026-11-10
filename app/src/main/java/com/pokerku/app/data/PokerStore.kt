package com.pokerku.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * PokerKu — storan tempatan (DataStore Preferences).
 * Simpan: baki chip pemain, statistik, tetapan (bahasa, kelajuan).
 * 100% offline — tiada akaun, tiada sync, tiada server.
 */

private val Context.pokerDataStore: DataStore<Preferences> by preferencesDataStore(name = "pokerku")

data class PokerSettings(
    val chips: Int = 1000,
    val bahasa: String = "en",
    val kelajuan: String = "sederhana", // perlahan | sederhana | laju
    val tanganMain: Int = 0,
    val tanganMenang: Int = 0,
    val potTerbesar: Int = 0,
    val tanganTerbaik: String = "",
    val cipMenangBersih: Int = 0
)

class PokerStore(private val context: Context) {

    private object Keys {
        val CHIPS = intPreferencesKey("chips")
        val BAHASA = stringPreferencesKey("bahasa")
        val KELAJUAN = stringPreferencesKey("kelajuan")
        val TANGAN_MAIN = intPreferencesKey("tangan_main")
        val TANGAN_MENANG = intPreferencesKey("tangan_menang")
        val POT_TERBESAR = intPreferencesKey("pot_terbesar")
        val TANGAN_TERBAIK = stringPreferencesKey("tangan_terbaik")
        val CIP_MENANG_BERSIH = intPreferencesKey("cip_menang_bersih")
    }

    val settings: Flow<PokerSettings> = context.pokerDataStore.data.map { p ->
        PokerSettings(
            chips = p[Keys.CHIPS] ?: 1000,
            bahasa = p[Keys.BAHASA] ?: "en",
            kelajuan = p[Keys.KELAJUAN] ?: "sederhana",
            tanganMain = p[Keys.TANGAN_MAIN] ?: 0,
            tanganMenang = p[Keys.TANGAN_MENANG] ?: 0,
            potTerbesar = p[Keys.POT_TERBESAR] ?: 0,
            tanganTerbaik = p[Keys.TANGAN_TERBAIK] ?: "",
            cipMenangBersih = p[Keys.CIP_MENANG_BERSIH] ?: 0
        )
    }

    suspend fun setChips(chips: Int) {
        context.pokerDataStore.edit { it[Keys.CHIPS] = chips.coerceAtLeast(0) }
    }

    suspend fun setBahasa(bahasa: String) {
        context.pokerDataStore.edit { it[Keys.BAHASA] = bahasa }
    }

    suspend fun setKelajuan(kelajuan: String) {
        context.pokerDataStore.edit { it[Keys.KELAJUAN] = kelajuan }
    }

    suspend fun rekodTangan(
        menang: Boolean,
        pot: Int,
        tanganTerbaikBaru: String?,
        cipBersih: Int
    ) {
        context.pokerDataStore.edit { p ->
            p[Keys.TANGAN_MAIN] = (p[Keys.TANGAN_MAIN] ?: 0) + 1
            if (menang) p[Keys.TANGAN_MENANG] = (p[Keys.TANGAN_MENANG] ?: 0) + 1
            if (pot > (p[Keys.POT_TERBESAR] ?: 0)) p[Keys.POT_TERBESAR] = pot
            if (!tanganTerbaikBaru.isNullOrBlank()) {
                val ranking = listOf(
                    "hand.highcard", "hand.pair", "hand.twopair", "hand.trips",
                    "hand.straight", "hand.flush", "hand.fullhouse", "hand.quads",
                    "hand.straightflush", "hand.royalflush"
                )
                val lama = p[Keys.TANGAN_TERBAIK] ?: ""
                if (ranking.indexOf(tanganTerbaikBaru) > ranking.indexOf(lama)) {
                    p[Keys.TANGAN_TERBAIK] = tanganTerbaikBaru
                }
            }
            p[Keys.CIP_MENANG_BERSIH] = (p[Keys.CIP_MENANG_BERSIH] ?: 0) + cipBersih
        }
    }

    suspend fun resetStatistik() {
        context.pokerDataStore.edit { p ->
            p[Keys.TANGAN_MAIN] = 0
            p[Keys.TANGAN_MENANG] = 0
            p[Keys.POT_TERBESAR] = 0
            p[Keys.TANGAN_TERBAIK] = ""
            p[Keys.CIP_MENANG_BERSIH] = 0
        }
    }

    suspend fun resetCip() {
        context.pokerDataStore.edit { it[Keys.CHIPS] = 1000 }
    }
}
