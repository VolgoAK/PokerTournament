package com.volgoak.pokertournament.admob

import android.content.Context
import android.preference.PreferenceManager
import com.google.ads.consent.*
import com.volgoak.pokertournament.BuildConfig
import java.net.MalformedURLException
import java.net.URL

object ConsentManager {

    private const val SAVED_STATUS = "saved_status"

    var testDeviceId: String? = null
    var publisherId: String? = null
    var privacyLink: String? = null
    var currentConsentStatus = InnerConsentStatus.NOT_SET


    fun initConsent(context: Context) {
//        ConsentInformation.getInstance(context).debugGeography = DebugGeography.DEBUG_GEOGRAPHY_EEA

        if (BuildConfig.DEBUG) {
            ConsentInformation.getInstance(context)
                    .addTestDevice(testDeviceId)
        }

        loadStatusFromPrefs(context)

        if(currentConsentStatus == InnerConsentStatus.NOT_SET || currentConsentStatus == InnerConsentStatus.UNKNOWN) {
            checkConsent(context)
        }
    }

    private fun checkConsent(context: Context, doOnUnknown: () -> Unit = {}) {
        val consentInformation = ConsentInformation.getInstance(context)

        if (!consentInformation.isRequestLocationInEeaOrUnknown) {
            currentConsentStatus = InnerConsentStatus.NO_NEED
            saveConsentStatus(context, currentConsentStatus)
            return
        }

        val publisherIds = arrayOf(publisherId)
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: com.google.ads.consent.ConsentStatus) {
                currentConsentStatus = when (consentStatus) {
                    ConsentStatus.UNKNOWN -> InnerConsentStatus.UNKNOWN
                    ConsentStatus.PERSONALIZED -> InnerConsentStatus.PERSONALIZED
                    ConsentStatus.NON_PERSONALIZED -> InnerConsentStatus.NON_PERSONALIZED
                }
                saveConsentStatus(context, currentConsentStatus)
                if (currentConsentStatus == InnerConsentStatus.UNKNOWN) doOnUnknown()
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {

            }
        })
    }

    fun showConsentIfNeed(context: Context, buyCallBack: () -> Unit = {}) {
        when (currentConsentStatus) {
            InnerConsentStatus.UNKNOWN -> showConsent(context)
            InnerConsentStatus.NOT_SET -> checkConsent(context) { showConsentIfNeed(context, buyCallBack) }
            else -> return
        }
    }

    private fun saveConsentStatus(context: Context, status: InnerConsentStatus) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(SAVED_STATUS, status.toString())
                .apply()
    }

    private fun loadStatusFromPrefs(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val statusString = prefs.getString(SAVED_STATUS, InnerConsentStatus.NOT_SET.toString())
        currentConsentStatus = InnerConsentStatus.valueOf(statusString!!)
    }

    private fun showConsent(context: Context, buyCallBack: () -> Unit = {}) {
        var privacyUrl: URL? = null
        try {
            privacyUrl = URL(privacyLink)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            // Handle error.
        }

        var consentForm: ConsentForm? = null
        val builder = ConsentForm.Builder(context, privacyUrl)
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        consentForm?.show()
                    }

                    override fun onConsentFormOpened() {}

                    override fun onConsentFormClosed(
                            consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?) {
                        if(userPrefersAdFree == true) {
                            buyCallBack()
                        } else {
                            currentConsentStatus = if (consentStatus == ConsentStatus.PERSONALIZED) {
                                InnerConsentStatus.PERSONALIZED
                            } else {
                                InnerConsentStatus.NON_PERSONALIZED
                            }
                            saveConsentStatus(context, currentConsentStatus)
                        }
                    }

                    override fun onConsentFormError(errorDescription: String?) {}
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()

        consentForm = builder.build()
        consentForm?.load()
    }

    fun canShowAds(): Boolean {
        return currentConsentStatus == InnerConsentStatus.NO_NEED ||
                currentConsentStatus == InnerConsentStatus.PERSONALIZED ||
                currentConsentStatus == InnerConsentStatus.NON_PERSONALIZED
    }

    fun nonPersonalizedOnly(): Boolean {
        return currentConsentStatus == InnerConsentStatus.NON_PERSONALIZED
    }

    enum class InnerConsentStatus {
        NO_NEED,
        PERSONALIZED,
        NON_PERSONALIZED,
        UNKNOWN,
        AD_FREE,
        NOT_SET,
    }
}