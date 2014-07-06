package com.calcprogrammer1.calctunes.ContentPlaybackService;

import android.content.Context;

import com.calcprogrammer1.calctunes.ContentPlaylistFragment.PlaylistEditor;

import java.util.Random;

public class ContentPlaybackPlaylist implements ContentPlaybackService.ContentPlaybackType
{
    private int nowPlayingPos;

    private PlaylistEditor playlist;

    public ContentPlaybackPlaylist(PlaylistEditor pl, int contentPosition)
    {
        playlist = pl;
        nowPlayingPos = contentPosition;
    }

    @Override
    public void NextTrack()
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

    @Override
    public void PrevTrack()
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

    @Override
    public void RandomTrack()
    {
        nowPlayingPos = new Random().nextInt(playlist.maxIndex() + 1);
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
        return(playlist.playlistData.get(nowPlayingPos).filename);
    }

    @Override
    public boolean getContentStream()
    {
        return false;
    }

    @Override
    public int getContentType()
    {
        return ContentPlaybackService.CONTENT_TYPE_PLAYLIST;
    }

    @Override
    public void setContext(Context con)
    {

    }

    @Override
    public void CleanUp()
    {

    }
}
