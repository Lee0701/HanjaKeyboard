package ee.oyatl.hanjakbd.input

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import ee.oyatl.hanjakbd.keyboard.DefaultKeyboardSet
import ee.oyatl.hanjakbd.keyboard.KeyboardSet

abstract class SoftInputMode(
    private val normalLayout: List<String>,
    private val shiftedLayout: List<String>
): InputMode {
    protected var shiftPressed: Boolean = false
    protected lateinit var inputView: LinearLayout
    protected lateinit var keyboardSet: KeyboardSet

    override fun initView(context: Context): View {
        keyboardSet = DefaultKeyboardSet(this, normalLayout, shiftedLayout)

        inputView = LinearLayout(context)
        inputView.orientation = LinearLayout.VERTICAL
        inputView.addView(keyboardSet.initView(context))
        return inputView
    }

    override fun getView(): View {
        updateInputView()
        return inputView
    }

    override fun onShift() {
        shiftPressed = !shiftPressed
        updateInputView()
    }

    open fun updateInputView() {
        keyboardSet.getView(shiftPressed, false)
    }

    override fun reset() {
    }
}