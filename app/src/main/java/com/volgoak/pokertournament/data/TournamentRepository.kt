package com.volgoak.pokertournament.data

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.volgoak.pokertournament.data.model.*
import com.volgoak.pokertournament.utils.SingleLiveEvent

class TournamentRepository {

    private var roundNum: Int = 0
    private var roundTime: Long = 0
    private var increaseTime: Long = 0
    private var pauseLeftTime: Long = 0
    private lateinit var blindsList: MutableList<Blind>
    private lateinit var structure: Structure
    private lateinit var currentBlinds: Blind
    private lateinit var nextBlinds: Blind

    private val time: Long
        get() = System.currentTimeMillis()

    val nextRoundLiveEvent = SingleLiveEvent<Blind>()
    val blindsLD = MutableLiveData<BlindInfo>()
    val currentBlindsLD = MutableLiveData<CurrentBlindsInfo>()
    val tournamentInfoLD = MediatorLiveData<TournamentInfo>()
    private val timeToIncreaseLD = MutableLiveData<Long>()

    val tournamentInProgressLD = MutableLiveData<Boolean>(false)

    init {
        with(tournamentInfoLD) {
            addSource(tournamentInProgressLD) { inProgress ->
                value = (value ?: TournamentInfo()).copy(inProgress = inProgress)
            }

            addSource(currentBlindsLD) {
                value = (value ?: TournamentInfo()).copy(
                        currentBlinds = currentBlinds,
                        nextBlinds = nextBlinds
                )
            }

            addSource(timeToIncreaseLD) {time ->
                value = (value ?: TournamentInfo()).copy(
                        timeToIncrease = time
                )
            }
        }
    }

    fun beginTournament(config: TournamentConfig) {
        structure = config.structure
        roundNum = config.firstRoundIndex
        blindsList = structure.blinds.toMutableList()
        checkBlinds()
        currentBlinds = blindsList[config.firstRoundIndex]
        nextBlinds = blindsList[config.firstRoundIndex + 1]
        roundTime = config.roundTime

        increaseTime = time + roundTime
        tournamentInProgressLD.postValue(true)
        currentBlindsLD.postValue(CurrentBlindsInfo(currentBlinds, nextBlinds))
        timeToIncreaseLD.postValue(roundTime)
    }

    fun notifyTimer() {
        if(tournamentInProgressLD.value == true) {
            val timeToIncrease = increaseTime - time
            if (timeToIncrease < 0) startNextRound()

            blindsLD.postValue(
                    BlindInfo(currentBlinds.toString(), nextBlinds.toString(), timeToIncrease,
                            tournamentInProgressLD.value == true)
            )

            currentBlindsLD.postValue(CurrentBlindsInfo(currentBlinds, nextBlinds))
            timeToIncreaseLD.postValue(timeToIncrease)
        }
    }

    fun toggleState() {
        if(tournamentInProgressLD.value == true) {
            pause()
        } else {
            resume()
        }
    }

    private fun pause() {
        tournamentInProgressLD.postValue(false)
        pauseLeftTime = increaseTime - time
    }

    private fun resume() {
        tournamentInProgressLD.postValue(true)
        increaseTime = time + roundTime
        notifyTimer()
    }

    private fun startNextRound() {
        checkBlinds()
        roundNum++
        currentBlinds = blindsList[roundNum]
        nextBlinds = blindsList[roundNum + 1]
        increaseTime = time + roundTime

        nextRoundLiveEvent.postValue(blindsList[roundNum])
        currentBlindsLD.postValue(CurrentBlindsInfo(currentBlinds, nextBlinds))
    }

    private fun checkBlinds() {
        if (roundNum >= blindsList.size - 1) {
            val currentBlind = blindsList[blindsList.size - 1]
            val smallBlind = currentBlind.sb * 2
            val bigBlind = smallBlind * 2
            val newBlind = Blind(smallBlind, bigBlind)
            blindsList.add(newBlind)
        }
    }
}