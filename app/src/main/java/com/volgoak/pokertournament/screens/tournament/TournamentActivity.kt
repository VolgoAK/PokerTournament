package com.volgoak.pokertournament.screens.tournament

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.volgoak.pokertournament.R
import com.volgoak.pokertournament.admob.AdsManager
import com.volgoak.pokertournament.admob.Banner
import com.volgoak.pokertournament.admob.Interstitial
import com.volgoak.pokertournament.data.model.TournamentScreenState
import com.volgoak.pokertournament.extensions.observeSafe
import com.volgoak.pokertournament.screens.main.MainActivity
import com.volgoak.pokertournament.service.BlindsService
import kotlinx.android.synthetic.main.activity_tournament.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class TournamentActivity : AppCompatActivity() {

    private var mStopWasClicked: Boolean = false
    private var banner: Banner? = null
    private var interstitial: Interstitial? = null

    private val viewModel by viewModel<TournamentViewModel>()

    companion object {
        fun getIntent(context: Context) = Intent(context, TournamentActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

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
        banner?.onResume()
    }

    override fun onPause() {
        super.onPause()
        banner?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        banner?.onDestroy()
    }

    override fun onBackPressed() {
        tryToStopService()
    }

    //call when stop button clicked. If clicked second time in five seconds it stops service
    //else run timer
    private fun tryToStopService() {
        if (!mStopWasClicked) {
            Toast.makeText(this@TournamentActivity, R.string.tap_one_more, Toast.LENGTH_SHORT).show()
            mStopWasClicked = true
            Handler().postDelayed({ mStopWasClicked = false }, 3000)
        } else {
            startService(BlindsService.getStopIntent(this))
            val intent = Intent(this@TournamentActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            interstitial?.showAd()
            finish()
        }
    }

    private fun onStateEvent(state: TournamentScreenState) {
        tvTimeToNext.text = state.timeLeftText
        tvCurrentBlinds.text = state.currentBlindText
        tvNextBlinds.text = state.nextBlindText
        fabPause.setImageResource(
                if (state.inProgress)
                    R.drawable.ic_pause_black_24dp
                else
                    R.drawable.ic_play_arrow_black_24dp
        )
    }
}
