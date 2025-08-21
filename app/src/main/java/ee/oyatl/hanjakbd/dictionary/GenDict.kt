package ee.oyatl.hanjakbd.dictionary

import java.io.DataOutputStream
import java.io.File

fun main(args: Array<String>) {
    val (input, outDir) = args
    val indexDict = TrieDictionary()
    val revIndexDict = TrieDictionary()
    val contentDict = HanjaDictionary()
    val definitionDict = StringDictionary()
    var i = 0
    File(input).forEachLine { line ->
        val tokens = line.split('\t')
        if(tokens.size == 4) {
            val (hangul, hanja, freq, definition) = tokens
            indexDict.insert(hangul, i)
            revIndexDict.insert(hanja, i)
            contentDict.insert(hangul, hanja, freq.toInt(), "")
            definitionDict.insert(definition)
            i += 1
        }
    }
    val outDirFile = File(outDir)
    indexDict.write(DataOutputStream(File(outDirFile, "hanja_index.bin").outputStream()))
    revIndexDict.write(DataOutputStream(File(outDirFile, "hanja_rev_index.bin").outputStream()))
    contentDict.write(DataOutputStream(File(outDirFile, "hanja_content.bin").outputStream()))
    definitionDict.write(DataOutputStream(File(outDirFile, "hanja_definition.bin").outputStream()))
}