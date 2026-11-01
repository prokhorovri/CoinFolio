package me.prokhorov.coinfolio.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ITEM_TAG = "item"
private val ITEM_WIDTH = 160.dp
private val ITEM_HEIGHT = 64.dp
private val BOX_WIDTH = 320.dp

@RunWith(AndroidJUnit4::class)
class RepeatToFillColumnTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun showColumn(boxHeight: Dp) {
        composeRule.setContent {
            RepeatToFillColumn(modifier = Modifier.size(width = BOX_WIDTH, height = boxHeight)) {
                Box(modifier = Modifier.size(ITEM_WIDTH, ITEM_HEIGHT).testTag(ITEM_TAG))
            }
        }
    }

    // 200 / 64 = 3.125, so a fourth item is added and cut off at the bottom
    @Test
    fun a_height_that_is_not_a_multiple_adds_one_cut_off_item() {
        showColumn(boxHeight = 200.dp)

        composeRule.onAllNodesWithTag(ITEM_TAG).assertCountEquals(4)
    }

    @Test
    fun a_height_that_is_a_multiple_adds_no_extra_item() {
        showColumn(boxHeight = ITEM_HEIGHT * 3)

        composeRule.onAllNodesWithTag(ITEM_TAG).assertCountEquals(3)
    }

    @Test
    fun the_items_are_stacked_from_the_top() {
        showColumn(boxHeight = 200.dp)

        val items = composeRule.onAllNodesWithTag(ITEM_TAG)

        repeat(4) { index ->
            items[index].assertTopPositionInRootIsEqualTo(ITEM_HEIGHT * index)
        }
    }

    @Test
    fun the_items_are_centered_horizontally() {
        showColumn(boxHeight = 200.dp)

        val items = composeRule.onAllNodesWithTag(ITEM_TAG)

        repeat(4) { index ->
            items[index].assertLeftPositionInRootIsEqualTo((BOX_WIDTH - ITEM_WIDTH) / 2)
        }
    }
}
