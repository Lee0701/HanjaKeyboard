package ee.oyatl.hanjakbd

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import ee.oyatl.hanjakbd.input.AlphabetInputMode
import ee.oyatl.hanjakbd.input.HangulInputMode
import ee.oyatl.hanjakbd.input.InputMode

class IMEService: InputMethodService(), InputMode.Listener {

    private val inputModes: List<InputMode> = listOf(
        AlphabetInputMode(this),
        HangulInputMode(this)
    )

    private var currentInputModeIndex: Int = 0
    private val currentInputMode: InputMode get() = inputModes[currentInputModeIndex]

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        inputModes.forEach { it.initView(this) }
        return currentInputMode.getView()
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

    override fun onSwitch(type: InputMode.SwitchType) {
        currentInputModeIndex += 1
        if(currentInputModeIndex !in inputModes.indices) currentInputModeIndex = 0
        onReset()
    }

    override fun onReset() {
        setInputView(currentInputMode.getView())
    }
}