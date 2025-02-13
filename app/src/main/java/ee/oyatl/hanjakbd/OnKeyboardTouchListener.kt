package ee.oyatl.hanjakbd

import android.view.MotionEvent
import android.view.View

class OnKeyboardTouchListener(
    keyboardView: View
): View.OnTouchListener {

    private val pointers: MutableMap<Int, Pointer> = mutableMapOf()

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event ?: return false
        val pointerId = event.getPointerId(event.actionIndex)
        val pointerIndex = event.findPointerIndex(pointerId)
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val pointer = Pointer(
                    event.getX(pointerIndex).toInt(),
                    event.getY(pointerIndex).toInt()
                )
                pointers += pointerId to pointer
            }
            MotionEvent.ACTION_MOVE -> {
                val pointer = pointers[pointerId] ?: return false
                pointer.x = event.x.toInt()
                pointer.y = event.y.toInt()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                pointers -= pointerId
            }
        }
        return true
    }

    data class Pointer(
        val downX: Int,
        val downY: Int,
        var x: Int = downX,
        var y: Int = downY
    )

}