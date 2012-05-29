package com.calcprogrammer1.calctunes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ContentViewHandler
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
    
    //Playback Modes
    public static final int CONTENT_PLAYBACK_NONE = 0;
    public static final int CONTENT_PLAYBACK_FILESYSTEM = 1;
    public static final int CONTENT_PLAYBACK_LIBRARY = 2;
    public static final int CONTENT_PLAYBACK_PLAYLIST = 3;
    
    //Current Content View
    private int contentViewType = CONTENT_TYPE_NONE;
    private int contentViewMode = CONTENT_VIEW_NONE;
    
    //ListView to display on and context of main activity
    private ListView contentList;
    private Context c;
    
    //Cursors - one for content list view and one for playback
    private SQLiteDatabase libraryDatabase;

    private String viewCursorQuery;
    
    //Color
    private int interfaceColor;
    
    private LibraryDatabaseAdapter   libAdapter;
    private ContentFilesystemAdapter fileAdapter;
    //private ContentViewCallback cb;
    private ContentPlaybackService playbackservice;
    
    //Setup and Constructor Functions
    public ContentViewHandler(Context con, ListView lv, ContentPlaybackService player)
    {
        contentList = lv;
        c = con;
        playbackservice = player;
    }
    
    public void setListView(ListView lv)
    {
        contentList = lv;
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
            libAdapter.setNowPlaying(playbackservice.NowPlayingFile());
            libAdapter.setNowPlayingColor(interfaceColor);
        }
        else if(contentViewType == CONTENT_TYPE_FILESYSTEM)
        {
            contentViewMode = CONTENT_VIEW_FILESYSTEM;
            fileAdapter = new ContentFilesystemAdapter(c, contentName);
            fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
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
                fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                fileAdapter.setNowPlayingColor(interfaceColor);
                drawList();
            }
            else if(fileAdapter.files.get(position).isDirectory())
            {
                fileAdapter = new ContentFilesystemAdapter(c, fileAdapter.files.get(position).getPath());
                fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                fileAdapter.setNowPlayingColor(interfaceColor);
                drawList();
            }
            else
            {
                playbackservice.SetPlaybackContentSource(CONTENT_TYPE_FILESYSTEM, fileAdapter.files.get(position).getPath(), 0, null);

                fileAdapter.setNowPlaying(playbackservice.NowPlayingFile());
                fileAdapter.notifyDataSetChanged();                
            }
        }
        else if(contentViewMode == CONTENT_VIEW_LIBRARY_ALL)
        {
            Cursor playbackCursor = libraryDatabase.rawQuery(viewCursorQuery, null);
            playbackservice.SetPlaybackContentSource(CONTENT_TYPE_LIBRARY, null, position, playbackCursor);
            
            libAdapter.setNowPlaying(playbackservice.NowPlayingFile());
            libAdapter.notifyDataSetChanged();
        }
    }
    
    public void setAdaptersNowPlaying(String nowPlaying)
    {
        if(fileAdapter != null)
        {
            fileAdapter.setNowPlaying(nowPlaying);
            fileAdapter.notifyDataSetChanged();
        }
        
        if(libAdapter != null)
        {
            libAdapter.setNowPlaying(nowPlaying);
            libAdapter.notifyDataSetChanged();
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
