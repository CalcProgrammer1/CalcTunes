package com.calcprogrammer1.calctunes.ContentLibraryFragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.calcprogrammer1.calctunes.ContentPlaybackService;
import com.calcprogrammer1.calctunes.ContentFilesystemFragment.ContentFilesystemAdapter;

public class ContentLibraryFragment extends Fragment
{
    //ListView to display on
    private ListView rootView;
    
    //Library database adapter
    private ContentLibraryDatabaseAdapter libAdapter;
    
    //Cursors - one for content list view and one for playback
    private SQLiteDatabase libraryDatabase;

    //SQL Query String
    private String viewCursorQuery;
    
    // Shared Preferences
    private SharedPreferences appSettings;
    
    // Interface Color
    private int interfaceColor;
    
    // Current library file
    private String currentLibrary = "";
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
            interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
            
        }
    };
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //Service Connection///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    private ContentPlaybackService playbackservice;
    private boolean playbackservice_bound = false;
    private ServiceConnection playbackserviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            playbackservice = ((ContentPlaybackService.ContentPlaybackBinder)service).getService();
            playbackservice_bound = true;
            updateList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            playbackservice = null;
            playbackservice_bound = false;
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
        
        libAdapter = new ContentLibraryDatabaseAdapter(getActivity(), null, null);
       
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
            libraryDatabase = SQLiteDatabase.openOrCreateDatabase("/data/data/com.calcprogrammer1.calctunes/databases/" + currentLibrary + ".db", null);
            viewCursorQuery = "SELECT * FROM MYLIBRARY ORDER BY ARTIST, ALBUM, DISC, TRACK;";
            libAdapter = new ContentLibraryDatabaseAdapter(getActivity(), libraryDatabase, viewCursorQuery);
            libAdapter.setNowPlaying(playbackservice.NowPlayingFile());
            libAdapter.setNowPlayingColor(interfaceColor);
            rootView.setAdapter(libAdapter);
        rootView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                Cursor playbackCursor = libraryDatabase.rawQuery(viewCursorQuery, null);
                playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_LIBRARY, null, position, playbackCursor);
                libAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                libAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setLibrary(String newLibrary)
    {
        currentLibrary = newLibrary;
        Log.d("ContentLibraryFragment", "Loading new library " + currentLibrary);
    }
}
