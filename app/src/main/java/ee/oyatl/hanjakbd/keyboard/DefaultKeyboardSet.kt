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
    private lateinit var numberRowKeyboardView: View

    override fun initView(context: Context): View {
        normalKeyboardView = DefaultMobileKeyboard(listener, normalLayout).createView(context)
        shiftedKeyboardView = DefaultMobileKeyboard(listener, shiftedLayout).createView(context)
        val switcherView = FrameLayout(context)
        switcherView.addView(normalKeyboardView)
        switcherView.addView(shiftedKeyboardView)
        val bottomRowKeyboardView = DefaultBottomRowKeyboard(listener).createView(context)
        numberRowKeyboardView = DefaultNumberRowKeyboard(listener).createView(context)

        mainKeyboardView = LinearLayout(context)
        mainKeyboardView.orientation = LinearLayout.VERTICAL
        mainKeyboardView.addView(numberRowKeyboardView)
        mainKeyboardView.addView(switcherView)
        mainKeyboardView.addView(bottomRowKeyboardView)

        return mainKeyboardView
    }

    override fun getView(shifted: Boolean, candidates: Boolean): View {
        if(shifted) shiftedKeyboardView.bringToFront()
        else normalKeyboardView.bringToFront()
        if(candidates) numberRowKeyboardView.visibility = View.GONE
        else numberRowKeyboardView.visibility = View.VISIBLE
        return mainKeyboardView
    }
}