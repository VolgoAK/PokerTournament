package com.volgoak.pokertournament;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.artitk.licensefragment.model.License;
import com.artitk.licensefragment.model.LicenseType;
import com.artitk.licensefragment.support.v4.RecyclerViewLicenseFragment;

import java.util.ArrayList;

public class AboutActivity extends AppCompatActivity {

    private RecyclerViewLicenseFragment mLicenseFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        toolbar.setTitle("About app");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setSupportActionBar(toolbar);

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager_about);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_about);

        pager.setAdapter(new BlindsPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private Fragment createLicenseFragment(){
        if(mLicenseFragment == null) {
            mLicenseFragment = RecyclerViewLicenseFragment.newInstance();
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

            mLicenseFragment.addCustomLicense(licenses);
        }

        return mLicenseFragment;
    }

    class BlindsPagerAdapter extends FragmentPagerAdapter{

        public static final int ITEM_COUNT = 2;

        public BlindsPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new AboutFragment();
                case 1:
                    return createLicenseFragment();
                default:
                    throw new IndexOutOfBoundsException("Fragment position is out of range");
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.title_fragment_about);
                case 1:
                    return getString(R.string.title_opensource);
                default:
                    throw new IndexOutOfBoundsException("Fragment positions is out of range");
            }
        }

        @Override
        public int getCount() {
            return ITEM_COUNT;
        }
    }
}
