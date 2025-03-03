package ee.oyatl.hanjakbd.input

import ee.oyatl.hanjakbd.keyboard.Keyboard

class AlphabetInputMode(
    override val listener: InputMode.Listener,
    normalLayout: List<String>,
    shiftedLayout: List<String>,
    autoReleaseShift: Boolean = true
): SoftInputMode(normalLayout, shiftedLayout, autoReleaseShift) {

    override fun onChar(char: Char) {
        listener.onCommit(char.toString())
        autoReleaseShift()
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
        listener.onEditorAction()
    }

    private fun onDelete() {
        listener.onDelete(1, 0)
        listener.onCompose("")
    }
}