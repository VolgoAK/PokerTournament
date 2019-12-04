package com.volgoak.pokertournament.screens.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.volgoak.pokertournament.AboutActivity
import com.volgoak.pokertournament.R
import com.volgoak.pokertournament.admob.AdsManager
import com.volgoak.pokertournament.extensions.observeSafe
import com.volgoak.pokertournament.screens.creategame.CreateGameFragment
import com.volgoak.pokertournament.screens.tournament.TournamentActivity
import com.volgoak.pokertournament.service.BlindsService
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsManager.checkConsent(this)
        viewModel.startGameLiveData.observeSafe(this) { startGame() }

        if(supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, CreateGameFragment.newInstance())
                    .commit()
        }
    }

    private fun startGame() {
        ContextCompat.startForegroundService(this, BlindsService.getStartGameIntent(this))
        startActivity(TournamentActivity.getIntent(this))
        finish()
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
