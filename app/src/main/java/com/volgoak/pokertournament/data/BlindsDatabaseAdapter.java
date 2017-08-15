package com.volgoak.pokertournament.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.volgoak.pokertournament.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Volgoak on 29.01.2017.
 */

public class BlindsDatabaseAdapter {

    public static final String TAG = BlindsDatabaseAdapter.class.getSimpleName();

    public static final int DB_VERSION = 7;
    public static final String DB_NAME = "blinds_db";

    public static final String TABLE_STRUCTURES = "structures_table";
    public static final String TABLE_BLINDS = "blinds_table";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_STRUCTURE = "structure";
    public static final String COLUMN_STRUCTURE_ID = "structure_id";
    public static final String COLUMN_ROUND = "round";
    public static final String COLUMN_BLINDS = "blinds";
    public static final String COLUMN_MULT_PERCENTS = "mult_column";

    public static final String CREATE_TABLE_STRUCTURES = "CREATE TABLE " + TABLE_STRUCTURES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_STRUCTURE + " TEXT, "
            + COLUMN_MULT_PERCENTS + " INTEGER,"
            + "UNIQUE (" + COLUMN_STRUCTURE + ")"
            + ");";

    public static final String CREATE_TABLE_BLINDS = "CREATE TABLE " + TABLE_BLINDS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_STRUCTURE_ID + " INTEGER, "
            + COLUMN_ROUND + " INTEGER, "
            + COLUMN_BLINDS + " TEXT, "
            + " FOREIGN KEY (" + COLUMN_STRUCTURE_ID + ") REFERENCES " + TABLE_STRUCTURES + "(" + COLUMN_ID + ")"
            + ");";

    private Context mContext;
    private SQLiteDatabase mDB;

    public BlindsDatabaseAdapter(Context context){
        mContext = context;
        mDB = new BlindsDbOpenHelper(context).getWritableDatabase();

        //insert default structures only at first launch
        /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isBaseCreated = preferences.getBoolean(context.getString(R.string.db_created_pref), false);
        if(!isBaseCreated){
            addStructures();
            preferences.edit().putBoolean(context.getString(R.string.db_created_pref), true).apply();
        }*/
        if(isDbEmpty()){
            addStructures();
        }
    }

    public boolean isDbEmpty(){
        Cursor cursor = mDB.rawQuery("SELECT * FROM " + TABLE_STRUCTURES + " LIMIT 1", null);
        boolean isEmpty = !cursor.moveToFirst();
        cursor.close();
        return isEmpty;
    }

    public List<Structure> getStructures(){
        Cursor cursor = mDB.rawQuery("SELECT * FROM " + TABLE_STRUCTURES, null);
        ArrayList<Structure> structures = new ArrayList<>();

        while (cursor.moveToNext()){
            long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(COLUMN_STRUCTURE));
            structures.add(new Structure(name, id));
        }

        cursor.close();

        return structures;
    }

    public ArrayList<String> getBlinds(Structure structure){
        Cursor cursor = mDB.rawQuery("SELECT * FROM " + TABLE_BLINDS
            + " WHERE " + COLUMN_STRUCTURE_ID  + "=" + structure.id, null);

        ArrayList<String> blinds = new ArrayList<>();

        while(cursor.moveToNext()){
            blinds.add(cursor.getString(cursor.getColumnIndex(COLUMN_BLINDS)));
        }
        cursor.close();
        return blinds;
    }

    /**
     * Add default structures if database is empty
     */
    private void addStructures(){
        long slowID = addStructure(mContext.getString(R.string.structure_slow), 15);
        String[] slowBlinds = mContext.getResources().getStringArray(R.array.blinds_slow);
        for(int a = 0; a < slowBlinds.length; a++){
            addBlinds(slowID, a, slowBlinds[a]);
        }

        long midId = addStructure(mContext.getString(R.string.structure_mid), 20);
        String[] midBlinds = mContext.getResources().getStringArray(R.array.blinds_mid);
        for(int a = 0; a < midBlinds.length; a++){
            addBlinds(midId, a, midBlinds[a]);
        }

        long fastId = addStructure(mContext.getString(R.string.structure_fast), 50);
        String[] fastblinds = mContext.getResources().getStringArray(R.array.blinds_fast);
        for(int a = 0; a < fastblinds.length; a++){
            addBlinds(fastId, a, fastblinds[a]);
        }
    }

    private long addStructure(String structureName, int mult){
        Log.d(TAG, "addStructure: " + structureName);
        ContentValues values = new ContentValues();
        values.put(COLUMN_STRUCTURE, structureName);
        values.put(COLUMN_MULT_PERCENTS, mult);
        return mDB.insert(TABLE_STRUCTURES, null, values);
    }

    private void addBlinds(long structureId, int round, String blinds){
        ContentValues values = new ContentValues();
        values.put(COLUMN_STRUCTURE_ID, structureId);
        values.put(COLUMN_ROUND, round);
        values.put(COLUMN_BLINDS, blinds);
        mDB.insert(TABLE_BLINDS, null, values);
    }

    private class BlindsDbOpenHelper extends SQLiteOpenHelper{

        BlindsDbOpenHelper(Context context){
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "onCreate: creating database");
            db.execSQL(CREATE_TABLE_STRUCTURES);
            db.execSQL(CREATE_TABLE_BLINDS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLINDS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STRUCTURES);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLINDS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STRUCTURES);
            onCreate(db);
        }
    }
}
