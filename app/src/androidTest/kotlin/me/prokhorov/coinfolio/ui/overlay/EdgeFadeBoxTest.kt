package me.prokhorov.coinfolio.ui.overlay

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.FadeExtraHeight
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private val TOP_INSET = 30.dp
private val BOTTOM_INSET = 50.dp
private val SIDE_INSET = 10.dp
private val BOX_SIZE = 300.dp

/**
 * Checks that the padding the content gets and the fades always match. Method names use
 * underscores(due to 28minSdk)
 */
@RunWith(AndroidJUnit4::class)
class EdgeFadeBoxTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var contentPadding: PaddingValues

    // A bar sits below the top inset, so it adds to the top padding.
    private fun showBox(hasTopFade: Boolean, hasBottomFade: Boolean, topBarHeight: Dp = 0.dp) {
        composeRule.setContent {
            CoinFolioTheme {
                EdgeFadeBox(
                    contentPadding = PaddingValues(
                        start = SIDE_INSET,
                        top = TOP_INSET + topBarHeight,
                        end = SIDE_INSET,
                        bottom = BOTTOM_INSET,
                    ),
                    modifier = Modifier.size(BOX_SIZE),
                    topBarHeight = topBarHeight,
                    hasTopFade = hasTopFade,
                    hasBottomFade = hasBottomFade,
                ) { padding ->
                    contentPadding = padding
                }
            }
        }
    }

    @Test
    fun the_content_gets_room_for_each_fade() {
        showBox(hasTopFade = true, hasBottomFade = true)

        assertThat(contentPadding.calculateTopPadding()).isEqualTo(TOP_INSET + FadeExtraHeight)
        assertThat(contentPadding.calculateBottomPadding())
            .isEqualTo(BOTTOM_INSET + FadeExtraHeight)
    }

    @Test
    fun the_content_gets_only_the_insets_with_no_fades() {
        showBox(hasTopFade = false, hasBottomFade = false)

        assertThat(contentPadding.calculateTopPadding()).isEqualTo(TOP_INSET)
        assertThat(contentPadding.calculateBottomPadding()).isEqualTo(BOTTOM_INSET)
        composeRule.onNodeWithTag(TOP_FADE_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(BOTTOM_FADE_TAG).assertDoesNotExist()
    }

    @Test
    fun the_top_fade_covers_the_inset_and_the_band() {
        showBox(hasTopFade = true, hasBottomFade = false)

        val fade = composeRule.onNodeWithTag(TOP_FADE_TAG)

        fade.assertHeightIsEqualTo(TOP_INSET + FadeExtraHeight)
        fade.assertTopPositionInRootIsEqualTo(0.dp)
        composeRule.onNodeWithTag(BOTTOM_FADE_TAG).assertDoesNotExist()
    }

    @Test
    fun the_bottom_fade_covers_the_inset_and_the_band() {
        showBox(hasTopFade = false, hasBottomFade = true)

        val fade = composeRule.onNodeWithTag(BOTTOM_FADE_TAG)

        fade.assertHeightIsEqualTo(BOTTOM_INSET + FadeExtraHeight)
        // The fade ends on the bottom edge of the box.
        fade.assertTopPositionInRootIsEqualTo(BOX_SIZE - BOTTOM_INSET - FadeExtraHeight)
        composeRule.onNodeWithTag(TOP_FADE_TAG).assertDoesNotExist()
    }

    // The bar hides the end of the fade, so the content can start right below the bar.
    @Test
    fun with_a_bar_taller_than_the_band_the_content_starts_below_the_bar() {
        val barHeight = FadeExtraHeight * 3
        showBox(hasTopFade = true, hasBottomFade = false, topBarHeight = barHeight)

        assertThat(contentPadding.calculateTopPadding()).isEqualTo(TOP_INSET + barHeight)
        composeRule
            .onNodeWithTag(TOP_FADE_TAG)
            .assertHeightIsEqualTo(TOP_INSET + FadeExtraHeight)
    }

    @Test
    fun with_a_bar_shorter_than_the_band_the_content_starts_below_the_fade() {
        val barHeight = FadeExtraHeight / 2
        showBox(hasTopFade = true, hasBottomFade = false, topBarHeight = barHeight)

        assertThat(contentPadding.calculateTopPadding()).isEqualTo(TOP_INSET + FadeExtraHeight)
        composeRule
            .onNodeWithTag(TOP_FADE_TAG)
            .assertHeightIsEqualTo(TOP_INSET + FadeExtraHeight)
    }

    // The sides have no fade, so they pass through unchanged.
    @Test
    fun the_side_insets_pass_through() {
        showBox(hasTopFade = true, hasBottomFade = true)

        assertThat(contentPadding.calculateLeftPadding(LayoutDirection.Ltr)).isEqualTo(SIDE_INSET)
        assertThat(contentPadding.calculateRightPadding(LayoutDirection.Ltr)).isEqualTo(SIDE_INSET)
    }
}
