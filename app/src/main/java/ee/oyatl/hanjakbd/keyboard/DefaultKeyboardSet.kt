package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import ee.oyatl.hanjakbd.layout.LayoutQwerty

class DefaultKeyboardSet(
    private val listener: Keyboard.Listener,
    private val normalLayout: List<String>,
    private val shiftedLayout: List<String>
): KeyboardSet {
    private lateinit var mainKeyboardView: LinearLayout
    private lateinit var normalKeyboardView: View
    private lateinit var shiftedKeyboardView: View
    private lateinit var shiftLockedKeyboardView: View
    private lateinit var normalNumberRowKeyboardView: View
    private lateinit var shiftedNumberRowKeyboardView: View

    override fun initView(context: Context): View {
        mainKeyboardView = LinearLayout(context)
        mainKeyboardView.orientation = LinearLayout.VERTICAL
        run {
            val switcherView = FrameLayout(context)
            normalNumberRowKeyboardView = DefaultNumberRowKeyboard(listener, LayoutQwerty.NUMBER_ROW_LOWER).createView(context)
            shiftedNumberRowKeyboardView = DefaultNumberRowKeyboard(listener, LayoutQwerty.NUMBER_ROW_UPPER).createView(context)
            switcherView.addView(normalNumberRowKeyboardView)
            switcherView.addView(shiftedNumberRowKeyboardView)
            mainKeyboardView.addView(switcherView)
        }
        run {
            val switcherView = FrameLayout(context)
            normalKeyboardView = DefaultMobileKeyboard(listener, normalLayout, Keyboard.ShiftState.Unpressed).createView(context)
            shiftedKeyboardView = DefaultMobileKeyboard(listener, shiftedLayout, Keyboard.ShiftState.Pressed).createView(context)
            shiftLockedKeyboardView = DefaultMobileKeyboard(listener, shiftedLayout, Keyboard.ShiftState.Locked).createView(context)
            switcherView.addView(normalKeyboardView)
            switcherView.addView(shiftedKeyboardView)
            switcherView.addView(shiftLockedKeyboardView)
            mainKeyboardView.addView(switcherView)
        }

        run {
            val bottomRowKeyboardView = DefaultBottomRowKeyboard(listener).createView(context)
            mainKeyboardView.addView(bottomRowKeyboardView)
        }

        return mainKeyboardView
    }

    override fun getView(shiftState: Keyboard.ShiftState, candidates: Boolean): View {
        when(shiftState) {
            Keyboard.ShiftState.Unpressed -> {
                normalKeyboardView.bringToFront()
                normalNumberRowKeyboardView.bringToFront()
            }
            Keyboard.ShiftState.Pressed -> {
                shiftedKeyboardView.bringToFront()
                shiftedNumberRowKeyboardView.bringToFront()
            }
            Keyboard.ShiftState.Locked -> {
                shiftLockedKeyboardView.bringToFront()
                normalNumberRowKeyboardView.bringToFront()
            }
        }
        if(candidates) normalNumberRowKeyboardView.visibility = View.GONE
        else normalNumberRowKeyboardView.visibility = View.VISIBLE
        return mainKeyboardView
    }
}