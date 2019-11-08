package com.volgoak.pokertournament.screens.tournament

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.volgoak.pokertournament.data.TournamentRepository
import com.volgoak.pokertournament.data.model.TournamentState
import com.volgoak.pokertournament.extensions.parseTime

class TournamentViewModel(
        private val tournamentRepository: TournamentRepository
) : ViewModel() {

    val stateLiveData = MediatorLiveData<TournamentState>()

    init {
        stateLiveData.addSource(tournamentRepository.blindsLD) { info ->
            stateLiveData.postValue(
                    TournamentState(
                            info.currentBlinds,
                            info.nextBlinds,
                            info.timeToIncrease.parseTime(),
                            info.paused
                    )
            )
        }
    }
}