package ee.oyatl.hanjakbd

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import java.text.Normalizer

class IMEService: InputMethodService(), Keyboard.Listener {

    private val hangulComposer = HangulComposer(Layout2Set.COMBINATION_TABLE)
    private val wordComposer = WordComposer()

    private lateinit var candidateView: CandidateView
    private lateinit var normalKeyboardView: View
    private lateinit var shiftedKeyboardView: View
    private lateinit var numberRowKeyboardView: View

    private lateinit var dictionary: DiskDictionary
    private lateinit var adapter: CandidateView.Adapter

    private var shiftPressed: Boolean = false
    private var candidates: List<Candidate> = listOf()

    override fun onCreate() {
        super.onCreate()
        dictionary = DiskDictionary(resources.openRawResource(R.raw.dict))
    }

    override fun onCreateInputView(): View {
        normalKeyboardView = DefaultMobileKeyboard(this, Layout2Set.ROWS_LOWER).createView(this)
        shiftedKeyboardView = DefaultMobileKeyboard(this, Layout2Set.ROWS_UPPER).createView(this)
        val mainKeyboardView = FrameLayout(this)
        mainKeyboardView.addView(normalKeyboardView)
        mainKeyboardView.addView(shiftedKeyboardView)
        val bottomRowKeyboardView = DefaultBottomRowKeyboard(this).createView(this)
        numberRowKeyboardView = DefaultNumberRowKeyboard(this).createView(this)

        candidateView = CandidateView(this, null)
        adapter = CandidateView.Adapter { onItemClick(it) }
        candidateView.adapter = adapter

        val topRowView = FrameLayout(this)
        topRowView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.kbd_key_height)
        )
        topRowView.addView(candidateView)
        topRowView.addView(numberRowKeyboardView)

        val inputView = LinearLayout(this)
        inputView.orientation = LinearLayout.VERTICAL
        inputView.addView(topRowView)
        inputView.addView(mainKeyboardView)
        inputView.addView(bottomRowKeyboardView)
        return inputView
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    private fun reset() {
        hangulComposer.reset()
        wordComposer.reset()
        numberRowKeyboardView.bringToFront()
    }

    private fun updateCandidates() {
        adapter.submitList(candidates)
        if(candidates.isEmpty()) numberRowKeyboardView.bringToFront()
        else candidateView.bringToFront()
    }

    private fun onItemClick(candidate: Candidate) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(candidate.text, 1)
        candidates = listOf()
        updateCandidates()
        reset()
    }

    override fun onChar(char: Char) {
        val inputConnection = currentInputConnection ?: return
        if(candidates.isNotEmpty()) {
            candidates = listOf()
            updateCandidates()
        }
        val commit = normalizeOutput(hangulComposer.onChar(char))
        val compose = normalizeOutput(hangulComposer.composing.orEmpty())
        if(commit.isNotEmpty()) wordComposer.commit(commit)
        wordComposer.compose(compose)
        inputConnection.setComposingText(wordComposer.word, 1)

        if(shiftPressed) {
            shiftPressed = false
            updateShiftState()
        }
    }

    override fun onSpace() {
        val inputConnection = currentInputConnection ?: return
        if(candidates.isEmpty()) {
            if(wordComposer.word.isEmpty()) {
                inputConnection.commitText(" ", 1)
                reset()
            } else {
                candidates = dictionary.search(wordComposer.word)
                    .map { Candidate(it.result, it.frequency.toFloat()) }
                    .sortedByDescending { it.score }
                candidates = listOf(Candidate(wordComposer.word, 0f)) + candidates
                updateCandidates()
            }
        } else {
            inputConnection.commitText(candidates.firstOrNull()?.text.orEmpty() + " ", 1)
            candidates = listOf()
            updateCandidates()
            reset()
        }
    }

    override fun onDelete() {
        val inputConnection = currentInputConnection ?: return
        if(candidates.isNotEmpty()) {
            candidates = listOf()
            updateCandidates()
        } else {
            val length = hangulComposer.onDelete()
            val compose = normalizeOutput(hangulComposer.composing.orEmpty())
            val result = wordComposer.delete(length)
            if(!result) inputConnection.deleteSurroundingText(1, 0)
            wordComposer.compose(compose)
            inputConnection.setComposingText(wordComposer.word, 1)
        }
    }

    override fun onShift() {
        shiftPressed = !shiftPressed
        updateShiftState()
    }

    private fun updateShiftState() {
        if(shiftPressed) shiftedKeyboardView.bringToFront()
        else normalKeyboardView.bringToFront()
    }

    private fun normalizeOutput(text: String): String {
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC)
        val compat = nfc.map { Hangul.stdToCompat(it) }.joinToString("")
        return compat
    }

}