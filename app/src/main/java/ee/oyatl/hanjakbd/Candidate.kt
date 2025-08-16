package ee.oyatl.hanjakbd

data class Candidate(
    val index: Int,
    val text: String,
    val score: Float,
    val length: Int = text.length
)
