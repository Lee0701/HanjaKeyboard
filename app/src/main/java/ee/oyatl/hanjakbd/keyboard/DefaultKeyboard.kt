package ee.oyatl.hanjakbd.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.databinding.KbdKeyBinding
import ee.oyatl.hanjakbd.databinding.KbdKeyboardBinding
import ee.oyatl.hanjakbd.databinding.KbdRowBinding

abstract class DefaultKeyboard(
    override val listener: Keyboard.Listener
) : Keyboard {

    abstract fun buildRows(context: Context): List<KbdRowBinding>

    override fun createView(context: Context): View {
        val inflater = LayoutInflater.from(context)
        val keyboard = KbdKeyboardBinding.inflate(inflater)
        buildRows(context).forEach { keyboard.root.addView(it.root) }
        return keyboard.root
    }

    protected fun buildRow(context: Context, chars: String, height: Int): KbdRowBinding {
        val inflater = LayoutInflater.from(context)
        val row = KbdRowBinding.inflate(inflater)
        chars.forEach { char ->
            val key = buildKey(context, char, height)
            row.root.addView(key.root)
        }
        return row
    }

    protected fun buildKey(context: Context, char: Char, height: Int): KbdKeyBinding {
        val inflater = LayoutInflater.from(context)
        val key = KbdKeyBinding.inflate(inflater)
        key.label.text = char.toString()
        key.root.setOnClickListener { listener.onChar(char) }
        key.root.layoutParams = LinearLayout.LayoutParams(0, height).apply {
            weight = 1.0f
        }
        return key
    }

    protected fun buildSpacer(context: Context, width: Float): View {
        val spacer = View(context)
        spacer.layoutParams = LinearLayout.LayoutParams(0, 0).apply {
            weight = width
        }
        return spacer
    }

    @SuppressLint("ClickableViewAccessibility")
    protected fun buildSpecialKey(
        context: Context,
        @ColorRes bkg: Int,
        @DrawableRes icon: Int,
        width: Float,
        onTouch: (Boolean) -> Unit
    ): View {
        val inflater = LayoutInflater.from(context)
        val height = context.resources.getDimensionPixelSize(R.dimen.kbd_key_height)
        val key = KbdKeyBinding.inflate(inflater)
        key.bkg.imageTintList = ContextCompat.getColorStateList(context, bkg)
        key.icon.setImageResource(icon)
        key.root.setOnTouchListener { view, event ->
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    onTouch(true)
                    view.isPressed = true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    onTouch(false)
                    view.isPressed = false
                }
            }
            view.invalidate()
            true
        }
        key.root.layoutParams = LinearLayout.LayoutParams(0, height).apply {
            weight = width
        }
        return key.root
    }
}