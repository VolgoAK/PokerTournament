package com.volgoak.pokertournament.screens.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aigestudio.wheelpicker.WheelPicker
import com.volgoak.pokertournament.AboutActivity
import com.volgoak.pokertournament.R
import com.volgoak.pokertournament.screens.tournament.TournamentActivity
import com.volgoak.pokertournament.admob.AdsManager
import com.volgoak.pokertournament.data.model.Structure
import com.volgoak.pokertournament.data.toReadableText
import com.volgoak.pokertournament.extensions.observeSafe
import com.volgoak.pokertournament.service.BlindsService
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SAVED_TIME_POSITION = "saved_time"
        private const val SAVED_BLIND_POSITION = "saved_blind"
        private const val SAVED_STRUCTURE_POSITION = "saved_structure"

        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AdsManager.checkConsent(this)

        setupWheel(wheelRoundTime)
        setupWheel(wheelBlindsStructure)
        setupWheel(wheelStartBlinds)

        wheelBlindsStructure.setOnItemSelectedListener { _, _, position ->
            viewModel.onStructureChanged(position)
        }

        fabStart.setOnClickListener {
            viewModel.onStartGameClicked(
                    wheelBlindsStructure.currentItemPosition,
                    wheelRoundTime.currentItemPosition,
                    wheelStartBlinds.currentItemPosition)
        }

        viewModel.timeListLiveData.observeSafe(this) { timeList ->
            wheelRoundTime.data = timeList.map { it.text }
        }
        viewModel.structuresLiveData.observeSafe(this) { structures ->
            wheelBlindsStructure.data = structures.map { it.name }
        }
        viewModel.blindsListLiveData.observeSafe(this) { blinds ->
            wheelStartBlinds.data = blinds.map { it.toReadableText() }
        }
        viewModel.startGameLiveData.observeSafe(this) { gameParams ->
            startGame(gameParams.time, gameParams.structure, gameParams.blindsIndex)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val structurePosition = savedInstanceState.getInt(SAVED_STRUCTURE_POSITION, 0)
        wheelBlindsStructure.selectedItemPosition = structurePosition

        val blindPosition = savedInstanceState.getInt(SAVED_BLIND_POSITION, 0)
        wheelStartBlinds.selectedItemPosition = blindPosition

        val timePosition = savedInstanceState.getInt(SAVED_TIME_POSITION, 0)
        wheelRoundTime.selectedItemPosition = timePosition
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_STRUCTURE_POSITION, wheelBlindsStructure.currentItemPosition)
        outState.putInt(SAVED_BLIND_POSITION, wheelStartBlinds.currentItemPosition)
        outState.putInt(SAVED_TIME_POSITION, wheelRoundTime.currentItemPosition)
    }

    private fun startGame(roundTime: Long, structure: Structure, blindsIndex: Int) {
        val intent = BlindsService.getStartGameIntent(
                this,
                structure,
                roundTime,
                blindsIndex
        )
        ContextCompat.startForegroundService(this, intent)

        startActivity(TournamentActivity.getIntent(this))
        finish()
    }

    //prepare wheelPicker
    private fun setupWheel(wheel: WheelPicker) {
        with(wheel) {
            setAtmospheric(true)
            itemTextSize = resources.getDimensionPixelSize(R.dimen.wheel_text_size)
            isCurved = true
            isCyclic = true
            visibleItemCount = 4
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
