package ee.oyatl.hanjakbd.dictionary

import java.io.DataOutputStream
import java.io.File

fun main(args: Array<String>) {
    val (input, indexDictOut, contentDictOut, definitionDictOut) = args
    val indexDict = TrieDictionary()
    val contentDict = HanjaDictionary()
    val definitionDict = StringDictionary()
    var i = 0
    File(input).forEachLine { line ->
        val tokens = line.split('\t')
        if(tokens.size == 4) {
            val (hangul, hanja, freq, definition) = tokens
            indexDict.insert(hangul, i)
            contentDict.insert(hangul, hanja, freq.toInt(), "")
            definitionDict.insert(definition)
            i += 1
        }
    }
    indexDict.write(DataOutputStream(File(indexDictOut).outputStream()))
    contentDict.write(DataOutputStream(File(contentDictOut).outputStream()))
    definitionDict.write(DataOutputStream(File(definitionDictOut).outputStream()))
}