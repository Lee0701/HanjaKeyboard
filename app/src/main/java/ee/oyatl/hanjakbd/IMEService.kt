package ee.oyatl.hanjakbd

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import ee.oyatl.hanjakbd.keyboard.DefaultKeyboardSet
import ee.oyatl.hanjakbd.keyboard.Keyboard
import ee.oyatl.hanjakbd.keyboard.KeyboardSet
import java.text.Normalizer

class IMEService: InputMethodService(), Keyboard.Listener {

    private val hangulComposer = HangulComposer(Layout2Set.COMBINATION_TABLE)
    private val wordComposer = WordComposer()

    private lateinit var inputView: LinearLayout
    private lateinit var keyboardSet: KeyboardSet
    private lateinit var candidateView: CandidateView

    private lateinit var dictionary: DiskDictionary
    private lateinit var adapter: CandidateView.Adapter

    private var shiftPressed: Boolean = false
    private var candidates: List<Candidate> = listOf()

    override fun onCreate() {
        super.onCreate()
        dictionary = DiskDictionary(resources.openRawResource(R.raw.dict))
    }

    override fun onCreateInputView(): View {
        keyboardSet = DefaultKeyboardSet(this)

        val height = resources.getDimensionPixelSize(R.dimen.kbd_key_height)
        candidateView = CandidateView(this, null)
        candidateView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            height
        )
        adapter = CandidateView.Adapter { onItemClick(it) }
        candidateView.adapter = adapter

        inputView = LinearLayout(this)
        inputView.orientation = LinearLayout.VERTICAL
        inputView.addView(candidateView)
        inputView.addView(keyboardSet.initView(this))
        return inputView
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        updateInputView()
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
    }

    private fun updateCandidates() {
        adapter.submitList(candidates)
        updateInputView()
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
            updateInputView()
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
        updateInputView()
    }

    private fun updateInputView() {
        keyboardSet.getView(shiftPressed, candidates.isNotEmpty())
        setInputView(inputView)
        candidateView.visibility = if(candidates.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun normalizeOutput(text: String): String {
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC)
        val compat = nfc.map { Hangul.stdToCompat(it) }.joinToString("")
        return compat
    }

}