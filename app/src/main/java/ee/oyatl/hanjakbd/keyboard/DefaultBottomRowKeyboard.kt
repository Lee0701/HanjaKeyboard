package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.databinding.KbdRowBinding

class DefaultBottomRowKeyboard(
    override val listener: Keyboard.Listener
): DefaultKeyboard(listener) {
    override fun buildRows(context: Context): List<KbdRowBinding> {
        val bottomRow = buildRow(context, "")
        bottomRow.root.addView(buildSpacer(context, 2.0f))
        bottomRow.root.addView(buildSpecialKey(
            context,
            R.drawable.baseline_language_24,
            1.0f) { listener.onLanguage() })
        bottomRow.root.addView(buildSpecialKey(
            context,
            R.drawable.baseline_space_bar_24,
            4.0f
        ) { listener.onSpace() })
        bottomRow.root.addView(buildSpacer(context, 3.0f))
        return listOf(bottomRow)
    }
}