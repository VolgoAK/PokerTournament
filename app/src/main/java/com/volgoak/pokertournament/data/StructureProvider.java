package com.volgoak.pokertournament.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by alex on 3/5/18.
 */

public class StructureProvider {

    public static List<Structure> getStructures(Context context) {
        List<Structure> structures = null;
        try {
            InputStream is = context.getAssets().open("structures.json");
            int size = is.available();
            byte[] array = new byte[size];
            is.read(array);

            String json = new String(array, "UTF8");
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<Structure>>() {
            }.getType();
            structures = gson.fromJson(json, collectionType);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return structures;
    }
}
