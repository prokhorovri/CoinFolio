package me.prokhorov.coinfolio.data.remote

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import me.prokhorov.coinfolio.domain.model.Coin

private val VALID_FOR = 30.seconds

/**
 * Keeps the last answer of the market call for 30 seconds. CoinGecko allows about 5 to 15
 * requests per minute, and the portfolio asks again after every change of a holding.
 */
@Singleton
class MarketCoinsCache @Inject constructor(private val timeSource: TimeSource) {

    // One immutable entry, so a reader never sees a half written value. Two calls at the same
    // moment can both miss and both send a request. Switch to a Mutex if that starts to matter.
    @Volatile
    private var entry: Entry? = null

    /** The stored coins, or null when the ids are different or the answer is too old. */
    fun read(ids: List<String>): List<Coin>? {
        val current = entry ?: return null
        if (current.ids != ids.toSet()) return null
        if (current.loadedAt.elapsedNow() >= VALID_FOR) return null
        return current.coins
    }

    fun write(ids: List<String>, coins: List<Coin>) {
        entry = Entry(ids = ids.toSet(), coins = coins, loadedAt = timeSource.markNow())
    }

    private class Entry(val ids: Set<String>, val coins: List<Coin>, val loadedAt: TimeMark)
}
