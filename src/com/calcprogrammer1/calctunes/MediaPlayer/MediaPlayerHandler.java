package com.calcprogrammer1.calctunes.MediaPlayer;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.calcprogrammer1.calctunes.LibraryOperations;
import com.calcprogrammer1.calctunes.Interfaces.MediaPlayerHandlerInterface;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.media.audiofx.AudioEffect;

public class MediaPlayerHandler
{
    MediaPlayer mp;
    LosslessMediaCodecHandler ls;
    
    boolean running = false;
    boolean prepared = false;
    boolean playonprepare = false;
    
    public String current_path = "";
    public String current_title = "";
    public String current_album = "";
    public String current_artist = "";
    public String current_year = "";
    
    Context con;
    
    MediaPlayerHandlerInterface cb;
    
    public MediaPlayerHandler(Context context)
    {
        con = context;
    }
    
    public void setCallback(MediaPlayerHandlerInterface callback)
    {
        cb = callback;
    }
    
    public void initialize(String filePath)
    {
        current_path = filePath;
        initialize();
    }
    
    public void initialize()
    {
        stopPlayback();
        mp = new MediaPlayer();
        File song = new File(current_path);
        AudioFile f;
        try
        {
            f = LibraryOperations.readAudioFileReadOnly(song);
            Tag tag = f.getTag();
            current_artist = tag.getFirst(FieldKey.ARTIST);
            current_album = tag.getFirst(FieldKey.ALBUM);
            current_title = tag.getFirst(FieldKey.TITLE);
            current_year = tag.getFirst(FieldKey.YEAR);
            
            mp.reset();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(current_path);
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                public void onPrepared(MediaPlayer arg0)
                {
                    prepared = true;
                    if(playonprepare)
                    {
                        mp.start();
                        playonprepare = false;
                    }
                } 
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                public void onCompletion(MediaPlayer arg0)
                {
                    mp.stop();
                    prepared = false;
                    Intent i = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
                    i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mp.getAudioSessionId());
                    i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, con.getPackageName());
                    con.sendBroadcast(i);
                    mp.release();
                    mp = null;
                    current_path = "";
                    current_title = "";
                    current_artist = "";
                    current_album = "";
                    current_year = "";
                    if(cb != null) cb.onSongFinished();
                }
            });
            Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mp.getAudioSessionId());
            i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, con.getPackageName());
            con.sendBroadcast(i);
        }
        catch (Exception e)
        {
            mp.release();
            mp = null;
            ls = new LosslessMediaCodecHandler();
            ls.setCallback(new LosslessMediaCodecHandlerCallback()
            {
                public void onCompletion()
                {
                    prepared = false;
                    ls = null;
                    current_path = "";
                    current_title = "";
                    current_artist = "";
                    current_album = "";
                    current_year = "";
                    if(cb != null) cb.onSongFinished();
                }
            });
            ls.setDataSource(current_path);
            prepared = true;
            if(playonprepare)
            {
                ls.start();
                playonprepare = false;
            }
        }
    }
    
    public void startPlayback()
    {
        if(prepared)
        {
            if(mp != null)
            {
                mp.start();
            }
            else if(ls != null)
            {
                ls.start();
            }
        }
        else
        {
            playonprepare = true;
        }
    }
    
    public void stopPlayback()
    {
        if(prepared)
        {
            if(mp != null)
            {
                mp.stop();
                prepared = false;
                mp.release();
                mp = null;
            }
            else if(ls != null)
            {
                ls.stop();
                prepared = false;
                ls = null;
            }
            current_path = "";
            current_title = "";
            current_artist = "";
            current_album = "";
            current_year = "";
        }
        if(cb != null) cb.onStop();
    }
    
    public void pausePlayback()
    {
        if(prepared)
        {
            if(mp != null)
            {
                mp.pause();
            }
            else if(ls != null)
            {
                ls.pause();
            }
        }
    }
    
    public boolean isPlaying()
    {
        if(prepared)
        {
            if(mp != null)
            {
                return mp.isPlaying();
            }
            else if(ls != null)
            {
                return ls.isPlaying();
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    public void seekPlayback(int seekto)
    {
        if(prepared)
        {
            if(mp != null)
            {
                mp.pause();
                mp.seekTo(seekto);
                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener()
                {
                    public void onSeekComplete(MediaPlayer arg0)
                    {
                        mp.start();
                    }
                });
            }
            else if(ls != null)
            {
                ls.seekTo(seekto);
            }
        }
    }
    
    public int getCurrentPosition()
    {
        if(prepared)
        {
            if(mp != null)
            {
                return mp.getCurrentPosition();
            }
            else if(ls != null)
            {
                return ls.getCurrentPosition();
            }
            else
            {
                return 0;
            }
        }
        else
        {
            return 0;
        }
    }
    
    public int getDuration()
    {
        if(prepared)
        {
            if(mp != null)
            {
                return mp.getDuration();
            }
            else if(ls != null)
            {
                return ls.getDuration();
            }
            else
            {
                return 0;
            }
        }
        else
        {
            return 0;
        }
    }
}
