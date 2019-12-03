package com.volgoak.pokertournament.data.model

data class TournamentInfo(
        val currentBlinds: Blind = Blind(),
        val nextBlinds: Blind = Blind(),
        val timeToIncrease: Long = 1000,
        val inProgress: Boolean = false
)

data class CurrentBlindsInfo(
        val currentBlinds: Blind,
        val nextBlinds: Blind
)