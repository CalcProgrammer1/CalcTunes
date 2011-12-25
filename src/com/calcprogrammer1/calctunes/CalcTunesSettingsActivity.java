package com.calcprogrammer1.calctunes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

public class CalcTunesSettingsActivity extends Activity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingsactivity);
        
        SharedPreferences appSettings = getSharedPreferences("CalcTunes", MODE_PRIVATE);
        ((SeekBar) findViewById(R.id.settingsSeekBarRed)).setProgress(Color.red(appSettings.getInt("InterfaceColor", Color.BLUE)));
        ((SeekBar) findViewById(R.id.settingsSeekBarGreen)).setProgress(Color.green(appSettings.getInt("InterfaceColor", Color.BLUE)));
        ((SeekBar) findViewById(R.id.settingsSeekBarBlue)).setProgress(Color.blue(appSettings.getInt("InterfaceColor", Color.BLUE)));
        
        Spinner playMode = (Spinner) findViewById(R.id.settingsPlaybackModeSpinner);
        CharSequence modeList[] = {"Play All", "Random"};
        ArrayAdapter<CharSequence> playModeAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, modeList);
        playModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playMode.setAdapter(playModeAdapter);
    }
    
    public void saveButtonClick(View view)
    {
        int red = ((SeekBar) findViewById(R.id.settingsSeekBarRed)).getProgress();
        int grn = ((SeekBar) findViewById(R.id.settingsSeekBarGreen)).getProgress();
        int blu = ((SeekBar) findViewById(R.id.settingsSeekBarBlue)).getProgress();
        SharedPreferences appSettings = getSharedPreferences("CalcTunes", MODE_PRIVATE);
        SharedPreferences.Editor appSettingsEditor = appSettings.edit();
        appSettingsEditor.putInt("InterfaceColor", Color.rgb(red, grn, blu));
        appSettingsEditor.commit();
        setResult(Activity.RESULT_OK, null);
        finish();
    }
}
