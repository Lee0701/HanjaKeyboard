package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import ee.oyatl.hanjakbd.layout.LayoutQwerty

class DefaultKeyboardSet(
    private val config: KeyboardConfig,
    private val listener: Keyboard.Listener,
    private val normalLayout: List<String>,
    private val shiftedLayout: List<String>
): KeyboardSet {
    private lateinit var keyboardView: LinearLayout
    private lateinit var mainKeyboardSwitcher: ShiftKeyboardSwitcher
    private lateinit var numberKeyboardSwitcher: ShiftKeyboardSwitcher

    override fun initView(context: Context): View {
        keyboardView = LinearLayout(context)
        keyboardView.orientation = LinearLayout.VERTICAL
        run {
            val normal = DefaultNumberRowKeyboard(config, listener, LayoutQwerty.NUMBER_ROW_LOWER).createView(context)
            val shifted = DefaultNumberRowKeyboard(config, listener, LayoutQwerty.NUMBER_ROW_UPPER).createView(context)
            numberKeyboardSwitcher = ShiftKeyboardSwitcher(context, normal, shifted, normal)
            keyboardView.addView(numberKeyboardSwitcher.view)
        }
        run {
            val normal = DefaultMobileKeyboard(config, listener, normalLayout, Keyboard.ShiftState.Unpressed).createView(context)
            val shifted = DefaultMobileKeyboard(config, listener, shiftedLayout, Keyboard.ShiftState.Pressed).createView(context)
            val locked = DefaultMobileKeyboard(config, listener, shiftedLayout, Keyboard.ShiftState.Locked).createView(context)
            mainKeyboardSwitcher = ShiftKeyboardSwitcher(context, normal, shifted, locked)
            keyboardView.addView(mainKeyboardSwitcher.view)
        }
        run {
            val bottomRowKeyboardView = DefaultBottomRowKeyboard(config, listener).createView(context)
            keyboardView.addView(bottomRowKeyboardView)
        }
        return keyboardView
    }

    override fun getView(shiftState: Keyboard.ShiftState, candidates: Boolean): View {
        mainKeyboardSwitcher.switch(shiftState)
        numberKeyboardSwitcher.switch(shiftState)
        if(candidates) numberKeyboardSwitcher.view.visibility = View.GONE
        else numberKeyboardSwitcher.view.visibility = View.VISIBLE
        return keyboardView
    }
}