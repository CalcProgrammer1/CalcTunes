package com.calcprogrammer1.calctunes.SourceList;

import java.io.File;
import java.util.ArrayList;

import com.calcprogrammer1.calctunes.ContentPlaybackService;
import com.calcprogrammer1.calctunes.Activities.CalcTunesLibraryBuilderActivity;
import com.calcprogrammer1.calctunes.Activities.CalcTunesSubsonicBuilderActivity;
import com.calcprogrammer1.calctunes.Interfaces.SourceListInterface;
import com.calcprogrammer1.calctunes.Library.LibraryScannerTask;
import com.calcprogrammer1.calctunes.SourceTypes.LibrarySource;
import com.calcprogrammer1.calctunes.SourceTypes.SubsonicSource;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

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
    ArrayList<SubsonicSource> subsonicList = new ArrayList<SubsonicSource>();
    
    //Sources List Callback
    SourceListInterface callback;
    
    private SharedPreferences appSettings;
    
    private int interfaceColor;
    private int selectedGroup;
    private int selectedChild;
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
            interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
            setInterfaceColor(interfaceColor);
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
        
        //Read the Source Lists
        readSourceLists();
        
        adapter = new SourceListAdapter(getActivity());
        adapter.attachLibraryList(libraryList);
        adapter.attachSubsonicList(subsonicList);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        view = new ExpandableListView(getActivity());
        view.setGroupIndicator(null);
        view.setDividerHeight(0);
        updateListView();
        registerForContextMenu(view);
        setInterfaceColor(interfaceColor);
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
            int id = (int) info.id;
            
            switch(item.getItemId())
            {
                case CONTEXT_MENU_NEW_LIBRARY:
                    startActivityForResult(new Intent(getActivity().getBaseContext(), CalcTunesLibraryBuilderActivity.class), 1);
                    break;
                    
                case CONTEXT_MENU_EDIT_LIBRARY:
                    Intent libIntent = new Intent(getActivity().getBaseContext(), CalcTunesLibraryBuilderActivity.class);
                    libIntent.putExtra("EditFilename", libraryList.get(id).filename);
                    startActivityForResult(libIntent, 1);
                    break;
                
                case CONTEXT_MENU_DELETE_LIBRARY:
                    File libraryToDelete = new File(libraryList.get(id).filename);
                    libraryToDelete.delete();
                    readSourceLists();
                    updateListView();
                    Toast.makeText(getActivity().getBaseContext(), "Library Deleted", Toast.LENGTH_SHORT).show();
                    break;
                
                case CONTEXT_MENU_RESCAN_LIBRARY:
                    LibraryScannerTask task = new LibraryScannerTask(getActivity().getBaseContext());
                    task.execute(libraryList.get(id).name);
                    break;
                    
                case CONTEXT_MENU_NEW_SUBSONIC:
                    startActivityForResult(new Intent(getActivity().getBaseContext(), CalcTunesSubsonicBuilderActivity.class), 1);
                    break;
                    
                case CONTEXT_MENU_EDIT_SUBSONIC:
                    Intent subIntent = new Intent(getActivity().getBaseContext(), CalcTunesSubsonicBuilderActivity.class);
                    subIntent.putExtra("EditFilename", subsonicList.get(id).filename);
                    startActivityForResult(subIntent, 1);
                    break;
              
                case CONTEXT_MENU_DELETE_SUBSONIC:
                    File subsonicToDelete = new File(subsonicList.get(id).filename);
                    subsonicToDelete.delete();
                    readSourceLists();
                    updateListView();
                    Toast.makeText(getActivity().getBaseContext(), "Subsonic Server Deleted", Toast.LENGTH_SHORT).show();
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
        subsonicList = SourceListOperations.readSubsonicList(SourceListOperations.getSubsonicPath(getActivity()));
    }
    
    //Update List View
    // 
    // Updates the Fragment's ExpandableListView with the currently-loaded source information
    public void updateListView()
    {
        adapter.attachLibraryList(libraryList);
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
    
    public void setInterfaceColor(int color)
    {
        interfaceColor = color;
        adapter.setNowPlayingColor(interfaceColor);
    }
}
