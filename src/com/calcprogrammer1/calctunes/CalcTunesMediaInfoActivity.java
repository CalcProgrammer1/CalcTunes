package com.calcprogrammer1.calctunes;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class CalcTunesMediaInfoActivity extends Activity
{
    SharedPreferences appSettings;
    int interfaceColor;
    
    String mediaInfoPath;
    
    TextView mediaInfoTitle;
    TextView mediaInfoArtist;
    TextView mediaInfoAlbum;
    TextView mediaInfoGenre;
    TextView mediaInfoYear;
    TextView mediaInfoTrack;
    TextView mediaInfoDisc;
    TextView mediaInfoFormat;
    TextView mediaInfoBitrate;
    
    ImageView mediaInfoAlbumArt;
    ImageView mediaInfoFormatLogo;
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
            interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
            updateInterfaceColor(interfaceColor);
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.mediainfo);

        //Get intent
        Intent intent;
        intent = getIntent();
        Bundle extras = intent.getExtras();
        mediaInfoPath = extras.getString("TrackFilename");
        
        appSettings = getSharedPreferences("CalcTunes",MODE_PRIVATE);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
        
        updateGuiElements();
    }
    
    public void updateGuiElements()
    {
        mediaInfoTitle = (TextView) findViewById(R.id.mediaInfoTitle);
        mediaInfoArtist = (TextView) findViewById(R.id.mediaInfoArtist);
        mediaInfoAlbum = (TextView) findViewById(R.id.mediaInfoAlbum);
        mediaInfoGenre = (TextView) findViewById(R.id.mediaInfoGenre);
        mediaInfoYear = (TextView) findViewById(R.id.mediaInfoYear);
        mediaInfoTrack = (TextView) findViewById(R.id.mediaInfoTrack);
        mediaInfoDisc = (TextView) findViewById(R.id.mediaInfoDisc);
        mediaInfoFormat = (TextView) findViewById(R.id.mediaInfoFormat);
        mediaInfoBitrate = (TextView) findViewById(R.id.mediaInfoBitrate);
        mediaInfoAlbumArt = (ImageView) findViewById(R.id.mediaInfoAlbumArt);
        mediaInfoFormatLogo = (ImageView) findViewById(R.id.mediaInfoFormatLogo);
        
        File file = new File(mediaInfoPath);
        AudioFile f = LibraryOperations.readAudioFileReadOnly(file);
        Tag tag = f.getTag();
        AudioHeader header = f.getAudioHeader();
        
        mediaInfoTitle.setText(tag.getFirst(FieldKey.TITLE));
        mediaInfoArtist.setText(tag.getFirst(FieldKey.ARTIST));
        mediaInfoAlbum.setText(tag.getFirst(FieldKey.ALBUM));
        mediaInfoGenre.setText(tag.getFirst(FieldKey.GENRE));
        mediaInfoYear.setText(tag.getFirst(FieldKey.YEAR));
        mediaInfoTrack.setText(tag.getFirst(FieldKey.TRACK));
        mediaInfoDisc.setText(tag.getFirst(FieldKey.DISC_NO));
        mediaInfoFormat.setText(header.getFormat());
        mediaInfoBitrate.setText(header.getBitRate());
        mediaInfoAlbumArt.setImageBitmap(AlbumArtManager.getAlbumArt(tag.getFirst(FieldKey.ARTIST), tag.getFirst(FieldKey.ALBUM), this));
        
        //If ICS, make the title border match the ICS Holo theme
        if(Integer.valueOf(android.os.Build.VERSION.SDK) > 10)
        {
            findViewById(R.id.title_border).setBackgroundResource(android.R.color.holo_blue_light);
        }
        
        if(header.getFormat().equals("FLAC 16 bits") | header.getFormat().equals("FLAC 8 bits"))
        {
            mediaInfoFormatLogo.setImageResource(R.drawable.icon_format_flac);
        }
        else if(header.getFormat().equals("MPEG-1 Layer 3"))
        {
            mediaInfoFormatLogo.setImageResource(R.drawable.icon_format_mp3);
        }
    }
    
    public void updateInterfaceColor(int color)
    {
        
    }
}
