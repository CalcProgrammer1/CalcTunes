package com.calcprogrammer1.calctunes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;

interface ContentPlaybackCallback
{
    void onTrackEnd();
    void onMediaInfoUpdated();
}

public class ContentPlaybackService extends Service
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Content Type, View, and Playback Mode Constants////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    //Content Types
    public static final int CONTENT_TYPE_NONE = 0;
    public static final int CONTENT_TYPE_FILESYSTEM = 1;
    public static final int CONTENT_TYPE_LIBRARY = 2;
    public static final int CONTENT_TYPE_PLAYLIST = 3;
    
    //Content View Modes
    public static final int CONTENT_VIEW_NONE = 0;
    public static final int CONTENT_VIEW_FILESYSTEM = 1;
    public static final int CONTENT_VIEW_PLAYLIST_ALL = 2;
    public static final int CONTENT_VIEW_LIBRARY_ALL = 3;
    public static final int CONTENT_VIEW_LIBRARY_ARTIST = 4;
    
    //Playback Modes
    public static final int CONTENT_PLAYBACK_NONE = 0;
    public static final int CONTENT_PLAYBACK_FILESYSTEM = 1;
    public static final int CONTENT_PLAYBACK_LIBRARY = 2;
    public static final int CONTENT_PLAYBACK_PLAYLIST = 3;
    
    //Current Content Type
    private int contentPlayMode = CONTENT_PLAYBACK_NONE;
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Local variables////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    //MediaPlayer Handler for playback
    private MediaPlayerHandler mediaplayer;
    
    //Cursors - one for content list view and one for playback
    //private SQLiteDatabase libraryDatabase;
    private Cursor playbackCursor;
    //private String playbackCursorQuery;
    
    //Now Playing
    private String nowPlayingFile = new String();
    private int nowPlayingCursorPos = -1;
    
    private ContentPlaybackCallback cb;
    
    //Now Playing Notification
    private Notification notification;
    private NotificationManager notificationManager;
    private static int notificationId = 2;
    
    //Application Settings
    SharedPreferences appSettings;
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Callback Functions/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    MediaPlayerHandlerCallback mediaplayerCallback = new MediaPlayerHandlerCallback(){
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
        }
    };
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Class functions////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void setCallback(ContentPlaybackCallback callback)
    {
        cb = callback;
        if(cb != null)
        {
            cb.onMediaInfoUpdated();
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
    
    public void SeekPlayback(int position)
    {
        mediaplayer.seekPlayback(position);
    }
    
    public boolean isPlaying()
    {
        return mediaplayer.isPlaying();
    }
    
    public void SetPlaybackContentSource(int contentType, String contentString, int contentPosition, Cursor contentCursor)
    {
        
        if(contentType == CONTENT_TYPE_LIBRARY)
        {
            playbackCursor = contentCursor;
            playbackCursor.moveToPosition(contentPosition);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            nowPlayingCursorPos = contentPosition;              
        }
        else if(contentType == CONTENT_TYPE_FILESYSTEM)
        {
            nowPlayingFile = contentString;
        }
        contentPlayMode = contentType;
        
        //Initialize media playback
        mediaplayer.stopPlayback();
        mediaplayer.initialize(nowPlayingFile);
        
        updateNotification();
        
        if(cb != null)
        {
            cb.onMediaInfoUpdated();
        }
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
        if(contentPlayMode == CONTENT_PLAYBACK_FILESYSTEM)
        {
            nowPlayingFile = "";
        }
        else if(contentPlayMode == CONTENT_PLAYBACK_LIBRARY)
        {
            nowPlayingCursorPos = -1;
            nowPlayingFile = "";
            if(playbackCursor != null)
            {
            playbackCursor.close();
            playbackCursor = null;
            }
        }
        
        mediaplayer.stopPlayback();
        
        updateNotification();
        
        if(cb != null)
        {
            cb.onMediaInfoUpdated();
        }
        contentPlayMode = CONTENT_PLAYBACK_NONE;
    }
    
    public void NextTrack()
    {
        if(contentPlayMode == CONTENT_PLAYBACK_FILESYSTEM)
        {
            nowPlayingFile = "";
            mediaplayer.stopPlayback();            
        }
        else if(contentPlayMode == CONTENT_PLAYBACK_LIBRARY)
        {
            if(nowPlayingCursorPos >= playbackCursor.getCount()-1)
            {
                nowPlayingCursorPos = 0;
            }
            else
            {
                nowPlayingCursorPos++;
            }
            playbackCursor.moveToPosition(nowPlayingCursorPos);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            mediaplayer.stopPlayback();
            mediaplayer.initialize(nowPlayingFile);
            mediaplayer.startPlayback();
        }

        updateNotification();
        
        if(cb != null)
        {
            cb.onMediaInfoUpdated();
        }
    }
    
    public void PrevTrack()
    {
        if(contentPlayMode == CONTENT_PLAYBACK_FILESYSTEM)
        {
            nowPlayingFile = "";
            mediaplayer.stopPlayback();
        }
        else if(contentPlayMode == CONTENT_PLAYBACK_LIBRARY)
        {
            if(nowPlayingCursorPos <= 0)
            {
                nowPlayingCursorPos = playbackCursor.getCount() - 1;
            }
            else
            {
                nowPlayingCursorPos--;
            }
            playbackCursor.moveToPosition(nowPlayingCursorPos);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            
            mediaplayer.stopPlayback();
            mediaplayer.initialize(nowPlayingFile);
            mediaplayer.startPlayback();
        }

        updateNotification();
        
        if(cb != null)
        {
            cb.onMediaInfoUpdated();
        }
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
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Service Functions//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final IBinder mBinder = new ContentPlaybackBinder();
    
    public class ContentPlaybackBinder extends Binder
    {
        ContentPlaybackService getService()
        {
            return ContentPlaybackService.this;
        }
    }

    @Override
    public void onCreate()
    {
        mediaplayer = new MediaPlayerHandler();
        mediaplayer.setCallback(mediaplayerCallback);
        
        //Get the application preferences
        appSettings = getSharedPreferences("CalcTunes",MODE_PRIVATE);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        
        if(appSettings.getBoolean("service_notification", true))
        {
            initializeNotification();
        }
    }
    
    @Override
    public void onDestroy()
    {
        //Stop the notification
        endNotification();
    }
    
    @Override
    public IBinder onBind(Intent arg0)
    {
        return mBinder;
    }
}
