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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.calcprogrammer1.calctunes.ContentPlaybackService;
import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.ContentFilesystemFragment.ContentFilesystemFragment;
import com.calcprogrammer1.calctunes.ContentLibraryFragment.ContentLibraryFragment;
import com.calcprogrammer1.calctunes.ContentPlaylistFragment.ContentPlaylistFragment;
import com.calcprogrammer1.calctunes.ContentSubsonicFragment.ContentSubsonicFragment;
import com.calcprogrammer1.calctunes.Interfaces.ContentFragmentInterface;
import com.calcprogrammer1.calctunes.Interfaces.ContentPlaybackInterface;
import com.calcprogrammer1.calctunes.Interfaces.NowPlayingFragmentInterface;
import com.calcprogrammer1.calctunes.Interfaces.SourceListInterface;
import com.calcprogrammer1.calctunes.MediaInfo.MediaInfoFragment;
import com.calcprogrammer1.calctunes.NowPlaying.NowPlayingFragment;
import com.calcprogrammer1.calctunes.SourceList.SourceListFragment;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.github.ysamlan.horizontalpager.HorizontalPager;

public class CalcTunesActivity extends FragmentActivity
{    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Class Variables////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Horizontal Pager that holds fragments
    private HorizontalPager horizontalpager;

    //Title bar icon and text
    private TextView        title_text;
    @SuppressWarnings("unused")
    private ImageView       title_icon;
    
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

    //Currently open content fragment
    private int currentContentSource = ContentPlaybackService.CONTENT_TYPE_NONE;
	
    //Filename if opened by Intent
    String openFile = null;
    
    //Interface Color
    int interfaceColor;

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
            playbackservice.registerCallback(playbackCallback);
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
                setContentSource(playbackservice.GetPlaybackContentString(), playbackservice.GetPlaybackContentType());
                horizontalpager.setCurrentScreen(1, false);
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
    
    ContentPlaybackInterface playbackCallback = new ContentPlaybackInterface(){
        @Override
        public void onTrackEnd()
        {
            //Do nothing on track end
        }

        @Override
        public void onMediaInfoUpdated()
        {
        }
        
    };
    
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
            mediainfofragment.setTrackInfoFromFile(playbackservice.NowPlayingFile());
            horizontalpager.setCurrentScreen(3, true);
        }
    };
    
    ContentFragmentInterface contentLibraryFragmentCallback = new ContentFragmentInterface(){

        @Override
        public void OnTrackInfoRequest(String file)
        {
            mediainfofragment.setTrackInfoFromFile(file);
            horizontalpager.setCurrentScreen(3, true);
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
        
        //Get the application preferences
        appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);

        //Set the content view
        setContentView(R.layout.main);

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
            playbackservice.registerCallback(playbackCallback);
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
                startActivityForResult(new Intent(this, CalcTunesLibraryBuilderActivity.class), 1);
                break;
                
            case R.id.exitApplication:
                ButtonExitClick(null);
                break;
            
            case R.id.minimizeApplication:
                ButtonMinimizeClick(null);
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
        title_text = (TextView) findViewById(R.id.title_text);
        title_icon = (ImageView) findViewById(R.id.title_icon);
        horizontalpager = (HorizontalPager) findViewById(R.id.horizontal_pager);                       
        updateInterfaceColor(interfaceColor);
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
            title_text.setText(contentName);
        }
        else if(contentType == ContentPlaybackService.CONTENT_TYPE_FILESYSTEM)
        {
            currentContentSource = ContentPlaybackService.CONTENT_TYPE_FILESYSTEM;
            filesystemfragment = new ContentFilesystemFragment();
            filesystemfragment.setDirectory(contentName);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentListFragmentContainer, filesystemfragment).commit();
            title_text.setText(contentName);
        }
        else if(contentType == ContentPlaybackService.CONTENT_TYPE_PLAYLIST)
        {
            currentContentSource = ContentPlaybackService.CONTENT_TYPE_PLAYLIST;
            playlistfragment = new ContentPlaylistFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentListFragmentContainer, playlistfragment).commit();
            title_text.setText("Playlist View");
        }
        else if(contentType == ContentPlaybackService.CONTENT_TYPE_SUBSONIC)
        {
            currentContentSource = ContentPlaybackService.CONTENT_TYPE_SUBSONIC;
            subsonicfragment = new ContentSubsonicFragment();
            subsonicfragment.setSubsonicSource(contentName);
            getSupportFragmentManager().beginTransaction().replace(R.id.contentListFragmentContainer, subsonicfragment).commit();
            subsonicfragment.setCallback(contentLibraryFragmentCallback);
            title_text.setText(contentName);
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
    
    public void updateInterfaceColor(int color)
    {
        findViewById(R.id.title_border).setBackgroundColor(color);
        findViewById(R.id.lower_border).setBackgroundColor(color);
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
            
            Log.d("CalcTunesActivity", "Exit Intent Received");
            Exit();
        }
    };
}
