package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import android.media.AudioManager
import android.util.TypedValue
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.databinding.KbdRowBinding

class DefaultBottomRowKeyboard(
    override val config: KeyboardConfig,
    override val listener: Keyboard.Listener
): DefaultKeyboard() {
    override fun getKeyHeight(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            config.rowHeight.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    override fun buildRows(context: Context): List<KbdRowBinding> {
        val row = buildRow(context, "")
        row.root.addView(buildSpecialKey(
            context,
            R.color.kbd_key_mod_bkg,
            R.drawable.baseline_alternate_email_24,
            1.5f
        ) { pressed -> if(pressed) {
            performFeedback(row.root, AudioManager.FX_KEYPRESS_STANDARD)
            listener.onSpecial(Keyboard.SpecialKey.Symbols)
        } })
        row.root.addView(buildKey(context, ',').root)
        row.root.addView(buildSpecialKey(
            context,
            R.color.kbd_key_mod_bkg,
            R.drawable.baseline_language_24,
            1.0f
        ) { pressed -> if(pressed) {
            performFeedback(row.root, AudioManager.FX_KEYPRESS_STANDARD)
            listener.onSpecial(Keyboard.SpecialKey.Language)
        } })
        row.root.addView(buildSpecialKey(
            context,
            R.color.kbd_key_bkg,
            R.drawable.baseline_space_bar_24,
            4.0f
        ) { pressed -> if(pressed) {
            performFeedback(row.root, AudioManager.FX_KEYPRESS_SPACEBAR)
            listener.onSpecial(Keyboard.SpecialKey.Space)
        } })
        row.root.addView(buildKey(context, '.').root)
        row.root.addView(buildSpecialKey(
            context,
            R.color.kbd_key_return_bkg,
            R.drawable.baseline_keyboard_return_24,
            1.5f
        ) { pressed -> if(pressed) {
            performFeedback(row.root, AudioManager.FX_KEYPRESS_DELETE)
            listener.onSpecial(Keyboard.SpecialKey.Return)
        } })
        return listOf(row)
    }
}