package com.calcprogrammer1.calctunes.SourceList;

import java.io.File;
import java.util.ArrayList;

import com.calcprogrammer1.calctunes.Dialogs.PlaylistBuilderDialog;
import com.calcprogrammer1.calctunes.Dialogs.SubsonicBuilderDialog;
import com.calcprogrammer1.calctunes.Dialogs.LibraryBuilderDialog;
import com.calcprogrammer1.calctunes.ContentPlaybackService.ContentPlaybackService;
import com.calcprogrammer1.calctunes.Interfaces.SourceListInterface;
import com.calcprogrammer1.calctunes.Library.LibraryScannerTask;
import com.calcprogrammer1.calctunes.SourceTypes.LibrarySource;
import com.calcprogrammer1.calctunes.SourceTypes.PlaylistSource;
import com.calcprogrammer1.calctunes.SourceTypes.SubsonicSource;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;

public class SourceListFragment extends Fragment
{
    //Source Group Constants
    public static final int SOURCE_GROUP_LIBRARY  = 0;
    public static final int SOURCE_GROUP_PLAYLIST = 1;
    public static final int SOURCE_GROUP_SYSTEM   = 2;
    public static final int SOURCE_GROUP_SUBSONIC = 3;
    public static final int TOTAL_SOURCE_GROUP    = 4;
    //Context Menu constants
    public static final int CONTEXT_MENU_NEW_LIBRARY     = 0;
    public static final int CONTEXT_MENU_EDIT_LIBRARY    = 1;
    public static final int CONTEXT_MENU_DELETE_LIBRARY  = 2;
    public static final int CONTEXT_MENU_RESCAN_LIBRARY  = 3;
    public static final int CONTEXT_MENU_NEW_PLAYLIST    = 4;
    public static final int CONTEXT_MENU_RENAME_PLAYLIST = 5;
    public static final int CONTEXT_MENU_DELETE_PLAYLIST = 6;
    public static final int CONTEXT_MENU_NEW_SUBSONIC    = 7;
    public static final int CONTEXT_MENU_EDIT_SUBSONIC   = 8;
    public static final int CONTEXT_MENU_DELETE_SUBSONIC = 9;
    
    //Sources List View
    ExpandableListView  view;
    SourceListAdapter   adapter;
    
    //Sources List Data
    ArrayList<LibrarySource> libraryList = new ArrayList<LibrarySource>();
    ArrayList<PlaylistSource> playlistList = new ArrayList<PlaylistSource>();
    ArrayList<SubsonicSource> subsonicList = new ArrayList<SubsonicSource>();
    
    //Sources List Callback
    SourceListInterface callback;
    
    private SharedPreferences appSettings;

    private int selectedGroup;
    private int selectedChild;
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
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

        //Register refresh event receiver
        getActivity().registerReceiver(refreshReceiver, new IntentFilter("com.calcprogrammer1.calctunes.SOURCE_REFRESH_EVENT"));

        //Read the Source Lists
        readSourceLists();
        
        adapter = new SourceListAdapter(getActivity());
        adapter.attachLibraryList(libraryList);
        adapter.attachPlaylistList(playlistList);
        adapter.attachSubsonicList(subsonicList);
    }

    public void onDestroy()
    {
        super.onDestroy();
        getActivity().unregisterReceiver(refreshReceiver);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        view = new ExpandableListView(getActivity());
        view.setGroupIndicator(null);
        view.setDividerHeight(0);
        updateListView();
        registerForContextMenu(view);
        return view;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// CONTEXT MENU //////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        if(v == view)
        {
            ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            int type = ExpandableListView.getPackedPositionType(info.packedPosition);
            int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            //int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
            
            if(type == 0) //Group
            {
                if(group == 0) //Libraries
                {
                    menu.add(1, CONTEXT_MENU_NEW_LIBRARY, Menu.NONE, "New Library");
                }
                else if(group == 1) //Playlists
                {
                    menu.add(1, CONTEXT_MENU_NEW_PLAYLIST, Menu.NONE, "New Playlist");
                }
                else if(group == 3) //Subsonic Servers
                {
                    menu.add(1, CONTEXT_MENU_NEW_SUBSONIC, Menu.NONE, "New Subsonic Server");
                }
            }
            else if(type == 1) //Child
            {
                if(group == 0) //Library
                {
                    menu.add(1, CONTEXT_MENU_EDIT_LIBRARY,    Menu.NONE, "Edit Library");
                    menu.add(1, CONTEXT_MENU_DELETE_LIBRARY,  Menu.NONE, "Delete Library");
                    menu.add(1, CONTEXT_MENU_RESCAN_LIBRARY,  Menu.NONE, "Rescan Library");
                }
                else if(group == 1) //Playlist
                {
                    menu.add(1, CONTEXT_MENU_RENAME_PLAYLIST, Menu.NONE, "Rename Playlist");
                    menu.add(1, CONTEXT_MENU_DELETE_PLAYLIST, Menu.NONE, "Delete Playlist");
                }
                else if(group == 3) //Subsonic Server
                {
                    menu.add(1, CONTEXT_MENU_EDIT_SUBSONIC,   Menu.NONE, "Edit Subsonic Server");
                    menu.add(1, CONTEXT_MENU_DELETE_SUBSONIC, Menu.NONE, "Delete Subsonic Server");
                }
            }
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getGroupId() == 1)
        {
            ExpandableListContextMenuInfo info= (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
            final int id = (int) info.id;
            
            switch(item.getItemId())
            {
                case CONTEXT_MENU_NEW_LIBRARY:
                    {
                        LibraryBuilderDialog dialog = new LibraryBuilderDialog(getActivity());
                        dialog.show();
                        readSourceLists();
                        updateListView();
                    }
                    break;
                    
                case CONTEXT_MENU_EDIT_LIBRARY:
                    {
                        LibraryBuilderDialog dialog = new LibraryBuilderDialog(getActivity());
                        dialog.EditExistingLibrary(libraryList.get(id).filename);
                        dialog.show();
                        readSourceLists();
                        updateListView();
                    }
                    break;
                
                case CONTEXT_MENU_DELETE_LIBRARY:
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                switch (which)
                                {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        File libraryToDelete = new File(libraryList.get(id).filename);
                                        libraryToDelete.delete();
                                        readSourceLists();
                                        updateListView();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Are you sure you want to delete this library?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
                    break;
                
                case CONTEXT_MENU_RESCAN_LIBRARY:
                    LibraryScannerTask task = new LibraryScannerTask(getActivity());
                    task.execute(libraryList.get(id).name);
                    break;

                case CONTEXT_MENU_NEW_PLAYLIST:
                    {
                        PlaylistBuilderDialog dialog = new PlaylistBuilderDialog(getActivity());
                        dialog.show();
                        readSourceLists();
                        updateListView();
                    }
                    break;

                case CONTEXT_MENU_DELETE_PLAYLIST:
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                switch (which)
                                {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        File playlistToDelete = new File(playlistList.get(id).filename);
                                        playlistToDelete.delete();
                                        readSourceLists();
                                        updateListView();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Are you sure you want to delete this playlist?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
                    break;

                case CONTEXT_MENU_NEW_SUBSONIC:
                    {
                        SubsonicBuilderDialog dialog = new SubsonicBuilderDialog(getActivity());
                        dialog.show();
                        readSourceLists();
                        updateListView();
                    }
                    break;
                    
                case CONTEXT_MENU_EDIT_SUBSONIC:
                    {
                        SubsonicBuilderDialog dialog = new SubsonicBuilderDialog(getActivity());
                        dialog.EditExistingSubsonic(subsonicList.get(id).filename);
                        dialog.show();
                        readSourceLists();
                        updateListView();
                    }
                    break;
              
                case CONTEXT_MENU_DELETE_SUBSONIC:
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                switch (which)
                                {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        File subsonicToDelete = new File(subsonicList.get(id).filename);
                                        subsonicToDelete.delete();
                                        readSourceLists();
                                        updateListView();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Are you sure you want to delete this Subsonic source?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
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
    /////////////////////////////////// LIST DATA FUNCTIONS ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public void setCallback(SourceListInterface callb)
    {
        callback = callb;
    }
    
    //Read Source Lists
    //
    // This function populates the source lists by reading library, playlist, and subsonic server
    // sources from saved source files stored in the CalcTunes storage directory.
    // It then updates the view to display the newly-loaded information.
    public void readSourceLists()
    {
        libraryList  = SourceListOperations.readLibraryList(SourceListOperations.getLibraryPath(getActivity()));
        playlistList = SourceListOperations.readPlaylistList(SourceListOperations.getPlaylistPath(getActivity()));
        subsonicList = SourceListOperations.readSubsonicList(SourceListOperations.getSubsonicPath(getActivity()));
    }
    
    //Update List View
    // 
    // Updates the Fragment's ExpandableListView with the currently-loaded source information
    public void updateListView()
    {
        adapter.attachLibraryList(libraryList);
        adapter.attachPlaylistList(playlistList);
        adapter.attachSubsonicList(subsonicList);
        view.setAdapter(adapter);
        view.setOnChildClickListener(new OnChildClickListener() 
        {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                selectedGroup = groupPosition;
                selectedChild = childPosition;
                
                adapter.setSelected(selectedGroup, selectedChild);
                
                switch(selectedGroup)
                {
                    case SOURCE_GROUP_LIBRARY:
                        callback.callback(ContentPlaybackService.CONTENT_TYPE_LIBRARY, libraryList.get(selectedChild).filename);
                        break;
                        
                    case SOURCE_GROUP_PLAYLIST:
                        callback.callback(ContentPlaybackService.CONTENT_TYPE_PLAYLIST, playlistList.get(selectedChild).filename);
                        break;
                        
                    case SOURCE_GROUP_SYSTEM:
                        callback.callback(ContentPlaybackService.CONTENT_TYPE_FILESYSTEM, null);
                        break;
                        
                    case SOURCE_GROUP_SUBSONIC:
                        callback.callback(ContentPlaybackService.CONTENT_TYPE_SUBSONIC, subsonicList.get(selectedChild).filename);
                        break;
                }
                return true;
            }       
        });
        view.expandGroup(0);
        view.expandGroup(1);
        view.expandGroup(2);
        view.expandGroup(3);
    }

    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            readSourceLists();
            updateListView();
        }
    };
}
