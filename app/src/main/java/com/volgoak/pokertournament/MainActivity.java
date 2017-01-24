package com.volgoak.pokertournament;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.volgoak.pokertournament.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = "MainActivity";

    private ActivityMainBinding mBinding;
    // TODO: 23.01.2017 make local
    private ArrayAdapter<String> mStructureAdapter;
    private ArrayAdapter<String> mMinutsAdapter;

    // TODO: 23.01.2017 if service is running start tournament activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //create arrayAdapter for minuteSpinner and link it to spinner
        mMinutsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                 getResources().getStringArray(R.array.time_for_round));
        mBinding.spRoundTimeMain.setAdapter(mMinutsAdapter);
        //same with the structureSpinner
        mStructureAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.blinds_structures));
        mBinding.spBlindsStructureMain.setAdapter(mStructureAdapter);

        //create listener for the start button
        mBinding.btStartMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
    }

    private void startGame(){
        String minutesString = mBinding.spRoundTimeMain.getSelectedItem().toString();
        int minutesInt = Integer.parseInt(minutesString);
        long roundTime = minutesInt * 60 * 1000;

        int blindsStructureNum = mBinding.spBlindsStructureMain.getSelectedItemPosition();
        String[] blindsArray = null;

        switch (blindsStructureNum){
            case 0 :
                blindsArray = getResources().getStringArray(R.array.blinds_slow);
                break;
            case 1 :
                blindsArray = getResources().getStringArray(R.array.blinds_mid);
                break;
            case 2 :
                blindsArray = getResources().getStringArray(R.array.blinds_fast);
        }
        //Put tournament info into Intent and start service
        Intent intent = new Intent(this, BlindsService.class);
        intent.putExtra(BlindsService.EXTRA_BLINDS_ARRAY, blindsArray);
        intent.putExtra(BlindsService.EXTRA_ROUND_TIME, roundTime);
        intent.setAction(BlindsService.START_GAME_ACTION);

        startService(intent);

        //Start tournament activity
        Intent activityIntent = new Intent(this, TournamentActivity.class);
        startActivity(activityIntent);
    }

}
