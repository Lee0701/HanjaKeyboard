package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import ee.oyatl.hanjakbd.databinding.KbdRowBinding

class DefaultNumberRowKeyboard(
    listener: Keyboard.Listener
): DefaultKeyboard(listener) {
    override fun buildRows(context: Context): List<KbdRowBinding> {
        val row = buildRow(context, "1234567890")
        return listOf(row)
    }
}