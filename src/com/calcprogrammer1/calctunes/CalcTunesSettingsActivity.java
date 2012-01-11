package com.calcprogrammer1.calctunes;

import com.example.android.apis.graphics.ColorPickerDialog;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class CalcTunesSettingsActivity extends PreferenceActivity implements ColorPickerDialog.OnColorChangedListener 
{
    ColorPickerDialog color_picker;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settingsactivity);
        
        color_picker = new ColorPickerDialog(this, this, Color.BLUE);
        
        Preference interface_color_pref = (Preference) findPreference("interface_color");
        interface_color_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference arg0)
            {
                Toast.makeText(getBaseContext(), "Stuff Happend", Toast.LENGTH_SHORT).show();
                
                color_picker.show();
                return true;
            } 
        });
    }

    public void colorChanged(int color)
    {
        SharedPreferences appSettings = getSharedPreferences("CalcTunes", MODE_PRIVATE);
        SharedPreferences.Editor appSettingsEditor = appSettings.edit();
        appSettingsEditor.putInt("InterfaceColor", color);
        appSettingsEditor.commit();
    }
}
