package com.calcprogrammer1.calctunes.ContentPlaybackService;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Random;

public class ContentPlaybackLibrary implements ContentPlaybackService.ContentPlaybackType
{
    private int nowPlayingPos;
    private int nowPlayingMax;

    private Cursor playbackCursor;

    private String nowPlayingFile;

    public ContentPlaybackLibrary(String contentString, int contentPosition, Context c)
    {
        SQLiteDatabase libraryDatabase = SQLiteDatabase.openOrCreateDatabase(c.getDatabasePath(contentString + ".db"), null);
        playbackCursor = libraryDatabase.rawQuery("SELECT * FROM MYLIBRARY ORDER BY ARTIST, ALBUM, DISC, TRACK;", null);
        playbackCursor.moveToPosition(contentPosition);
        nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
        nowPlayingPos  = contentPosition;
        nowPlayingMax  = playbackCursor.getCount() - 1;
    }

    @Override
    public void NextTrack()
    {
        if(nowPlayingPos >= nowPlayingMax)
        {
            nowPlayingPos = 0;
        }
        else
        {
            nowPlayingPos++;
        }
        playbackCursor.moveToPosition(nowPlayingPos);
        nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
    }

    @Override
    public void PrevTrack()
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
        playbackCursor.moveToPosition(nowPlayingPos);
        nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
    }

    @Override
    public void RandomTrack()
    {
        nowPlayingPos = new Random().nextInt(nowPlayingMax + 1);
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
        return(nowPlayingFile);
    }

    @Override
    public boolean getContentStream()
    {
        return false;
    }

    @Override
    public int getContentType()
    {
        return ContentPlaybackService.CONTENT_TYPE_LIBRARY;
    }

    @Override
    public void CleanUp()
    {
        if(playbackCursor != null)
        {
            playbackCursor.close();
            playbackCursor = null;
        }
    }
}
