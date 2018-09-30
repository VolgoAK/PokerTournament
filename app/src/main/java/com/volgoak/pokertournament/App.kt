package com.volgoak.pokertournament

import android.app.Application
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.DebugGeography
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