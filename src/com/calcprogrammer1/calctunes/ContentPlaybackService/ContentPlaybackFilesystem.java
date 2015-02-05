package com.calcprogrammer1.calctunes.ContentPlaybackService;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class ContentPlaybackFilesystem implements ContentPlaybackService.ContentPlaybackType
{
    private String currentDirectory;
    private String contentString;
    private String nowPlayingFile;

    public ContentPlaybackFilesystem(String contString)
    {
        contentString = contString;
        nowPlayingFile = contentString;
        File current = new File(nowPlayingFile);
        currentDirectory = current.getParent();
        Log.d("ContentPlaybackFilesystem", "Path: " + currentDirectory);
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
    public String getNowPlayingUri()
    {
        return nowPlayingFile;
    }

    @Override
    public boolean getNowPlayingStream()
    {
        return false;
    }

    @Override
    public String getContentString()
    {
        return contentString;
    }

    @Override
    public int getContentType()
    {
        return ContentPlaybackService.CONTENT_TYPE_FILESYSTEM;
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
