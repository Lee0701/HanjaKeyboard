package ee.oyatl.hanjakbd

import ee.oyatl.hanjakbd.Hangul.toJong
import ee.oyatl.hanjakbd.Hangul.toJung

object Layout2Set {
    val ROWS_LOWER: List<String> = listOf(
        "ㅂㅈㄷㄱㅅㅛㅕㅑㅐㅔ",
        "ㅁㄴㅇㄹㅎㅗㅓㅏㅣ",
        "ㅋㅌㅊㅍㅠㅜㅡ"
    )
    val ROWS_UPPER: List<String> = listOf(
        "ㅃㅉㄸㄲㅆㅛㅕㅑㅒㅖ",
        "ㅁㄴㅇㄹㅎㅗㅓㅏㅣ",
        "ㅋㅌㅊㅍㅠㅜㅡ"
    )
    val COMBINATION_TABLE: Map<Pair<Char, Char>, Char> = mapOf(
        toJung('ㅗ')!! to toJung('ㅏ')!! to toJung('ㅘ')!!,
        toJung('ㅗ')!! to toJung('ㅐ')!! to toJung('ㅙ')!!,
        toJung('ㅗ')!! to toJung('ㅣ')!! to toJung('ㅚ')!!,
        toJung('ㅜ')!! to toJung('ㅓ')!! to toJung('ㅝ')!!,
        toJung('ㅜ')!! to toJung('ㅔ')!! to toJung('ㅞ')!!,
        toJung('ㅜ')!! to toJung('ㅣ')!! to toJung('ㅟ')!!,
        toJung('ㅡ')!! to toJung('ㅣ')!! to toJung('ㅢ')!!,
        toJong('ㄱ')!! to toJong('ㅅ')!! to toJong('ㄳ')!!,
        toJong('ㄴ')!! to toJong('ㅈ')!! to toJong('ㄵ')!!,
        toJong('ㄴ')!! to toJong('ㅎ')!! to toJong('ㄶ')!!,
        toJong('ㄹ')!! to toJong('ㄱ')!! to toJong('ㄺ')!!,
        toJong('ㄹ')!! to toJong('ㅁ')!! to toJong('ㄻ')!!,
        toJong('ㄹ')!! to toJong('ㅂ')!! to toJong('ㄼ')!!,
        toJong('ㄹ')!! to toJong('ㅅ')!! to toJong('ㄽ')!!,
        toJong('ㄹ')!! to toJong('ㅌ')!! to toJong('ㄾ')!!,
        toJong('ㄹ')!! to toJong('ㅍ')!! to toJong('ㄿ')!!,
        toJong('ㄹ')!! to toJong('ㅎ')!! to toJong('ㅀ')!!,
        toJong('ㅂ')!! to toJong('ㅅ')!! to toJong('ㅄ')!!,
    )
}