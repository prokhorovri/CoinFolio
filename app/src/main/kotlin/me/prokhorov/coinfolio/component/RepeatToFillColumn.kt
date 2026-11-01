package me.prokhorov.coinfolio.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.SubcomposeLayout
import kotlin.math.ceil

/**
 * Repeats [item] from the top until the height is full.
 * Items are centered horizontally, and the last one is cut off at the bottom.
 * Needs a fixed height. Don't use it inside a scrolling container.
 * The item must have one root layout.
 */
@Composable
fun RepeatToFillColumn(modifier: Modifier = Modifier, item: @Composable () -> Unit) {
    SubcomposeLayout(modifier = modifier.clipToBounds()) { constraints ->
        check(constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
            "RepeatToFillColumn needs a bounded size"
        }
        val itemConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // Measures one item first, so the count comes from its real height.
        val measurables = subcompose(slotId = 0, content = item)
        check(measurables.size == 1) { "RepeatToFillColumn needs an item with one root layout" }
        val firstPlaceable = measurables.first().measure(itemConstraints)

        // A zero height would give an endless count, so one item is enough.
        val count = if (firstPlaceable.height == 0) {
            1
        } else {
            ceil(constraints.maxHeight.toFloat() / firstPlaceable.height).toInt()
        }

        val placeables = listOf(firstPlaceable) + (1 until count).map { index ->
            subcompose(slotId = index, content = item).first().measure(itemConstraints)
        }

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.place(x = (constraints.maxWidth - placeable.width) / 2, y = y)
                y += placeable.height
            }
        }
    }
}
