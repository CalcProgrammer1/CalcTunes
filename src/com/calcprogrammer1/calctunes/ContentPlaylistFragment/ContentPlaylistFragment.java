package com.calcprogrammer1.calctunes.ContentPlaylistFragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class ContentPlaylistFragment extends Fragment
{
    private SharedPreferences appSettings;
    
    //Playlist List View
    ExpandableListView  view;
    ContentPlaylistAdapter   adapter;
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
            //interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
            //setInterfaceColor(interfaceColor);
        }
    };
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// FRAGMENT CREATE FUNCTIONS /////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
        
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        
        //Get the application preferences
        appSettings = getActivity().getSharedPreferences("CalcTunes", Activity.MODE_PRIVATE);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        //interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        view = new ExpandableListView(getActivity());
        view.setGroupIndicator(null);
        view.setDividerHeight(0);
        //updateListView();
        registerForContextMenu(view);
        //setInterfaceColor(interfaceColor);
        return view;
    }
}
