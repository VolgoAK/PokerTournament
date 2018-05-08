package com.volgoak.pokertournament;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.volgoak.pokertournament.admob.AdsManager;
import com.volgoak.pokertournament.admob.Banner;
import com.volgoak.pokertournament.admob.Interstitial;
import com.volgoak.pokertournament.databinding.ActivityTournamentBinding;
import com.volgoak.pokertournament.utils.BlindEvent;
import com.volgoak.pokertournament.utils.ControlEvent;
import com.volgoak.pokertournament.utils.NotificationUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TournamentActivity extends AppCompatActivity {

    public static final String TAG = "TournamentActivity";
    //BroadcastReceiver fields
    public static final String TIME_TO_INCREASE = "time_to_increase";
    public static final String CURRENT_BLIND = "current_blind";
    public static final String NEXT_BLIND = "next_blind";

    private ActivityTournamentBinding mBinder;

    private boolean mStopWasClicked;
    private boolean isTimerActive;

    private Banner banner;
    private Interstitial interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);

        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_tournament);

        if (savedInstanceState != null) {
            String time = savedInstanceState.getString(TIME_TO_INCREASE);
            mBinder.tvTimeToNextTournament.setText(time);
            String blinds = savedInstanceState.getString(CURRENT_BLIND);
            mBinder.tvCurrentBlindsTourn.setText(blinds);
            String nextBlinds = savedInstanceState.getString(NEXT_BLIND);
            mBinder.tvNextBlindsTour.setText(nextBlinds);
//            String stateButton = savedInstanceState.getString(CHANGE_STATE_TEXT);
//            mBinder.btPauseTournament.setText(stateButton);
        }

        //set listener for pause/resume button
        mBinder.btPauseTournament.setOnClickListener(v -> changeTimerState());
        mBinder.btPauseTournament.setEnabled(false);
        //set listener for stop button
        mBinder.btEndTournament.setOnClickListener(v -> tryToStopService());
        mBinder.btEndTournament.setEnabled(false);

        //set font for clock
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/digits_bold.ttf");
        mBinder.tvTimeToNextTournament.setTypeface(font);
        mBinder.tvCurrentBlindsTourn.setTypeface(font);
        mBinder.tvNextBlindsTour.setTypeface(font);

        if(AdsManager.INSTANCE.getInitialized()) {
            LinearLayout bannerLL = findViewById(R.id.llBanner);
            banner = new Banner(this);
            banner.loadAdRequest();
            banner.setTargetView(bannerLL);

            interstitial = new Interstitial(this);
            interstitial.loadAd();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        if(banner != null) {
            banner.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        if(banner != null) {
            banner.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(banner != null) {
            banner.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        tryToStopService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TIME_TO_INCREASE, mBinder.tvTimeToNextTournament.getText().toString());
        outState.putString(CURRENT_BLIND, mBinder.tvCurrentBlindsTourn.getText().toString());
        outState.putString(NEXT_BLIND, mBinder.tvNextBlindsTour.getText().toString());
        // TODO: 29.07.2017 save image for pause button
    }

    //call when stop button clicked. If clicked second time in five seconds it stops service
    //else run timer
    private void tryToStopService() {
        if (!mStopWasClicked) {
            Toast.makeText(TournamentActivity.this, R.string.tap_one_more, Toast.LENGTH_SHORT).show();
            mStopWasClicked = true;
            new Handler().postDelayed(() -> mStopWasClicked = false, 3000);
        } else {
            EventBus.getDefault().post(new ControlEvent(ControlEvent.Type.STOP));
            Intent intent = new Intent(TournamentActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            interstitial.showAd();
            finish();
        }
    }

    private void changeTimerState() {
        EventBus.getDefault().post(new ControlEvent(ControlEvent.Type.CHANGE_STATE));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBlindEvent(BlindEvent event) {
        mBinder.tvTimeToNextTournament.setText(NotificationUtil.parseTime(event.millisToNext));
        mBinder.tvCurrentBlindsTourn.setText(event.currentBlind.toString());
        mBinder.tvNextBlindsTour.setText(event.nextBlind.toString());

        if(event.active != isTimerActive) {
            isTimerActive = event.active;
            mBinder.btPauseTournament.setImageResource(isTimerActive
                    ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp);
        }

        mBinder.btPauseTournament.setEnabled(true);
        mBinder.btEndTournament.setEnabled(true);
    }
}
