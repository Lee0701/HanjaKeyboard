package ee.oyatl.hanjakbd

import java.text.Normalizer

object Hangul {
    const val CHO = "ᄀᄁᄂᄃᄄᄅᄆᄇᄈᄉᄊᄋᄌᄍᄎᄏᄐᄑᄒ"
    const val JUNG = "ᅡᅢᅣᅤᅥᅦᅧᅨᅩᅪᅫᅬᅭᅮᅯᅰᅱᅲᅳᅴᅵ"
    const val JONG = "ᆨᆩᆪᆫᆬᆭᆮᆯᆰᆱᆲᆳᆴᆵᆶᆷᆸᆹᆺᆻᆼᆽᆾᆿᇀᇁᇂ"

    const val CONSONANT = "ㄱㄲㄳㄴㄵㄶㄷㄸㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅃㅄㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
    const val VOWEL = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"

    const val CVT_CHO = "ᄀᄁ ᄂ  ᄃᄄᄅ       ᄆᄇᄈ ᄉᄊᄋᄌᄍᄎᄏᄐᄑᄒ"
    const val CVT_JONG = "ᆨᆩᆪᆫᆬᆭᆮ ᆯᆰᆱᆲᆳᆴᆵᆶᆷᆸ ᆹᆺᆻᆼᆽ ᆾᆿᇀᇁᇂ"

    fun nfc(text: CharSequence): CharSequence {
        return Normalizer.normalize(text, Normalizer.Form.NFC)
    }

    fun nfd(text: CharSequence): CharSequence {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
    }

    fun isSyllable(char: Char): Boolean = char.code in 0xac00 .. 0xd7a3

    fun isCho(char: Char): Boolean = char in CHO
    fun isJung(char: Char): Boolean = char in JUNG
    fun isJong(char: Char): Boolean = char in JONG

    fun isConsonant(char: Char): Boolean = char in CONSONANT
    fun isVowel(char: Char): Boolean = char in VOWEL

    fun toConsonant(char: Char): Char? = (CONSONANT + CONSONANT).getOrNull((CVT_CHO + CVT_JONG).indexOf(char))
    fun toVowel(char: Char): Char? = VOWEL.getOrNull(JUNG.indexOf(char))

    fun toCho(char: Char): Char? = CVT_CHO.getOrNull(CONSONANT.indexOf(char))
    fun toJong(char: Char): Char? = CVT_JONG.getOrNull(CONSONANT.indexOf(char))
    fun toJung(char: Char): Char? = JUNG.getOrNull(VOWEL.indexOf(char))

    fun jongToCho(char: Char): Char? = toConsonant(char)?.let { toCho(it) }

    fun stdToCompat(char: Char): Char? =
        if(isCho(char) || isJong(char)) toConsonant(char)
        else if(isJung(char)) toVowel(char)
        else char

    fun type(char: Char?): Type {
        return when {
            char == null -> Type.NonHangul
            isSyllable(char) -> Type.Syllable
            isCho(char) -> Type.Cho
            isJung(char) -> Type.Jung
            isJong(char) -> Type.Jong
            isConsonant(char) -> Type.Consonant
            isVowel(char) -> Type.Vowel
            else -> Type.NonHangul
        }
    }

    enum class Type {
        NonHangul, Syllable, Cho, Jung, Jong, Consonant, Vowel
    }
}