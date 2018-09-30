package com.volgoak.pokertournament

import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.Toast

import com.volgoak.pokertournament.admob.AdsManager
import com.volgoak.pokertournament.admob.Banner
import com.volgoak.pokertournament.admob.Interstitial
import com.volgoak.pokertournament.databinding.ActivityTournamentBinding
import com.volgoak.pokertournament.utils.BlindEvent
import com.volgoak.pokertournament.utils.ControlEvent
import com.volgoak.pokertournament.utils.NotificationUtil

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TournamentActivity : AppCompatActivity() {

    private lateinit var mBinder: ActivityTournamentBinding

    private var mStopWasClicked: Boolean = false
    private var isTimerActive: Boolean = false

    private var banner: Banner? = null
    private var interstitial: Interstitial? = null

    companion object {
        val TAG = "TournamentActivity"
        //BroadcastReceiver fields
        val TIME_TO_INCREASE = "time_to_increase"
        val CURRENT_BLIND = "current_blind"
        val NEXT_BLIND = "next_blind"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_tournament)

        if (savedInstanceState != null) {
            val time = savedInstanceState.getString(TIME_TO_INCREASE)
            mBinder.tvTimeToNextTournament.text = time
            val blinds = savedInstanceState.getString(CURRENT_BLIND)
            mBinder.tvCurrentBlindsTourn.text = blinds
            val nextBlinds = savedInstanceState.getString(NEXT_BLIND)
            mBinder.tvNextBlindsTour.text = nextBlinds
            //            String stateButton = savedInstanceState.getString(CHANGE_STATE_TEXT);
            //            mBinder.btPauseTournament.setText(stateButton);
        }

        //set listener for pause/resume button
        mBinder.btPauseTournament.setOnClickListener { v -> changeTimerState() }
        mBinder.btPauseTournament.isEnabled = false
        //set listener for stop button
        mBinder.btEndTournament.setOnClickListener { v -> tryToStopService() }
        mBinder.btEndTournament.isEnabled = false

        //set font for clock
        val font = Typeface.createFromAsset(assets, "fonts/digits_bold.ttf")
        mBinder.tvTimeToNextTournament.typeface = font
        mBinder.tvCurrentBlindsTourn.typeface = font
        mBinder.tvNextBlindsTour.typeface = font

        if (AdsManager.initialized) {
            val bannerLL = findViewById<LinearLayout>(R.id.llBanner)
            banner = Banner(this)
            banner?.loadAdRequest()
            banner?.setTargetView(bannerLL)

            interstitial = AdsManager.getInterstitial(this)
            interstitial?.loadAd()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        banner?.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        banner?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        banner?.onDestroy()
    }

    override fun onBackPressed() {
        tryToStopService()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TIME_TO_INCREASE, mBinder.tvTimeToNextTournament.text.toString())
        outState.putString(CURRENT_BLIND, mBinder.tvCurrentBlindsTourn.text.toString())
        outState.putString(NEXT_BLIND, mBinder.tvNextBlindsTour.text.toString())
        // TODO: 29.07.2017 save image for pause button
    }

    //call when stop button clicked. If clicked second time in five seconds it stops service
    //else run timer
    private fun tryToStopService() {
        if (!mStopWasClicked) {
            Toast.makeText(this@TournamentActivity, R.string.tap_one_more, Toast.LENGTH_SHORT).show()
            mStopWasClicked = true
            Handler().postDelayed({ mStopWasClicked = false }, 3000)
        } else {
            EventBus.getDefault().post(ControlEvent(ControlEvent.Type.STOP))
            val intent = Intent(this@TournamentActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            interstitial!!.showAd()
            finish()
        }
    }

    private fun changeTimerState() {
        EventBus.getDefault().post(ControlEvent(ControlEvent.Type.CHANGE_STATE))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlindEvent(event: BlindEvent) {
        mBinder.tvTimeToNextTournament.text = NotificationUtil.parseTime(event.millisToNext)
        mBinder.tvCurrentBlindsTourn.text = event.currentBlind.toString()
        mBinder.tvNextBlindsTour.text = event.nextBlind.toString()

        if (event.active != isTimerActive) {
            isTimerActive = event.active
            mBinder.btPauseTournament.setImageResource(if (isTimerActive)
                R.drawable.ic_pause_black_24dp
            else
                R.drawable.ic_play_arrow_black_24dp)
        }

        mBinder.btPauseTournament.isEnabled = true
        mBinder.btEndTournament.isEnabled = true
    }
}
