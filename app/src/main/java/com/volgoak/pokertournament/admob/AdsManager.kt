package com.volgoak.pokertournament.admob

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.volgoak.pokertournament.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter


/**
 * Created by alex on 5/8/18.
 */
object AdsManager {

    private const val APP_ID_KEY = "app_id"
    private const val BANNER_ID_KEY = "banner_id"
    private const val TEST_DEVICE_KEY = "test_device_id"
    private const val INTERSTITIAL_ID_KEY = "interstitial_id"
    private const val PUBLISHER_ID_KEY = "publisher_id"
    private const val PRIVACY_LINK = "privacy_link"

    private const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"

    private var appId: String? = null
    private var bannerId: String? = null
    private var interstitialId: String? = null
    private var testDeviceId: String? = null
    private var publisherId: String? = null
    private var privacyLink: String? = null

    var initialized = false

    fun initAds(context: Context) {
        try {
            val jsonObject = loadIds(context)
            appId = jsonObject.optString(APP_ID_KEY)
            bannerId = jsonObject.optString(BANNER_ID_KEY)
            interstitialId = jsonObject.optString(INTERSTITIAL_ID_KEY)
            testDeviceId = jsonObject.optString(TEST_DEVICE_KEY)
            publisherId = jsonObject.optString(PUBLISHER_ID_KEY)
            privacyLink = jsonObject.optString(PRIVACY_LINK)

            MobileAds.initialize(context, appId)

            ConsentManager.publisherId = publisherId
            ConsentManager.testDeviceId = testDeviceId
            ConsentManager.privacyLink = privacyLink
            ConsentManager.initConsent(context)

            initialized = true
        } catch (ex: Exception) {
            //Not initialized, do nothing just log
            Crashlytics.logException(ex)
        }
    }

    fun checkConsent(context: Context) {
        ConsentManager.showConsentIfNeed(context)
    }

    fun createBannerRequest(): AdRequest? {
        if(ConsentManager.canShowAds().not()) {
            return null
        }
        val adRequestBuilder = AdRequest.Builder()
        if (BuildConfig.DEBUG) {
            adRequestBuilder.addTestDevice(testDeviceId)
        }

        if(ConsentManager.nonPersonalizedOnly()) {
            val extras = Bundle()
            extras.putString("npa", "1")
            adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }

        return adRequestBuilder.build()
    }

    fun getBannerId(): String? {
        return bannerId
    }

    fun getInterstitial(context: Context): Interstitial? {
        if(ConsentManager.canShowAds().not()){
            return null
        }

        val builder = AdRequest.Builder()
        if(BuildConfig.DEBUG) {
            builder.addTestDevice(testDeviceId)
        }

        if(ConsentManager.nonPersonalizedOnly()) {
            val extras = Bundle()
            extras.putString("npa", "1")
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }

        return Interstitial(context, interstitialId!!, builder.build())
    }

    private fun loadIds(context: Context): JSONObject {
        val inputStream = context.assets.open("ads.json")
        val bufferedStream = BufferedReader(InputStreamReader(inputStream))

        val builder = StringBuilder()
        var line = bufferedStream.readLine()

        while (line != null) {
            builder.append(line)
            line = bufferedStream.readLine()
        }

        return JSONObject(builder.toString())
    }


}