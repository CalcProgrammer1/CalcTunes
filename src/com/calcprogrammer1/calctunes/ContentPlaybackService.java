package com.calcprogrammer1.calctunes;

import java.util.ArrayList;
import java.util.Random;

import com.calcprogrammer1.calctunes.Interfaces.*;
import com.calcprogrammer1.calctunes.MediaPlayer.MediaPlayerHandler;
import com.calcprogrammer1.calctunes.MediaPlayer.RemoteControlReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;


public class ContentPlaybackService extends Service
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Content Type, View, and Playback Mode Constants////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    //Content Types
    public static final int CONTENT_TYPE_NONE                   = 0;
    public static final int CONTENT_TYPE_FILESYSTEM             = 1;
    public static final int CONTENT_TYPE_LIBRARY                = 2;
    public static final int CONTENT_TYPE_PLAYLIST               = 3;
    public static final int CONTENT_TYPE_SUBSONIC               = 4;

    //Content View Modes
    public static final int CONTENT_VIEW_NONE                   = 0;
    public static final int CONTENT_VIEW_FILESYSTEM             = 1;
    public static final int CONTENT_VIEW_PLAYLIST_ALL           = 2;
    public static final int CONTENT_VIEW_LIBRARY_ALL            = 3;
    public static final int CONTENT_VIEW_LIBRARY_ARTIST         = 4;
    
    //Playback Source Types
    public static final int CONTENT_PLAYBACK_NONE               = 0;
    public static final int CONTENT_PLAYBACK_FILESYSTEM         = 1;
    public static final int CONTENT_PLAYBACK_LIBRARY            = 2;
    public static final int CONTENT_PLAYBACK_PLAYLIST           = 3;

    //Playback Order Modes
    public static final int CONTENT_PLAYBACK_MODE_IN_ORDER      = 0;
    public static final int CONTENT_PLAYBACK_MODE_RANDOM        = 1;
    public static final int CONTENT_PLAYBACK_MODE_REPEAT_ONE    = 2;
    public static final int CONTENT_PLAYBACK_MODE_REPEAT_ALBUM  = 3;
    public static final int CONTENT_PLAYBACK_MODE_REPEAT_ARTIST = 4;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Local variables////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int     playbackMode            = CONTENT_PLAYBACK_MODE_IN_ORDER;
    private int     currentContentType      = CONTENT_PLAYBACK_NONE;
    private String  currentContentString    = "";
    private int     auto_start              = 0;    
    private MediaPlayerHandler      mediaplayer;
    private SharedPreferences       appSettings;
    private Cursor                  playbackCursor;
    private String  nowPlayingFile  = new String();
    private int     nowPlayingPos   = -1;
    private int     nowPlayingMax   = -1;
    private ArrayList<ContentPlaybackInterface> callbacks;
    private Notification notification;
    private NotificationManager notificationManager;
    private static int notificationId = 2;  
    private boolean random          = false;
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Callback Functions/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    MediaPlayerHandlerInterface mediaplayerCallback = new MediaPlayerHandlerInterface(){
        public void onSongFinished()
        {
            NextTrack();
        }

        public void onStop()
        {
        }
    };
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;

            if(appSettings.getBoolean("service_notification", true))
            {
                initializeNotification();
            }
            else
            {
                endNotification();
            }

            SetPlaybackMode(appSettings.getInt("playback_mode", CONTENT_PLAYBACK_MODE_IN_ORDER));
        }
    };
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Class functions////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void registerCallback(ContentPlaybackInterface callback)
    {
        if(callbacks == null)
        {
            callbacks = new ArrayList<ContentPlaybackInterface>();
        }
        
        if(callback != null)
        {
            callbacks.add(callback);
            callback.onMediaInfoUpdated();
        }
    }
    
    //Access to MediaPlayer data
    public String NowPlayingTitle()
    {
        return mediaplayer.current_title;
    }
    
    public String NowPlayingArtist()
    {
        return mediaplayer.current_artist;
    }
    
    public String NowPlayingAlbum()
    {
        return mediaplayer.current_album;
    }
    
    public String NowPlayingYear()
    {
        return mediaplayer.current_year;
    }
    
    public String NowPlayingFile()
    {
        return nowPlayingFile;
    }
    
    public int NowPlayingDuration()
    {
        return mediaplayer.getDuration();
    }
    
    public int NowPlayingPosition()
    {
        return mediaplayer.getCurrentPosition();
    }

    public void SetPlaybackMode(int mode)
    {
        playbackMode = mode;
    }

    public void SeekPlayback(int position)
    {
        mediaplayer.seekPlayback(position);
    }
    
    public boolean isPlaying()
    {
        return mediaplayer.isPlaying();
    }
    
    public void SetPlaybackContentSource(int contentType, String contentString, int contentPosition)
    {
        
        if(contentType == CONTENT_TYPE_LIBRARY)
        {
            SQLiteDatabase libraryDatabase = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(contentString + ".db"), null);
            playbackCursor = libraryDatabase.rawQuery("SELECT * FROM MYLIBRARY ORDER BY ARTIST, ALBUM, DISC, TRACK;", null);
            playbackCursor.moveToPosition(contentPosition);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            nowPlayingPos  = contentPosition;       
            nowPlayingMax  = playbackCursor.getCount() - 1;
        }
        else if(contentType == CONTENT_TYPE_FILESYSTEM)
        {
            nowPlayingFile = contentString;
            nowPlayingPos  = 0;
            nowPlayingMax  = 0;
        }
        currentContentType   = contentType;
        currentContentString = contentString;
        
        mediaplayer.stopPlayback();
        mediaplayer.initialize(nowPlayingFile);
        
        updateNotification();
        
        notifyMediaInfoUpdated();
    }
    
    public int GetPlaybackContentType()
    {
        return currentContentType;
    }
    
    public String GetPlaybackContentString()
    {
        return currentContentString;
    }
    
    public void StartPlayback()
    {
        mediaplayer.startPlayback();
    }
    
    public void PausePlayback()
    {
        mediaplayer.pausePlayback();
    }
    
    public void StopPlayback()
    {
        if(currentContentType == CONTENT_PLAYBACK_FILESYSTEM)
        {
            nowPlayingFile = "";
        }
        else if(currentContentType == CONTENT_PLAYBACK_LIBRARY)
        {
            nowPlayingFile = "";
            nowPlayingPos  = -1;
            nowPlayingMax  = 0;
            if(playbackCursor != null)
            {
            playbackCursor.close();
            playbackCursor = null;
            }
        }
        
        mediaplayer.stopPlayback();
        
        updateNotification();
        
        notifyMediaInfoUpdated();
        
        currentContentType = CONTENT_PLAYBACK_NONE;
    }
    
    public void NextTrack()
    {
        if(currentContentType == CONTENT_PLAYBACK_FILESYSTEM)
        {
            nowPlayingFile = "";
            mediaplayer.stopPlayback();            
        }
        else if(currentContentType == CONTENT_PLAYBACK_LIBRARY)
        {
            if( playbackMode == CONTENT_PLAYBACK_MODE_RANDOM )
            {
                nowPlayingPos = new Random().nextInt(nowPlayingMax + 1);
            }
            else
            {
                if(nowPlayingPos >= nowPlayingMax)
                {
                    nowPlayingPos = 0;
                }
                else
                {
                    nowPlayingPos++;
                }
            }
            playbackCursor.moveToPosition(nowPlayingPos);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            mediaplayer.stopPlayback();
            mediaplayer.initialize(nowPlayingFile);
            mediaplayer.startPlayback();
        }

        updateNotification();
        
        notifyMediaInfoUpdated();
    }
    
    public void PrevTrack()
    {
        if(currentContentType == CONTENT_PLAYBACK_FILESYSTEM)
        {
            nowPlayingFile = "";
            mediaplayer.stopPlayback();
        }
        else if(currentContentType == CONTENT_PLAYBACK_LIBRARY)
        {
            if(nowPlayingPos <= 0)
            {
                nowPlayingPos = playbackCursor.getCount() - 1;
            }
            else
            {
                nowPlayingPos--;
            }
            playbackCursor.moveToPosition(nowPlayingPos);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            
            mediaplayer.stopPlayback();
            mediaplayer.initialize(nowPlayingFile);
            mediaplayer.startPlayback();
        }

        updateNotification();
        
        notifyMediaInfoUpdated();
    }

    @SuppressWarnings("deprecation")
    private void initializeNotification()
    {
        notification = new Notification(R.drawable.icon, "CalcTunes Playback Service Started", System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(this, "CalcTunes", "Now Playing: ", PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0));
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }
    
    @SuppressWarnings("deprecation")
    private void updateNotification()
    {
        if(appSettings.getBoolean("service_notification", true))
        {
            notification.setLatestEventInfo(this, "CalcTunes", "Now Playing: " + mediaplayer.current_title, PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0));
            notificationManager.notify(notificationId, notification);
        }
    }
    
    private void endNotification()
    {
        notificationManager.cancel(notificationId);
    }
    
    private void notifyMediaInfoUpdated()
    {
        if(callbacks != null)
        {
            for(ContentPlaybackInterface callback : callbacks)
            {
                if(callback != null)
                {
                    callback.onMediaInfoUpdated();
                }
                else
                {
                    callbacks.remove(callback);
                }
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Service Functions//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final IBinder mBinder = new ContentPlaybackBinder();
    
    public class ContentPlaybackBinder extends Binder
    {
        public ContentPlaybackService getService()
        {
            return ContentPlaybackService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.d("ContentPlaybackService", "onStartCommand");
        Bundle extras = intent.getExtras();
        if(extras != null)
        {
            auto_start = extras.getInt("auto_start", 0);
        }
        else
        {
            auto_start = 0;
        }
        
        if(auto_start == 1)
        {           
            Log.d("ContentPlaybackService", "Automatic playback starting");
            
            SetPlaybackContentSource(CONTENT_TYPE_LIBRARY, appSettings.getString("auto_play_lib", "Music"), 0);
            StartPlayback();
        }
        
        return START_REDELIVER_INTENT;
    }
    
    @Override
    public void onCreate()
    {
        mediaplayer = new MediaPlayerHandler(this);
        mediaplayer.setCallback(mediaplayerCallback);
        ((AudioManager)getSystemService(AUDIO_SERVICE)).registerMediaButtonEventReceiver(new ComponentName( this, RemoteControlReceiver.class ) );
               
        //Register media buttons receiver
        registerReceiver(remoteReceiver, new IntentFilter("com.calcprogrammer1.calctunes.REMOTE_BUTTON_EVENT"));
        
        //Get the application preferences
        appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        
        if(appSettings.getBoolean("service_notification", true))
        {
            initializeNotification();
        }
    }
    
    @Override
    public void onDestroy()
    {
        //Stop the media player
        mediaplayer.stopPlayback();
        
        //Unregister media buttons receiver
        unregisterReceiver(remoteReceiver);
        
        //Stop the notification
        endNotification();
    }
    
    @Override
    public IBinder onBind(Intent arg0)
    {
        return mBinder;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
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
            Log.d("service", "received intent");
            int keyCode = intent.getExtras().getInt("keyEvent");
            
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    NextTrack();
                    break;
                   
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    PrevTrack();
                    break;
                    
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    StopPlayback();
                    break;
                    
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    PausePlayback();
                    break;
                    
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    StartPlayback();
                    break;
                    
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if(isPlaying())
                    {
                        PausePlayback();
                    }
                    else
                    {
                        StartPlayback();
                    }
                    break;
            }
        }
    };
}
