package com.cylonid.nativealpha.ui.webapp

enum class WebAppMenuStyle(
    val id: Int,
) {
    BOTTOM_SHEET(0),
    FLOATING_DIALOG(1),
    CLASSIC_DROPDOWN(2),
    ;

    companion object {
        fun fromId(id: Int): WebAppMenuStyle = entries.firstOrNull { it.id == id } ?: BOTTOM_SHEET
    }
}
