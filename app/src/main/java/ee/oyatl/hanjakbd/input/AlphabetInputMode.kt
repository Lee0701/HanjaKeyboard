package ee.oyatl.hanjakbd.input

import ee.oyatl.hanjakbd.keyboard.Keyboard
import ee.oyatl.hanjakbd.layout.LayoutQwerty

class AlphabetInputMode(
    override val listener: InputMode.Listener
): SoftInputMode(
    LayoutQwerty.ROWS_LOWER,
    LayoutQwerty.ROWS_UPPER
) {
    override fun onChar(char: Char) {
        listener.onCommit(char.toString())
        if(shiftPressed) {
            shiftPressed = false
            updateInputView()
        }
    }

    override fun onSpecial(type: Keyboard.SpecialKey) {
        when(type) {
            Keyboard.SpecialKey.Space -> onSpace()
            Keyboard.SpecialKey.Return -> onReturn()
            Keyboard.SpecialKey.Delete -> onDelete()
            else -> super.onSpecial(type)
        }
    }

    private fun onSpace() {
        listener.onCommit(" ")
    }

    private fun onReturn() {
        listener.onCommit("\n")
    }

    private fun onDelete() {
        listener.onDelete(1, 0)
    }
}