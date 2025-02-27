package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.databinding.KbdRowBinding

class DefaultBottomRowKeyboard(
    override val listener: Keyboard.Listener
): DefaultKeyboard(listener) {
    override fun buildRows(context: Context): List<KbdRowBinding> {
        val height = context.resources.getDimensionPixelSize(R.dimen.kbd_key_height)
        val row = buildRow(context, "", height)
        row.root.addView(buildSpacer(context, 2.5f))
        row.root.addView(buildSpecialKey(
            context,
            R.color.kbd_key_mod_bkg,
            R.drawable.baseline_language_24,
            1.0f) { pressed -> if(pressed) listener.onSpecial(Keyboard.SpecialKey.Language) })
        row.root.addView(buildSpecialKey(
            context,
            R.color.kbd_key_bkg,
            R.drawable.baseline_space_bar_24,
            4.0f
        ) { pressed -> if(pressed) listener.onSpecial(Keyboard.SpecialKey.Space) })
        row.root.addView(buildSpacer(context, 2.5f))
        return listOf(row)
    }
}