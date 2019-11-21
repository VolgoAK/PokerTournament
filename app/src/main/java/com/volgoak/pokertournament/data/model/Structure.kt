package com.volgoak.pokertournament.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Structure(
        val name: String,
        val blinds: List<Blind>
): Parcelable