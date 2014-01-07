package com.calcprogrammer1.calctunes.Activities;

import com.calcprogrammer1.calctunes.*;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class CalcTunesSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    SharedPreferences appSettings;
    SharedPreferences.Editor appSettingsEditor;
    
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settingsactivity);

        appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        appSettings.registerOnSharedPreferenceChangeListener(this);
        appSettingsEditor = appSettings.edit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
    {
        Log.d("onSharedPreferencesChanged", "sharedPreferences changed.  key: " + arg1);
    }
}
