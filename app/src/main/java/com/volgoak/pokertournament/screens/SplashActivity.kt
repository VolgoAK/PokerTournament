package com.volgoak.pokertournament.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.volgoak.pokertournament.screens.main.MainActivity
import com.volgoak.pokertournament.TournamentActivity
import com.volgoak.pokertournament.service.ServiceStateRepository
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private val serviceStateRepository: ServiceStateRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(serviceStateRepository.serviceRunning) {
            startActivity(TournamentActivity.getIntent(this))
        } else {
            startActivity(MainActivity.getIntent(this))
        }
        finish()
    }
}
