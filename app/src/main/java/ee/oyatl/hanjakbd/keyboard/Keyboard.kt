package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import android.view.View

interface Keyboard {
    val listener: Listener

    fun createView(context: Context): View

    interface Listener {
        fun onChar(char: Char)
        fun onSpecial(type: SpecialKey)
    }

    enum class SpecialKey {
        Shift, Caps,
        Space, Return, Delete,
        Language, Symbols,
    }
}