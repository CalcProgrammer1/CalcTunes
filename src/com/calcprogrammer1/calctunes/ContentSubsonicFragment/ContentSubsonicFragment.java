package com.calcprogrammer1.calctunes.ContentSubsonicFragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.calcprogrammer1.calctunes.ContentPlaybackService;
import com.calcprogrammer1.calctunes.ContentLibraryFragment.ContentListAdapter;
import com.calcprogrammer1.calctunes.ContentLibraryFragment.ContentListElement;
import com.calcprogrammer1.calctunes.Interfaces.ContentFragmentInterface;
import com.calcprogrammer1.calctunes.Interfaces.ContentPlaybackInterface;
import com.calcprogrammer1.calctunes.Interfaces.SubsonicConnectionCallback;
import com.calcprogrammer1.calctunes.Subsonic.SubsonicConnection;

@SuppressWarnings("unused")

public class ContentSubsonicFragment extends Fragment
{
    public static final int CONTEXT_MENU_ADD_ARTIST_TO_PLAYLIST = 0;
    public static final int CONTEXT_MENU_ADD_ALBUM_TO_PLAYLIST  = 1;
    public static final int CONTEXT_MENU_ADD_TRACK_TO_PLAYLIST  = 2;
    public static final int CONTEXT_MENU_VIEW_TRACK_INFO        = 3;
    public static final int CONTEXT_MENU_DWNLD_TRACK_TRANSCODED = 4;
    public static final int CONTEXT_MENU_DWNLD_TRACK_ORIGINAL   = 5;
    
    //ListView to display on
    private ListView rootView;
    
    //Callback
    private ContentFragmentInterface callback;
    
    //Library database adapter
    private ContentListAdapter libAdapter;
       
    //Subsonic Connection
    SubsonicConnection subcon;
    
    // Shared Preferences
    private SharedPreferences appSettings;
    
    // Current library file
    private String currentLibrary = "";
    
    private int play_id;
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
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
            playbackservice.registerCallback(playbackCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            playbackservice = null;
            playbackservice_bound = false;
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
            libAdapter.setNowPlaying(playbackservice.NowPlayingFile());
            libAdapter.notifyDataSetChanged();
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
        
        //Start or Reconnect to the CalcTunes Playback Service
        getActivity().startService(new Intent(getActivity(), ContentPlaybackService.class));
        getActivity().bindService(new Intent(getActivity(), ContentPlaybackService.class), playbackserviceConnection, Context.BIND_AUTO_CREATE);
        
        libAdapter = new ContentListAdapter(getActivity());
       
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        rootView = new ListView(getActivity());
        return rootView;
    }
    
    public void setCallback(ContentFragmentInterface call)
    {
        callback = call;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// CONTEXT MENU //////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        if(v == rootView)
        {
            int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
            switch(subcon.listData.get(position).type)
            {
                case ContentListElement.LIBRARY_LIST_TYPE_HEADING:
                    menu.add(2, CONTEXT_MENU_ADD_ARTIST_TO_PLAYLIST, Menu.NONE, "Add Artist to Playlist");
                    break;
                    
                case ContentListElement.LIBRARY_LIST_TYPE_ALBUM:
                    menu.add(2, CONTEXT_MENU_ADD_ALBUM_TO_PLAYLIST, Menu.NONE, "Add Album to Playlist");
                    break;
                   
                case ContentListElement.LIBRARY_LIST_TYPE_TRACK:
                    menu.add(2, CONTEXT_MENU_DWNLD_TRACK_TRANSCODED, Menu.NONE, "Download Track (Transcoded)");
                    menu.add(2, CONTEXT_MENU_DWNLD_TRACK_ORIGINAL,   Menu.NONE, "Download Track (Original)");
                    break;
            }
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getGroupId() == 2)
        {
            int position = (int) ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
            
            switch(item.getItemId())
            {
                case CONTEXT_MENU_ADD_ARTIST_TO_PLAYLIST:
                    break;
                    
                case CONTEXT_MENU_ADD_ALBUM_TO_PLAYLIST:
                    break;
                    
                case CONTEXT_MENU_ADD_TRACK_TO_PLAYLIST:
                    break;
                    
                case CONTEXT_MENU_VIEW_TRACK_INFO:
                    callback.OnTrackInfoRequest(subcon.listData.get(position).path);
                    break;
                    
                case CONTEXT_MENU_DWNLD_TRACK_TRANSCODED:
                    subcon.downloadTranscodedOgg(position);
                    break;
                    
                case CONTEXT_MENU_DWNLD_TRACK_ORIGINAL:
                    subcon.downloadOriginal(position);
                    break;
            }
        }
        else
        {
            return false;
        }
        
        return(super.onOptionsItemSelected(item));
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// LIST UPDATING FUNCTIONS ///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public void updateList()
    {
            //Load list of artists
            subcon.getArtistListAsync();
            
            libAdapter = new ContentListAdapter(getActivity());
            libAdapter.attachList(subcon.listData);
            libAdapter.setNowPlaying(playbackservice.NowPlayingFile());
            
            rootView.setAdapter(libAdapter);
            rootView.setDivider(null);
            rootView.setDividerHeight(0);
            
            registerForContextMenu(rootView);
            
        rootView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                switch(subcon.listData.get(position).type)
                {
                    case ContentListElement.LIBRARY_LIST_TYPE_HEADING:
                        if(subcon.listData.get(position).expanded)
                        {
                            subcon.collapseArtist(position);
                        }
                        else
                        {
                            subcon.expandArtistAsync(position);
                        }
                        break;
                        
                    case ContentListElement.LIBRARY_LIST_TYPE_ALBUM:
                        if(subcon.listData.get(position).expanded)
                        {
                            subcon.collapseAlbum(position);
                        }
                        else
                        {
                            subcon.expandAlbumAsync(position);
                        }
                        break;
                        
                    case ContentListElement.LIBRARY_LIST_TYPE_TRACK:
                        
                        Log.d("Subsonic Fragment", "Path:" + subcon.listData.get(position).path);
                        play_id = (int)subcon.listData.get(position).id;
                        subcon.downloadTranscodedOgg(position);
                        break;
                }
                libAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setSubsonicSource(String subSource)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        
        subcon = new SubsonicConnection(subSource);
        subcon.SetCallback(subsonic_callback);
    }
    
    SubsonicConnectionCallback subsonic_callback = new SubsonicConnectionCallback(){

        public void onListUpdated()
        {
            libAdapter.attachList(subcon.listData);
            libAdapter.notifyDataSetChanged();
        }
        
        @Override
        public void onTrackLoaded(int id, String filename)
        {
            if(id == play_id) playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_FILESYSTEM, filename, 0);
        }
        
    };
}
