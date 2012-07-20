package com.calcprogrammer1.calctunes.Library;

public class libraryListElement
{
    public static int LIBRARY_OK = 1;
    public static int LIBRARY_UNAVAILABLE = 2;
    public static int LIBRARY_UPDATING = 3;
    public static int LIBRARY_OFFLINE = 4;
    
    public int status;
    public String filename;
    public String name;
}
