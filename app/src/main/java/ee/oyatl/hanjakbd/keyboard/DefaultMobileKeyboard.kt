package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.databinding.KbdRowBinding

class DefaultMobileKeyboard(
    override val listener: Keyboard.Listener,
    private val rows: List<String>
): DefaultKeyboard(listener) {
    override fun buildRows(context: Context): List<KbdRowBinding> {
        val row1 = buildRow(context, rows[0])
        val row2 = buildRow(context, rows[1])
        val row3 = buildRow(context, rows[2])

        row2.root.addView(buildSpacer(context, 0.5f), 0)
        row2.root.addView(buildSpacer(context, 0.5f))

        row3.root.addView(buildSpecialKey(
            context,
            R.drawable.baseline_shift_24px,
            1.5f
        ) { listener.onShift() }, 0)
        row3.root.addView(buildSpecialKey(
            context,
            R.drawable.baseline_backspace_24,
            1.5f
        ) { listener.onDelete() })

        return listOf(row1, row2, row3)
    }
}