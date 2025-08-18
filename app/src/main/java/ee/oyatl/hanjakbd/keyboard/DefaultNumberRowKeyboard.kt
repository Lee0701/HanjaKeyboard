package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import android.util.TypedValue
import ee.oyatl.hanjakbd.databinding.KbdRowBinding

class DefaultNumberRowKeyboard(
    override val config: KeyboardConfig,
    override val listener: Keyboard.Listener,
    private val row: String
): DefaultKeyboard() {
    override fun getKeyHeight(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            config.numberRowHeight.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    override fun buildRows(context: Context): List<KbdRowBinding> {
        val row = buildRow(context, row)
        return listOf(row)
    }
}