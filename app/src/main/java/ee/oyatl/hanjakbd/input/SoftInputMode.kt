package ee.oyatl.hanjakbd.input

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import ee.oyatl.hanjakbd.input.InputMode.SwitchType
import ee.oyatl.hanjakbd.keyboard.DefaultKeyboardSet
import ee.oyatl.hanjakbd.keyboard.Keyboard
import ee.oyatl.hanjakbd.keyboard.KeyboardSet
import java.util.concurrent.locks.Lock

abstract class SoftInputMode(
    private val normalLayout: List<String>,
    private val shiftedLayout: List<String>
): InputMode {
    protected var shiftState: Keyboard.ShiftState = Keyboard.ShiftState.Unpressed
    private var shiftPressing: Boolean = false
    private var shiftTime: Long = 0
    private var inputWhileShifted: Boolean = false
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

    override fun onSpecial(type: Keyboard.SpecialKey) {
        when(type) {
            Keyboard.SpecialKey.Language -> onLanguage()
            Keyboard.SpecialKey.Symbols -> onSymbols()
            else -> return
        }
    }

    override fun onShift(pressed: Boolean) {
        shiftPressing = pressed
        val oldShiftState = shiftState
        if(pressed) onShiftPressed()
        else onShiftReleased()
        if(shiftState != oldShiftState) updateInputView()
    }

    private fun onShiftPressed() {
        when(shiftState) {
            Keyboard.ShiftState.Unpressed -> {
                shiftState = Keyboard.ShiftState.Pressed
            }
            Keyboard.ShiftState.Pressed -> {
                val diff = System.currentTimeMillis() - shiftTime
                if(diff < 300) shiftState = Keyboard.ShiftState.Locked
                else shiftState = Keyboard.ShiftState.Unpressed
            }
            Keyboard.ShiftState.Locked -> {
                shiftState = Keyboard.ShiftState.Unpressed
            }
        }
    }

    private fun onShiftReleased() {
        when(shiftState) {
            Keyboard.ShiftState.Unpressed -> {
            }
            Keyboard.ShiftState.Pressed -> {
                if(inputWhileShifted) shiftState = Keyboard.ShiftState.Unpressed
                else shiftState = Keyboard.ShiftState.Pressed
            }
            Keyboard.ShiftState.Locked -> {
            }
        }
        shiftTime = System.currentTimeMillis()
        inputWhileShifted = false
    }

    private fun onLanguage() {
        listener.onSwitch(SwitchType.NextInputMode)
    }

    private fun onSymbols() {
        listener.onSwitch(SwitchType.ToggleSymbolMode)
    }

    protected fun autoReleaseShift() {
        if(shiftState == Keyboard.ShiftState.Pressed) {
            if(!shiftPressing) {
                shiftState = Keyboard.ShiftState.Unpressed
                updateInputView()
            } else {
                inputWhileShifted = true
            }
        }
    }

    open fun updateInputView() {
        keyboardSet.getView(shiftState, false)
    }

    override fun reset() {
    }
}