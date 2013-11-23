package com.calcprogrammer1.calctunes.ContentFilesystemFragment;

import com.calcprogrammer1.calctunes.ContentPlaybackService;
import com.calcprogrammer1.calctunes.Interfaces.ContentPlaybackInterface;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ContentFilesystemFragment extends Fragment
{
    //ListView to display on
    private ListView rootView;
    
    // Filesystem content adapter for listview
    private ContentFilesystemAdapter fileAdapter;
    
    // Shared Preferences
    private SharedPreferences appSettings;
    
    // Interface Color
    private int interfaceColor;
    
    // Current directory
    private String currentDirectory = "/";
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
            interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
            fileAdapter.setNowPlayingColor(interfaceColor);
            fileAdapter.notifyDataSetChanged(); 
        }
    };
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //Service Connection///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    private ContentPlaybackService playbackservice;
    //private boolean playbackservice_bound = false;
    
    private ServiceConnection playbackserviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            playbackservice = ((ContentPlaybackService.ContentPlaybackBinder)service).getService();
            //playbackservice_bound = true;
            updateList();
            playbackservice.registerCallback(playbackCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            playbackservice = null;
            //playbackservice_bound = false;
        }    
    };
    
    ContentPlaybackInterface playbackCallback = new ContentPlaybackInterface(){
        @Override
        public void onTrackEnd()
        {
            //Do nothing on track end
        }

        @Override
        public void onMediaInfoUpdated()
        {
            fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
            fileAdapter.notifyDataSetChanged(); 
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
        appSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
        
        //Start or Reconnect to the CalcTunes Playback Service
        getActivity().startService(new Intent(getActivity(), ContentPlaybackService.class));
        getActivity().bindService(new Intent(getActivity(), ContentPlaybackService.class), playbackserviceConnection, Context.BIND_AUTO_CREATE);
        
        //Create starting file adapter
        fileAdapter = new ContentFilesystemAdapter(getActivity(), currentDirectory);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        rootView = new ListView(getActivity());
        registerForContextMenu(rootView);
        return rootView;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// LIST UPDATING FUNCTIONS ///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public void updateList()
    {
        fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
        fileAdapter.setNowPlayingColor(interfaceColor);
        rootView.setAdapter(fileAdapter);
        rootView.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                if(position == 0 && !fileAdapter.currentDirectory.getPath().equals("/"))
                {
                    currentDirectory = fileAdapter.currentDirectory.getParent();
                    fileAdapter = new ContentFilesystemAdapter(getActivity(), currentDirectory);
                    fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                    fileAdapter.setNowPlayingColor(interfaceColor);
                    updateList();
                }
                else if(fileAdapter.files.get(position).isDirectory())
                {
                    currentDirectory = fileAdapter.files.get(position).getPath();
                    fileAdapter = new ContentFilesystemAdapter(getActivity(), currentDirectory);
                    fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                    fileAdapter.setNowPlayingColor(interfaceColor);
                    updateList();
                }
                else
                {
                    playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_FILESYSTEM, fileAdapter.files.get(position).getPath(), 0);
                    fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                    fileAdapter.notifyDataSetChanged();                
                }
            }   
        });
    }

    public void setDirectory(String newDirectory)
    {
        currentDirectory = newDirectory;
        fileAdapter = new ContentFilesystemAdapter(getActivity(), currentDirectory);
    }
}
