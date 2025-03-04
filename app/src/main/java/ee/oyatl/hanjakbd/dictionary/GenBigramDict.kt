package ee.oyatl.hanjakbd.dictionary

import java.io.DataOutputStream
import java.io.File

fun main(args: Array<String>) {
    val (inUnigrams, inBigrams, outVocab, outDict, outBigrams) = args
    val vocab = mutableListOf<DiskVocabDictionary.Entry>()
    File(inUnigrams).forEachLine { line ->
        val tokens = line.split('\t')
        if(tokens.size == 2) {
            val (word, freq) = tokens
            vocab += DiskVocabDictionary.Entry(word, freq.toInt())
        }
    }
    val revVocab = vocab.mapIndexed { i, entry -> entry.result to i }.toMap()
    val unigrams = IndexDictionary()
    revVocab.forEach { (key, index) -> unigrams.insert(key, index) }
    val bigrams = IndexDictionary()
    File(inBigrams).forEachLine { line ->
        val tokens = line.split('\t')
        if(tokens.size == 2) {
            val (grams, freq) = tokens
            val key = grams.split(' ').map { revVocab[it]!!.toChar() }.joinToString("")
            bigrams.insert(key, freq.toInt())
        }
    }
    DiskVocabDictionary.write(File(outVocab).outputStream(), vocab)
    unigrams.write(DataOutputStream(File(outDict).outputStream()))
    bigrams.write(DataOutputStream(File(outBigrams).outputStream()))
}