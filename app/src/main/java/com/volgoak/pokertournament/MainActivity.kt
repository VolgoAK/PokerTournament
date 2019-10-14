package com.volgoak.pokertournament

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aigestudio.wheelpicker.WheelPicker
import com.google.firebase.analytics.FirebaseAnalytics
import com.volgoak.pokertournament.admob.AdsManager
import com.volgoak.pokertournament.data.Structure
import com.volgoak.pokertournament.data.StructureProvider
import com.volgoak.pokertournament.service.BlindsService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SAVED_TIME_POSITION = "saved_time"
        private const val SAVED_BLIND_POSITION = "saved_blind"
        private const val SAVED_STRUCTURE_POSITION = "saved_structure"

        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private var mStructureList: List<Structure>? = null
    private var timeList = listOf<String>()

    /**
     * Get all system services and check if BlindsService
     * already ran
     * @return true if BlindsService ran
     *//*
    private val isTournamentStarted: Boolean
        get() {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val servicesInfo = activityManager.getRunningServices(Integer.MAX_VALUE)

            val blindsService = BlindsService::class.java.name
            var serviceRunning = false

            for (a in servicesInfo.indices) {
                if (blindsService == servicesInfo[a].service.className) {
                    serviceRunning = true
                    break
                }
            }
            return serviceRunning
        }*/
    //todo check is service already started

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseAnalytics.getInstance(this)

        //check if BlindsService already started
        val isServiceRunning = false // todo set real value
        //if a BlindsService started launch TournamentActivity
        if (isServiceRunning) {
            val intent = Intent(this, TournamentActivity::class.java)
            startActivity(intent)
            finish()
        }

        //get strings with time options and setup wheelPicker with time
        timeList = (1..60).toList()
                .map { it.toString() }

        wheelRoundTime.data = timeList
        setupWheel(wheelRoundTime)

        //setup structure wheelPicker
        mStructureList = StructureProvider.getStructures(this)
        wheelBlindsStructure.data = mStructureList!!
        setupWheel(wheelBlindsStructure)
        wheelBlindsStructure.setOnItemSelectedListener { picker, data, position -> renewBlindsList(position) }

        //setup blinds wheel
        renewBlindsList(0)
        setupWheel(wheelStartBlinds)

        //create listener for the start button
        fabStart.setOnClickListener { startGame() }

        AdsManager.checkConsent(this)
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

    private fun startGame() {
        //get selected round time and parse it to millis
        val selectedMinuts = wheelRoundTime.currentItemPosition
        val minutesString = timeList!![selectedMinuts]
        val minutesLong = java.lang.Long.parseLong(minutesString)
        val roundTime = minutesLong * 60 * 1000

        val startBlindPosition = wheelStartBlinds.currentItemPosition - 1

        val selectedStructure = mStructureList!![wheelBlindsStructure.currentItemPosition]

        //Put tournament info into Intent and start service
        val intent = BlindsService.getStartGameIntent(
                this,
                selectedStructure,
                roundTime,
                startBlindPosition
        )

        ContextCompat.startForegroundService(this, intent)

        //Start tournament activity
        val activityIntent = Intent(this, TournamentActivity::class.java)
        startActivity(activityIntent)

        //finish activity to avoid returning while tournament in progress
        finish()
    }

    //prepare wheelPicker
    private fun setupWheel(wheel: WheelPicker) {
        wheel.setAtmospheric(true)
        wheel.itemTextSize = resources.getDimensionPixelSize(R.dimen.wheel_text_size)
        wheel.isCurved = true
        wheel.isCyclic = true
        wheel.visibleItemCount = 4
    }

    //renew start blind wheel
    private fun renewBlindsList(position: Int) {
        val structure = mStructureList!![position]
        wheelStartBlinds.data = structure.blinds
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
