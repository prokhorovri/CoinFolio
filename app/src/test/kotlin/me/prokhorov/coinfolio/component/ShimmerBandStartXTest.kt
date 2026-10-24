package me.prokhorov.coinfolio.component

import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val BOX_WIDTH = 200f
private const val BAND_WIDTH = 100f

class ShimmerBandStartXTest {

    private fun startXAt(progress: Float) = shimmerBandStartX(progress, BOX_WIDTH, BAND_WIDTH)

    @Test
    fun `at the start the band ends at the left edge`() {
        val startX = startXAt(progress = 0f)

        assertThat(startX + BAND_WIDTH).isEqualTo(0f)
    }

    // With system animations off, the animation stops on this frame.
    @Test
    fun `at the end the band starts at the right edge`() {
        val startX = startXAt(progress = 1f)

        assertThat(startX).isEqualTo(BOX_WIDTH)
    }

    @Test
    fun `in the middle the band is centered in the box`() {
        val startX = startXAt(progress = 0.5f)

        assertThat(startX + BAND_WIDTH / 2).isEqualTo(BOX_WIDTH / 2)
    }
}
