package com.calcprogrammer1.calctunes;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;

import android.media.AudioManager;
import android.media.MediaPlayer;

public class MediaPlayerHandler
{
    MediaPlayer mp;
    
    boolean running = false;
    boolean prepared = false;
    
    String current_path;
    String current_title;
    String current_album;
    String current_artist;
    
    public MediaPlayerHandler()
    {
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
            current_artist = tag.getFirstArtist();
            current_album = tag.getFirstAlbum();
            current_title = tag.getFirstTitle();
            
            mp.reset();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(current_path);
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                public void onPrepared(MediaPlayer arg0)
                {
                    prepared = true;
                } 
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                public void onCompletion(MediaPlayer arg0)
                {
                    mp.stop();
                    prepared = false;
                    mp.release();
                    
                }
            });
        }catch (Exception e){}
    }
    
    public void startPlayback()
    {
        if(prepared)
        {
            mp.start();
        }
    }
    
    public void stopPlayback()
    {
        if(prepared)
        {
            mp.stop();
            prepared = false;
            mp.release();
        }
    }
    
    public void pausePlayback()
    {
        if(prepared)
        {
            mp.pause();
        }
    }
    
    public boolean isPlaying()
    {
        if(prepared)
        {
            return mp.isPlaying();
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
    }
    
    public int getCurrentPosition()
    {
        if(prepared)
        {
            return mp.getCurrentPosition();
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
            return mp.getDuration();
        }
        else
        {
            return 0;
        }
    }
}
