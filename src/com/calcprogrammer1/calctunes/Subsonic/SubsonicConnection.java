package com.calcprogrammer1.calctunes.Subsonic;

import java.io.File;
import java.util.ArrayList;

import android.os.AsyncTask;
import com.calcprogrammer1.calctunes.ContentLibraryFragment.ContentListElement;
import com.calcprogrammer1.calctunes.Interfaces.SubsonicAPICallback;
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
    
    private SubsonicAPI subsonicapi;
    
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
        subsonicapi.SetCallback(subsonic_callback);
        updateStatusAsync();
    }
    
    public void SetCallback(SubsonicConnectionCallback call)
    {
        callback = call;
    }
    
    SubsonicAPICallback subsonic_callback = new SubsonicAPICallback(){
        @Override
        public void onSubsonicDownloadComplete(int id, String filename)
        {
            for(int i = 0; i < listData.size(); i++)
            {
                if( listData.get(i).id == id )
                {
                    listData.get(i).cache = ContentListElement.CACHE_SDCARD_TRANSCODED;
                }
            }
            callback.onListUpdated();
            callback.onTrackLoaded(id, filename);
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
    
    public void downloadTranscodedOgg(int position)
    {
        subsonicapi.SubsonicStreamAsync((int)listData.get(position).id, listData.get(position).transPath + "." + listData.get(position).transExt, transbtrt, listData.get(position).transExt);
        listData.get(position).cache = ContentListElement.CACHE_DOWNLOADING;
        callback.onListUpdated();
    }
    
    public void downloadOriginal(int position)
    {
        subsonicapi.SubsonicDownloadAsync((int)listData.get(position).id, listData.get(position).origPath + "." + listData.get(position).origExt);
        listData.get(position).cache = ContentListElement.CACHE_DOWNLOADING;
        callback.onListUpdated();
    }
}
