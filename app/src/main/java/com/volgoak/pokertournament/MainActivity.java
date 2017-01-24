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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = "MainActivity";

    private ActivityMainBinding mBinding;
    // TODO: 23.01.2017 make local
    private ArrayAdapter<String> mStructureAdapter;
    private ArrayAdapter<String> mMinutsAdapter;

    private List<String> mStructureList;
    private List<String> mTimeList;

    // TODO: 23.01.2017 if service is running start tournament activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //create arrayAdapter for minuteSpinner and link it to spinner
        /*mMinutsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                 getResources().getStringArray(R.array.time_for_round));*/
        String[] timeArray = getResources().getStringArray(R.array.time_for_round);
        mTimeList = new ArrayList<>();
        Collections.addAll(mTimeList, timeArray);

        mBinding.wheelRoundTimeMain.setData(mTimeList);
        mBinding.wheelRoundTimeMain.setCurved(true);
        mBinding.wheelRoundTimeMain.setCyclic(true);
        mBinding.wheelRoundTimeMain.setVisibleItemCount(4);
        mBinding.wheelRoundTimeMain.setAtmospheric(true);
        mBinding.wheelRoundTimeMain.setItemTextSize(48);
        //same with the structureSpinner
        String[] structureArray = getResources().getStringArray(R.array.blinds_structures);
        mStructureList = new ArrayList<>();
        Collections.addAll(mStructureList, structureArray);

        mBinding.wheelBlindsStructureMain.setData(mStructureList);
        mBinding.wheelBlindsStructureMain.setVisibleItemCount(4);
        mBinding.wheelBlindsStructureMain.setCurved(true);
        mBinding.wheelBlindsStructureMain.setCyclic(true);
        mBinding.wheelBlindsStructureMain.setAtmospheric(true);
        mBinding.wheelBlindsStructureMain.setItemTextSize(48);

        //create listener for the start button
        mBinding.btStartMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
    }

    private void startGame(){
//        String minutesString = mBinding.spRoundTimeMain.getSelectedItem().toString();
        int selectedMinuts = mBinding.wheelRoundTimeMain.getSelectedItemPosition();
        String minutesString = mTimeList.get(selectedMinuts);
        int minutesInt = Integer.parseInt(minutesString);
        long roundTime = minutesInt * 60 * 1000;

        int blindsStructureNum = mBinding.wheelBlindsStructureMain.getSelectedItemPosition();
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
