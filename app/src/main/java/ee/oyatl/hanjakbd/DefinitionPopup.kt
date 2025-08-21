package ee.oyatl.hanjakbd

import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import ee.oyatl.hanjakbd.databinding.PopupDefinitionBinding

class DefinitionPopup(
    val hangul: String,
    val hanja: String,
    val definition: String
) {
    var popupWindow: PopupWindow? = null

    fun show(inputView: View, y: Int, height: Int) {
        val layoutInflater = LayoutInflater.from(inputView.context)
        val view = PopupDefinitionBinding.inflate(layoutInflater, null, false)
        val popup = PopupWindow(
            view.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            height,
            false
        )
        popup.showAtLocation(inputView, Gravity.TOP, 0, y)
        view.hanja.text = hanja
        view.hangul.text = hangul
        view.definition.text = Html.fromHtml(definition)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.definition.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        }
        this.popupWindow = popup
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }
}