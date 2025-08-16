package ee.oyatl.hanjakbd

object NonHangulConverter {

    val punctTable: Map<String, List<String>> = mapOf(
        "," to listOf("，", "、"),
        "." to listOf("。", "・"),
        "..." to listOf("…"),
        "'" to listOf("「", "」"),
        "\"" to listOf("『", "』"),
        "[" to listOf("【", "】"),
        "<" to listOf("〈", "〉", "《", "》")
    )

    fun convert(text: String): List<Candidate> {
        val result = mutableListOf<Candidate>()
        result += Candidate(-1, text, 0f)
        result += convertFullwidth(text)
        result += convertCJKPunct(text)
        return result.distinct()
    }

    fun convertFullwidth(text: String): Candidate {
        val result = text.map { c ->
            when(c) {
                0x20.toChar() -> 0x3000.toChar()
                in '!' .. '~' -> (c.code - 0x20 + 0xff00).toChar()
                else -> c
            }
        }.joinToString("")
        return Candidate(-1, result, 0f)
    }

    fun convertCJKPunct(text: String): List<Candidate> {
        val result = mutableListOf<Candidate>()
        if(text in punctTable) result += punctTable.getValue(text).map { Candidate(-1, it, 0f, text.length) }
        if(text.firstOrNull().toString() in punctTable) result += punctTable.getValue(text.firstOrNull().toString()).map { Candidate(-1, it, 0f) }
        return result
    }
}