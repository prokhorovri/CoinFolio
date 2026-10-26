package me.prokhorov.coinfolio.component

import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val WINDOW_WIDTH = 200f
private const val BAND_WIDTH = 100f

class ShimmerBandStartXTest {

    @Test
    fun `at the start the stripe ends at the left window edge`() {
        val startX = shimmerBandStartX(progress = 0f, WINDOW_WIDTH, BAND_WIDTH)

        assertThat(startX + BAND_WIDTH).isEqualTo(0f)
    }

    @Test
    fun `in the middle the stripe is centered in the window`() {
        val startX = shimmerBandStartX(progress = 0.5f, WINDOW_WIDTH, BAND_WIDTH)

        assertThat(startX + BAND_WIDTH / 2).isEqualTo(WINDOW_WIDTH / 2)
    }

    @Test
    fun `at the end the stripe starts at the right window edge`() {
        val startX = shimmerBandStartX(progress = 1f, WINDOW_WIDTH, BAND_WIDTH)

        assertThat(startX).isEqualTo(WINDOW_WIDTH)
    }
}
