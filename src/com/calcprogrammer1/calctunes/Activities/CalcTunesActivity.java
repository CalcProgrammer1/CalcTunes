/*---------------------------------------------------------------------------------------------*\
| CalcTunes - A desktop-like media player for Android!                                          |
|                                                                                               |
| CalcTunes features a two-panel GUI that supports multiple media sources (libraries) as well   |
| as playlists.  Libraries are defined by groups of folders which are scanned for media and     |
| organized by artist, album, and track tags.  Playlists are lists of individual media files.   |
|                                                                                               |
| Created by Adam Honse (CalcProgrammer1), calcprogrammer1@gmail.com, 12/16/2011                |
\*---------------------------------------------------------------------------------------------*/
package com.calcprogrammer1.calctunes.Activities;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.calcprogrammer1.calctunes.ContentPlaybackService.ContentPlaybackService;
import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.ContentFilesystemFragment.ContentFilesystemFragment;
import com.calcprogrammer1.calctunes.ContentLibraryFragment.ContentLibraryFragment;
import com.calcprogrammer1.calctunes.ContentPlaylistFragment.ContentPlaylistFragment;
import com.calcprogrammer1.calctunes.ContentSubsonicFragment.ContentSubsonicFragment;
import com.calcprogrammer1.calctunes.Interfaces.ContentFragmentInterface;
import com.calcprogrammer1.calctunes.Interfaces.NowPlayingFragmentInterface;
import com.calcprogrammer1.calctunes.Interfaces.SourceListInterface;
import com.calcprogrammer1.calctunes.MediaInfo.MediaInfoFragment;
import com.calcprogrammer1.calctunes.NowPlaying.NowPlayingFragment;
import com.calcprogrammer1.calctunes.SourceList.SourceListFragment;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.github.ysamlan.horizontalpager.HorizontalPager;

public class CalcTunesActivity extends ActionBarActivity
{
    static public int CONTEXT_MENU_SOURCE_LIST              = 0;
    static public int CONTEXT_MENU_CONTENT_LIBRARY          = 1;
    static public int CONTEXT_MENU_CONTENT_PLAYLIST         = 2;
    static public int CONTEXT_MENU_CONTENT_FILESYSTEM       = 3;
    static public int CONTEXT_MENU_CONTENT_SUBSONIC         = 4;
    static public int CONTEXT_MENU_MEDIA_INFO               = 5;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Class Variables////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Horizontal Pager that holds fragments
    private HorizontalPager horizontalpager;
    
    //Shared Preferences
    private SharedPreferences appSettings;
	
    //Fragments
    private SourceListFragment         sourcelistfragment;	
    private NowPlayingFragment         nowplayingfragment;
    private ContentFilesystemFragment  filesystemfragment;
    private ContentLibraryFragment     libraryfragment;
    private ContentPlaylistFragment    playlistfragment;
    private ContentSubsonicFragment    subsonicfragment;
    private MediaInfoFragment          mediainfofragment;

    //Menu Items
    MenuItem                            playbackModeItem;

    //Currently open content fragment
    private int currentContentSource = ContentPlaybackService.CONTENT_TYPE_NONE;
	
    //Filename if opened by Intent
    String openFile = null;

    //Shutting down the activity?
    private static boolean shuttingDown = false;
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Service Connection/////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private ContentPlaybackService playbackservice;
    private boolean playbackservice_bound = false;
    private ServiceConnection playbackserviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            playbackservice = ((ContentPlaybackService.ContentPlaybackBinder)service).getService();
            playbackservice_bound = true;
            createGuiElements();
            if(openFile != null)
            {
                File file = new File(openFile);
                setContentSource(file.getParent(), ContentPlaybackService.CONTENT_TYPE_FILESYSTEM);
                playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_FILESYSTEM, openFile, 0);
                playbackservice.StartPlayback();
                horizontalpager.setCurrentScreen(1, false);
            }
            if(playbackservice.GetPlaybackContentType() != ContentPlaybackService.CONTENT_TYPE_NONE)
            {
                Log.d("CalcTunesActivity", "Setting content source from playback service");
                Log.d("CalcTunesActivity", "Content String: " + playbackservice.GetPlaybackContentString() + " Content Type: " + playbackservice.GetPlaybackContentType());
                setContentSource(playbackservice.GetPlaybackContentString(), playbackservice.GetPlaybackContentType());
                horizontalpager.setCurrentScreen(1, false);
                notifyMediaInfoUpdated();
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
    
    SourceListInterface sourcelisthandlerCallback = new SourceListInterface(){
        public void callback(int contentType, String filename)
        {
            if(contentType == ContentPlaybackService.CONTENT_TYPE_FILESYSTEM)
            {
                setContentSource("/", contentType);
            }
            else if(contentType == ContentPlaybackService.CONTENT_TYPE_LIBRARY)
            {
                setContentSource(SourceListOperations.readLibraryFile(filename).name, ContentPlaybackService.CONTENT_TYPE_LIBRARY);
            }
            else if(contentType == ContentPlaybackService.CONTENT_TYPE_PLAYLIST)
            {
                setContentSource(filename, ContentPlaybackService.CONTENT_TYPE_PLAYLIST);
            }
            else if(contentType == ContentPlaybackService.CONTENT_TYPE_SUBSONIC)
            {
                setContentSource(filename, ContentPlaybackService.CONTENT_TYPE_SUBSONIC);
            }
            horizontalpager.setCurrentScreen(1, true);
        }
    };
    
    NowPlayingFragmentInterface nowPlayingFragmentCallback = new NowPlayingFragmentInterface(){
        public void onInfoButtonPressed()
        {
            if(playbackservice.GetPlaybackContentType() == ContentPlaybackService.CONTENT_TYPE_SUBSONIC)
            {
                mediainfofragment.setTrackInfoFromSubsonic(playbackservice.GetPlaybackSubsonicConnection(), playbackservice.GetNowPlayingSubsonicId());
            }
            else
            {
                mediainfofragment.setTrackInfoFromFile(playbackservice.GetNowPlayingString());
            }
            horizontalpager.setCurrentScreen(2, true);
        }
    };
    
    ContentFragmentInterface contentLibraryFragmentCallback = new ContentFragmentInterface(){

        @Override
        public void OnTrackInfoRequest(String file)
        {
            mediainfofragment.setTrackInfoFromFile(file);
            horizontalpager.setCurrentScreen(2, true);
        }
    };
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;

            UpdatePlaybackModeIcon();
        }
    };

    private void notifyMediaInfoUpdated()
    {
        Intent broadcast = new Intent();
        broadcast.setAction("com.calcprogrammer1.calctunes.PLAYBACK_INFO_UPDATED_EVENT");
        sendBroadcast(broadcast);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Class Overrides////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        //Get the application preferences
        appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);

        if(appSettings.getBoolean("light_theme", false))
        {
            setTheme(R.style.Theme_CalcTunes_Light);
        }

        super.onCreate(savedInstanceState);

        //Set the content view
        setContentView(R.layout.main);

        //Initialize Action Bar
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
        //Register app close receiver
        registerReceiver(remoteReceiver, new IntentFilter("com.calcprogrammer1.calctunes.CLOSE_APP_EVENT"));
        
        Intent serviceIntent = new Intent(this, com.calcprogrammer1.calctunes.MediaPlayer.RemoteControlService.class);
        startService(serviceIntent);
        
        shuttingDown = false;
        
        //Create or restore fragments
        if(savedInstanceState == null)
        {
            //Create Source List Fragment
            sourcelistfragment = new SourceListFragment();
            sourcelistfragment.setCallback(sourcelisthandlerCallback);
            getSupportFragmentManager().beginTransaction().add(R.id.sourceListFragmentContainer, sourcelistfragment).commit();
            
            //Create Now Playing Fragment
            nowplayingfragment = new NowPlayingFragment();
            nowplayingfragment.registerCallback(nowPlayingFragmentCallback);
            getSupportFragmentManager().beginTransaction().add(R.id.nowPlayingContainer, nowplayingfragment).commit();
            
            //Create Media Info Fragment
            mediainfofragment = new MediaInfoFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.mediaInfoContainer, mediainfofragment).commit();
            
            //Create Content Type Fragments
            libraryfragment = new ContentLibraryFragment();
            filesystemfragment = new ContentFilesystemFragment();
            subsonicfragment = new ContentSubsonicFragment();
        }
        else
        {
            //Restore Source List Fragment
            sourcelistfragment = (SourceListFragment) getSupportFragmentManager().findFragmentById(R.id.sourceListFragmentContainer);
            
            //Restore Now Playing Fragment
            nowplayingfragment = (NowPlayingFragment) getSupportFragmentManager().findFragmentById(R.id.nowPlayingContainer);
            
            //Restore Media Info Fragment
            mediainfofragment  = (MediaInfoFragment)  getSupportFragmentManager().findFragmentById(R.id.mediaInfoContainer);
            
            //Restore Content Fragment
            switch(currentContentSource)
            {
                case ContentPlaybackService.CONTENT_TYPE_LIBRARY:
                    libraryfragment = (ContentLibraryFragment) getSupportFragmentManager().findFragmentById(R.id.contentListFragmentContainer);
                    libraryfragment.setCallback(contentLibraryFragmentCallback);
                    break;
                    
                case ContentPlaybackService.CONTENT_TYPE_FILESYSTEM:
                    filesystemfragment = (ContentFilesystemFragment) getSupportFragmentManager().findFragmentById(R.id.contentListFragmentContainer);
                    break;
                
                case ContentPlaybackService.CONTENT_TYPE_PLAYLIST:
                    playlistfragment = (ContentPlaylistFragment) getSupportFragmentManager().findFragmentById(R.id.contentListFragmentContainer);
                    break;
                    
                case ContentPlaybackService.CONTENT_TYPE_SUBSONIC:
                    subsonicfragment = (ContentSubsonicFragment) getSupportFragmentManager().findFragmentById(R.id.contentListFragmentContainer);
                    break;
            }
        }

        //Check if CalcTunes was opened from a file browser, and if so, open the file
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action != null && action.equals(Intent.ACTION_VIEW))
        {
            try{
                Uri data = intent.getData();
                openFile = data.getPath();
            }catch(Exception e){}
        }
        
        //Start or Reconnect to the CalcTunes Playback Service
      	startService(new Intent(this, ContentPlaybackService.class));
       	bindService(new Intent(this, ContentPlaybackService.class), playbackserviceConnection, Context.BIND_AUTO_CREATE);
    }
    
	@Override
	public void onDestroy()
	{
	    super.onDestroy();
	    
	    //Unregister app close receiver
	    unregisterReceiver(remoteReceiver);
	    
	    if(playbackservice_bound)
	    {
	        unbindService(playbackserviceConnection);
	    }
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if(playbackservice_bound)
        {
            updateGuiElements();
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.mainoptionsmenu, menu);
        playbackModeItem = menu.findItem(R.id.playMode);
        UpdatePlaybackModeIcon();
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                horizontalpager.setCurrentScreen(0, true);
                break;

            case R.id.playModeInOrder:
                {
                    SharedPreferences.Editor editor = appSettings.edit();
                    editor.putInt("playback_mode", ContentPlaybackService.CONTENT_PLAYBACK_MODE_IN_ORDER);
                    editor.commit();
                }
                break;

            case R.id.playModeRandom:
                {
                    SharedPreferences.Editor editor = appSettings.edit();
                    editor.putInt("playback_mode", ContentPlaybackService.CONTENT_PLAYBACK_MODE_RANDOM);
                    editor.commit();
                }
                break;

            case R.id.exitApplication:
                ButtonExitClick();
                break;
            
            case R.id.minimizeApplication:
                ButtonMinimizeClick();
                break;
                
            case R.id.openSettings:
                ButtonSettingsClick();
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
                sourcelistfragment.readSourceLists();
                sourcelistfragment.updateListView();
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Other Activity Functions///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void createGuiElements()
    {
        updateGuiElements();
    }
    
    public void updateGuiElements()
    {
        horizontalpager = (HorizontalPager) findViewById(R.id.horizontal_pager);
    }
   
    //Set Content Source
    public void setContentSource(String contentName, int contentType)
    {
        if(contentType == ContentPlaybackService.CONTENT_TYPE_LIBRARY)
        {
            currentContentSource = ContentPlaybackService.CONTENT_TYPE_LIBRARY;
            libraryfragment = new ContentLibraryFragment();
            libraryfragment.setLibrary(contentName);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentListFragmentContainer, libraryfragment).commit();
            libraryfragment.setCallback(contentLibraryFragmentCallback);
        }
        else if(contentType == ContentPlaybackService.CONTENT_TYPE_FILESYSTEM)
        {
            currentContentSource = ContentPlaybackService.CONTENT_TYPE_FILESYSTEM;
            filesystemfragment = new ContentFilesystemFragment();
            filesystemfragment.setDirectory(contentName);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentListFragmentContainer, filesystemfragment).commit();
        }
        else if(contentType == ContentPlaybackService.CONTENT_TYPE_PLAYLIST)
        {
            currentContentSource = ContentPlaybackService.CONTENT_TYPE_PLAYLIST;
            playlistfragment = new ContentPlaylistFragment();
            playlistfragment.setPlaylist(contentName);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentListFragmentContainer, playlistfragment).commit();
        }
        else if(contentType == ContentPlaybackService.CONTENT_TYPE_SUBSONIC)
        {
            currentContentSource = ContentPlaybackService.CONTENT_TYPE_SUBSONIC;
            subsonicfragment = new ContentSubsonicFragment();
            subsonicfragment.setSubsonicSource(contentName);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentListFragmentContainer, subsonicfragment).commit();
            subsonicfragment.setCallback(contentLibraryFragmentCallback);
        }
    }
    
    public void Exit()
    {
        if(!shuttingDown)
        {
            shuttingDown = true;
            playbackservice.StopPlayback();
            unbindService(playbackserviceConnection);
            playbackservice_bound = false;
            stopService(new Intent(this, ContentPlaybackService.class));
            finish();
        }
    }
    
    public void Minimize()
    {
        finish();
    }

    public void UpdatePlaybackModeIcon()
    {
        if(playbackModeItem != null)
        {
            switch(appSettings.getInt("playback_mode", ContentPlaybackService.CONTENT_PLAYBACK_MODE_IN_ORDER))
            {
                case ContentPlaybackService.CONTENT_PLAYBACK_MODE_IN_ORDER:
                    playbackModeItem.setIcon(R.drawable.cached_to_sdcard_icon);
                    break;

                case ContentPlaybackService.CONTENT_PLAYBACK_MODE_RANDOM:
                    playbackModeItem.setIcon(R.drawable.downloading_to_sdcard_icon);
                    break;
            }
        }
    }

    public void ButtonSettingsClick()
    {
        startActivity(new Intent(this, CalcTunesSettingsActivity.class));
    }
    
    public void ButtonMinimizeClick()
    {
        Minimize();
    }
    
    public void ButtonExitClick()
    {
        Exit();
    }
    
    /*---------------------------------------------------------------------*\
    |                                                                       |
    |   Remote Control Broadcast Receiver                                   |
    |                                                                       |
    |   Receives intent com.calcprogrammer1.calctunes.REMOTE_BUTTON_EVENT   |
    |                                                                       |
    |   This intent contains a KeyEvent.KEYCODE_ value indicating which     |
    |   media button key was pressed.  It is sent from the Media Buttons    |
    |   event receiver for handling headset/Bluetooth key events.           |
    |                                                                       | 
    \*---------------------------------------------------------------------*/
    
    private BroadcastReceiver remoteReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent)
        {
            
            //Log.d("CalcTunesActivity", "Exit Intent Received");
            //Exit();
        }
    };
}
