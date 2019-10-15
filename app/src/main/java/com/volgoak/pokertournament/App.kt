package com.volgoak.pokertournament

import android.app.Application
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.DebugGeography
import com.volgoak.pokertournament.admob.AdsManager
import com.volgoak.pokertournament.di.appModule
import com.volgoak.pokertournament.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * Created by alex on 5/8/18.
 */
class App : Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(appModule, viewModelModule))
        }

        AdsManager.initAds(this)

        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}