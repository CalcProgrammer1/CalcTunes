package com.calcprogrammer1.calctunes.SourceTypes;

public class SubsonicSource
{
    public static int SUBSONIC_OK = 1;
    public static int SUBSONIC_UNAVAILABLE = 2;
    
    //Status
    public int status;
    
    //Filename of library XML
    public String filename = new String();
    
    //Library Name
    public String name = new String();
    
    //Server Address
    public String address = new String();
    
    //Server Port
    public int port;
    
    //Server Username
    public String username = new String();
    
    //Server Password
    public String password = new String();
    
    //Server Download Path
    public String downloadPath = new String();
    
    //Server Streaming Format
    public String streamingFormat = new String();
    
    //Server Streaming Bitrate
    public String streamingBitrate = new String();
}
