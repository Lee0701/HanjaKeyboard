package ee.oyatl.hanjakbd

import android.graphics.Rect
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import ee.oyatl.hanjakbd.dictionary.DiskHanjaDictionary
import ee.oyatl.hanjakbd.dictionary.DiskStringDictionary
import ee.oyatl.hanjakbd.dictionary.DiskTrieDictionary
import ee.oyatl.hanjakbd.input.AlphabetInputMode
import ee.oyatl.hanjakbd.input.HangulInputMode
import ee.oyatl.hanjakbd.input.HanjaDictionarySet
import ee.oyatl.hanjakbd.input.InputMode
import ee.oyatl.hanjakbd.keyboard.KeyboardConfig
import ee.oyatl.hanjakbd.layout.Layout2Set
import ee.oyatl.hanjakbd.layout.LayoutQwerty
import ee.oyatl.hanjakbd.layout.LayoutSymbol

class IMEService: InputMethodService(), InputMode.Listener, HangulInputMode.Listener {

    private var inputModes: List<List<InputMode>> = listOf()

    private var inputModeIndex: Int = 0
    private var inputSubModeIndex: Int = 0
    private val currentInputMode: InputMode get() = inputModes[inputModeIndex][inputSubModeIndex]

    private val rect = Rect()
    private val statusBarHeight: Int get() {
        window.window?.decorView?.getWindowVisibleDisplayFrame(rect) ?: return 0
        return rect.top
    }
    private var definitionPopup: DefinitionPopup? = null

    override fun onCreate() {
        super.onCreate()
        val indexDict = DiskTrieDictionary(resources.openRawResource(R.raw.hanja_index))
        val hanjaDict = DiskHanjaDictionary(resources.openRawResource(R.raw.hanja_content))
        val definitionDict = DiskStringDictionary(resources.openRawResource(R.raw.hanja_definition))
        val revIndexDict = DiskTrieDictionary(resources.openRawResource(R.raw.hanja_rev_index))
        val dictionarySet = HanjaDictionarySet(indexDict, hanjaDict, definitionDict, revIndexDict)
        val keyboardConfig = KeyboardConfig()
        val qwerty = AlphabetInputMode(keyboardConfig, this, LayoutQwerty.ROWS_LOWER, LayoutQwerty.ROWS_UPPER)
        val qwertySymbols = AlphabetInputMode(keyboardConfig, this, LayoutSymbol.ROWS_LOWER, LayoutSymbol.ROWS_UPPER, autoReleaseShift = false)
        val hangul = HangulInputMode(keyboardConfig, dictionarySet, this, Layout2Set.ROWS_LOWER, Layout2Set.ROWS_UPPER, Layout2Set.COMBINATION_TABLE)
        val hangulSymbols = HangulInputMode(keyboardConfig, dictionarySet, this, LayoutSymbol.ROWS_LOWER, LayoutSymbol.ROWS_UPPER, mapOf(), autoReleaseShift = false)
        this.inputModes = listOf(
            listOf(qwerty, qwertySymbols),
            listOf(hangul, hangulSymbols)
        )
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

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentInputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_MONITOR)
    }

    override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo?) {
        super.onUpdateCursorAnchorInfo(cursorAnchorInfo)
        val inputConnection = currentInputConnection ?: return
        cursorAnchorInfo ?: return

        // Reset composition if area is selected.
        if(cursorAnchorInfo.selectionStart != cursorAnchorInfo.selectionEnd) {
            val currentInputMode = this.currentInputMode
            inputConnection.finishComposingText()
            currentInputMode.reset()
            if(currentInputMode is HangulInputMode) {
                val selectedText = inputConnection.getSelectedText(0)
                currentInputMode.revSearch(selectedText.toString())
            }
        } else {
            // Reset composition if the cursor is not at the end of the composing text
            val composingStart = cursorAnchorInfo.composingTextStart
            val composingLength = cursorAnchorInfo.composingText?.length ?: 0
            val composingEnd = composingStart + composingLength
            if(cursorAnchorInfo.selectionStart != -1 && cursorAnchorInfo.selectionStart != composingEnd) {
                inputConnection.finishComposingText()
                currentInputMode.reset()
            }
        }
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

    override fun onDefinition(
        hangul: String,
        hanja: String,
        definition: String
    ) {
        val inputView = currentInputMode.getView()
        val y = -resources.displayMetrics.heightPixels + inputView.height
        val height = resources.displayMetrics.heightPixels - inputView.height - statusBarHeight
        definitionPopup?.dismiss()
        definitionPopup = DefinitionPopup(hangul, hanja, definition)
        definitionPopup?.show(inputView, y, height)
    }

    override fun onCloseDefinition() {
        definitionPopup?.dismiss()
        definitionPopup = null
    }
}