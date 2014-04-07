package com.calcprogrammer1.calctunes.ContentLibraryFragment;

public class ContentListElement
{
    public final static int  LIBRARY_LIST_TYPE_HEADING   = 0;
    public final static int  LIBRARY_LIST_TYPE_ALBUM     = 1;
    public final static int  LIBRARY_LIST_TYPE_TRACK     = 2;
    
    public final static int  CACHE_NONE                  = 0;
    public final static int  CACHE_SDCARD_TRANSCODED     = 1;
    public final static int  CACHE_SDCARD_ORIGINAL       = 2;
    public final static int  CACHE_DOWNLOADING           = 3;
    
    public boolean expanded;
    public int     level;
    public int     type;
        
    public String  artist;
    public String  album;
    public String  title;
    public String  year;
    public int     track;
    public int     time;
    public long    id;
    public String  origExt;
    public String  origPath;
    public String  transExt;
    public String  transPath;
    public int     cache;
}
