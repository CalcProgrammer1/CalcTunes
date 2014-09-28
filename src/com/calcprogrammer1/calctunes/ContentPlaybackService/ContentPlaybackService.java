package com.calcprogrammer1.calctunes.ContentPlaybackService;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.calcprogrammer1.calctunes.ContentPlaylistFragment.PlaylistEditor;
import com.calcprogrammer1.calctunes.Interfaces.*;
import com.calcprogrammer1.calctunes.LastFm;
import com.calcprogrammer1.calctunes.MediaPlayer.MediaPlayerHandler;
import com.calcprogrammer1.calctunes.MediaPlayer.RemoteControlReceiver;
import com.calcprogrammer1.calctunes.R;

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


public class ContentPlaybackService extends Service
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Content Type, View, and Playback Mode Constants////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public interface ContentPlaybackType
    {
        // Set now playing content to next track
        public void NextTrack();

        // Set now playing content to previous track
        public void PrevTrack();

        // Set now playing content to random track
        public void RandomTrack();

        // Set now playing content to next artist if possible
        public void NextArtist();

        // Set now playing content to previous artist if possible
        public void PrevArtist();

        // Return URI of content to play
        public String getContentUri();

        // Is content URI a stream?
        public boolean getContentStream();

        // Get type of content
        public int getContentType();

        // Set context
        public void setContext(Context con);

        // Called before closing the content source
        public void CleanUp();
    }

    //Content Types
    public static final int CONTENT_TYPE_NONE                   = 0;
    public static final int CONTENT_TYPE_FILESYSTEM             = 1;
    public static final int CONTENT_TYPE_LIBRARY                = 2;
    public static final int CONTENT_TYPE_PLAYLIST               = 3;
    public static final int CONTENT_TYPE_SUBSONIC               = 4;
    
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

    private ContentPlaybackType content = null;
    private int     playbackMode            = CONTENT_PLAYBACK_MODE_IN_ORDER;
    private int     auto_start              = 0;    
    private MediaPlayerHandler      mediaplayer;
    private SharedPreferences       appSettings;
    private int     multi_click_thrshld;
    private long    NextTime        = 0;
    private int     NextPressCount  = 0;
    private long    PrevTime        = 0;
    private Notification notification;
    private NotificationManager notificationManager;
    private static int notificationId = 2;
    private LastFm lastfm;
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

    public void SetPlaybackContent(ContentPlaybackType c)
    {
        content = c;

        refreshMediaPlayer();

        updateNotification();
        notifyMediaInfoUpdated();
    }

    public void SetPlaybackContentSource(int contentType, String contentString, int contentPosition)
    {
        
        if(contentType == CONTENT_TYPE_LIBRARY)
        {
            content = new ContentPlaybackLibrary(contentString, contentPosition, getApplicationContext());
        }
        else if(contentType == CONTENT_TYPE_FILESYSTEM)
        {
            content = new ContentPlaybackFilesystem(contentString);
        }
        else if(contentType == CONTENT_TYPE_SUBSONIC)
        {
            //content = new ContentPlaybackSubsonic();
        }

        refreshMediaPlayer();

        updateNotification();
        notifyMediaInfoUpdated();
    }
    
    public int GetPlaybackContentType()
    {
        if(content == null)
        {
            return CONTENT_TYPE_NONE;
        }
        else
        {
            return content.getContentType();
        }
    }
    
    public String GetPlaybackContentString()
    {
        if(content == null)
        {
            return "";
        }
        else
        {
            return content.getContentUri();
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
        if(content != null)
        content.CleanUp();

        if(mediaplayer != null)
        {
            if(mediaplayer.start_time != 0 && ((System.currentTimeMillis() / 1000L) > (mediaplayer.start_time + 30)))
            {
                lastfm.scrobble(mediaplayer.current_artist, mediaplayer.current_album, mediaplayer.current_title, mediaplayer.getDuration(), mediaplayer.start_time);
            }
            mediaplayer.stopPlayback();
        }

        updateNotification();
        notifyMediaInfoUpdated();
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
        new Thread(new Runnable()
        {
            public void run()
            {
                content.NextTrack();

                refreshMediaPlayer();
                mediaplayer.startPlayback();

                updateNotification();
                notifyMediaInfoUpdated();
            }
        }).start();
    }

    public void PrevTrack()
    {
        content.PrevTrack();

        refreshMediaPlayer();
        mediaplayer.startPlayback();

        updateNotification();
        notifyMediaInfoUpdated();
    }

    public void NextArtist()
    {
        content.NextArtist();

        refreshMediaPlayer();
        mediaplayer.startPlayback();

        updateNotification();
        notifyMediaInfoUpdated();
    }

    private void refreshMediaPlayer()
    {
        if(mediaplayer != null)
        {
            if(mediaplayer.start_time != 0 && ((System.currentTimeMillis() / 1000L) > (mediaplayer.start_time + 30)))
            {
                lastfm.scrobble(mediaplayer.current_artist, mediaplayer.current_album, mediaplayer.current_title, mediaplayer.getDuration(), mediaplayer.start_time);
            }
            mediaplayer.stopPlayback();
        }
        if(content.getContentStream())
        {
            mediaplayer.initializeStream(content.getContentUri());
        }
        else
        {
            mediaplayer.initializeFile(content.getContentUri());
        }
        lastfm.updateNowPlaying(mediaplayer.current_artist, mediaplayer.current_album, mediaplayer.current_title, mediaplayer.getDuration());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Notification Functions/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

        lastfm = new LastFm(this);

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
