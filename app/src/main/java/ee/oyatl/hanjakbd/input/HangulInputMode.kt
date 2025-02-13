package ee.oyatl.hanjakbd.input

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import ee.oyatl.hanjakbd.Candidate
import ee.oyatl.hanjakbd.CandidateView
import ee.oyatl.hanjakbd.DiskDictionary
import ee.oyatl.hanjakbd.Hangul
import ee.oyatl.hanjakbd.HangulComposer
import ee.oyatl.hanjakbd.layout.Layout2Set
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.WordComposer
import ee.oyatl.hanjakbd.keyboard.DefaultKeyboardSet
import ee.oyatl.hanjakbd.keyboard.KeyboardSet
import java.text.Normalizer

class HangulInputMode(
    override val listener: InputMode.Listener
): InputMode {

    private val hangulComposer = HangulComposer(Layout2Set.COMBINATION_TABLE)
    private val wordComposer = WordComposer()

    private lateinit var inputView: LinearLayout
    private lateinit var keyboardSet: KeyboardSet
    private lateinit var candidateView: CandidateView

    private lateinit var dictionary: DiskDictionary
    private lateinit var adapter: CandidateView.Adapter

    private var shiftPressed: Boolean = false
    private var candidates: List<Candidate> = listOf()

    override fun initView(context: Context): View {
        dictionary = DiskDictionary(context.resources.openRawResource(R.raw.dict))

        keyboardSet = DefaultKeyboardSet(this, Layout2Set.ROWS_LOWER, Layout2Set.ROWS_UPPER)

        val height = context.resources.getDimensionPixelSize(R.dimen.kbd_key_height)
        candidateView = CandidateView(context, null)
        candidateView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            height
        )
        adapter = CandidateView.Adapter { onItemClick(it) }
        candidateView.adapter = adapter

        inputView = LinearLayout(context)
        inputView.orientation = LinearLayout.VERTICAL
        inputView.addView(candidateView)
        inputView.addView(keyboardSet.initView(context))
        return inputView
    }

    override fun getView(): View {
        keyboardSet.getView(shiftPressed, candidates.isNotEmpty())
        candidateView.visibility = if(candidates.isEmpty()) View.GONE else View.VISIBLE
        return inputView
    }

    override fun onChar(char: Char) {
        if(candidates.isNotEmpty()) {
            candidates = listOf()
            updateCandidates()
        }
        val commit = normalizeOutput(hangulComposer.onChar(char))
        val compose = normalizeOutput(hangulComposer.composing.orEmpty())
        if(commit.isNotEmpty()) wordComposer.commit(commit)
        wordComposer.compose(compose)
        listener.onCompose(wordComposer.word)

        if(shiftPressed) {
            shiftPressed = false
            updateInputView()
        }
    }

    override fun onSpace() {
        if(candidates.isEmpty()) {
            if(wordComposer.word.isEmpty()) {
                listener.onCommit(" ")
                reset()
            } else {
                candidates = dictionary.search(wordComposer.word)
                    .map { Candidate(it.result, it.frequency.toFloat()) }
                    .sortedByDescending { it.score }
                candidates = listOf(Candidate(wordComposer.word, 0f)) + candidates
                updateCandidates()
            }
        } else {
            listener.onCommit(candidates.firstOrNull()?.text.orEmpty() + " ")
            candidates = listOf()
            updateCandidates()
            reset()
        }
    }

    override fun onDelete() {
        if(candidates.isNotEmpty()) {
            candidates = listOf()
            updateCandidates()
        } else {
            val length = hangulComposer.onDelete()
            val compose = normalizeOutput(hangulComposer.composing.orEmpty())
            val result = wordComposer.delete(length)
            if(!result) listener.onDelete(1, 0)
            wordComposer.compose(compose)
            listener.onCompose(wordComposer.word)
        }
    }

    override fun onShift() {
        shiftPressed = !shiftPressed
        updateInputView()
    }

    override fun reset() {
        hangulComposer.reset()
        wordComposer.reset()
    }

    private fun updateCandidates() {
        adapter.submitList(candidates)
        updateInputView()
    }

    private fun updateInputView() {
        keyboardSet.getView(shiftPressed, candidates.isNotEmpty())
        listener.onReset()
        candidateView.visibility = if(candidates.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun onItemClick(candidate: Candidate) {
        listener.onCommit(candidate.text)
        candidates = listOf()
        updateCandidates()
        reset()
    }

    private fun normalizeOutput(text: String): String {
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC)
        val compat = nfc.map { Hangul.stdToCompat(it) }.joinToString("")
        return compat
    }
}