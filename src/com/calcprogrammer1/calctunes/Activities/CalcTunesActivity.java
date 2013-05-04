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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.widget.*;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Color;
import com.calcprogrammer1.calctunes.*;
import com.calcprogrammer1.calctunes.Interfaces.*;
import com.calcprogrammer1.calctunes.MediaInfo.MediaInfoFragment;
import com.calcprogrammer1.calctunes.NowPlaying.NowPlayingFragment;
import com.calcprogrammer1.calctunes.SourceList.SourceListFragment;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.Subsonic.SubsonicAPI;
import com.github.ysamlan.horizontalpager.HorizontalPager;

public class CalcTunesActivity extends FragmentActivity
{    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Class Variables////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	HorizontalPager horizontalpager;
	
	ContentViewHandler viewhandler;
	
    SharedPreferences appSettings;
	
	View sourcelistframe;
	SourceListFragment sourcelistfragment;
	
	NowPlayingFragment nowplayingfragment;
	
	MediaInfoFragment mediainfofragment;
	
	ListView mainlist;
    
    String openFile = null;
    int interfaceColor;
    
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
                viewhandler.setContentSource(file.getParent(), ContentViewHandler.CONTENT_TYPE_FILESYSTEM);
                viewhandler.drawList();
                playbackservice.SetPlaybackContentSource(ContentPlaybackService.CONTENT_TYPE_FILESYSTEM, openFile, 0, null);
                playbackservice.StartPlayback();
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
    
    MediaButtonsHandlerInterface buttonsCallback = new MediaButtonsHandlerInterface(){
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
    
    ContentPlaybackInterface playbackCallback = new ContentPlaybackInterface(){
        @Override
        public void onTrackEnd()
        {
            //Do nothing on track end
        }

        @Override
        public void onMediaInfoUpdated()
        {
            viewhandler.setAdaptersNowPlaying(playbackservice.NowPlayingFile());
        }
        
    };
    
    SourceListInterface sourcelisthandlerCallback = new SourceListInterface(){
        public void callback(int contentType, String filename)
        {
            if(contentType == ContentViewHandler.CONTENT_TYPE_FILESYSTEM)
            {
                viewhandler.setContentSource("/mnt/sdcard", contentType);
                viewhandler.drawList();
            }
            else if(contentType == ContentViewHandler.CONTENT_TYPE_LIBRARY)
            {
                viewhandler.setContentSource(SourceListOperations.readLibraryFile(filename).name, ContentViewHandler.CONTENT_TYPE_LIBRARY);
                viewhandler.drawList();
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
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //Get the application preferences
        appSettings = getSharedPreferences("CalcTunes", MODE_PRIVATE);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
        
        //boolean smallScreenMode = appSettings.getBoolean("small_screen_layout", true);
        
        //if(smallScreenMode)
        //{
            //Set content view to small screen layout
        //    setContentView(R.layout.main_smallscreen);
        //}
        //else
        //{
            //Set content view to normal screen layout
            setContentView(R.layout.main);
        //}

        //Check if CalcTunes was opened from a file browser, and if so, open the file
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_VIEW))
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
	    if(playbackservice_bound)
	    {
	        unbindService(playbackserviceConnection);
	    }
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        //setContentView(R.layout.main);
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
                sourcelistfragment.readSourceLists();
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
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Other Activity Functions///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void createGuiElements()
    {
      
    	viewhandler = new ContentViewHandler(this, mainlist, playbackservice);
        
        updateGuiElements();
    }
    
    public void updateGuiElements()
    {   
        horizontalpager = (HorizontalPager) findViewById(R.id.horizontal_pager);   
        sourcelistframe = findViewById(R.id.sourceListFrame);                     
        mainlist = (ListView) findViewById(R.id.libraryListView);
        viewhandler.setListView(mainlist);
        updateInterfaceColor(interfaceColor);
        viewhandler.drawList();
    }
   
    public void Exit()
    {
        playbackservice.StopPlayback();
        unbindService(playbackserviceConnection);
        playbackservice_bound = false;
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
    
    public void updateInterfaceColor(int color)
    {
        findViewById(R.id.title_border).setBackgroundColor(color);
        findViewById(R.id.lower_border).setBackgroundColor(color);
        viewhandler.setHighlightColor(color);
    }
    
    public void ButtonSidebarClick(View view)
    {
        //this button is pointless, so currently it is a subsonic api test
        //get around stupid android 4.0 restrictions that are dumb
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        SubsonicAPI sub = new SubsonicAPI("192.168.3.100:4040", "user", "password");
        if( sub.SubsonicPing() && sub.SubsonicGetLicense() )
        {
            int id = sub.SubsonicGetMusicFolders().get(0).id;
            sub.SubsonicGetMusicDirectory(id);
            sub.SubsonicGetIndexes();
            SubsonicAPI.SubsonicSong song = sub.SubsonicGetAlbum(sub.SubsonicGetArtist(sub.SubsonicGetArtists().get(0).id).get(0).id).get(0);
            Log.d("SubsonicTest", "Song: " + song.title);
            sub.SubsonicStream(song.id, 160, "ogg");
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
        startActivityForResult(new Intent(this, CalcTunesLibraryBuilderActivity.class), 1);
    }
}