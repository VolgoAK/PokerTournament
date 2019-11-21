package com.volgoak.pokertournament.screens.tournament

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.Toast
import com.volgoak.pokertournament.R

import com.volgoak.pokertournament.admob.AdsManager
import com.volgoak.pokertournament.admob.Banner
import com.volgoak.pokertournament.admob.Interstitial
import com.volgoak.pokertournament.data.model.TournamentScreenState
import com.volgoak.pokertournament.extensions.observeSafe
import com.volgoak.pokertournament.screens.main.MainActivity
import com.volgoak.pokertournament.utils.BlindEvent
import com.volgoak.pokertournament.utils.ControlEvent
import com.volgoak.pokertournament.utils.NotificationUtil
import kotlinx.android.synthetic.main.activity_tournament.*

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class TournamentActivity : AppCompatActivity() {

    private var mStopWasClicked: Boolean = false
    private var isTimerActive: Boolean = false

    private var banner: Banner? = null
    private var interstitial: Interstitial? = null

    private val viewModel by viewModel<TournamentViewModel>()

    companion object {
        private const val TIME_TO_INCREASE = "time_to_increase"
        private const val CURRENT_BLIND = "current_blind"
        private const val NEXT_BLIND = "next_blind"

        fun getIntent(context: Context) = Intent(context, TournamentActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        

        if (savedInstanceState != null) {
            val time = savedInstanceState.getString(TIME_TO_INCREASE)
            tvTimeToNext.text = time
            val blinds = savedInstanceState.getString(CURRENT_BLIND)
            tvCurrentBlinds.text = blinds
            val nextBlinds = savedInstanceState.getString(NEXT_BLIND)
            tvNextBlinds.text = nextBlinds
            //            String stateButton = savedInstanceState.getString(CHANGE_STATE_TEXT);
            //            fabPause.setText(stateButton);
        }

        //set listener for pause/resume button
        fabPause.setOnClickListener { viewModel.toggleTournamentState() }
        //set listener for stop button
        fabFinish.setOnClickListener { v -> tryToStopService() }

        //set font for clock
        val font = Typeface.createFromAsset(assets, "fonts/digits_bold.ttf")
        tvTimeToNext.typeface = font
        tvCurrentBlinds.typeface = font
        tvNextBlinds.typeface = font

        if (AdsManager.initialized) {
            val bannerLL = findViewById<LinearLayout>(R.id.llBanner)
            banner = Banner(this)
            banner?.loadAdRequest()
            banner?.setTargetView(bannerLL)

            interstitial = AdsManager.getInterstitial(this)
            interstitial?.loadAd()
        }

        viewModel.stateLiveData.observeSafe(this, ::onStateEvent)
    }

    override fun onResume() {
        super.onResume()
        /*if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)*/

        banner?.onResume()
    }

    override fun onPause() {
        super.onPause()
       /* if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)*/

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
        outState.putString(TIME_TO_INCREASE, tvTimeToNext.text.toString())
        outState.putString(CURRENT_BLIND, tvCurrentBlinds.text.toString())
        outState.putString(NEXT_BLIND, tvNextBlinds.text.toString())
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
            interstitial?.showAd()
            finish()
        }
    }

    private fun changeTimerState() {
        EventBus.getDefault().post(ControlEvent(ControlEvent.Type.CHANGE_STATE))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlindEvent(event: BlindEvent) {
        tvTimeToNext.text = NotificationUtil.parseTime(event.millisToNext)
        tvCurrentBlinds.text = event.currentBlind.toString()
        tvNextBlinds.text = event.nextBlind.toString()

        if (event.active != isTimerActive) {
            isTimerActive = event.active
            fabPause.setImageResource(if (isTimerActive)
                R.drawable.ic_pause_black_24dp
            else
                R.drawable.ic_play_arrow_black_24dp)
        }
    }

    private fun onStateEvent(state: TournamentScreenState) {
        tvTimeToNext.text = state.timeLeftText
        tvCurrentBlinds.text = state.currentBlindText
        tvNextBlinds.text = state.nextBlindText
    }
}
