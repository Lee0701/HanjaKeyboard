package ee.oyatl.hanjakbd.input

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearSnapHelper
import ee.oyatl.hanjakbd.Candidate
import ee.oyatl.hanjakbd.CandidateView
import ee.oyatl.hanjakbd.Hangul
import ee.oyatl.hanjakbd.HangulComposer
import ee.oyatl.hanjakbd.NonHangulConverter
import ee.oyatl.hanjakbd.R
import ee.oyatl.hanjakbd.WordComposer
import ee.oyatl.hanjakbd.databinding.CandidateViewBinding
import ee.oyatl.hanjakbd.dictionary.DiskHanjaDictionary
import ee.oyatl.hanjakbd.dictionary.DiskStringDictionary
import ee.oyatl.hanjakbd.dictionary.DiskTrieDictionary
import ee.oyatl.hanjakbd.keyboard.Keyboard
import ee.oyatl.hanjakbd.keyboard.KeyboardConfig
import java.text.Normalizer

class HangulInputMode(
    config: KeyboardConfig,
    override val listener: InputMode.Listener,
    val hangulListener: Listener,
    normalLayout: List<String>,
    shiftedLayout: List<String>,
    combinationTable: Map<Pair<Char, Char>, Char>,
    autoReleaseShift: Boolean = true
): SoftInputMode(config, normalLayout, shiftedLayout, autoReleaseShift) {
    private val hangulComposer = HangulComposer(combinationTable)
    private val wordComposer = WordComposer()

    private lateinit var candidateView: CandidateViewBinding

    private lateinit var indexDict: DiskTrieDictionary
    private lateinit var hanjaDict: DiskHanjaDictionary
    private lateinit var definitionDict: DiskStringDictionary
    private lateinit var adapter: CandidateView.Adapter

    private var candidates: List<Candidate> = listOf()

    override fun initView(context: Context): View {
        indexDict = DiskTrieDictionary(context.resources.openRawResource(R.raw.hanja_index))
        hanjaDict = DiskHanjaDictionary(context.resources.openRawResource(R.raw.hanja_content))
        definitionDict = DiskStringDictionary(context.resources.openRawResource(R.raw.hanja_definition))

        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            config.numberRowHeight.toFloat(),
            context.resources.displayMetrics
        ).toInt()
        val layoutInflater = LayoutInflater.from(context)
        candidateView = CandidateViewBinding.inflate(layoutInflater)
        candidateView.root.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            height
        )
        adapter = CandidateView.Adapter(
            onItemClick = { onItemClick(it) },
            onItemLongClick = { onItemLongClick(it) }
        )
        candidateView.recyclerView.adapter = adapter

        val inputView = super.initView(context) as LinearLayout
        inputView.addView(candidateView.root, 0)
        return inputView
    }

    override fun onChar(char: Char) {
        if(candidates.isNotEmpty()) {
            listener.onCommit(wordComposer.word)
            reset()
        }
        val commit = normalizeOutput(hangulComposer.onChar(char))
        val compose = normalizeOutput(hangulComposer.composing.orEmpty())
        if(commit.isNotEmpty()) commit.forEach { wordComposer.commit(it.toString()) }
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
        candidateView.root.visibility = if(candidates.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateCandidates() {
        adapter.submitList(candidates)
        updateInputView()
        hangulListener.onCloseDefinition()
    }

    private fun onItemClick(candidate: Candidate) {
        listener.onCommit(candidate.text)
        wordComposer.consume(candidate.length)
        listener.onCompose(wordComposer.word)
        if(wordComposer.word.isNotEmpty()) convertWordAndDisplayCandidates()
        else clearCandidates()
        hangulComposer.reset()
    }

    private fun onItemLongClick(candidate: Candidate) {
        if(candidate.index < 0) return
        val (hangul, hanja) = hanjaDict.get(candidate.index)
        val definition = definitionDict.get(candidate.index)
        hangulListener.onDefinition(hangul, hanja, definition)
    }

    private fun convertWordAndDisplayCandidates() {
        candidates = convert(wordComposer.word)
        updateCandidates()
    }

    private fun clearCandidates() {
        candidates = emptyList()
        updateCandidates()
    }

    private fun convert(text: String): List<Candidate> {
        if(text.isNotEmpty() && Hangul.type(text[0]) == Hangul.Type.NonHangul) {
            val subtext = (1 .. text.length).reversed()
                .map { l -> text.take(l) }
                .find { t -> t.all { Hangul.type(it) == Hangul.Type.NonHangul } }
            return nonHangulConvert(subtext ?: return emptyList())
        }
        val hanjaResult = (1 .. text.length).map { l ->
            indexDict.search(text.take(l))
                .map { it to hanjaDict.get(it) }
                .map { Candidate(it.first, it.second.hanja, it.second.frequency.toFloat()) }
                .filter { it.text.length == l }
        }.flatten()
            .sortedByDescending { it.score }
            .sortedByDescending { it.text.length }
        return getDefaultCandidates(text) + hanjaResult
    }

    private fun nonHangulConvert(text: String): List<Candidate> {
        return NonHangulConverter.convert(text)
    }

    private fun getDefaultCandidates(text: String): List<Candidate> {
        return listOf(
            Candidate(-1, wordComposer.word, 0f),
            Candidate(-1, wordComposer.word.firstOrNull()?.toString().orEmpty(), 0f)
        ).filter { it.text.isNotEmpty() }.distinct()
    }

    private fun normalizeOutput(text: String): String {
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC)
        val compat = nfc.map { Hangul.stdToCompat(it) }.joinToString("")
        return compat
    }

    interface Listener {
        fun onDefinition(hangul: String, hanja: String, definition: String)
        fun onCloseDefinition()
    }
}