package com.calcprogrammer1.calctunes.ContentPlaylistFragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.calcprogrammer1.calctunes.Activities.CalcTunesActivity;
import com.calcprogrammer1.calctunes.ContentPlaybackService.ContentPlaybackPlaylist;
import com.calcprogrammer1.calctunes.ContentPlaybackService.ContentPlaybackService;

public class ContentPlaylistFragment extends Fragment implements ListView.OnItemClickListener
{
    public static final int CONTEXT_MENU_REMOVE_FROM_PLAYLIST   = 0;
    public static final int CONTEXT_MENU_MOVE_UP                = 1;
    public static final int CONTEXT_MENU_MOVE_DOWN              = 2;

    //Shared Preferences
    private SharedPreferences appSettings;
    
    //Playlist Views
    ListView view;
    ContentPlaylistAdapter   adapter;

    //Playlist Filename
    private String playlistFilename;

    //Playlist Editor
    private PlaylistEditor playlist;

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
            adapter.setNowPlaying(playbackservice.GetNowPlayingString());
            adapter.notifyDataSetChanged();
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// CONTEXT MENU //////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        if(v == view)
        {
            int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
            menu.add(CalcTunesActivity.CONTEXT_MENU_CONTENT_PLAYLIST, CONTEXT_MENU_REMOVE_FROM_PLAYLIST, Menu.NONE, "Remove Track From Playlist");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if (item.getGroupId() == CalcTunesActivity.CONTEXT_MENU_CONTENT_PLAYLIST)
        {
            final int position = (int) ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;

            switch (item.getItemId())
            {
                case CONTEXT_MENU_REMOVE_FROM_PLAYLIST:
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    playlist.playlistData.remove(position);
                                    playlist.writePlaylistFile(null);
                                    updateList();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure you want to remove this track?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
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
    /////////////////////////////////// FRAGMENT CREATE FUNCTIONS /////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void setPlaylist(String filename)
    {
        playlistFilename = filename;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        
        //Get the application preferences
        appSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);

        //Create a playlist editor
        playlist = new PlaylistEditor(getActivity());
        playlist.readPlaylistFile(playlistFilename);

        //Start or Reconnect to the CalcTunes Playback Service
        getActivity().startService(new Intent(getActivity(), ContentPlaybackService.class));
        getActivity().bindService(new Intent(getActivity(), ContentPlaybackService.class), playbackserviceConnection, Context.BIND_AUTO_CREATE);

        //Register media info update receiver
        getActivity().registerReceiver(infoUpdateReceiver, new IntentFilter("com.calcprogrammer1.calctunes.PLAYBACK_INFO_UPDATED_EVENT"));
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
        view = new ListView(getActivity());
        updateList();
        return view;
    }

    public void updateList()
    {
        view.setDividerHeight(0);
        adapter = new ContentPlaylistAdapter(getActivity());
        adapter.setPlaylist(playlist);
        view.setAdapter(adapter);
        view.setOnItemClickListener(this);
        registerForContextMenu(view);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        adapter.setNowPlaying(playlist.playlistData.get(position).filename);
        adapter.notifyDataSetChanged();
        ContentPlaybackPlaylist playlistsource = new ContentPlaybackPlaylist(playlist, position);
        playbackservice.SetPlaybackContent(playlistsource);
    }
}
