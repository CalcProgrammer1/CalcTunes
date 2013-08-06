package com.calcprogrammer1.calctunes.Interfaces;

public interface SubsonicConnectionCallback
{
    public void onListUpdated();
    public void onTrackLoaded(int id, String filename);
}
