package ee.oyatl.hanjakbd

import android.graphics.Rect
import android.graphics.text.LineBreaker
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import ee.oyatl.hanjakbd.databinding.PopupDefinitionBinding
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
    private var popupWindow: PopupWindow? = null

    override fun onCreate() {
        super.onCreate()
        val indexDict = DiskTrieDictionary(resources.openRawResource(R.raw.hanja_index))
        val hanjaDict = DiskHanjaDictionary(resources.openRawResource(R.raw.hanja_content))
        val definitionDict = DiskStringDictionary(resources.openRawResource(R.raw.hanja_definition))
        val dictionarySet = HanjaDictionarySet(indexDict, hanjaDict, definitionDict)
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
        this@IMEService.popupWindow?.dismiss()
        val inputView = currentInputMode.getView()
        val height = resources.displayMetrics.heightPixels - inputView.height - statusBarHeight
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
        view.definition.text = Html.fromHtml(definition)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.definition.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        }
        this@IMEService.popupWindow = popup
    }

    override fun onCloseDefinition() {
        this@IMEService.popupWindow?.dismiss()
        this@IMEService.popupWindow = null
    }
}