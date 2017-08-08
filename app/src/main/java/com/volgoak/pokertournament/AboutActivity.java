package com.volgoak.pokertournament;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.artitk.licensefragment.model.License;
import com.artitk.licensefragment.model.LicenseType;
import com.artitk.licensefragment.support.v4.RecyclerViewLicenseFragment;

import java.util.ArrayList;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.about_container, createLicenseFragment());
        ft.commit();
    }

    private Fragment createLicenseFragment(){
        RecyclerViewLicenseFragment fragment = RecyclerViewLicenseFragment.newInstance();
        ArrayList<License> licenses = new ArrayList<>();
        licenses.add(new License(this, "Support Design", LicenseType.APACHE_LICENSE_20, "2016", "Android Open Source Project"));
        licenses.add(new License(this, "Support ConstraintLayout", LicenseType.APACHE_LICENSE_20, "2015", "Android Open Source Project"));
        licenses.add(new License(this, "Support CardView", LicenseType.APACHE_LICENSE_20, "2015", "Android Open Source Project"));
        licenses.add(new License(this, "CursorRecyclerAdapter", LicenseType.MIT_LICENSE, "2014", "Matthieu Harlé"));
        licenses.add(new License(this, "Google PlayServices GCM", LicenseType.APACHE_LICENSE_20, "2015", "Google Inc"));
        licenses.add(new License(this, "AppCompat v7", LicenseType.APACHE_LICENSE_20, "2015", "Android Open Source Project"));
        licenses.add(new License(this, "Support RecyclerView v7", LicenseType.APACHE_LICENSE_20, "2015", "Android Open Source Project"));
        licenses.add(new License(this, "Support v4", LicenseType.APACHE_LICENSE_20, "2014", "Android Open Source Project"));
        licenses.add(new License(this, "DSEG font family", R.raw.font_license, "2017", "Keshikan"));

        fragment.addCustomLicense(licenses);
        return fragment;
    }
}
