package com.volgoak.pokertournament;

import android.app.ActivityManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.aigestudio.wheelpicker.WheelPicker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.volgoak.pokertournament.admob.AdsManager;

import com.volgoak.pokertournament.admob.ConsentManager;
import com.volgoak.pokertournament.data.Structure;

import com.volgoak.pokertournament.data.StructureProvider;
import com.volgoak.pokertournament.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = "MainActivity";
    public static final String SAVED_TIME_POSITION = "saved_time";
    public static final String SAVED_BLIND_POSITION = "saved_blind";
    public static final String SAVED_STRUCTURE_POSITION = "saved_structure";

    private ActivityMainBinding mBinding;

    private List<Structure> mStructureList;
    private List<String> mTimeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAnalytics.getInstance(this);

        //check if BlindsService already started
        boolean isServiceRunning = isTournamentStarted();
        //if a BlindsService started launch TournamentActivity
        if(isServiceRunning){
            Intent intent = new Intent(this, TournamentActivity.class);
            startActivity(intent);
            finish();
        }
        //initialize dataBinder
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //get strings with time options and setup wheelPicker with time
        mTimeList = new ArrayList<>();
        for(int i = 1; i < 41; i++) mTimeList.add(String.valueOf(i));

        mBinding.wheelRoundTimeMain.setData(mTimeList);
        setupWheel(mBinding.wheelRoundTimeMain);

        //setup structure wheelPicker
        mStructureList = StructureProvider.getStructures(this);
        mBinding.wheelBlindsStructureMain.setData(mStructureList);
        setupWheel(mBinding.wheelBlindsStructureMain);
        mBinding.wheelBlindsStructureMain.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                renewBlindsList(position);
            }
        });

        //setup blinds wheel
        renewBlindsList(0);
        setupWheel(mBinding.wheelStartBlindMain);

        //create listener for the start button
        mBinding.btStartMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

        AdsManager.INSTANCE.checkConsent(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int structurePosition = savedInstanceState.getInt(SAVED_STRUCTURE_POSITION, 0);
        mBinding.wheelBlindsStructureMain.setSelectedItemPosition(structurePosition);

        int blindPosition = savedInstanceState.getInt(SAVED_BLIND_POSITION, 0);
        mBinding.wheelStartBlindMain.setSelectedItemPosition(blindPosition);

        int timePosition = savedInstanceState.getInt(SAVED_TIME_POSITION, 0);
        mBinding.wheelRoundTimeMain.setSelectedItemPosition(timePosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_STRUCTURE_POSITION, mBinding.wheelBlindsStructureMain.getCurrentItemPosition());
        outState.putInt(SAVED_BLIND_POSITION, mBinding.wheelStartBlindMain.getCurrentItemPosition());
        outState.putInt(SAVED_TIME_POSITION, mBinding.wheelRoundTimeMain.getCurrentItemPosition());
    }

    private void startGame(){
        //get selected round time and parse it to millis
        int selectedMinuts = mBinding.wheelRoundTimeMain.getCurrentItemPosition();
        Log.d(TAG, "startGame: selectedPosition " + selectedMinuts);
        String minutesString = mTimeList.get(selectedMinuts);
        Log.d(TAG, "startGame: minutesString = " + minutesString);
        long minutesLong = Long.parseLong(minutesString);
        long roundTime = minutesLong * 60 * 1000;

        int startBlindPosition = mBinding.wheelStartBlindMain.getCurrentItemPosition();

        Structure selectedStructure = mStructureList
                .get(mBinding.wheelBlindsStructureMain.getCurrentItemPosition());

        //Put tournament info into Intent and start service
        Intent intent = new Intent(this, BlindsService.class);
        intent.putExtra(BlindsService.EXTRA_STRUCTURE, selectedStructure);
        intent.putExtra(BlindsService.EXTRA_ROUND_TIME, roundTime);
        intent.putExtra(BlindsService.EXTRA_START_ROUND, --startBlindPosition);
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
    private void renewBlindsList(int position){
        Structure structure = mStructureList.get(position);
        mBinding.wheelStartBlindMain.setData(structure.getBlinds());
    }

    /**
     * Get all system services and check if BlindsService
     * already ran
     * @return true if BlindsService ran
     */
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_about :
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
