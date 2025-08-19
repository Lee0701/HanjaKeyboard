package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.databinding.KbdRowBinding

class DefaultMobileKeyboard(
    override val config: KeyboardConfig,
    override val listener: Keyboard.Listener,
    private val rows: List<String>,
    private val shiftState: Keyboard.ShiftState
): DefaultKeyboard() {
    override fun getKeyHeight(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            config.rowHeight.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    override fun buildRows(context: Context): List<KbdRowBinding> {
        val row1 = buildRow(context, rows[0])
        val row2 = buildRow(context, rows[1])
        val row3 = buildRow(context, rows[2])

        if(rows[1].length != 10) {
            val space = (10 - rows[1].length) / 2f
            row2.root.addView(buildSpacer(context, space), 0)
            row2.root.addView(buildSpacer(context, space))
        }

        val icon = when(shiftState) {
            Keyboard.ShiftState.Unpressed -> R.drawable.baseline_shift_24
            Keyboard.ShiftState.Pressed -> R.drawable.baseline_shift_fill_24
            Keyboard.ShiftState.Locked -> R.drawable.baseline_shift_lock_fill_24
        }

        row3.root.addView(buildSpecialKey(
            context,
            R.color.kbd_key_mod_bkg,
            icon,
            1.5f
        ) { pressed ->
            if(pressed) performFeedback(row3.root, AudioManager.FX_KEYPRESS_STANDARD)
            listener.onShift(pressed)
        }, 0)

        val handler = Handler(Looper.getMainLooper())
        fun repeat() {
            listener.onSpecial(Keyboard.SpecialKey.Delete)
            handler.postDelayed({ repeat() }, 50)
        }
        row3.root.addView(buildSpecialKey(
            context,
            R.color.kbd_key_mod_bkg,
            R.drawable.baseline_backspace_24,
            1.5f
        ) { pressed ->
            if(pressed) {
                performFeedback(row3.root, AudioManager.FX_KEYPRESS_DELETE)
                listener.onSpecial(Keyboard.SpecialKey.Delete)
                handler.postDelayed({ repeat() }, 500)
            } else {
                handler.removeCallbacksAndMessages(null)
            }
            Unit
        })

        return listOf(row1, row2, row3)
    }
}