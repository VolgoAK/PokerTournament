package com.volgoak.pokertournament.data

import androidx.lifecycle.MutableLiveData
import com.volgoak.pokertournament.data.model.Blind
import com.volgoak.pokertournament.data.model.BlindInfo
import com.volgoak.pokertournament.data.model.Structure
import com.volgoak.pokertournament.data.model.TournamentConfig
import com.volgoak.pokertournament.utils.SingleLiveEvent

class TournamentRepository {

    private var roundNum: Int = 0
    private var roundTime: Long = 0
    private var increaseTime: Long = 0
    private var pauseLeftTime: Long = 0
    private lateinit var blindsList: MutableList<Blind>
    private lateinit var structure: Structure
    private lateinit var currentBlinds: String
    private lateinit var nextBlinds: String

    private val time: Long
        get() = System.currentTimeMillis()

    val nextRoundLD = SingleLiveEvent<Blind>()
    val blindsLD = MutableLiveData<BlindInfo>()
    val tournamentInProgressLD = MutableLiveData<Boolean>(false)

    fun beginTournament(config: TournamentConfig) {
        structure = config.structure
        roundNum = config.firstRoundIndex
        blindsList = structure.blinds
        checkBlinds()
        currentBlinds = blindsList[config.firstRoundIndex].toString()
        nextBlinds = blindsList[config.firstRoundIndex + 1].toString()
        roundTime = config.roundTime

        increaseTime = time + roundTime
        tournamentInProgressLD.postValue(true)
    }

    fun notifyTimer() {
        if(tournamentInProgressLD.value == true) {
            val timeToIncrease = increaseTime - time
            if (timeToIncrease < 0) startNextRound()

            blindsLD.postValue(
                    BlindInfo(currentBlinds, nextBlinds, timeToIncrease,
                            tournamentInProgressLD.value == true)
            )
        }
    }

    fun pause() {
        tournamentInProgressLD.postValue(false)
        pauseLeftTime = increaseTime - time
    }

    fun resume() {
        tournamentInProgressLD.postValue(true)
        increaseTime = time + roundTime
        notifyTimer()
    }

    private fun startNextRound() {
        checkBlinds()
        roundNum++
        currentBlinds = blindsList[roundNum].toString()
        nextBlinds = blindsList[roundNum + 1].toString()
        increaseTime = time + roundTime

        nextRoundLD.postValue(blindsList[roundNum])
    }

    private fun checkBlinds() {
        if (roundNum >= blindsList.size - 1) {
            val currentBlind = blindsList[blindsList.size - 1]
            val smallBlind = currentBlind.sb * 2
            val bigBlind = smallBlind * 2
            val newBlind = Blind()
            newBlind.sb = smallBlind
            newBlind.bb = bigBlind
            blindsList.add(newBlind)
        }
    }
}