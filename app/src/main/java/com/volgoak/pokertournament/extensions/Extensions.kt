package com.volgoak.pokertournament.extensions

fun Long.parseTime(): String {
    val sec = (this / 1000 % 60).toInt()
    val min = (this / 1000 / 60).toInt()

    return String.format("%02d:%02d", min, sec)
}