package com.calcprogrammer1.calctunes.ContentPlaybackService;

import android.os.Build;

import com.calcprogrammer1.calctunes.ContentLibraryFragment.ContentListElement;
import com.calcprogrammer1.calctunes.Subsonic.SubsonicConnection;

public class ContentPlaybackSubsonic implements ContentPlaybackService.ContentPlaybackType
{
    private SubsonicConnection subcon;
    private int position;
    private String nowPlayingUri;
    private boolean nowPlayingStream;

    public ContentPlaybackSubsonic(SubsonicConnection sc, int pos)
    {
        subcon = sc;
        position = pos;

        if(subcon.listData.get(position).cache == ContentListElement.CACHE_SDCARD_ORIGINAL)
        {
            nowPlayingUri = subcon.listData.get(position).origPath + "." + subcon.listData.get(position).origExt;
            nowPlayingStream = false;
        }
        else if( subcon.listData.get(position).cache == ContentListElement.CACHE_SDCARD_TRANSCODED)
        {
            nowPlayingUri = subcon.listData.get(position).transPath + "." + subcon.listData.get(position).transExt;
            nowPlayingStream = false;
        }
        else
        {
            // Android versions < API 10 (Android 3.0) cannot stream HTTP sources with MediaPlayer
            // On these devices, download the transcoded file before playback
            // On API 10 or greater, open the mediaplayer for streaming directly
            if (Build.VERSION.SDK_INT < 10)
            {
                subcon.downloadTranscoded(position);
                nowPlayingUri = subcon.listData.get(position).transPath + "." + subcon.listData.get(position).transExt;
                nowPlayingStream = false;
            }
            else
            {
                nowPlayingUri = subcon.streamUrlTranscoded(position);
                nowPlayingStream = true;
            }
        }
    }

    @Override
    public void NextTrack()
    {

    }

    @Override
    public void PrevTrack()
    {

    }

    @Override
    public void RandomTrack()
    {

    }

    @Override
    public void NextArtist()
    {

    }

    @Override
    public void PrevArtist()
    {

    }

    @Override
    public String getContentUri()
    {
        return nowPlayingUri;
    }

    @Override
    public boolean getContentStream()
    {
        return nowPlayingStream;
    }

    @Override
    public int getContentType()
    {
        return ContentPlaybackService.CONTENT_TYPE_SUBSONIC;
    }

    @Override
    public void CleanUp()
    {

    }
}
