package ee.oyatl.hanjakbd.keyboard

data class KeyboardConfig(
    val rowHeight: Int = 50,
    val numberRowHeight: Int = 40,
    val soundFeedback: Boolean = true,
    val hapticFeedback: Boolean = true
)