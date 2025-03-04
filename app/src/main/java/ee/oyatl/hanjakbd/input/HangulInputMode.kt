package ee.oyatl.hanjakbd.input

import android.content.Context
import android.view.View
import android.view.ViewGroup
import ee.oyatl.hanjakbd.Candidate
import ee.oyatl.hanjakbd.CandidateView
import ee.oyatl.hanjakbd.Hangul
import ee.oyatl.hanjakbd.HangulComposer
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.WordComposer
import ee.oyatl.hanjakbd.dictionary.DiskDictionary
import ee.oyatl.hanjakbd.dictionary.DiskIndexDictionary
import ee.oyatl.hanjakbd.dictionary.DiskVocabDictionary
import ee.oyatl.hanjakbd.keyboard.Keyboard
import java.text.Normalizer

class HangulInputMode(
    override val listener: InputMode.Listener,
    normalLayout: List<String>,
    shiftedLayout: List<String>,
    combinationTable: Map<Pair<Char, Char>, Char>,
    autoReleaseShift: Boolean = true
): SoftInputMode(normalLayout, shiftedLayout, autoReleaseShift) {
    private val hangulComposer = HangulComposer(combinationTable)
    private val wordComposer = WordComposer()

    private lateinit var candidateView: CandidateView

    private lateinit var hanjaDict: DiskDictionary
    private lateinit var vocabDict: DiskVocabDictionary
    private lateinit var unigramsDict: DiskIndexDictionary
    private lateinit var bigramsDict: DiskIndexDictionary
    private lateinit var adapter: CandidateView.Adapter

    private var candidates: List<Candidate> = listOf()

    override fun initView(context: Context): View {
        hanjaDict = DiskDictionary(context.resources.openRawResource(R.raw.hanja))
        vocabDict = DiskVocabDictionary(context.resources.openRawResource(R.raw.vocab))
        unigramsDict = DiskIndexDictionary((context.resources.openRawResource(R.raw.unigrams)))
        bigramsDict = DiskIndexDictionary(context.resources.openRawResource(R.raw.bigrams))

        val height = context.resources.getDimensionPixelSize(R.dimen.kbd_key_number_height)
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
            listener.onCommit(wordComposer.word)
            reset()
        }
        val commit = normalizeOutput(hangulComposer.onChar(char))
        val compose = normalizeOutput(hangulComposer.composing.orEmpty())
        if(commit.isNotEmpty()) wordComposer.commit(commit)
        wordComposer.compose(compose)
        listener.onCompose(wordComposer.word)
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
        if(candidates.isEmpty()) {
            if(wordComposer.word.isEmpty()) {
                listener.onCommit(" ")
                reset()
            } else {
                convertWordAndDisplayCandidates()
            }
        } else {
            listener.onCommit(wordComposer.word + " ")
            reset()
        }
    }

    private fun onReturn() {
        if(candidates.isEmpty()) {
            listener.onEditorAction()
        } else {
            listener.onCommit(wordComposer.word)
        }
        reset()
    }

    private fun onDelete() {
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
        clearCandidates()
        hangulComposer.reset()
        wordComposer.reset()
    }

    override fun updateInputView() {
        keyboardSet.getView(shiftState, candidates.isNotEmpty())
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
        val hanjaResult = (1 .. text.length).map { l ->
            hanjaDict.search(text.take(l))
                .filter { it.result.length == l }
                .map { Candidate(it.result, it.frequency.toFloat()) }
        }.flatten().sortedByDescending { it.score }
        val bigramResult = (1 .. text.length).asSequence()
            .map { l ->
                val firsts = unigramsDict.search(text.take(l))
                val seconds = (1 .. l + 1).flatMap { l2 ->
                    unigramsDict.search(text.drop(l).take(l2))
                }.take(10)
                firsts.flatMap { first -> seconds.map { second -> listOf(first, second) } }
            }
            .flatten().map { it to bigramsDict.search(it) }
            .filter { (_, value) -> value.isNotEmpty() }
            .map { (key, value) -> key.map { vocabDict[it] } to value }
            .map { (key, value) -> Candidate(
                text = key.joinToString("") { it.result },
                score = key.map { it.frequency }.reduce(Int::times) * value[0].toFloat()
            ) }
            .sortedByDescending { it.score }
            .distinct()
            .toList()
        return (bigramResult + hanjaResult)
            .sortedByDescending { it.score }
            .sortedByDescending { it.text.length }
    }

    private fun normalizeOutput(text: String): String {
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC)
        val compat = nfc.map { Hangul.stdToCompat(it) }.joinToString("")
        return compat
    }
}