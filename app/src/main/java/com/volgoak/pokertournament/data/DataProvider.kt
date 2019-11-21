package com.volgoak.pokertournament.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.volgoak.pokertournament.data.model.RoundTime
import com.volgoak.pokertournament.data.model.Structure
import java.io.IOException

/**
 * Created by alex on 3/5/18.
 */

class DataProvider(private val context: Context,
                   private val gson: Gson) {

    fun getStructures(): List<Structure> {
        var structures: List<Structure>? = null
        try {
            val bytes = context.assets.open("structures.json").readBytes()
            val json = String(bytes)
            val collectionType = object : TypeToken<List<Structure>>() {}.type
            structures = gson.fromJson<List<Structure>>(json, collectionType)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return structures ?: emptyList()
    }

    fun getRoundTimes(): List<RoundTime> {
        return (1..60).toList()
                .map {
                    val time = it * 1000 * 60L
                    RoundTime(time, it.toString())
                }
    }
}
