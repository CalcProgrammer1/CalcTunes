/*---------------------------------------------------------------------------------------------*\
| CalcTunes - A desktop-like media player for Android!                                          |
|                                                                                               |
| CalcTunes features a two-panel GUI that supports multiple media sources (libraries) as well   |
| as playlists.  Libraries are defined by groups of folders which are scanned for media and     |
| organized by artist, album, and track tags.  Playlists are lists of individual media files.   |
|                                                                                               |
| Created by Adam Honse (CalcProgrammer1), calcprogrammer1@gmail.com, 12/16/2011                |
\*---------------------------------------------------------------------------------------------*/
package com.calcprogrammer1.calctunes;

import android.app.Activity;
import android.util.Log;
import android.view.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.*;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.LibraryOperations;

import java.io.File;

public class CalcTunesActivity extends Activity
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Static Menu Operations/////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    final int CONTEXT_MENU_NEW_LIBRARY     = 0;
    final int CONTEXT_MENU_EDIT_LIBRARY    = 1;
    final int CONTEXT_MENU_DELETE_LIBRARY  = 2;
    final int CONTEXT_MENU_RESCAN_LIBRARY  = 3;
    final int CONTEXT_MENU_NEW_PLAYLIST    = 4;
    final int CONTEXT_MENU_RENAME_PLAYLIST = 5;
    final int CONTEXT_MENU_DELETE_PLAYLIST = 6;
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Class Variables////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	TextView artisttext;
	TextView albumtext;
	TextView tracktext;
	TextView trackyear;
	ImageView albumartview;
	
	ContentViewHandler viewhandler;
	
    SharedPreferences appSettings;
    
	SeekBar trackseek;
	SeekHandler trackseekhandler;
	
	View sourcelistframe;
	ExpandableListView sourcelist;
	SourceListHandler sourcelisthandler;
	
	ListView mainlist;

    MediaButtonsHandler buttons;
    
    String openFile = null;
    int interfaceColor;
    boolean sidebarHidden = false;
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Service Connection/////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private ContentPlaybackService playbackservice;
    private Boolean playbackservice_bound = false;
    private ServiceConnection playbackserviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            playbackservice = ((ContentPlaybackService.ContentPlaybackBinder)service).getService();
            playbackservice_bound = true;
            createGuiElements();
            sourcelisthandler.refreshLibraryList();
            playbackservice.setCallback(playbackCallback);
            if(openFile != null)
            {
                playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_FILESYSTEM, openFile, 0, null);
                playbackservice.StartPlayback();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            playbackservice = null;
            playbackservice_bound = false;
        }    
    };
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Callbacks//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    MediaButtonsHandlerCallback buttonsCallback = new MediaButtonsHandlerCallback(){
        public void onMediaNextPressed()
        {
            ButtonNextClick(null);
        }

        public void onMediaPrevPressed()
        {
            ButtonPrevClick(null);
        }

        public void onMediaPlayPausePressed()
        {
            ButtonPlayPauseClick(null);
        }

        public void onMediaStopPressed()
        {
            ButtonStopClick(null);
        }  
    };
    
    ContentPlaybackCallback playbackCallback = new ContentPlaybackCallback(){
        @Override
        public void onTrackEnd()
        {
            //Do nothing on track end
        }

        @Override
        public void onMediaInfoUpdated()
        {
            //On media info updated, update all the text fields and album art display
            artisttext.setText(playbackservice.NowPlayingArtist());
            albumtext.setText(playbackservice.NowPlayingAlbum());
            tracktext.setText(playbackservice.NowPlayingTitle());
            trackyear.setText(playbackservice.NowPlayingYear());
            albumartview.setImageBitmap(AlbumArtManager.getAlbumArtFromCache(playbackservice.NowPlayingArtist(), playbackservice.NowPlayingAlbum(), CalcTunesActivity.this));
            
            viewhandler.setAdaptersNowPlaying(playbackservice.NowPlayingFile());
        }
        
    };
    
    SourceListCallback sourcelisthandlerCallback = new SourceListCallback(){
        public void callback(int contentType, String filename)
        {
            if(contentType == ContentViewHandler.CONTENT_TYPE_FILESYSTEM)
            {
                viewhandler.setContentSource("/mnt/sdcard", contentType);
                viewhandler.drawList();
            }
            else if(contentType == ContentViewHandler.CONTENT_TYPE_LIBRARY)
            {
                viewhandler.setContentSource(LibraryOperations.readLibraryName(filename), ContentViewHandler.CONTENT_TYPE_LIBRARY);
                viewhandler.drawList();
            }
        }
    };
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
            interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
            updateInterfaceColor(interfaceColor);
        }
    };
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Class Overrides////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //Set content view to main layout
        setContentView(R.layout.main);
        
        //Check if CalcTunes was opened from a file browser, and if so, open the file
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_VIEW))
        {
            try{
                
                Uri data = intent.getData();
                openFile = data.getPath();
                Log.d("asdf", "file: " + openFile);
                
            }catch(Exception e){}
        }
        
        //Start or Reconnect to the CalcTunes Playback Service
      	startService(new Intent(this, ContentPlaybackService.class));
       	bindService(new Intent(this, ContentPlaybackService.class), playbackserviceConnection, Context.BIND_AUTO_CREATE);
        
       	//Get the application preferences
        appSettings = getSharedPreferences("CalcTunes",MODE_PRIVATE);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
    }
    
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.main);
        if(playbackservice_bound)
        {
            updateGuiElements();
            sourcelisthandler.refreshLibraryList();
            playbackservice.setCallback(playbackCallback);
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.mainoptionsmenu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.createLibrary:
                startActivityForResult(new Intent(this, LibraryBuilderActivity.class), 1);
                break;
                
            case R.id.exitApplication:
                ButtonExitClick(null);
                break;
            
            case R.id.minimizeApplication:
                ButtonMinimizeClick(null);
                break;
                
            case R.id.collapseSidebar:
                ButtonSidebarClick(null);
                break;
                
            case R.id.openSettings:
                ButtonSettingsClick(null);
                break;
        }
        return true;
    }
    
    //Process result from library builder activity
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == Activity.RESULT_OK)
        {
            if(requestCode == 1)
            {
                sourcelisthandler.refreshLibraryList();
            }
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_MEDIA_NEXT)
        {
            ButtonNextClick(null);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        {
            ButtonPrevClick(null);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        {
            ButtonPlayPauseClick(null);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_MEDIA_STOP)
        {
            ButtonStopClick(null);
        }
        return false;       
    }
    
    //Function for creating a context (right click/long tap) menu for various UI elements
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        //Context menu for source list broken down into different menu categories
        if(v == findViewById(R.id.sourceListView))
        {
            ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            int type = ExpandableListView.getPackedPositionType(info.packedPosition);
            int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
            
            //Type=0 is group, Type=1 is child
            if(type == 0)
            {
                if(group == 0) //Libraries
                {
                    menu.add(1, CONTEXT_MENU_NEW_LIBRARY, Menu.NONE, "New Library");
                }
                else if(group == 1) //Playlists
                {
                    menu.add(1, CONTEXT_MENU_NEW_PLAYLIST, Menu.NONE, "New Playlist");
                }
            }
            else if(type == 1)
            {
                if(group == 0)
                {
                    menu.add(1, CONTEXT_MENU_EDIT_LIBRARY,    Menu.NONE, "Edit Library");
                    menu.add(1, CONTEXT_MENU_DELETE_LIBRARY,  Menu.NONE, "Delete Library");
                    menu.add(1, CONTEXT_MENU_RESCAN_LIBRARY,  Menu.NONE, "Rescan Library");
                }
                else if(group == 1)
                {
                    menu.add(1, CONTEXT_MENU_RENAME_PLAYLIST, Menu.NONE, "Rename Playlist");
                    menu.add(1, CONTEXT_MENU_DELETE_PLAYLIST, Menu.NONE, "Delete Playlist");
                }
            }
        }
    }
    
    //Function to handle all context menu events
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        ExpandableListContextMenuInfo info= (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        int id = (int) info.id;

        switch(item.getItemId())
        {
            case CONTEXT_MENU_NEW_LIBRARY:
                ButtonAddClick(null);
                break;
                
            case CONTEXT_MENU_EDIT_LIBRARY:
                Intent intent = new Intent(getBaseContext(), LibraryBuilderActivity.class);
                intent.putExtra("EditFilename", sourcelisthandler.getLibraryList().get(id).filename);
                intent.putExtra("EditName", sourcelisthandler.getLibraryList().get(id).name);
                startActivityForResult(intent, 1);
                break;
            
            case CONTEXT_MENU_DELETE_LIBRARY:
                File libraryToDelete = new File(sourcelisthandler.getLibraryList().get(id).filename);
                libraryToDelete.delete();
                sourcelisthandler.refreshLibraryList();
                Toast.makeText(this, "Library Deleted", Toast.LENGTH_SHORT).show();
                break;
            
            case CONTEXT_MENU_RESCAN_LIBRARY:
                LibraryScannerTask task = new LibraryScannerTask(this);
                task.execute(sourcelisthandler.getLibraryList().get(id).name);
                break;
        }
        
        return(super.onOptionsItemSelected(item));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Other Activity Functions///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createGuiElements()
    {
        buttons = new MediaButtonsHandler(this);
        buttons.setCallback(buttonsCallback);
        
    	viewhandler = new ContentViewHandler(this, mainlist, playbackservice);

        sourcelisthandler = new SourceListHandler(this, sourcelist);
        sourcelisthandler.setCallback(sourcelisthandlerCallback);
        
        updateGuiElements();
    }
    
    public void updateGuiElements()
    {        
        artisttext = (TextView) findViewById(R.id.text_artistname);
        albumtext = (TextView) findViewById(R.id.text_albumname);
        tracktext = (TextView) findViewById(R.id.text_trackname);
        trackyear = (TextView) findViewById(R.id.text_trackyear);
        albumartview = (ImageView) findViewById(R.id.imageAlbumArt);
        
        trackseek = (SeekBar) findViewById(R.id.seekBar_track);
        trackseekhandler = new SeekHandler(trackseek, playbackservice, this);
        
        sourcelistframe = findViewById(R.id.sourceListFrame);
        sourcelist = (ExpandableListView) findViewById(R.id.sourceListView);
        sourcelisthandler.setListView(sourcelist);
        sourcelisthandler.updateList();
        
        registerForContextMenu(sourcelist);
        
        if(sidebarHidden)
        {
            sourcelistframe.setVisibility(View.GONE);
        }
        
        //If ICS, make the title border match the ICS Holo theme
        if(Integer.valueOf(android.os.Build.VERSION.SDK) > 10)
        {
            findViewById(R.id.title_border).setBackgroundResource(android.R.color.holo_blue_light);
        }
        
        mainlist = (ListView) findViewById(R.id.libraryListView);
        viewhandler.setListView(mainlist);
        updateInterfaceColor(interfaceColor);
        viewhandler.drawList();
    }
   
    public void Exit()
    {
        trackseekhandler.pause();
        playbackservice.StopPlayback();
        unbindService(playbackserviceConnection);
        stopService(new Intent(this, ContentPlaybackService.class));
        finish();
    }
    
    public void Minimize()
    {
        finish();
    }
    
    public void ButtonStopClick(View view)
    {
    	playbackservice.StopPlayback();
    }
    
    public void ButtonPlayPauseClick(View view)
    {
        if(playbackservice.isPlaying())
        {
            playbackservice.PausePlayback();
        }
        else
        {
            playbackservice.StartPlayback();
        }
    }
    
    public void ButtonNextClick(View view)
    {
        playbackservice.NextTrack();
    }
    
    public void ButtonPrevClick(View view)
    {
        playbackservice.PrevTrack();
    }
    
    public void ButtonInfoClick(View view)
    {
        Intent intent = new Intent(getBaseContext(), CalcTunesMediaInfoActivity.class);
        intent.putExtra("TrackFilename", playbackservice.NowPlayingFile());
        startActivity(intent);
    }
    
    public void updateInterfaceColor(int color)
    {
        int[] colors = {Color.BLACK, color};
        GradientDrawable back = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
        back.setShape(GradientDrawable.RECTANGLE);
        sourcelisthandler.setInterfaceColor(color);
        back = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        findViewById(R.id.lower_frame).setBackgroundDrawable(back);
        viewhandler.setHighlightColor(color);
        trackseekhandler.setInterfaceColor(color);
    }
    
    public void ButtonSidebarClick(View view)
    {
        if(sidebarHidden)
        {
            sourcelistframe.setVisibility(View.VISIBLE);
            sidebarHidden = false;
        }
        else
        {
            sourcelistframe.setVisibility(View.GONE);
            sidebarHidden = true;
        }  
    }
    
    public void ButtonSettingsClick(View view)
    {
        startActivity(new Intent(this, CalcTunesSettingsActivity.class));
    }
    
    public void ButtonMinimizeClick(View view)
    {
        Minimize();
    }
    
    public void ButtonExitClick(View view)
    {
        Exit();
    }

    public void ButtonAddClick(View view)
    {
        startActivityForResult(new Intent(this, LibraryBuilderActivity.class), 1);
    }
}