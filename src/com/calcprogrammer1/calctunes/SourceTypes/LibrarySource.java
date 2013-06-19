package com.calcprogrammer1.calctunes.SourceTypes;

import java.util.ArrayList;

public class LibrarySource
{
    public static int LIBRARY_OK = 1;
    public static int LIBRARY_UNAVAILABLE = 2;
    public static int LIBRARY_UPDATING = 3;
    public static int LIBRARY_OFFLINE = 4;
    
    //Status
    public int status;
    
    //Filename of library XML
    public String filename;
    
    //Library Name
    public String name;
    
    //Library Folders
    public ArrayList<String> folders = new ArrayList<String>();
}
