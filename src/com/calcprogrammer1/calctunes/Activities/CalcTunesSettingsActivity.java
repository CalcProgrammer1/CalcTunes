package com.calcprogrammer1.calctunes.Activities;

import com.calcprogrammer1.calctunes.*;
import com.example.android.apis.graphics.ColorPickerDialog;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class CalcTunesSettingsActivity extends PreferenceActivity implements ColorPickerDialog.OnColorChangedListener, OnSharedPreferenceChangeListener
{
    ColorPickerDialog color_picker;
    SharedPreferences appSettings;
    SharedPreferences.Editor appSettingsEditor;
    
    Preference interface_color_pref;
    
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settingsactivity);
        
        color_picker = new ColorPickerDialog(this, this, Color.BLUE);
        appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        appSettings.registerOnSharedPreferenceChangeListener(this);
        appSettingsEditor = appSettings.edit();
        
        interface_color_pref =             (Preference)         findPreference("interface_color");
        
        interface_color_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference arg0)
            {
                color_picker.show();
                return true;
            } 
        });
    }

    public void colorChanged(int color)
    {
        SharedPreferences.Editor appSettingsEditor = appSettings.edit();
        appSettingsEditor.putInt("InterfaceColor", color);
        appSettingsEditor.commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
    {
        Log.d("onSharedPreferencesChanged", "sharedPreferences changed.  key: " + arg1);
    }
}
