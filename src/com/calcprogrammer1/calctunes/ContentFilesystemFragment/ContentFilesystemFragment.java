package com.calcprogrammer1.calctunes.ContentFilesystemFragment;

import com.calcprogrammer1.calctunes.ContentLibraryFragment.ContentListElement;
import com.calcprogrammer1.calctunes.ContentPlaybackService;
import com.calcprogrammer1.calctunes.ContentPlaylistFragment.PlaylistEditor;
import com.calcprogrammer1.calctunes.ContentPlaylistFragment.PlaylistElement;
import com.calcprogrammer1.calctunes.Dialogs.AddToPlaylistDialog;
import com.calcprogrammer1.calctunes.Dialogs.FolderReorganizeDialog;
import com.calcprogrammer1.calctunes.Interfaces.ContentFilesystemAdapterInterface;
import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.ArrayList;

public class ContentFilesystemFragment extends Fragment implements View.OnClickListener
{
    //View to display on
    private View rootView;

    //ListView for main files
    private ListView mainView;

    //Selected items text
    private TextView selectedItemsView;

    //Add to playlist button
    private Button addToPlaylistButton;

    // Filesystem content adapter for listview
    private ContentFilesystemAdapter fileAdapter;
    
    // Shared Preferences
    private SharedPreferences appSettings;
    
    // Current directory
    private String currentDirectory = "/";

    // Selected Files
    private ArrayList<String> selectedFiles = new ArrayList<String>();

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
            //playbackservice_bound = true;
            updateList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            playbackservice = null;
            //playbackservice_bound = false;
        }    
    };

    private BroadcastReceiver infoUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
            fileAdapter.notifyDataSetChanged(); 
        }  
    };

    ContentFilesystemAdapterInterface adapterCallback = new ContentFilesystemAdapterInterface(){
        @Override
        public void onCheckboxClicked(int position, String filename, boolean checked)
        {
            if(checked)
            {
                if(!selectedFiles.contains(filename))
                {
                    selectedFiles.add(filename);
                }
            }
            else
            {
                selectedFiles.remove(filename);
            }
            selectedItemsView.setText(selectedFiles.size() + " files selected");
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

        //Create starting file adapter
        fileAdapter = new ContentFilesystemAdapter(getActivity(), currentDirectory);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        rootView = inflater.inflate(R.layout.filesystemview, group, false);
        mainView = (ListView) rootView.findViewById(R.id.listViewFilesystemMainList);
        registerForContextMenu(mainView);
        selectedItemsView = (TextView) rootView.findViewById(R.id.textViewFilesystemItemsSelected);
        addToPlaylistButton = (Button) rootView.findViewById(R.id.buttonFilesystemAddToPlaylist);
        addToPlaylistButton.setOnClickListener(this);
        return rootView;
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.buttonFilesystemAddToPlaylist:
                AddToPlaylistDialog dialog = new AddToPlaylistDialog(getActivity());
                dialog.addFileList(selectedFiles);
                dialog.show();
                break;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// CONTEXT MENU //////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        if(v == mainView)
        {
            int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
            boolean isDir = fileAdapter.files.get(position).isDirectory();

            if(isDir)
            {
                menu.add(3, 0 , Menu.NONE, "Organize this directory");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getGroupId() == 3)
        {
            int position = (int) ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
            FolderReorganizeDialog dialog = new FolderReorganizeDialog(getActivity(), fileAdapter.files.get(position).getAbsolutePath());
            dialog.show();
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
        fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
        fileAdapter.setCallback(adapterCallback);
        fileAdapter.setCheckList(selectedFiles);
        mainView.setAdapter(fileAdapter);
        mainView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                if (position == 0 && !fileAdapter.currentDirectory.getPath().equals("/"))
                {
                    currentDirectory = fileAdapter.currentDirectory.getParent();
                    fileAdapter = new ContentFilesystemAdapter(getActivity(), currentDirectory);
                    fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                    updateList();
                } else if (fileAdapter.files.get(position).isDirectory())
                {
                    currentDirectory = fileAdapter.files.get(position).getPath();
                    fileAdapter = new ContentFilesystemAdapter(getActivity(), currentDirectory);
                    fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                    updateList();
                } else
                {
                    playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_FILESYSTEM, fileAdapter.files.get(position).getPath(), 0);
                    fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                    fileAdapter.notifyDataSetChanged();
                }
            }
        });
        registerForContextMenu(rootView);
    }

    public void setDirectory(String newDirectory)
    {
        currentDirectory = newDirectory;
        fileAdapter = new ContentFilesystemAdapter(getActivity(), currentDirectory);
    }
}
