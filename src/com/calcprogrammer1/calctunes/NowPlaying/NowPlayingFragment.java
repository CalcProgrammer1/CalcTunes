package com.calcprogrammer1.calctunes.NowPlaying;

import java.util.ArrayList;

import com.calcprogrammer1.calctunes.*;
import com.calcprogrammer1.calctunes.Interfaces.ContentPlaybackInterface;
import com.calcprogrammer1.calctunes.Interfaces.NowPlayingFragmentInterface;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class NowPlayingFragment extends Fragment
{
    // Fragment main view
    View  view;
    
    // Views from inflated XML layout
    TextView artisttext;
    TextView albumtext;
    TextView tracktext;
    ImageView albumartview;
    SeekBar trackseek;
    SeekHandler trackseekhandler;
    
    // Shared Preferences
    private SharedPreferences appSettings;
    
    // Interface Color
    private int interfaceColor;
    
    // Callbacks
    private ArrayList<NowPlayingFragmentInterface> callbacks;
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
            interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
            setInterfaceColor(interfaceColor);
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
        interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
        
        //Start or Reconnect to the CalcTunes Playback Service
        getActivity().startService(new Intent(getActivity(), ContentPlaybackService.class));
        getActivity().bindService(new Intent(getActivity(), ContentPlaybackService.class), playbackserviceConnection, Context.BIND_AUTO_CREATE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        view = inflater.inflate(R.layout.nowplaying, group, false);
        registerForContextMenu(view);
        return view;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(playbackservice_bound)
        {
            getActivity().unbindService(playbackserviceConnection);
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if(playbackservice_bound)
        {
            updateGuiElements();
            playbackservice.registerCallback(playbackCallback);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// SERVICE CONNECTION /////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    private ContentPlaybackService playbackservice;
    private boolean playbackservice_bound = false;
    private ServiceConnection playbackserviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            playbackservice = ((ContentPlaybackService.ContentPlaybackBinder)service).getService();
            playbackservice_bound = true;
            updateGuiElements();
            playbackservice.registerCallback(playbackCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            playbackservice = null;
            playbackservice_bound = false;
        }    
    };
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// CALLBACKS /////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    ContentPlaybackInterface playbackCallback = new ContentPlaybackInterface(){
        @Override
        public void onTrackEnd()
        {
            //Do nothing on track end
        }

        @Override
        public void onMediaInfoUpdated()
        {
            //On media info updated, update all the text fields and album art display
            artisttext.setText(playbackservice.NowPlayingArtist());
            albumtext.setText(playbackservice.NowPlayingAlbum());
            tracktext.setText(playbackservice.NowPlayingTitle());
            albumartview.setImageBitmap(AlbumArtManager.getAlbumArtFromCache(playbackservice.NowPlayingArtist(), playbackservice.NowPlayingAlbum(), getActivity()));
        }  
    };
   
    public void registerCallback(NowPlayingFragmentInterface callback)
    {
        if(callbacks == null)
        {
            callbacks = new ArrayList<NowPlayingFragmentInterface>();
        }
        
        if(callback != null)
        {
            callbacks.add(callback);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// GUI UPDATE /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public void updateGuiElements()
    {   
        // Connect to information views
        artisttext   = (TextView)  getView().findViewById(R.id.text_artistname);
        albumtext    = (TextView)  getView().findViewById(R.id.text_albumname);
        tracktext    = (TextView)  getView().findViewById(R.id.text_trackname);
        albumartview = (ImageView) getView().findViewById(R.id.imageAlbumArt);
        trackseek    = (SeekBar)   getView().findViewById(R.id.seekBar_track);
        
        trackseekhandler = new SeekHandler(trackseek, playbackservice, getActivity());
                           
        setInterfaceColor(interfaceColor);
        
        // Register click function for album art/track info button
        getView().findViewById(R.id.imageAlbumArt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                ButtonInfoClick(v);
            }
        });

        // Register click function for stop button
        getView().findViewById(R.id.button_stop).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                ButtonStopClick(v);
            }
        });

        // Register click function for play/pause button
        getView().findViewById(R.id.button_playpause).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                ButtonPlayPauseClick(v);
            }
        });
        
        getView().findViewById(R.id.button_next).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                ButtonNextClick(v);
            }
        });
        
        getView().findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                ButtonPrevClick(v);
            }
        });
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// BUTTONS ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public void ButtonStopClick(View view)
    {
        playbackservice.StopPlayback();
    }
    
    public void ButtonPlayPauseClick(View view)
    {
        if(playbackservice.isPlaying())
        {
            playbackservice.PausePlayback();
        }
        else
        {
            playbackservice.StartPlayback();
        }
    }
    
    public void ButtonNextClick(View view)
    {
        playbackservice.NextTrack();
    }
    
    public void ButtonPrevClick(View view)
    {
        playbackservice.PrevTrack();
    }
    
    public void ButtonInfoClick(View view)
    {
        for(NowPlayingFragmentInterface callback : callbacks)
        {
            if(callback != null)
            {
                callback.onInfoButtonPressed();
            }
            else
            {
                callbacks.remove(callback);
            }
        }
    }
    
    public void setInterfaceColor(int color)
    {
        interfaceColor = color;
        trackseekhandler.setInterfaceColor(interfaceColor);
    }
}
