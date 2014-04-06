package com.calcprogrammer1.calctunes;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.calcprogrammer1.calctunes.ContentPlaylistFragment.PlaylistEditor;
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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;


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
    private int     multi_click_thrshld;
    private long    NextTime        = 0;
    private int     NextPressCount  = 0;
    private long    PrevTime        = 0;
    private Notification notification;
    private NotificationManager notificationManager;
    private static int notificationId = 2;  
    private boolean random          = false;
    private PlaylistEditor playlist;

    private Timer NextTimer;

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

            multi_click_thrshld = Integer.parseInt(appSettings.getString("multi_click_thrshld", "500"));
            SetPlaybackMode(appSettings.getInt("playback_mode", CONTENT_PLAYBACK_MODE_IN_ORDER));
        }
    };
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Class functions////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
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

    public void SetPlaybackContentSourcePlaylist(PlaylistEditor playlist, int contentPosition)
    {
        currentContentType = CONTENT_TYPE_PLAYLIST;
        this.playlist = playlist;
        nowPlayingPos = contentPosition;
        nowPlayingFile = playlist.playlistData.get(nowPlayingPos).filename;

        mediaplayer.stopPlayback();
        mediaplayer.initialize(nowPlayingFile);

        updateNotification();

        notifyMediaInfoUpdated();
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

    public void NextPressed()
    {
        if( System.currentTimeMillis() - NextTime < multi_click_thrshld )
        {
            if(NextTimer != null)
            {
                NextTimer.cancel();
            }
            NextTimer = new Timer("NextTimer", true);
            NextPressCount++;
            NextTimer.schedule( new TimerTask(){
                public void run()
                {
                    new ButtonPressTask().execute(2);
                }
            }, multi_click_thrshld);
        }
        else
        {
            NextTimer = new Timer("NextTimer", true);
            NextTimer.schedule(new TimerTask()
            {
                public void run()
                {
                    new ButtonPressTask().execute(1);
                }
            }, multi_click_thrshld);
        }
        NextTime = System.currentTimeMillis();
    }

    public void PrevPressed()
    {
        if( System.currentTimeMillis() - PrevTime < multi_click_thrshld )
        {
            Log.d("ContentPlaybackService", "Double clicked Prev Track");
        }
        PrevTime = System.currentTimeMillis();
        new ButtonPressTask().execute(0);
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
        else if(currentContentType == CONTENT_PLAYBACK_PLAYLIST)
        {
            if( playbackMode == CONTENT_PLAYBACK_MODE_RANDOM )
            {
                nowPlayingPos = new Random().nextInt(playlist.maxIndex() + 1);
            }
            else
            {
                if(nowPlayingPos >= playlist.maxIndex())
                {
                    nowPlayingPos = 0;
                }
                else
                {
                    nowPlayingPos++;
                }
            }
            nowPlayingFile = playlist.playlistData.get(nowPlayingPos).filename;
            mediaplayer.stopPlayback();
            mediaplayer.initialize(nowPlayingFile);
            mediaplayer.startPlayback();
        }

        updateNotification();
        
        notifyMediaInfoUpdated();
    }

    public void NextArtist()
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
                    String currentAlbum = playbackCursor.getString(playbackCursor.getColumnIndex("ALBUM"));
                    for( ; nowPlayingPos < nowPlayingMax; nowPlayingPos++)
                    {
                        playbackCursor.moveToPosition(nowPlayingPos);
                        String newAlbum = playbackCursor.getString(playbackCursor.getColumnIndex("ALBUM"));
                        if(!currentAlbum.equals(newAlbum))
                        {
                            break;
                        }
                    }
            }
            playbackCursor.moveToPosition(nowPlayingPos);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            mediaplayer.stopPlayback();
            mediaplayer.initialize(nowPlayingFile);
            mediaplayer.startPlayback();
        }
        else if(currentContentType == CONTENT_PLAYBACK_PLAYLIST)
        {
            if( playbackMode == CONTENT_PLAYBACK_MODE_RANDOM )
            {
                nowPlayingPos = new Random().nextInt(playlist.maxIndex() + 1);
            }
            else
            {
                if(nowPlayingPos >= playlist.maxIndex())
                {
                    nowPlayingPos = 0;
                }
                else
                {
                    nowPlayingPos++;
                }
            }
            nowPlayingFile = playlist.playlistData.get(nowPlayingPos).filename;
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
        else if(currentContentType == CONTENT_PLAYBACK_PLAYLIST)
        {
            if(nowPlayingPos <= 0)
            {
                nowPlayingPos = playlist.maxIndex();
            }
            else
            {
                nowPlayingPos--;
            }
            nowPlayingFile = playlist.playlistData.get(nowPlayingPos).filename;

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
        Intent broadcast = new Intent();
        broadcast.setAction("com.calcprogrammer1.calctunes.PLAYBACK_INFO_UPDATED_EVENT");
        sendBroadcast(broadcast);
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

        multi_click_thrshld = Integer.parseInt(appSettings.getString("multi_click_thrshld", "500"));
        SetPlaybackMode(appSettings.getInt("playback_mode", CONTENT_PLAYBACK_MODE_IN_ORDER));

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

    public class ButtonPressTask extends AsyncTask<Object, Void, Void>
    {
        private int id;

        @Override
        protected Void doInBackground(Object... params)
        {
            id       = (Integer)params[0];
            switch(id)
            {
                case 0:
                    PrevTrack();
                    break;

                case 1:
                    NextTrack();
                    break;

                case 2:
                    NextArtist();
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
        }
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
                    NextPressed();
                    break;
                   
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    PrevPressed();
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
