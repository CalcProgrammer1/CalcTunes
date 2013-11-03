package com.calcprogrammer1.calctunes.Activities;

import com.calcprogrammer1.calctunes.*;
import com.example.android.apis.graphics.ColorPickerDialog;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class CalcTunesSettingsActivity extends PreferenceActivity implements ColorPickerDialog.OnColorChangedListener
{
    ColorPickerDialog color_picker;
    SharedPreferences appSettings;
    SharedPreferences.Editor appSettingsEditor;
    
    Preference interface_color_pref;
    CheckBoxPreference car_mode_pref;
    CheckBoxPreference hp_mode_pref;
    CheckBoxPreference auto_close_pref;
    CheckBoxPreference system_service_notification_pref;
    CheckBoxPreference small_screen_layout_pref;
    
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settingsactivity);
        
        color_picker = new ColorPickerDialog(this, this, Color.BLUE);
        appSettings = getSharedPreferences("CalcTunes", MODE_PRIVATE);
        appSettingsEditor = appSettings.edit();
        
        interface_color_pref =             (Preference)         findPreference("interface_color");
        car_mode_pref =                    (CheckBoxPreference) findPreference("car_mode");
        hp_mode_pref =                     (CheckBoxPreference) findPreference("hp_mode");
        auto_close_pref =                  (CheckBoxPreference) findPreference("auto_close");
        system_service_notification_pref = (CheckBoxPreference) findPreference("service_notification");
        small_screen_layout_pref =         (CheckBoxPreference) findPreference("small_screen_layout");
        
        interface_color_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference arg0)
            {
                color_picker.show();
                return true;
            } 
        });
        
        car_mode_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference arg0)
            {
                appSettingsEditor.putBoolean("car_mode", car_mode_pref.isChecked());
                appSettingsEditor.commit();
                return true;                
            }
        });

        hp_mode_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference arg0)
            {
                appSettingsEditor.putBoolean("hp_mode", hp_mode_pref.isChecked());
                appSettingsEditor.commit();
                return true;                
            }
        });
   
        auto_close_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference arg0)
            {
                appSettingsEditor.putBoolean("auto_close", auto_close_pref.isChecked());
                appSettingsEditor.commit();
                return true;                
            }
        });
        
        system_service_notification_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference arg0)
            {
                appSettingsEditor.putBoolean("service_notification", system_service_notification_pref.isChecked());
                appSettingsEditor.commit();
                return true;
            }
        });
        
        small_screen_layout_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference arg0)
            {
                appSettingsEditor.putBoolean("small_screen_layout", small_screen_layout_pref.isChecked());
                appSettingsEditor.commit();
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
