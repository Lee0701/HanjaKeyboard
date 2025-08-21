package ee.oyatl.hanjakbd.input

import ee.oyatl.hanjakbd.dictionary.DiskHanjaDictionary
import ee.oyatl.hanjakbd.dictionary.DiskStringDictionary
import ee.oyatl.hanjakbd.dictionary.DiskTrieDictionary

data class HanjaDictionarySet(
    val indexDict: DiskTrieDictionary,
    val hanjaDict: DiskHanjaDictionary,
    val definitionDict: DiskStringDictionary?,
    val revIndexDict: DiskTrieDictionary?
)