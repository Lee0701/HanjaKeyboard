package ee.oyatl.hanjakbd.input

import android.content.Context
import android.view.View
import ee.oyatl.hanjakbd.keyboard.Keyboard

interface InputMode: Keyboard.Listener {
    val listener: Listener

    fun reset()
    fun initView(context: Context): View
    fun getView(): View

    interface Listener {
        fun onCompose(text: String)
        fun onCommit(text: String)
        fun onDelete(before: Int, after: Int)
        fun onEditorAction()
        fun onSwitch(type: SwitchType)
        fun onReset()
    }

    enum class SwitchType {
        ToggleSymbolMode, NextInputMode
    }
}