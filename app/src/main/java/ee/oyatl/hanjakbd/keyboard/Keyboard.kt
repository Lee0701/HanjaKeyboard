package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import android.view.View

interface Keyboard {
    val listener: Listener

    fun createView(context: Context): View

    interface Listener {
        fun onChar(char: Char)
        fun onSpace()
        fun onDelete()
        fun onShift()
        fun onLanguage()
        fun onSymbol()
    }
}