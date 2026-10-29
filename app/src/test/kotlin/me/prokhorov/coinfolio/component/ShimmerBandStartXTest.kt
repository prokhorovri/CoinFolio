package me.prokhorov.coinfolio.component

import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val FRAME_WIDTH = 200f
private const val BAND_WIDTH = 100f

class ShimmerBandStartXTest {

    @Test
    fun `at the start the stripe ends at the left frame edge`() {
        val startX = shimmerBandStartX(progress = 0f, FRAME_WIDTH, BAND_WIDTH)

        assertThat(startX + BAND_WIDTH).isEqualTo(0f)
    }

    @Test
    fun `in the middle the stripe is centered in the frame`() {
        val startX = shimmerBandStartX(progress = 0.5f, FRAME_WIDTH, BAND_WIDTH)

        assertThat(startX + BAND_WIDTH / 2).isEqualTo(FRAME_WIDTH / 2)
    }

    @Test
    fun `at the end the stripe starts at the right frame edge`() {
        val startX = shimmerBandStartX(progress = 1f, FRAME_WIDTH, BAND_WIDTH)

        assertThat(startX).isEqualTo(FRAME_WIDTH)
    }
}
