package me.prokhorov.coinfolio.theme

import androidx.compose.ui.graphics.toArgb
import com.google.common.truth.Truth.assertThat
import me.prokhorov.coinfolio.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * The splash background is defined in XML, so this test checks that it matches
 * the light and dark ColorScheme.
 */
@RunWith(RobolectricTestRunner::class)
class SplashBackgroundColorTest {

    @Test
    fun `light splash background matches the light ColorScheme`() {
        assertThat(splashBackground()).isEqualTo(LightColorScheme.background.toArgb())
    }

    @Test
    @Config(qualifiers = "night")
    fun `dark splash background matches the dark ColorScheme`() {
        assertThat(splashBackground()).isEqualTo(DarkColorScheme.background.toArgb())
    }

    private fun splashBackground(): Int {
        return RuntimeEnvironment.getApplication().getColor(R.color.splash_background)
    }
}
