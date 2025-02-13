package ee.oyatl.hanjakbd.input

import android.content.Context
import android.view.View
import ee.oyatl.hanjakbd.keyboard.Keyboard

interface InputMode: Keyboard.Listener {
    val listener: Listener

    fun reset()
    fun initView(context: Context): View
    fun getView(): View

    override fun onSymbol() {
    }

    override fun onLanguage() {
        listener.onSwitch(SwitchType.NextInputMode)
    }

    interface Listener {
        fun onCompose(text: String)
        fun onCommit(text: String)
        fun onDelete(before: Int, after: Int)
        fun onSwitch(type: SwitchType)
        fun onReset()
    }

    enum class SwitchType {
        ToggleSymbolMode, NextInputMode
    }
}