package ee.oyatl.hanjakbd.dictionary

import java.io.InputStream
import java.lang.StringBuilder
import java.nio.ByteBuffer

class DiskHanjaDictionary(input: InputStream) {
    private val data = ByteBuffer.wrap(input.readBytes())

    fun get(index: Int): HanjaDictionary.Entry {
        var p = data.getInt(index * 4)
        val key = getChars(data, p)
        p += key.length*2 + 2
        val value = getChars(data, p)
        p += value.length*2 + 2
        val frequency = data.getInt(p)
        p += 4
        val extra = getChars(data, p)
        p += extra.length*2 + 2
        return HanjaDictionary.Entry(key, value, frequency, extra)
    }

    private fun getChars(bb: ByteBuffer, idx: Int): String {
        val sb = StringBuilder()
        var i = 0
        while(true) {
            val c = bb.getChar(idx + i)
            if(c.code == 0) break
            sb.append(c)
            i += 2
        }
        return sb.toString()
    }
}