package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.databinding.KbdRowBinding

class DefaultNumberRowKeyboard(
    listener: Keyboard.Listener,
    private val row: String
): DefaultKeyboard(listener) {
    override fun buildRows(context: Context): List<KbdRowBinding> {
        val height = context.resources.getDimensionPixelSize(R.dimen.kbd_key_number_height)
        val row = buildRow(context, row, height)
        return listOf(row)
    }
}