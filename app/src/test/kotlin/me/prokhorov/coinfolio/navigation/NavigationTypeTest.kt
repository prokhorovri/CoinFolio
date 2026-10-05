package me.prokhorov.coinfolio.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NavigationTypeTest {

    @Test
    fun `a tab in a narrow window shows the bottom bar`() {
        val type = navigationType(isTopLevel = true, isWideWindow = false)

        assertThat(type).isEqualTo(NavigationType.BAR)
    }

    @Test
    fun `a tab in a wide window shows the rail`() {
        val type = navigationType(isTopLevel = true, isWideWindow = true)

        assertThat(type).isEqualTo(NavigationType.RAIL)
    }

    @Test
    fun `a screen with no tab shows no navigation in any window`() {
        val narrow = navigationType(isTopLevel = false, isWideWindow = false)
        val wide = navigationType(isTopLevel = false, isWideWindow = true)

        assertThat(narrow).isEqualTo(NavigationType.NONE)
        assertThat(wide).isEqualTo(NavigationType.NONE)
    }
}
