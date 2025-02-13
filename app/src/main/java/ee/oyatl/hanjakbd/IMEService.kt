package ee.oyatl.hanjakbd

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import ee.oyatl.hanjakbd.input.HangulInputMode
import ee.oyatl.hanjakbd.input.InputMode

class IMEService: InputMethodService(), InputMode.Listener {

    private val inputModes: List<InputMode> = listOf(
        HangulInputMode(this)
    )

    private var currentInputModeIndex: Int = 0
    private val currentInputMode: InputMode get() = inputModes[currentInputModeIndex]

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        return currentInputMode.initView(this)
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        onReset()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onCompose(text: String) {
        currentInputConnection?.setComposingText(text, 1)
    }

    override fun onCommit(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    override fun onDelete(before: Int, after: Int) {
        currentInputConnection?.deleteSurroundingText(before, after)
    }

    override fun onReset() {
        setInputView(currentInputMode.getView())
    }
}