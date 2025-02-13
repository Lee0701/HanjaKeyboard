package ee.oyatl.hanjakbd.input

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

    override fun onSpace() {
        listener.onCommit(" ")
    }

    override fun onDelete() {
        listener.onDelete(1, 0)
    }
}