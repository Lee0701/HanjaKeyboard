package ee.oyatl.hanjakbd

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import ee.oyatl.hanjakbd.databinding.PopupDefinitionBinding
import ee.oyatl.hanjakbd.input.AlphabetInputMode
import ee.oyatl.hanjakbd.input.HangulInputMode
import ee.oyatl.hanjakbd.input.InputMode
import ee.oyatl.hanjakbd.layout.Layout2Set
import ee.oyatl.hanjakbd.layout.LayoutQwerty
import ee.oyatl.hanjakbd.layout.LayoutSymbol

class IMEService: InputMethodService(), InputMode.Listener {

    private val inputModes: List<List<InputMode>>

    private var inputModeIndex: Int = 0
    private var inputSubModeIndex: Int = 0
    private val currentInputMode: InputMode get() = inputModes[inputModeIndex][inputSubModeIndex]

    private var popupWindow: PopupWindow? = null

    private val hangulModeListener: HangulInputMode.Listener = object: HangulInputMode.Listener {
        override fun onDefinition(
            hangul: String,
            hanja: String,
            definition: String
        ) {
            this@IMEService.popupWindow?.dismiss()
            val inputView = currentInputMode.getView()
            val height = resources.displayMetrics.heightPixels - inputView.height
            val y = -resources.displayMetrics.heightPixels + inputView.height
            val view = PopupDefinitionBinding.inflate(layoutInflater, null, false)
            val popup = PopupWindow(
                view.root,
                ViewGroup.LayoutParams.MATCH_PARENT,
                height,
                false
            )
            popup.showAtLocation(inputView, Gravity.TOP, 0, y)
            view.hanja.text = hanja
            view.hangul.text = hangul
            view.definition.text = definition.replace("\\n", "\n\n")
            this@IMEService.popupWindow = popup
        }

        override fun onCloseDefinition() {
            this@IMEService.popupWindow?.dismiss()
            this@IMEService.popupWindow = null
        }
    }

    init {
        val qwerty = AlphabetInputMode(this, LayoutQwerty.ROWS_LOWER, LayoutQwerty.ROWS_UPPER)
        val hangul = HangulInputMode(this, hangulModeListener, Layout2Set.ROWS_LOWER, Layout2Set.ROWS_UPPER, Layout2Set.COMBINATION_TABLE)
        val symbols = AlphabetInputMode(this, LayoutSymbol.ROWS_LOWER, LayoutSymbol.ROWS_UPPER, autoReleaseShift = false)
        this.inputModes = listOf(
            listOf(qwerty, symbols),
            listOf(hangul, symbols)
        )
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        inputModes.flatten().toSet().forEach { it.initView(this) }
        return currentInputMode.getView()
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        currentInputMode.reset()
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

    override fun onEditorAction() {
        val actionId = currentInputEditorInfo?.actionId
        if(actionId != null) currentInputConnection?.performEditorAction(actionId)
        else currentInputConnection?.commitText("\n", 1)
    }

    override fun onSwitch(type: InputMode.SwitchType) {
        currentInputConnection?.finishComposingText()
        currentInputMode.reset()
        when(type) {
            InputMode.SwitchType.NextInputMode -> {
                inputSubModeIndex = 0
                inputModeIndex += 1
                if(inputModeIndex !in inputModes.indices) inputModeIndex = 0
            }
            InputMode.SwitchType.ToggleSymbolMode -> {
                inputSubModeIndex += 1
                if(inputSubModeIndex !in inputModes[inputModeIndex].indices) inputSubModeIndex = 0
            }
        }
        onReset()
    }

    override fun onReset() {
        setInputView(currentInputMode.getView())
    }

}