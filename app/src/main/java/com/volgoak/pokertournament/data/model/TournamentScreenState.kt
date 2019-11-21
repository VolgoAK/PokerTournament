package com.volgoak.pokertournament.data.model

data class TournamentScreenState(
        val currentBlindText: String = "",
        val nextBlindText: String = "",
        val timeLeftText: String = "00:00",
        val inProgress: Boolean = false
)