package com.volgoak.pokertournament.data.model

data class BlindInfo(
        val currentBlinds: String,
        val nextBlinds: String,
        val timeToIncrease: Long,
        val paused: Boolean
)