package com.calcprogrammer1.calctunes.ContentLibraryFragment;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.calcprogrammer1.calctunes.Activities.CalcTunesActivity;
import com.calcprogrammer1.calctunes.ContentPlaybackService.ContentPlaybackService;
import com.calcprogrammer1.calctunes.Dialogs.AddToPlaylistDialog;
import com.calcprogrammer1.calctunes.Interfaces.ContentFragmentInterface;

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
    
    // Current library file
    private String currentLibrary = "";

    private boolean playbackservice_bound = false;

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
    //private boolean playbackservice_bound = false;
    
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

    private BroadcastReceiver infoUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            libAdapter.setNowPlaying(playbackservice.GetNowPlayingString());
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

        //Register media info update receiver
        getActivity().registerReceiver(infoUpdateReceiver, new IntentFilter("com.calcprogrammer1.calctunes.PLAYBACK_INFO_UPDATED_EVENT"));

        libAdapter = new ContentListAdapter(getActivity());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(playbackservice_bound)
        {
            getActivity().unbindService(playbackserviceConnection);
        }

        getActivity().unregisterReceiver(infoUpdateReceiver);
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
                    menu.add(CalcTunesActivity.CONTEXT_MENU_CONTENT_LIBRARY, CONTEXT_MENU_ADD_ARTIST_TO_PLAYLIST, Menu.NONE, "Add Artist to Playlist");
                    break;
                    
                case ContentListElement.LIBRARY_LIST_TYPE_ALBUM:
                    menu.add(CalcTunesActivity.CONTEXT_MENU_CONTENT_LIBRARY, CONTEXT_MENU_ADD_ALBUM_TO_PLAYLIST, Menu.NONE, "Add Album to Playlist");
                    break;
                   
                case ContentListElement.LIBRARY_LIST_TYPE_TRACK:
                    menu.add(CalcTunesActivity.CONTEXT_MENU_CONTENT_LIBRARY, CONTEXT_MENU_ADD_TRACK_TO_PLAYLIST, Menu.NONE, "Add Track to Playlist");
                    menu.add(CalcTunesActivity.CONTEXT_MENU_CONTENT_LIBRARY, CONTEXT_MENU_VIEW_TRACK_INFO, Menu.NONE, "View Track Info");
                    break;
            }
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getGroupId() == CalcTunesActivity.CONTEXT_MENU_CONTENT_LIBRARY)
        {
            int position = (int) ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
            
            switch(item.getItemId())
            {
                case CONTEXT_MENU_ADD_ARTIST_TO_PLAYLIST:
                    {
                        AddToPlaylistDialog dialog = new AddToPlaylistDialog(getActivity());
                        ArrayList<String> fileList = new ArrayList<String>();
                        Cursor tmp = libraryDatabase.rawQuery("SELECT PATH FROM MYLIBRARY WHERE ARTIST = \"" + listData.get(position).artist + "\" ORDER BY ALBUM, DISC, TRACK;", null);
                        tmp.moveToFirst();
                        do
                        {
                            fileList.add(tmp.getString(tmp.getColumnIndex("PATH")));
                        } while(tmp.moveToNext());
                        dialog.addFileList(fileList);
                        dialog.show();
                    }
                    break;
                    
                case CONTEXT_MENU_ADD_ALBUM_TO_PLAYLIST:
                    {
                        AddToPlaylistDialog dialog = new AddToPlaylistDialog(getActivity());
                        ArrayList<String> fileList = new ArrayList<String>();
                        Cursor tmp = libraryDatabase.rawQuery("SELECT PATH FROM MYLIBRARY WHERE ARTIST = \"" + listData.get(position).artist + "\" AND ALBUM = \""
                                + listData.get(position).album + "\" ORDER BY DISC, TRACK;", null);

                        tmp.moveToFirst();
                        do
                        {
                            fileList.add(tmp.getString(tmp.getColumnIndex("PATH")));
                        } while(tmp.moveToNext());
                        dialog.addFileList(fileList);
                        dialog.show();
                    }
                    break;
                    
                case CONTEXT_MENU_ADD_TRACK_TO_PLAYLIST:
                    {
                        AddToPlaylistDialog dialog = new AddToPlaylistDialog(getActivity());
                        ArrayList<String> fileList = new ArrayList<String>();
                        fileList.add(listData.get(position).origPath);
                        dialog.addFileList(fileList);
                        dialog.show();
                    }
                    break;
                    
                case CONTEXT_MENU_VIEW_TRACK_INFO:
                    callback.OnTrackInfoRequest(listData.get(position).origPath);
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
        Log.d("ContentLibraryFragment", "Current library: " + currentLibrary);
        //Load list of artists
        libraryDatabase = SQLiteDatabase.openOrCreateDatabase(getActivity().getDatabasePath(currentLibrary + ".db"), null);
        if(libraryDatabase != null)
        {
            listData = new ArrayList<ContentListElement>();
            viewCursorQuery = "SELECT * FROM MYLIBRARY ORDER BY ARTIST, ALBUM, DISC, TRACK;";

            Cursor tmp = libraryDatabase.rawQuery("SELECT DISTINCT ARTIST FROM MYLIBRARY ORDER BY ARTIST;", null);

            tmp.moveToFirst();
            do
            {
                ContentListElement newElement = new ContentListElement();

                newElement.type = ContentListElement.LIBRARY_LIST_TYPE_HEADING;
                newElement.artist = tmp.getString(tmp.getColumnIndex("ARTIST"));

                listData.add(newElement);
            } while (tmp.moveToNext());

            libAdapter = new ContentListAdapter(getActivity());
            libAdapter.attachList(listData);
            libAdapter.setNowPlaying(playbackservice.GetNowPlayingString());

            rootView.setAdapter(libAdapter);
            rootView.setDivider(null);
            rootView.setDividerHeight(0);

            registerForContextMenu(rootView);

            rootView.setOnItemClickListener(new OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
                {
                    switch (listData.get(position).type)
                    {
                        case ContentListElement.LIBRARY_LIST_TYPE_HEADING:
                            if (listData.get(position).expanded)
                            {
                                while (true)
                                {
                                    if ((position + 1 < listData.size()) &&
                                            (listData.get(position + 1).type == ContentListElement.LIBRARY_LIST_TYPE_ALBUM
                                                    || listData.get(position + 1).type == ContentListElement.LIBRARY_LIST_TYPE_TRACK))
                                    {
                                        listData.remove(position + 1);
                                    } else
                                    {
                                        break;
                                    }
                                }
                                listData.get(position).expanded = false;
                            } else
                            {
                                Cursor tmp = libraryDatabase.rawQuery("SELECT ALBUM, YEAR FROM MYLIBRARY WHERE _id IN (SELECT MIN(_id) FROM (SELECT * FROM MYLIBRARY WHERE ARTIST = \"" + listData.get(position).artist.replace("\"", "\"\"") + "\") GROUP BY ALBUM) ORDER BY ALBUM;", null);
                                tmp.moveToFirst();
                                int i = 1;
                                do
                                {
                                    ContentListElement newElement = new ContentListElement();

                                    newElement.type = ContentListElement.LIBRARY_LIST_TYPE_ALBUM;
                                    newElement.artist = listData.get(position).artist;
                                    newElement.album = tmp.getString(tmp.getColumnIndex("ALBUM"));
                                    newElement.year = tmp.getString(tmp.getColumnIndex("YEAR"));

                                    listData.add(position + i++, newElement);
                                } while (tmp.moveToNext());
                                listData.get(position).expanded = true;
                            }
                            break;

                        case ContentListElement.LIBRARY_LIST_TYPE_ALBUM:
                            if (listData.get(position).expanded)
                            {
                                while (true)
                                {
                                    if ((position + 1 < listData.size()) && (listData.get(position + 1).type == ContentListElement.LIBRARY_LIST_TYPE_TRACK))
                                    {
                                        listData.remove(position + 1);
                                    } else
                                    {
                                        break;
                                    }
                                }
                                listData.get(position).expanded = false;
                            } else
                            {
                                Cursor tmp = libraryDatabase.rawQuery("SELECT TRACK, TITLE, TIME, PATH, _id, DISC FROM MYLIBRARY WHERE ARTIST = \"" + listData.get(position).artist.replace("\"", "\"\"") + "\" AND ALBUM = \""
                                        + listData.get(position).album.replace("\"", "\"\"") + "\" ORDER BY DISC, TRACK;", null);

                                tmp.moveToFirst();
                                int i = 1;
                                do
                                {
                                    ContentListElement newElement = new ContentListElement();

                                    newElement.type = ContentListElement.LIBRARY_LIST_TYPE_TRACK;
                                    newElement.artist = listData.get(position).artist;
                                    newElement.year = listData.get(position).year;
                                    newElement.album = listData.get(position).album;
                                    newElement.title = tmp.getString(tmp.getColumnIndex("TITLE"));
                                    newElement.track = tmp.getInt(tmp.getColumnIndex("TRACK"));
                                    newElement.time = tmp.getInt(tmp.getColumnIndex("TIME"));
                                    newElement.id = tmp.getInt(tmp.getColumnIndex("_id"));
                                    newElement.origPath = tmp.getString(tmp.getColumnIndex("PATH"));

                                    listData.add(position + i++, newElement);
                                } while (tmp.moveToNext());
                                listData.get(position).expanded = true;
                            }
                            break;

                        case ContentListElement.LIBRARY_LIST_TYPE_TRACK:
                            Cursor playbackCursor = libraryDatabase.rawQuery(viewCursorQuery, null);

                            //Find position that matches id
                            playbackCursor.moveToFirst();

                            while (true)
                            {
                                if (playbackCursor.getLong(playbackCursor.getColumnIndex("_id")) == listData.get(position).id)
                                {
                                    break;
                                }
                                playbackCursor.moveToNext();
                            }
                            playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_LIBRARY, currentLibrary, playbackCursor.getPosition());
                            libAdapter.setNowPlaying(playbackservice.GetNowPlayingString());
                            break;
                    }
                    libAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void setLibrary(String newLibrary)
    {
        currentLibrary = newLibrary;
        Log.d("ContentLibraryFragment", "Loading new library " + currentLibrary);
    }
}
