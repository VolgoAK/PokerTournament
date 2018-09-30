package com.volgoak.pokertournament.admob

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

class Interstitial(context: Context,
                   interstitialId: String,
                   val request: AdRequest) {

    private var mInterstitialAd = InterstitialAd(context)

    init {
        mInterstitialAd.adUnitId = interstitialId
    }


    fun setAdListener(adListener: AdListener) {
        mInterstitialAd.adListener = adListener
    }


    fun loadAd() {
        if (!mInterstitialAd.isLoaded && !mInterstitialAd.isLoading) {
            mInterstitialAd.loadAd(request)
        }
    }

    fun showAd() {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        }
    }

    fun onPause() {
        mInterstitialAd.adListener = null
    }
}