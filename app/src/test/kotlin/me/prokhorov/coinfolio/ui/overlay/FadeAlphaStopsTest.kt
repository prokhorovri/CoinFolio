package me.prokhorov.coinfolio.ui.overlay

import com.google.common.truth.Truth.assertThat
import org.junit.Test

// A status bar of 24dp plus the 24dp band.
private const val BAND_START_IN_THE_MIDDLE = 0.5f

class FadeAlphaStopsTest {

    private val stops = fadeAlphaStops(BAND_START_IN_THE_MIDDLE)

    @Test
    fun `the screen edge is solid`() {
        assertThat(stops.first()).isEqualTo(0f to 1f)
    }

    @Test
    fun `the inset ends at the bar alpha`() {
        assertThat(stops[1]).isEqualTo(BAND_START_IN_THE_MIDDLE to BAR_ALPHA)
    }

    @Test
    fun `the inner end is clear`() {
        assertThat(stops.last()).isEqualTo(1f to 0f)
    }

    @Test
    fun `the positions never go back`() {
        val positions = stops.map { it.first }

        assertThat(positions).isInOrder()
    }

    @Test
    fun `the alpha never goes up`() {
        val alphas = stops.map { it.second }

        assertThat(alphas).isInOrder(reverseOrder<Float>())
    }

    // With no inset, like a screen with no system bar, the band starts at the edge.
    @Test
    fun `with no inset the band starts at the bar alpha`() {
        val stops = fadeAlphaStops(bandStartFraction = 0f)

        assertThat(stops[1]).isEqualTo(0f to BAR_ALPHA)
    }
}
