package com.volgoak.pokertournament.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlindInfo(
        val currentBlinds: String = "0/0",
        val nextBlinds: String = "0/0",
        val timeToIncrease: Long = 1000,
        val inProgress: Boolean = false
): Parcelable

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