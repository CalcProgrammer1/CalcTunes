package com.calcprogrammer1.calctunes.Subsonic;

import java.io.File;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;

import com.calcprogrammer1.calctunes.ContentLibraryFragment.ContentListElement;
import com.calcprogrammer1.calctunes.Interfaces.SubsonicConnectionCallback;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.SourceTypes.SubsonicSource;

@SuppressWarnings("unused")

public class SubsonicConnection
{
    private String      url         = "";
    private String      user        = "";
    private String      password    = "";
    private String      transfrmt   = "mp3";
    private String      transpath   = "";
    private String      origpath    = "";

    private int         transbtrt   = 192;
    private boolean     available   = false;
    private boolean     licensed    = false;
    public SubsonicAPI subsonicapi;
    
    private SubsonicConnectionCallback callback;
    
    public ArrayList<ContentListElement> listData;
    
    public SubsonicConnection(String ur, String usr, String passwd)
    {
        url         = ur;
        user        = usr;
        password    = passwd;

        subsonicapi = new SubsonicAPI(url, user, password);
    }
    
    public SubsonicConnection(String subsonicsourcefilename)
    {
        SubsonicSource source = SourceListOperations.readSubsonicFile(subsonicsourcefilename);
        
        url         = source.address + ":" + source.port;
        user        = source.username;
        password    = source.password;
        transpath   = source.transPath;
        origpath    = source.origPath;

        transfrmt   = source.streamingFormat;
        transbtrt   = Integer.parseInt(source.streamingBitrate);

        subsonicapi = new SubsonicAPI(url, user, password);

        updateStatusAsync();
    }
    
    public void SetCallback(SubsonicConnectionCallback call)
    {
        callback = call;
    }

    public void registerDownloadReceiver(Context con)
    {
        con.registerReceiver(subsonicDownloadReceiver, new IntentFilter("com.calcprogrammer1.calctunes.SUBSONIC_DOWNLOADED_EVENT"));
    }
    
    private BroadcastReceiver subsonicDownloadReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();
            int id = extras.getInt("id");
            boolean transcode = extras.getBoolean("transcode");
            boolean finished  = extras.getBoolean("finished");
            for (int i = 0; i < listData.size(); i++)
            {
                if (listData.get(i).id == id)
                {
                    if(!finished)
                    {
                        listData.get(i).cache = ContentListElement.CACHE_DOWNLOADING;
                    }
                    else if(transcode)
                    {
                        listData.get(i).cache = ContentListElement.CACHE_SDCARD_TRANSCODED;
                    }
                    else
                    {
                        listData.get(i).cache = ContentListElement.CACHE_SDCARD_ORIGINAL;
                    }
                }
            }
            callback.onListUpdated();
        }
    };
    
    public boolean updateStatus()
    {
        // Start by pinging the server to see if it is alive
        if(subsonicapi.SubsonicPing())
        {
            available = true;
            // Check if the server wants to allow API connections
            if(subsonicapi.SubsonicGetLicense())
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
    
    public void updateStatusAsync()
    {
        new updateStatusTask().execute();
    }
    
    public class updateStatusTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params)
        {
            updateStatus();
            return null;
        }
    }
    
    public void getArtistList()
    {
        //Load list of artists
        listData = new ArrayList<ContentListElement>();
        
        ArrayList<SubsonicAPI.SubsonicArtist> artists = subsonicapi.SubsonicGetArtists();
        
        for(int i = 0; i < artists.size(); i++)
        {
            ContentListElement newElement = new ContentListElement();
            
            newElement.type   = ContentListElement.LIBRARY_LIST_TYPE_HEADING;
            newElement.artist = artists.get(i).name;
            newElement.id     = artists.get(i).id;
            
            listData.add(newElement);
        }
    }
    
    public void getArtistListAsync()
    {
        new getArtistListTask().execute();
    }
    
    public class getArtistListTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params)
        {
            getArtistList();
            return null;
        }
        
        protected void onPostExecute(Void result)
        {
            callback.onListUpdated();
        }
    };
    
    public void expandArtist(int position)
    {
        ArrayList<SubsonicAPI.SubsonicAlbum> albums = subsonicapi.SubsonicGetArtist((int)listData.get(position).id);
        for(int i = 0; i < albums.size(); i++)
        {
            ContentListElement newElement = new ContentListElement();
            
            newElement.type   = ContentListElement.LIBRARY_LIST_TYPE_ALBUM;
            newElement.artist = listData.get(position).artist;
            newElement.album  = albums.get(i).name;
            //newElement.year   = albums.get(i).year;
            newElement.id     = albums.get(i).id;
            
            listData.add(position + (i + 1), newElement);
        }
        listData.get(position).expanded = true;
    }
    
    public void expandArtistAsync(int position)
    {
        new expandArtistTask().execute(position);
    }
    
    public class expandArtistTask extends AsyncTask<Integer, Void, Void>{
        @Override
        protected Void doInBackground(Integer... params)
        {
            expandArtist(params[0]);
            return null;
        }
        
        protected void onPostExecute(Void result)
        {
            callback.onListUpdated();
        }
    }
    
    public void collapseArtist(int position)
    {
        while(true)
        {
            if((position + 1 < listData.size()) &&
              (listData.get(position + 1).type == ContentListElement.LIBRARY_LIST_TYPE_ALBUM
            || listData.get(position + 1).type == ContentListElement.LIBRARY_LIST_TYPE_TRACK ))
            {
                listData.remove(position + 1);
            }
            else
            {
                break;
            }
        }
        listData.get(position).expanded = false;   
    }

    public void deleteCachedFile(int position)
    {
        switch(listData.get(position).cache)
        {
            case ContentListElement.CACHE_SDCARD_TRANSCODED:
                {
                    File deleteFile = new File(listData.get(position).transPath + "." + listData.get(position).transExt);
                    deleteFile.delete();
                    listData.get(position).cache = ContentListElement.CACHE_NONE;
               }
            break;

            case ContentListElement.CACHE_SDCARD_ORIGINAL:
                {
                    File deleteFile = new File(listData.get(position).origPath + "." + listData.get(position).origExt);
                    deleteFile.delete();
                    listData.get(position).cache = ContentListElement.CACHE_NONE;
                }
            break;
        }
    }
    
    public void expandAlbum(int position)
    {
        ArrayList<SubsonicAPI.SubsonicSong> songs = subsonicapi.SubsonicGetAlbum((int)listData.get(position).id);
        for(int i = 0; i < songs.size(); i++)
        {
            ContentListElement newElement = new ContentListElement();
            
            newElement.type      = ContentListElement.LIBRARY_LIST_TYPE_TRACK;
            newElement.artist    = listData.get(position).artist;
            newElement.year      = listData.get(position).year;
            newElement.album     = songs.get(i).album;
            newElement.title     = songs.get(i).title;
            newElement.track     = songs.get(i).track;
            newElement.time      = songs.get(i).duration;
            newElement.id        = songs.get(i).id;
            newElement.cache     = ContentListElement.CACHE_NONE;
            newElement.origExt   = songs.get(i).suffix;
            newElement.origPath  = origpath + "/" + SourceListOperations.makeFilename(songs.get(i).artist) + "/" + SourceListOperations.makeFilename(songs.get(i).album)
                                  + "/" + String.format("%02d", songs.get(i).track) + " " + SourceListOperations.makeFilename(songs.get(i).title);
            newElement.transExt  = transfrmt;
            newElement.transPath = transpath + "/" + SourceListOperations.makeFilename(songs.get(i).artist) + "/" + SourceListOperations.makeFilename(songs.get(i).album)
                                  + "/" + String.format("%02d", songs.get(i).track) + " " + SourceListOperations.makeFilename(songs.get(i).title);

            File testFile = new File( newElement.transPath + "." + newElement.transExt );

            if(testFile.exists())
            {
                newElement.cache = ContentListElement.CACHE_SDCARD_TRANSCODED;
            }
            else
            {
                testFile = new File( newElement.origPath + "." + newElement.origExt );

                if(testFile.exists())
                {
                    newElement.cache = ContentListElement.CACHE_SDCARD_ORIGINAL;
                }
            }
            listData.add(position + (i + 1), newElement);
        }
        listData.get(position).expanded = true;
    }
    
    public void expandAlbumAsync(int position)
    {
        new expandAlbumTask().execute(position);
    }
    
    public class expandAlbumTask extends AsyncTask<Integer, Void, Void>{
        @Override
        protected Void doInBackground(Integer... params)
        {
            expandAlbum(params[0]);
            return null;
        }
        
        protected void onPostExecute(Void result)
        {
            callback.onListUpdated();
        }
    }
    
    public void collapseAlbum(int position)
    {
        while(true)
        {
            if((position + 1 < listData.size()) && (listData.get(position + 1).type == ContentListElement.LIBRARY_LIST_TYPE_TRACK))
            {
                listData.remove(position + 1);
            }
            else
            {
                break;
            }
        }
        listData.get(position).expanded = false; 
    }

    public String streamUrlTranscoded(int position)
    {
        return(subsonicapi.SubsonicStreamURL((int)listData.get(position).id, listData.get(position).transExt, transbtrt));
    }

    public void downloadTranscoded(int position, Context con)
    {
        if(listData.get(position).type == ContentListElement.LIBRARY_LIST_TYPE_HEADING)
        {
            final Context c = con;
            final int pos = position;
            new Thread(new Runnable()
            {
                public void run()
                {
                    ArrayList<SubsonicAPI.SubsonicAlbum> albums = subsonicapi.SubsonicGetArtist(listData.get(pos).id);
                    for(int x = 0; x < albums.size(); x++)
                    {
                        ArrayList<SubsonicAPI.SubsonicSong> songs = subsonicapi.SubsonicGetAlbum(albums.get(x).id);
                        for(int y = 0; y < songs.size(); y++)
                        {
                            Intent i = new Intent(c, SubsonicDownloaderService.class);
                            i.putExtra("url", url);
                            i.putExtra("user", user);
                            i.putExtra("password", password);
                            i.putExtra("id", songs.get(y).id);
                            i.putExtra("transcode", true);
                            i.putExtra("bitRate", transbtrt);
                            i.putExtra("format", transfrmt);
                            i.putExtra("downloadPath", transpath + "/" + SourceListOperations.makeFilename(songs.get(y).artist) + "/" + SourceListOperations.makeFilename(songs.get(y).album)
                                    + "/" + String.format("%02d", songs.get(y).track) + " " + SourceListOperations.makeFilename(songs.get(y).title) + "." + transfrmt);
                            c.startService(i);
                        }
                    }
                }
            }).start();
        }
        if(listData.get(position).type == ContentListElement.LIBRARY_LIST_TYPE_ALBUM)
        {
            final Context c = con;
            final int pos = position;
            new Thread(new Runnable()
            {
                public void run()
                {
                    ArrayList<SubsonicAPI.SubsonicSong> songs = subsonicapi.SubsonicGetAlbum(listData.get(pos).id);
                    for(int x = 0; x < songs.size(); x++)
                    {
                        Intent i = new Intent(c, SubsonicDownloaderService.class);
                        i.putExtra("url",           url);
                        i.putExtra("user",          user);
                        i.putExtra("password",      password);
                        i.putExtra("id",            songs.get(x).id);
                        i.putExtra("transcode",     true);
                        i.putExtra("bitRate",       transbtrt);
                        i.putExtra("format",        transfrmt);
                        i.putExtra("downloadPath",  transpath + "/" + SourceListOperations.makeFilename(songs.get(x).artist) + "/" + SourceListOperations.makeFilename(songs.get(x).album)
                                + "/" + String.format("%02d", songs.get(x).track) + " " + SourceListOperations.makeFilename(songs.get(x).title) + "." + transfrmt);
                        c.startService(i);
                    }
                }
            }).start();
        }
        else if(listData.get(position).type == ContentListElement.LIBRARY_LIST_TYPE_TRACK)
        {
            Intent i = new Intent(con, SubsonicDownloaderService.class);
            i.putExtra("url",           url);
            i.putExtra("user",          user);
            i.putExtra("password",      password);
            i.putExtra("id",            listData.get(position).id);
            i.putExtra("transcode",     true);
            i.putExtra("bitRate",       transbtrt);
            i.putExtra("format",        listData.get(position).transExt);
            i.putExtra("downloadPath",  listData.get(position).transPath + "." + listData.get(position).transExt);
            con.startService(i);
        }
    }
    
    public void downloadOriginal(int position, Context con)
    {
        if(listData.get(position).type == ContentListElement.LIBRARY_LIST_TYPE_HEADING)
        {
            final Context c = con;
            final int pos = position;
            new Thread(new Runnable()
            {
                public void run()
                {
                    ArrayList<SubsonicAPI.SubsonicAlbum> albums = subsonicapi.SubsonicGetArtist(listData.get(pos).id);
                    for(int x = 0; x < albums.size(); x++)
                    {
                        ArrayList<SubsonicAPI.SubsonicSong> songs = subsonicapi.SubsonicGetAlbum(albums.get(x).id);
                        for(int y = 0; y < songs.size(); y++)
                        {
                            Intent i = new Intent(c, SubsonicDownloaderService.class);
                            i.putExtra("url",           url);
                            i.putExtra("user",          user);
                            i.putExtra("password",      password);
                            i.putExtra("id",            songs.get(y).id);
                            i.putExtra("transcode",     false);
                            i.putExtra("bitRate",       0);
                            i.putExtra("format",        "");
                            i.putExtra("downloadPath", origpath + "/" + SourceListOperations.makeFilename(songs.get(y).artist) + "/" + SourceListOperations.makeFilename(songs.get(y).album)
                                    + "/" + String.format("%02d", songs.get(y).track) + " " + SourceListOperations.makeFilename(songs.get(y).title) + "." + songs.get(y).suffix);
                            c.startService(i);
                        }
                    }
                }
            }).start();
        }
        if(listData.get(position).type == ContentListElement.LIBRARY_LIST_TYPE_ALBUM)
        {
            final Context c = con;
            final int pos = position;
            new Thread(new Runnable()
            {
                public void run()
                {
                    ArrayList<SubsonicAPI.SubsonicSong> songs = subsonicapi.SubsonicGetAlbum(listData.get(pos).id);
                    for(int x = 0; x < songs.size(); x++)
                    {
                        Intent i = new Intent(c, SubsonicDownloaderService.class);
                        i.putExtra("url",            url);
                        i.putExtra("user",          user);
                        i.putExtra("password",      password);
                        i.putExtra("id",            songs.get(x).id);
                        i.putExtra("transcode",     false);
                        i.putExtra("bitRate",       0);
                        i.putExtra("format",        "");
                        i.putExtra("downloadPath", origpath + "/" + SourceListOperations.makeFilename(songs.get(x).artist) + "/" + SourceListOperations.makeFilename(songs.get(x).album)
                                + "/" + String.format("%02d", songs.get(x).track) + " " + SourceListOperations.makeFilename(songs.get(x).title) + "." + songs.get(x).suffix);
                        c.startService(i);
                    }
                }
            }).start();
        }
        else if(listData.get(position).type == ContentListElement.LIBRARY_LIST_TYPE_TRACK)
        {
            Intent i = new Intent(con, SubsonicDownloaderService.class);
            i.putExtra("url",           url);
            i.putExtra("user",          user);
            i.putExtra("password",      password);
            i.putExtra("id",            listData.get(position).id);
            i.putExtra("transcode",     false);
            i.putExtra("bitRate",       0);
            i.putExtra("format",        "");
            i.putExtra("downloadPath",  listData.get(position).origPath + "." + listData.get(position).origExt);
            con.startService(i);
        }
    }
}
