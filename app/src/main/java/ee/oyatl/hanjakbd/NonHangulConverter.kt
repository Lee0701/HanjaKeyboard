package ee.oyatl.hanjakbd

object NonHangulConverter {

    val punctTable: Map<String, List<String>> = mapOf(
        "," to listOf("，", "、"),
        "." to listOf("。", "・"),
        "..." to listOf("…"),
        "'" to listOf("「", "」"),
        "\"" to listOf("『", "』"),
        "[" to listOf("「", "【"),
        "]" to listOf("」", "】"),
        "{" to listOf("『"),
        "}" to listOf("』"),
        "<" to listOf("〈", "《"),
        ">" to listOf("〉", "》"),
        "=" to listOf("々", "〃")
    )

    val cjkNumberTable: Map<Char, Char> = mapOf(
        '0' to '〇',
        '1' to '一',
        '2' to '二',
        '3' to '三',
        '4' to '四',
        '5' to '五',
        '6' to '六',
        '7' to '七',
        '8' to '八',
        '9' to '九'
    )

    val cjkFullNumberTable: Map<Char, Char> = mapOf(
        '0' to '零',
        '1' to '壹',
        '2' to '貳',
        '3' to '參',
        '4' to '髟',
        '5' to '伍',
        '6' to '陸',
        '7' to '柒',
        '8' to '捌',
        '9' to '玖'
    )

    fun convert(text: String): List<Candidate> {
        val result = mutableListOf<Candidate>()
        result += Candidate(-1, text, 0f)
        result += convertFullwidth(text)
        result += convertCJKNumber(text)
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

    fun convertCJKNumber(text: String): List<Candidate> {
        val result = mutableListOf<Candidate>()
        val cjkNumber = text.mapNotNull { c -> cjkNumberTable[c] }.joinToString("")
        if(cjkNumber.length == text.length) result += Candidate(-1, cjkNumber, 0f, cjkNumber.length)
        val cjkFullNumber = text.mapNotNull { c -> cjkFullNumberTable[c] }.joinToString("")
        if(cjkFullNumber.length == text.length) result += Candidate(-1, cjkFullNumber, 0f, cjkFullNumber.length)
        return result
    }
}