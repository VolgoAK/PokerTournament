package com.volgoak.pokertournament;

import android.app.ActivityManager;
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

import com.aigestudio.wheelpicker.WheelPicker;
import com.volgoak.pokertournament.data.BlindsDatabaseAdapter;
import com.volgoak.pokertournament.data.Structure;
import com.volgoak.pokertournament.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = "MainActivity";

    private ActivityMainBinding mBinding;

    private List<Structure> mStructureList;
    private List<String> mTimeList;
    private List<String> mBlindsList;
    private BlindsDatabaseAdapter mDbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check if TournamentService already started
        boolean isServiceRunning = isTournamentStarted();
        if(isServiceRunning){
            Intent intent = new Intent(this, TournamentActivity.class);
            startActivity(intent);
            finish();
        }

        //initialize sql database adapter
        mDbAdapter = new BlindsDatabaseAdapter(this);
        //initialize dataBinder
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //get strings with time options and setup wheelPicker with time
        String[] timeArray = getResources().getStringArray(R.array.time_for_round);
        mTimeList = new ArrayList<>();
        Collections.addAll(mTimeList, timeArray);

        mBinding.wheelRoundTimeMain.setData(mTimeList);
        setupWheel(mBinding.wheelRoundTimeMain);

        //setup structure wheelPicker
        mStructureList = mDbAdapter.getStructures();
        mBinding.wheelBlindsStructureMain.setData(mStructureList);
        setupWheel(mBinding.wheelBlindsStructureMain);
        mBinding.wheelBlindsStructureMain.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                renewBlindsList();
            }
        });

        //setup blinds wheel
        renewBlindsList();
        setupWheel(mBinding.wheelStartBlindMain);

        //create listener for the start button
        mBinding.btStartMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
    }

    private void startGame(){
        //get selected round time and parse it to millis
        int selectedMinuts = mBinding.wheelRoundTimeMain.getCurrentItemPosition();
        Log.d(TAG, "startGame: selectedPosition " + selectedMinuts);
        String minutesString = mTimeList.get(selectedMinuts);
        Log.d(TAG, "startGame: minutesString = " + minutesString);
        long minutesLong = Long.parseLong(minutesString);
        long roundTime = minutesLong * 60 * 1000;

        //get selected structure from wheelPicker
        int structurePosition = mBinding.wheelBlindsStructureMain.getCurrentItemPosition();
        Structure selectedStructure = mStructureList.get(structurePosition);
        //get blinds of selected structure as a list of strings
        /*
        we don't need this since we have list mBlinds which contains all blinds of structure
        List<String> blindsList = mDbAdapter.getBlinds(selectedStructure);
        String[] blindsArray =  blindsList.toArray(new String[0]);*/

        int startBlindPosition = mBinding.wheelStartBlindMain.getCurrentItemPosition();
        Log.d(TAG, "startGame: blinds position = " + startBlindPosition);
        String[] blindsArray = new String[mBlindsList.size() - startBlindPosition];
        for(int a = startBlindPosition, b = 0; a < mBlindsList.size(); a++, b++){
            blindsArray[b] = mBlindsList.get(a);
        }
        //Put tournament info into Intent and start service
        Intent intent = new Intent(this, BlindsService.class);
        intent.putExtra(BlindsService.EXTRA_BLINDS_ARRAY, blindsArray);
        intent.putExtra(BlindsService.EXTRA_ROUND_TIME, roundTime);
        intent.putExtra(BlindsService.EXTRA_START_BLINDS, startBlindPosition);
        intent.setAction(BlindsService.START_GAME_ACTION);
        startService(intent);

        //Start tournament activity
        Intent activityIntent = new Intent(this, TournamentActivity.class);
        startActivity(activityIntent);

        Log.d(TAG, "startGame: newVersion");
        //finish activity to avoid returning while tournament in progress
        finish();
    }

    //prepare wheelPicker
    private void setupWheel(WheelPicker wheel){
        wheel.setAtmospheric(true);
        wheel.setItemTextSize(getResources().getDimensionPixelSize(R.dimen.wheel_text_size));
        wheel.setCurved(true);
        wheel.setCyclic(true);
        wheel.setVisibleItemCount(4);
    }

    //renew start blind wheel
    private void renewBlindsList(){
        Structure structure = mStructureList.get(mBinding.wheelBlindsStructureMain.getCurrentItemPosition());
        mBlindsList = mDbAdapter.getBlinds(structure);
        mBinding.wheelStartBlindMain.setData(mBlindsList);
    }

    private boolean isTournamentStarted(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> servicesInfo = activityManager.getRunningServices(Integer.MAX_VALUE);

        String blindsService = BlindsService.class.getName();
        boolean serviceRunning = false;

        for(int a = 0; a < servicesInfo.size(); a++){
            Log.d(TAG, servicesInfo.get(a).service.getClassName());
            if(blindsService.equals(servicesInfo.get(a).service.getClassName())){
                serviceRunning = true;
                break;
            }
        }
        return serviceRunning;
    }

}
