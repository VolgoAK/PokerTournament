package com.volgoak.pokertournament.screens.tournament

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.volgoak.pokertournament.data.TournamentRepository
import com.volgoak.pokertournament.data.model.TournamentScreenState
import com.volgoak.pokertournament.data.toReadableText
import com.volgoak.pokertournament.extensions.parseTime

class TournamentViewModel(
        private val tournamentRepository: TournamentRepository
) : ViewModel() {

    val stateLiveData = Transformations.map(tournamentRepository.tournamentInfoLD) { info ->
        TournamentScreenState(
                info.currentBlinds.toReadableText(),
                info.nextBlinds.toReadableText(),
                info.timeToIncrease.parseTime(),
                info.inProgress
        )
    }

    fun toggleTournamentState() {
        tournamentRepository.toggleState()
    }
}