package com.volgoak.pokertournament.screens.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.volgoak.pokertournament.data.DataProvider
import com.volgoak.pokertournament.data.TournamentRepository
import com.volgoak.pokertournament.data.model.Blind
import com.volgoak.pokertournament.data.model.RoundTime
import com.volgoak.pokertournament.data.model.Structure
import com.volgoak.pokertournament.data.model.TournamentConfig
import com.volgoak.pokertournament.utils.SingleLiveEvent

class MainViewModel(
        private val dataProvider: DataProvider,
        private val tournamentRepository: TournamentRepository
) : ViewModel() {

    val structuresLiveData = MutableLiveData<List<Structure>>()
    val timeListLiveData = MutableLiveData<List<RoundTime>>()
    val blindsListLiveData = MutableLiveData<List<Blind>>()

    val startGameLiveData = SingleLiveEvent<GameParams>()

    init {
        val structures = dataProvider.getStructures()
        structuresLiveData.value = structures
        blindsListLiveData.value = structures[0].blinds
        timeListLiveData.value = dataProvider.getRoundTimes()
    }

    fun onStructureChanged(position: Int) {
        structuresLiveData.value?.let {
            blindsListLiveData.value = it[position].blinds
        }
    }

    fun onStartGameClicked(structurePosition: Int, timePosition: Int, blindPosition: Int) {

        val time = (timeListLiveData.value ?: dataProvider.getRoundTimes())[timePosition].time
        val structure = (structuresLiveData.value
                ?: dataProvider.getStructures())[structurePosition]
        tournamentRepository.beginTournament(TournamentConfig(structure, blindPosition, time))
        startGameLiveData.value = GameParams(time, structure, blindPosition)
    }

    data class GameParams(val time: Long, val structure: Structure, val blindsIndex: Int)
}