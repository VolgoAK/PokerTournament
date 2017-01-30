package com.volgoak.pokertournament;

/**
 * Created by Volgoak on 30.01.2017.
 */

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.volgoak.pokertournament.data.BlindsDatabaseAdapter;
import com.volgoak.pokertournament.data.Structure;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DataBaseTest {

    @Test
    public void testGetStructures(){
        Context context = InstrumentationRegistry.getTargetContext();
        BlindsDatabaseAdapter adapter = new BlindsDatabaseAdapter(context);
        List<Structure> structures = adapter.getStructures();

        assertNotNull(structures);
        assertTrue(structures.size() > 0);
    }
}
