package com.volgoak.pokertournament

import android.app.Application
import com.volgoak.pokertournament.admob.AdsManager

/**
 * Created by alex on 5/8/18.
 */
class App : Application(){

    override fun onCreate() {
        super.onCreate()
        AdsManager.initAds(this)
    }
}