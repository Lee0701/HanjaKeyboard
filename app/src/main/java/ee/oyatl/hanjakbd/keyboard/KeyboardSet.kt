package ee.oyatl.hanjakbd.keyboard

import android.content.Context
import android.view.View

interface KeyboardSet {
    fun initView(context: Context): View
    fun getView(shifted: Boolean, candidates: Boolean): View
}