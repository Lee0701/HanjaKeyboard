package ee.oyatl.hanjakbd

class WordComposer {
    private val history: MutableList<String> = mutableListOf()
    private var composing: String = ""
    val word: String get() = history.lastOrNull().orEmpty() + composing

    fun compose(text: String) {
        composing = text
    }

    fun commit(text: String) {
        history += history.lastOrNull().orEmpty() + text
        composing = ""
    }

    fun delete(length: Int): Boolean {
        (0 until length).forEach { _ ->
            history.removeLastOrNull() ?: return false
        }
        return true
    }

    fun reset() {
        history.clear()
        composing = ""
    }
}