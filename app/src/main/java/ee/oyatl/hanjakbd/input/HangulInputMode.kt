package ee.oyatl.hanjakbd.input

import android.content.Context
import android.view.View
import android.view.ViewGroup
import ee.oyatl.hanjakbd.Candidate
import ee.oyatl.hanjakbd.CandidateView
import ee.oyatl.hanjakbd.dictionary.DiskDictionary
import ee.oyatl.hanjakbd.Hangul
import ee.oyatl.hanjakbd.HangulComposer
import ee.oyatl.hanjakbd.layout.Layout2Set
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.WordComposer
import java.text.Normalizer

class HangulInputMode(
    override val listener: InputMode.Listener
): SoftInputMode(
    Layout2Set.ROWS_LOWER,
    Layout2Set.ROWS_UPPER
) {
    private val hangulComposer = HangulComposer(Layout2Set.COMBINATION_TABLE)
    private val wordComposer = WordComposer()

    private lateinit var candidateView: CandidateView

    private lateinit var hanjaDict: DiskDictionary
    private lateinit var hangulDict: DiskDictionary
    private lateinit var adapter: CandidateView.Adapter

    private var candidates: List<Candidate> = listOf()

    override fun initView(context: Context): View {
        hanjaDict = DiskDictionary(context.resources.openRawResource(R.raw.hanja))
        hangulDict = DiskDictionary(context.resources.openRawResource(R.raw.hangul))

        val height = context.resources.getDimensionPixelSize(R.dimen.kbd_key_height)
        candidateView = CandidateView(context, null)
        candidateView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            height
        )
        adapter = CandidateView.Adapter { onItemClick(it) }
        candidateView.adapter = adapter

        val inputView = super.initView(context) as ViewGroup
        inputView.addView(candidateView, 0)
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
                convertWordAndDisplayCandidates()
            }
        } else {
            listener.onCommit(wordComposer.word + " ")
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

    override fun reset() {
        super.reset()
        hangulComposer.reset()
        wordComposer.reset()
    }

    override fun updateInputView() {
        keyboardSet.getView(shiftPressed, candidates.isNotEmpty())
        candidateView.visibility = if(candidates.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateCandidates() {
        adapter.submitList(candidates)
        updateInputView()
    }

    private fun onItemClick(candidate: Candidate) {
        listener.onCommit(candidate.text)
        wordComposer.consume(candidate.text.length)
        listener.onCompose(wordComposer.word)
        if(wordComposer.word.isNotEmpty()) convertWordAndDisplayCandidates()
        else clearCandidates()
        hangulComposer.reset()
    }

    private fun convertWordAndDisplayCandidates() {
        candidates = convert(wordComposer.word)
        if(candidates.isEmpty()) candidates = listOf(Candidate(wordComposer.word, 0f))
        updateCandidates()
    }

    private fun clearCandidates() {
        candidates = emptyList()
        updateCandidates()
    }

    private fun convert(text: String): List<Candidate> {
        return (1 .. text.length).reversed().map { l ->
            val key = text.take(l)
            (hanjaDict.search(key) + hangulDict.search(key))
                .filter { it.result.length == l }
                .sortedByDescending { it.frequency }
                .map { Candidate(it.result, it.frequency.toFloat()) }
        }.flatten()
    }

    private fun normalizeOutput(text: String): String {
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC)
        val compat = nfc.map { Hangul.stdToCompat(it) }.joinToString("")
        return compat
    }
}