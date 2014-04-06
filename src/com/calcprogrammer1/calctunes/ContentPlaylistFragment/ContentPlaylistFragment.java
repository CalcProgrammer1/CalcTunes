package com.calcprogrammer1.calctunes.ContentPlaylistFragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.calcprogrammer1.calctunes.ContentPlaybackService;

import java.util.ArrayList;

public class ContentPlaylistFragment extends Fragment implements ListView.OnItemClickListener
{
    //Shared Preferences
    private SharedPreferences appSettings;
    
    //Playlist Views
    ListView view;
    ContentPlaylistAdapter   adapter;

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
    //private boolean playbackservice_bound = false;

    private ServiceConnection playbackserviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            playbackservice = ((ContentPlaybackService.ContentPlaybackBinder)service).getService();
            updateList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            playbackservice = null;
        }
    };

    private BroadcastReceiver infoUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            adapter.setNowPlaying(playbackservice.NowPlayingFile());
            adapter.notifyDataSetChanged();
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// FRAGMENT CREATE FUNCTIONS /////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public ContentPlaylistFragment()
    {
        playlist = new PlaylistEditor(getActivity());
    }

    public void setPlaylist(String filename)
    {
        playlist.readPlaylistFile(filename);
    }

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
        playbackservice.SetPlaybackContentSourcePlaylist(playlist, position);
    }
}
