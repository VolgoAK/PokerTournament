package com.volgoak.pokertournament.admob

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

class Interstitial(context: Context) {

    private var mInterstitialAd = InterstitialAd(context)
    private var mAdRequest: AdRequest
    private var mAdRequestBuilder: AdRequest.Builder

    init {
        mInterstitialAd = InterstitialAd(context)
        mInterstitialAd.adUnitId = AdsManager.getInterstitialId()
        mAdRequestBuilder = AdRequest.Builder()

        mAdRequest = mAdRequestBuilder.build()
    }


    fun setAdListener(adListener: AdListener) {
        mInterstitialAd.adListener = adListener
    }


    fun loadAd() {
        if (!mInterstitialAd.isLoaded && !mInterstitialAd.isLoading) {
            mInterstitialAd.loadAd(mAdRequest)
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