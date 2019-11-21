package com.volgoak.pokertournament.data.model

import android.os.Parcel
import android.os.Parcelable

data class Blind(
        val sb: Int = 0,
        val bb: Int = 0,
        val ante: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(sb)
        parcel.writeInt(bb)
        parcel.writeInt(ante)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Blind> {
        override fun createFromParcel(parcel: Parcel): Blind {
            return Blind(parcel)
        }

        override fun newArray(size: Int): Array<Blind?> {
            return arrayOfNulls(size)
        }
    }
}