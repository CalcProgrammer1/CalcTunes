package com.calcprogrammer1.calctunes.ContentLibraryFragment;

import java.util.ArrayList;

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
import com.calcprogrammer1.calctunes.Interfaces.ContentFragmentInterface;
import com.calcprogrammer1.calctunes.Interfaces.ContentPlaybackInterface;

public class ContentLibraryFragment extends Fragment
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
    
    //Cursors - one for content list view and one for playback
    private SQLiteDatabase libraryDatabase;

    //SQL Query String
    private String viewCursorQuery;
    
    //Library list
    ArrayList<ContentListElement> listData = new ArrayList<ContentListElement>();
    
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
            libraryDatabase = SQLiteDatabase.openOrCreateDatabase(getActivity().getDatabasePath(currentLibrary + ".db"), null);
            listData = new ArrayList<ContentListElement>();
            viewCursorQuery = "SELECT * FROM MYLIBRARY ORDER BY ARTIST, ALBUM, DISC, TRACK;";
            
            Cursor tmp = libraryDatabase.rawQuery("SELECT DISTINCT ARTIST FROM MYLIBRARY ORDER BY ARTIST;", null);
            
            tmp.moveToFirst();
            do
            {
                ContentListElement newElement = new ContentListElement();
                
                newElement.type   = ContentListElement.LIBRARY_LIST_TYPE_HEADING;
                newElement.artist = tmp.getString(tmp.getColumnIndex("ARTIST"));
                
                listData.add(newElement);
            } while(tmp.moveToNext());
            
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
                            Cursor tmp = libraryDatabase.rawQuery("SELECT ALBUM, YEAR FROM MYLIBRARY WHERE _id IN (SELECT MIN(_id) FROM (SELECT * FROM MYLIBRARY WHERE ARTIST = \"" + listData.get(position).artist + "\") GROUP BY ALBUM) ORDER BY ALBUM;", null);
                            tmp.moveToFirst();
                            int i = 1;
                            do
                            {
                                ContentListElement newElement = new ContentListElement();
                                
                                newElement.type   = ContentListElement.LIBRARY_LIST_TYPE_ALBUM;
                                newElement.artist = listData.get(position).artist;
                                newElement.album  = tmp.getString(tmp.getColumnIndex("ALBUM"));
                                newElement.year   = tmp.getString(tmp.getColumnIndex("YEAR"));
                                
                                listData.add(position+i++, newElement);
                            } while(tmp.moveToNext());
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
                            Cursor tmp = libraryDatabase.rawQuery("SELECT TRACK, TITLE, TIME, PATH, _id, DISC FROM MYLIBRARY WHERE ARTIST = \"" + listData.get(position).artist + "\" AND ALBUM = \""
                                                        + listData.get(position).album + "\" ORDER BY DISC, TRACK;", null);
                            
                            tmp.moveToFirst();
                            int i = 1;
                            do
                            {
                                ContentListElement newElement = new ContentListElement();
                                
                                newElement.type   = ContentListElement.LIBRARY_LIST_TYPE_TRACK;
                                newElement.artist = listData.get(position).artist;
                                newElement.year   = listData.get(position).year;
                                newElement.album  = listData.get(position).album;
                                newElement.song   = tmp.getString(tmp.getColumnIndex("TITLE"));
                                newElement.track  = tmp.getInt(tmp.getColumnIndex("TRACK"));
                                newElement.time   = tmp.getInt(tmp.getColumnIndex("TIME"));
                                newElement.id     = tmp.getLong(tmp.getColumnIndex("_id"));
                                newElement.path   = tmp.getString(tmp.getColumnIndex("PATH"));
                                
                                listData.add(position+i++, newElement);
                            } while(tmp.moveToNext());
                            listData.get(position).expanded = true;
                        }
                        break;
                        
                    case ContentListElement.LIBRARY_LIST_TYPE_TRACK:
                        Cursor playbackCursor = libraryDatabase.rawQuery(viewCursorQuery, null);
                        
                        //Find position that matches id
                        playbackCursor.moveToFirst();
                        
                        while(true)
                        {
                            if(playbackCursor.getLong(playbackCursor.getColumnIndex("_id")) == listData.get(position).id)
                            {
                                break;
                            }
                            playbackCursor.moveToNext();
                        }
                        playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_LIBRARY, currentLibrary, playbackCursor.getPosition());
                        libAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                        break;
                }
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
