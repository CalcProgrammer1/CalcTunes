package com.calcprogrammer1.calctunes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

interface ContentListCallback
{
    void callback(String file);
}

public class ContentListHandler
{
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
    
    //Current Content View
    private int contentViewType = CONTENT_TYPE_NONE;
    private int contentViewMode = CONTENT_VIEW_NONE;
    
    //ListView to display on and context of main activity
    private ListView contentList;
    private Context c;
    
    //Cursors - one for content list view and one for playback
    private SQLiteDatabase libraryDatabase;

    private String viewCursorQuery;
    private Cursor playbackCursor;
    private String playbackCursorQuery;
    
    //Now Playing
    private String nowPlayingFile = new String();
    private int nowPlayingCursorPos = -1;
    
    //Color
    private int interfaceColor;
    
    private LibraryDatabaseAdapter   libAdapter;
    private ContentFilesystemAdapter fileAdapter;
    private ContentListCallback cb;
    
    //Setup and Constructor Functions
    public ContentListHandler(Context con, ListView lv)
    {
        contentList = lv;
        c = con;
    }
    
    public void setListView(ListView lv)
    {
        contentList = lv;
    }
    
    public void setCallback(ContentListCallback callback)
    {
        cb = callback;
    }
    
    //Set Content Source
    public void setContentSource(String contentName, int contentType)
    {
        contentViewType = contentType;
        if(contentViewType == CONTENT_TYPE_LIBRARY)
        {
            contentViewMode = CONTENT_VIEW_LIBRARY_ALL;
            libraryDatabase = SQLiteDatabase.openOrCreateDatabase("/data/data/com.calcprogrammer1.calctunes/databases/" + contentName + ".db", null);
            viewCursorQuery = "SELECT * FROM MYLIBRARY ORDER BY ARTIST, ALBUM, DISC, TRACK;";
            libAdapter = new LibraryDatabaseAdapter(c, libraryDatabase, viewCursorQuery);
            libAdapter.setNowPlayingColor(interfaceColor);
        }
        else if(contentViewType == CONTENT_TYPE_FILESYSTEM)
        {
            contentViewMode = CONTENT_VIEW_FILESYSTEM;
            fileAdapter = new ContentFilesystemAdapter(c, contentName);
            fileAdapter.setNowPlayingColor(interfaceColor);
        }
        else if(contentViewType == CONTENT_TYPE_PLAYLIST)
        {
            
        }
    }
    
    public void drawList()
    {
        if(contentViewMode == CONTENT_VIEW_FILESYSTEM)
        {
            contentList.setAdapter(fileAdapter);
        }
        else if(contentViewMode == CONTENT_VIEW_LIBRARY_ALL)
        {
            contentList.setAdapter(libAdapter);
        }
        else
        {
            
        }
        contentList.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                libraryClickHandler(arg0, arg1, arg2, arg3);
            }   
        });
    }

    public void libraryClickHandler(AdapterView<?> parent, View view, int position, long id)
    {
        if(contentViewMode == CONTENT_VIEW_FILESYSTEM)
        {
            if(position == 0 && !fileAdapter.currentDirectory.getPath().equals("/"))
            {
                fileAdapter = new ContentFilesystemAdapter(c, fileAdapter.currentDirectory.getParent());
                fileAdapter.setNowPlayingColor(interfaceColor);
                drawList();
            }
            else if(fileAdapter.files.get(position).isDirectory())
            {
                fileAdapter = new ContentFilesystemAdapter(c, fileAdapter.files.get(position).getPath());
                fileAdapter.setNowPlayingColor(interfaceColor);
                drawList();
            }
            else
            {
                nowPlayingFile = fileAdapter.files.get(position).getPath();
                cb.callback(nowPlayingFile);
                
            }
        }
        else if(contentViewMode == CONTENT_VIEW_LIBRARY_ALL)
        {
            playbackCursorQuery = viewCursorQuery;
            playbackCursor = libraryDatabase.rawQuery(playbackCursorQuery, null);
            playbackCursor.moveToPosition(position);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            nowPlayingCursorPos = position;
            
            libAdapter.setNowPlaying(nowPlayingFile);
            libAdapter.notifyDataSetChanged();
            
            cb.callback(nowPlayingFile);
        }
    }

    public String CurTrack()
    {
        return playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
    }
    
    public void StopNotify()
    {
        if(contentViewType == CONTENT_TYPE_FILESYSTEM)
        {
            nowPlayingFile = "";
        }
        else if(contentViewType == CONTENT_TYPE_LIBRARY)
        {
            nowPlayingCursorPos = -1;
            nowPlayingFile = "";
            if(playbackCursor != null)
            {
            playbackCursor.close();
            playbackCursor = null;
            }
            if(libAdapter != null)
            {
                libAdapter.setNowPlaying(nowPlayingFile);
                libAdapter.notifyDataSetChanged();
            }
        }
    }
    
    public String NextTrack()
    {
        if(contentViewType == CONTENT_TYPE_FILESYSTEM)
        {
            nowPlayingFile = "";
            return null;
        }
        else if(contentViewType == CONTENT_TYPE_LIBRARY)
        {
            nowPlayingCursorPos ++;
            playbackCursor.moveToPosition(nowPlayingCursorPos);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            libAdapter.setNowPlaying(nowPlayingFile);
            libAdapter.notifyDataSetChanged();
            return nowPlayingFile;
        }
        else
        {
            return null;
        }
    }
    
    public String PrevTrack()
    {
        if(contentViewType == CONTENT_TYPE_FILESYSTEM)
        {
            nowPlayingFile = "";
            return null;
        }
        else if(contentViewType == CONTENT_TYPE_LIBRARY)
        {
            nowPlayingCursorPos --;
            playbackCursor.moveToPosition(nowPlayingCursorPos);
            nowPlayingFile = playbackCursor.getString(playbackCursor.getColumnIndex("PATH"));
            libAdapter.setNowPlaying(nowPlayingFile);
            libAdapter.notifyDataSetChanged();
            return nowPlayingFile;
        }
        else
        {
            return null;
        }
    }

    public void setHighlightColor(int color)
    {
        interfaceColor = color;
        if(contentViewMode == CONTENT_VIEW_LIBRARY_ALL)
        {
            libAdapter.setNowPlayingColor(interfaceColor);
            libAdapter.notifyDataSetChanged();
        }
    }
}
