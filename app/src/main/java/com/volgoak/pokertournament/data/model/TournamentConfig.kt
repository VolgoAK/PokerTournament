package com.volgoak.pokertournament.data.model

data class TournamentConfig(
        val structure: Structure,
        val firstRoundIndex: Int,
        val roundTime: Long
)