package me.prokhorov.coinfolio.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NavigationTypeTest {

    @Test
    fun `a tab in a narrow window shows the bottom bar with that tab selected`() {
        val type = navigationType(
            topLevelDestination = TopLevelDestination.PORTFOLIO,
            isWideWindow = false,
        )

        assertThat(type).isEqualTo(NavigationType.Bar(TopLevelDestination.PORTFOLIO))
    }

    @Test
    fun `a tab in a wide window shows the rail with that tab selected`() {
        val type = navigationType(
            topLevelDestination = TopLevelDestination.PORTFOLIO,
            isWideWindow = true,
        )

        assertThat(type).isEqualTo(NavigationType.Rail(TopLevelDestination.PORTFOLIO))
    }

    @Test
    fun `a screen with no tab shows no navigation in any window`() {
        val narrow = navigationType(topLevelDestination = null, isWideWindow = false)
        val wide = navigationType(topLevelDestination = null, isWideWindow = true)

        assertThat(narrow).isEqualTo(NavigationType.None)
        assertThat(wide).isEqualTo(NavigationType.None)
    }
}
