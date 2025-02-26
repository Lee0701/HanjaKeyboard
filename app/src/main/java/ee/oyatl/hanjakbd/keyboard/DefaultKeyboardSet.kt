package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout

class DefaultKeyboardSet(
    private val listener: Keyboard.Listener,
    private val normalLayout: List<String>,
    private val shiftedLayout: List<String>
): KeyboardSet {
    private lateinit var mainKeyboardView: LinearLayout
    private lateinit var normalKeyboardView: View
    private lateinit var shiftedKeyboardView: View
    private lateinit var shiftLockedKeyboardView: View
    private lateinit var numberRowKeyboardView: View

    override fun initView(context: Context): View {
        normalKeyboardView = DefaultMobileKeyboard(listener, normalLayout, Keyboard.ShiftState.Unpressed).createView(context)
        shiftedKeyboardView = DefaultMobileKeyboard(listener, shiftedLayout, Keyboard.ShiftState.Pressed).createView(context)
        shiftLockedKeyboardView = DefaultMobileKeyboard(listener, shiftedLayout, Keyboard.ShiftState.Locked).createView(context)
        val switcherView = FrameLayout(context)
        switcherView.addView(normalKeyboardView)
        switcherView.addView(shiftedKeyboardView)
        switcherView.addView(shiftLockedKeyboardView)
        val bottomRowKeyboardView = DefaultBottomRowKeyboard(listener).createView(context)
        numberRowKeyboardView = DefaultNumberRowKeyboard(listener).createView(context)

        mainKeyboardView = LinearLayout(context)
        mainKeyboardView.orientation = LinearLayout.VERTICAL
        mainKeyboardView.addView(numberRowKeyboardView)
        mainKeyboardView.addView(switcherView)
        mainKeyboardView.addView(bottomRowKeyboardView)

        return mainKeyboardView
    }

    override fun getView(shiftState: Keyboard.ShiftState, candidates: Boolean): View {
        when(shiftState) {
            Keyboard.ShiftState.Unpressed -> normalKeyboardView.bringToFront()
            Keyboard.ShiftState.Pressed -> shiftedKeyboardView.bringToFront()
            Keyboard.ShiftState.Locked -> shiftLockedKeyboardView.bringToFront()
        }
        if(candidates) numberRowKeyboardView.visibility = View.GONE
        else numberRowKeyboardView.visibility = View.VISIBLE
        return mainKeyboardView
    }
}