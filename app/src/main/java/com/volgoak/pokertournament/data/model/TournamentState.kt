package com.volgoak.pokertournament.data.model

data class TournamentState(
        val currentBlindText: String = "",
        val nextBlindText: String = "",
        val timeLeftText: String = "00:00",
        val paused: Boolean = false
)