package com.calcprogrammer1.calctunes.ContentSubsonicFragment;

import java.util.ArrayList;

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
import android.os.StrictMode;
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
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.SourceTypes.SubsonicSource;
import com.calcprogrammer1.calctunes.Subsonic.SubsonicAPI;

public class ContentSubsonicFragment extends Fragment
{
    public static final int CONTEXT_MENU_ADD_ARTIST_TO_PLAYLIST = 0;
    public static final int CONTEXT_MENU_ADD_ALBUM_TO_PLAYLIST  = 1;
    public static final int CONTEXT_MENU_ADD_TRACK_TO_PLAYLIST  = 2;
    public static final int CONTEXT_MENU_VIEW_TRACK_INFO        = 3;
    
    //ListView to display on
    private ListView rootView;
    
    //Callback
    private ContentFragmentInterface callback;
    
    //Library database adapter
    private ContentListAdapter libAdapter;
       
    //Library list
    ArrayList<ContentListElement> listData = new ArrayList<ContentListElement>();
    
    //Subsonic API
    SubsonicAPI subsonicapi;
    
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
            libAdapter.setNowPlayingColor(interfaceColor);
            libAdapter.notifyDataSetChanged(); 
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
        appSettings = getActivity().getSharedPreferences("CalcTunes", Activity.MODE_PRIVATE);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
        
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
            switch(listData.get(position).type)
            {
                case ContentListElement.LIBRARY_LIST_TYPE_HEADING:
                    menu.add(2, CONTEXT_MENU_ADD_ARTIST_TO_PLAYLIST, Menu.NONE, "Add Artist to Playlist");
                    break;
                    
                case ContentListElement.LIBRARY_LIST_TYPE_ALBUM:
                    menu.add(2, CONTEXT_MENU_ADD_ALBUM_TO_PLAYLIST, Menu.NONE, "Add Album to Playlist");
                    break;
                   
                case ContentListElement.LIBRARY_LIST_TYPE_TRACK:
                    menu.add(2, CONTEXT_MENU_ADD_TRACK_TO_PLAYLIST, Menu.NONE, "Add Track to Playlist");
                    menu.add(2, CONTEXT_MENU_VIEW_TRACK_INFO, Menu.NONE, "View Track Info");
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
                    callback.OnTrackInfoRequest(listData.get(position).path);
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
            listData = new ArrayList<ContentListElement>();
            
            ArrayList<SubsonicAPI.SubsonicArtist> artists = subsonicapi.SubsonicGetArtists();
            
            for(int i = 0; i < artists.size(); i++)
            {
                ContentListElement newElement = new ContentListElement();
                
                newElement.type   = ContentListElement.LIBRARY_LIST_TYPE_HEADING;
                newElement.artist = artists.get(i).name;
                newElement.id     = artists.get(i).id;
                
                listData.add(newElement);
            }
            if(getActivity() == null) Log.d("Subsonic Fragment", "Activity Context is Null");
            libAdapter = new ContentListAdapter(getActivity());
            libAdapter.attachList(listData);
            libAdapter.setNowPlaying(playbackservice.NowPlayingFile());
            libAdapter.setNowPlayingColor(interfaceColor);
            
            rootView.setAdapter(libAdapter);
            rootView.setDivider(null);
            rootView.setDividerHeight(0);
            
            registerForContextMenu(rootView);
            
        rootView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                switch(listData.get(position).type)
                {
                    case ContentListElement.LIBRARY_LIST_TYPE_HEADING:
                        if(listData.get(position).expanded)
                        {
                            while(true)
                            {
                                if((position + 1 < listData.size()) &&
                                  (listData.get(position + 1).type == ContentListElement.LIBRARY_LIST_TYPE_ALBUM
                                || listData.get(position + 1).type == ContentListElement.LIBRARY_LIST_TYPE_TRACK ))
                                {
                                    listData.remove(position + 1);
                                }
                                else
                                {
                                    break;
                                }
                            }
                            listData.get(position).expanded = false;
                        }
                        else
                        {
                            ArrayList<SubsonicAPI.SubsonicAlbum> albums = subsonicapi.SubsonicGetArtist((int)listData.get(position).id);
                            for(int i = 0; i < albums.size(); i++)
                            {
                                ContentListElement newElement = new ContentListElement();
                                
                                newElement.type   = ContentListElement.LIBRARY_LIST_TYPE_ALBUM;
                                newElement.artist = listData.get(position).artist;
                                newElement.album  = albums.get(i).name;
                                //newElement.year   = albums.get(i).year;
                                newElement.id     = albums.get(i).id;
                                
                                listData.add(position + (i + 1), newElement);
                            }
                            listData.get(position).expanded = true;
                        }
                        break;
                        
                    case ContentListElement.LIBRARY_LIST_TYPE_ALBUM:
                        if(listData.get(position).expanded)
                        {
                            while(true)
                            {
                                if((position + 1 < listData.size()) && (listData.get(position + 1).type == ContentListElement.LIBRARY_LIST_TYPE_TRACK))
                                {
                                    listData.remove(position + 1);
                                }
                                else
                                {
                                    break;
                                }
                            }
                            listData.get(position).expanded = false; 
                        }
                        else
                        {
                            ArrayList<SubsonicAPI.SubsonicSong> songs = subsonicapi.SubsonicGetAlbum((int)listData.get(position).id);
                            for(int i = 0; i < songs.size(); i++)
                            {
                                ContentListElement newElement = new ContentListElement();
                                
                                newElement.type   = ContentListElement.LIBRARY_LIST_TYPE_TRACK;
                                newElement.artist = listData.get(position).artist;
                                newElement.year   = listData.get(position).year;
                                newElement.album  = songs.get(i).album;
                                newElement.song   = songs.get(i).title;
                                newElement.track  = songs.get(i).track;
                                newElement.time   = songs.get(i).duration;
                                newElement.id     = songs.get(i).id;
                                newElement.path   = songs.get(i).suffix;
                                
                                listData.add(position + (i + 1), newElement);
                            }
                            
                            listData.get(position).expanded = true;
                        }
                        break;
                        
                    case ContentListElement.LIBRARY_LIST_TYPE_TRACK:
                        
                        Log.d("Subsonic Fragment", "Path:" + listData.get(position).path);
                        subsonicapi.SubsonicStream((int)listData.get(position).id, listData.get(position).track + " " + listData.get(position).song + ".ogg",
                                "ogg");
                        playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_FILESYSTEM, "/storage/sdcard0/calctunes/" + listData.get(position).track + " " + listData.get(position).song + ".ogg", 0, null);
                        libAdapter.setNowPlaying(playbackservice.NowPlayingFile());
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
        SubsonicSource source = SourceListOperations.readSubsonicFile(subSource);
        
        subsonicapi = new SubsonicAPI(source.address + ":" + source.port, source.username, source.password);
    }
}
