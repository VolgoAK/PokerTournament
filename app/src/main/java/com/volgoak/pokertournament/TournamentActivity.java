package com.volgoak.pokertournament;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.volgoak.pokertournament.databinding.ActivityTournamentBinding;

public class TournamentActivity extends AppCompatActivity {

    //BroadcastReceiver fields
    public static final String RECEIVER_CODE = "com.volgoak.pokertournament.BlindsBroadCastReceiver";
    public static final String TIME_TO_INCREASE = "time_to_increase";
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private ActivityTournamentBinding mBinder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);

        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_tournament);

    }






}
