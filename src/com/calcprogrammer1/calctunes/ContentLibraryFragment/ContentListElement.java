package com.calcprogrammer1.calctunes.ContentLibraryFragment;

import android.graphics.Bitmap;

public class ContentListElement
{
    public final static int  LIBRARY_LIST_TYPE_HEADING   = 0;
    public final static int  LIBRARY_LIST_TYPE_ALBUM     = 1;
    public final static int  LIBRARY_LIST_TYPE_TRACK     = 2;
    
    public final static int  CACHE_NONE                  = 0;
    public final static int  CACHE_SDCARD                = 1;
    public final static int  CACHE_DOWNLOADING           = 2;
    
    public boolean expanded;
    public int     level;
    public int     type;
        
    public String  artist;
    public String  album;
    public String  song;
    public String  year;
    public int     track;
    public int     time;
    public long    id;
    public String  path;
    public int     cache;
}
