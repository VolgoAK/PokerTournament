package com.volgoak.pokertournament;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        WebView wv = (WebView) getView().findViewById(R.id.web_about_frag);
        String url = String.format("file:///android_asset/%s-about.html", getString(R.string.html_prefix));
        wv.loadUrl(url);
//        wv.loadUrl("file:///android_asset/htm.html");
//        wv.loadData(getString(R.string.html), "text/html", "UTF-8");
    }
}
