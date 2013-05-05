package com.calcprogrammer1.calctunes.Subsonic;

public class SubsonicConnection
{
    private String      url         = "";
    private String      user        = "";
    private String      password    = "";
    private boolean     available   = false;
    private boolean     licensed    = false;
    
    private SubsonicAPI sub;
    
    public SubsonicConnection(String ur, String usr, String passwd)
    {
        url         = ur;
        user        = usr;
        password    = passwd;
        
        sub         = new SubsonicAPI(url, user, password);
    }
    
    public boolean updateStatus()
    {
        // Start by pinging the server to see if it is alive
        if(sub.SubsonicPing())
        {
            available = true;
            // Check if the server wants to allow API connections
            if(sub.SubsonicGetLicense())
            {
                licensed = true;
                return true;
            }
            else
            {
                licensed = false;
            }
        }
        else
        {
            available = false;
        }
        return false;
    }
    
    
}
