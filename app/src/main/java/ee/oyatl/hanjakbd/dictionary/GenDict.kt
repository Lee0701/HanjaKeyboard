package ee.oyatl.hanjakbd.dictionary

import java.io.DataOutputStream
import java.io.File

fun main(args: Array<String>) {
    val (input, hangulTrieOutput, contentOutput) = args
    val hangulTrie = TrieDictionary()
    val contentDict = HanjaDictionary()
    var i = 0
    File(input).forEachLine { line ->
        val tokens = line.split('\t')
        if(tokens.size == 4) {
            val (hangul, hanja, freq, definition) = tokens
            hangulTrie.insert(hangul, i)
            contentDict.insert(hangul, hanja, freq.toInt(), "")
            i += 1
        }
    }
    hangulTrie.write(DataOutputStream(File(hangulTrieOutput).outputStream()))
    contentDict.write(DataOutputStream(File(contentOutput).outputStream()))
}