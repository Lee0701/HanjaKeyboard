package ee.oyatl.hanjakbd

class HangulComposer(
    private val combinationTable: Map<Pair<Char, Char>, Char>
) {
    private val reversedCombinationTable = combinationTable
        .map { (key, value) -> value to key }.toMap()

    private val history: MutableList<String> = mutableListOf()
    private val last: Char? get() = history.lastOrNull()?.lastOrNull()
    val composing: String? get() = history.lastOrNull()

    fun onChar(char: Char): String {
        when(Hangul.type(char)) {
            Hangul.Type.Consonant -> when(Hangul.type(last)) {
                Hangul.Type.Cho -> {
                    val combination = combinationTable[last to Hangul.toCho(char)]
                    if(combination != null) history += composing?.dropLast(1) + combination
                    else return commitAndCompose(Hangul.toCho(char).toString())
                }
                Hangul.Type.Jung -> {
                    val cho = Hangul.toCho(char)
                    val jong = Hangul.toJong(char)
                    if(jong != ' ') history += composing + jong.toString()
                    else return commitAndCompose(cho.toString())
                }
                Hangul.Type.Jong -> {
                    val combination = combinationTable[last to Hangul.toJong(char)]
                    if(combination != null) history += composing?.dropLast(1) + combination
                    else return commitAndCompose(Hangul.toCho(char).toString())
                }
                else -> return commitAndCompose(Hangul.toCho(char).toString())
            }
            Hangul.Type.Vowel -> when(Hangul.type(last)) {
                Hangul.Type.Cho -> {
                    history += composing + Hangul.toJung(char).toString()
                }
                Hangul.Type.Jung -> {
                    val combination = combinationTable[last to Hangul.toJung(char)]
                    if(combination != null) history += composing?.dropLast(1) + combination
                    else return commitAndCompose(Hangul.toJung(char).toString())
                }
                Hangul.Type.Jong -> {
                    val composing = composing ?: return commitComposing()
                    var commit = composing.dropLast(1)
                    val decomposed = reversedCombinationTable[composing.last()]
                    if(decomposed != null) commit += decomposed.first
                    val cho = Hangul.jongToCho(decomposed?.second ?: composing.last()).toString()
                    history.clear()
                    history += cho
                    history += cho + Hangul.toJung(char)
                    return commit
                }
                else -> return commitAndCompose(Hangul.toJung(char).toString())
            }
            else -> return commitComposing() + char.toString()
        }
        return ""
    }

    fun onDelete(): Int {
        if(history.isEmpty()) return 1
        history.removeLastOrNull()
        return 0
    }

    fun onReverse(char: Char) {
        val nfd = Hangul.nfd(char.toString())
        if(nfd.length < 2) return
        val cho = nfd[0]
        val jung = nfd[1]
        val jong = if(nfd.length == 3) nfd[2] else null
        history += cho.toString()
        val revJung = reversedCombinationTable[jung]
        if(revJung != null) {
            val (first, _) = revJung
            history += "$cho$first"
        }
        history += "$cho$jung"
        if(jong != null) {
            val revJong = reversedCombinationTable[jong]
            if(revJong != null) {
                val (first, _) = revJong
                history += "$cho$jung$first"
            }
            history += "$cho$jung$jong"
        }
    }

    fun reset() {
        history.clear()
    }

    private fun commitComposing(): String {
        val composing = composing
        history.clear()
        return composing.orEmpty()
    }

    private fun commitAndCompose(compose: String): String {
        val commit = commitComposing()
        history += compose
        return commit
    }
}