package com.calcprogrammer1.calctunes.Subsonic;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.util.Log;

public class SubsonicAPI
{
    //Class to store "musicFolder" elements
    public class SubsonicMusicFolder
    {
        public int id;
        public String name;
    }
    
    //Class to store "directory" elements
    public class SubsonicDirectory
    {
        public int id;
        public String name;
        public ArrayList<SubsonicDirectoryChild> children;
    }
    
    //Class to store "directory/child" elements
    public class SubsonicDirectoryChild
    {
        public int id;
        public String title;
        public String artist;
        public boolean isDir;
    }
    
    //Class to store "artist" elements
    public class SubsonicArtist
    {
        public int id;
        public String name;
    }
    
    //Class to store "album" elements
    public class SubsonicAlbum
    {
        public int id;
        public String name;
        public String artist;
        public int artistId;
        public int songCount;
        public int duration;
    }
    
    //Class to store "song" elements
    public class SubsonicSong
    {
        public int id;
        public int parent;
        public String title;
        public String album;
        public String artist;
        public boolean isDir;
        public int coverArt;
        public int duration;
        public int bitRate;
        public int track;
        public int year;
        public String genre;
        public int size;
        public String suffix;
        public String contentType;
        public boolean isVideo;
        public String path;
        public int albumId;
        public int artistId;
        public String type;
        public String transcodedSuffix;
        public String transcodedContentType;
    }
    
    //Subsonic protocol user and server parameters
    private String ServerURL;
    private String UserName;
    private String UserPass;
    private String AppName = "CalcTunes";
    private String APIVersion = "1.8.0";
    
    //String for building HTTP requests
    private String HTTPRequest;
    
    //String to store received XML data
    private String XMLData;
    
    //Document object to parse XML data into
    private Document DocData;
    
    //Node List to store XML node items
    private NodeList NodeData;
       
    public SubsonicAPI(String server, String username, String password)
    {
        ServerURL = server;
        UserName = username;
        UserPass = password;
    }
    
    private String buildHTTPRequest(String method)
    {
        return "http://" + ServerURL + "/rest/" + method + ".view?u=" + UserName + "&p=" + UserPass + "&v=" + APIVersion + "&c=" + AppName;
    }

    //return a boolean value from a node list
    public Boolean getNamedBoolean(NodeList data, int id, String item)
    {
        Boolean retVal = false;
        try
        {
            retVal = Boolean.parseBoolean(data.item(id).getAttributes().getNamedItem(item).getNodeValue());
        }
        catch(Exception e)
        {
            Log.d("SubsonicAPI", "Warning: no node " + item + " at node ID " + id + "!");
        }
        return retVal;
    }
    
    //return an integer value from a node list
    public int getNamedInteger(NodeList data, int id, String item)
    {
        int retVal = 0;
        try
        {
            retVal = Integer.parseInt(data.item(id).getAttributes().getNamedItem(item).getNodeValue());
        }
        catch(Exception e)
        {
            Log.d("SubsonicAPI", "Warning: no node " + item + " at node ID " + id + "!");
        }
        return retVal;
    }
    
    //return a string value from a node list
    public String getNamedString(NodeList data, int id, String item)
    {
        String retVal = "";
        try
        {
            retVal = data.item(id).getAttributes().getNamedItem(item).getNodeValue();
        }
        catch(Exception e)
        {
            Log.d("SubsonicAPI", "Warning: no node " + item + " at node ID " + id + "!");
        }
        return retVal;
    }

    //Ping the Subsonic server
    //  - Returns TRUE on success
    //  - Returns FALSE on failure
    public boolean SubsonicPing()
    {
        HTTPRequest = buildHTTPRequest("ping");
        XMLData = CalcTunesXMLParser.getXmlFromUrl(HTTPRequest);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("subsonic-response");
        if(NodeData.getLength() == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    //Get the Subsonic server license
    //  - I recommend using the Supersonic fork which doesn't require a license, but this is an unfortunate
    //  part of an otherwise great protocol.  CalcTunes will always be *free*!
    //
    //  - Since the license "feature" is crap anyways, we just want to check the validity to ensure the
    //  server is going to give us music!  If not, let the user know there's a free server option!
    public boolean SubsonicGetLicense()
    {
        HTTPRequest = buildHTTPRequest("getLicense");
        XMLData = CalcTunesXMLParser.getXmlFromUrl(HTTPRequest);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("subsonic-response");
        if(NodeData.getLength() == 1)
        {
            NodeData = DocData.getElementsByTagName("license");
            if(NodeData.getLength() == 1)
            {
                if(getNamedBoolean(NodeData, 0, "valid"))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    //Get all top-level music folders
    public ArrayList<SubsonicMusicFolder> SubsonicGetMusicFolders()
    {
        HTTPRequest = buildHTTPRequest("getMusicFolders");
        XMLData = CalcTunesXMLParser.getXmlFromUrl(HTTPRequest);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("subsonic-response");
        if(NodeData.getLength() == 1)
        {
            NodeData = DocData.getElementsByTagName("musicFolder");
            ArrayList<SubsonicMusicFolder> musicFolders = new ArrayList<SubsonicMusicFolder>();
            for(int i = 0; i < NodeData.getLength(); i++)
            {
                SubsonicMusicFolder musicFolder = new SubsonicMusicFolder();
                musicFolder.id   = getNamedInteger(NodeData, i, "id"    );
                musicFolder.name = getNamedString (NodeData, i, "name"  );
                musicFolders.add(musicFolder);
            }
            return musicFolders;
        }
        return null;
    }
    
    //Get all artists
    public ArrayList<SubsonicArtist> SubsonicGetIndexes()
    {
        HTTPRequest = buildHTTPRequest("getIndexes");
        XMLData = CalcTunesXMLParser.getXmlFromUrl(HTTPRequest);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("subsonic-response");
        if(NodeData.getLength() == 1)
        {
            ArrayList<SubsonicArtist> artists = new ArrayList<SubsonicArtist>();
            NodeData = DocData.getElementsByTagName("artist");
            for(int i = 0; i < NodeData.getLength(); i++)
            {
                SubsonicArtist artist = new SubsonicArtist();
                artist.id   = getNamedInteger(NodeData, i, "id"     );
                artist.name = getNamedString (NodeData, i, "name"   );
                artists.add(artist);
            }
            return artists;
        }
        return null;
    }

    //Get all artists
    public ArrayList<SubsonicArtist> SubsonicGetArtists()
    {
        HTTPRequest = buildHTTPRequest("getArtists");
        XMLData = CalcTunesXMLParser.getXmlFromUrl(HTTPRequest);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("subsonic-response");
        if(NodeData.getLength() == 1)
        {
            ArrayList<SubsonicArtist> artists = new ArrayList<SubsonicArtist>();
            NodeData = DocData.getElementsByTagName("artist");
            for(int i = 0; i < NodeData.getLength(); i++)
            {
                SubsonicArtist artist = new SubsonicArtist();
                artist.id   = getNamedInteger(NodeData, i, "id"     );
                artist.name = getNamedString (NodeData, i, "name"   );
                artists.add(artist);
            }
            return artists;
        }
        return null;
    }

    //Get artist's albums
    public ArrayList<SubsonicAlbum> SubsonicGetArtist(int id)
    {
        HTTPRequest = buildHTTPRequest("getArtist") + "&id=" + id;
        XMLData = CalcTunesXMLParser.getXmlFromUrl(HTTPRequest);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("subsonic-response");
        if(NodeData.getLength() == 1)
        {
            ArrayList<SubsonicAlbum> albums = new ArrayList<SubsonicAlbum>();
            NodeData = DocData.getElementsByTagName("album");
            for(int i = 0; i < NodeData.getLength(); i++)
            {
                SubsonicAlbum album = new SubsonicAlbum();
                album.id        = getNamedInteger(NodeData, i, "id"         );
                album.name      = getNamedString (NodeData, i, "name"       );
                album.artist    = getNamedString (NodeData, i, "artist"     );
                album.artistId  = getNamedInteger(NodeData, i, "artistId"   );
                album.songCount = getNamedInteger(NodeData, i, "songCount"  );
                album.duration  = getNamedInteger(NodeData, i, "duration"   );
                albums.add(album);
            }
            return albums;
        }
        return null;
    }
    
    //Get album's songs
    public ArrayList<SubsonicSong> SubsonicGetAlbum(int id)
    {
        HTTPRequest = buildHTTPRequest("getAlbum") + "&id=" + id;
        XMLData = CalcTunesXMLParser.getXmlFromUrl(HTTPRequest);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("subsonic-response");
        if(NodeData.getLength() == 1)
        {
            ArrayList<SubsonicSong> songs = new ArrayList<SubsonicSong>();
            NodeData = DocData.getElementsByTagName("song");
            for(int i = 0; i < NodeData.getLength(); i++)
            {
                SubsonicSong song = new SubsonicSong();
                song.id         = getNamedInteger(NodeData, i, "id"         );
                song.parent     = getNamedInteger(NodeData, i, "parent"     );
                song.title      = getNamedString (NodeData, i, "title"      );
                song.album      = getNamedString (NodeData, i, "album"      );
                song.artist     = getNamedString (NodeData, i, "artist"     );
                song.isDir      = getNamedBoolean(NodeData, i, "isDir"      );
                song.coverArt   = getNamedInteger(NodeData, i, "coverArt"   );
                song.duration   = getNamedInteger(NodeData, i, "duration"   );
                song.bitRate    = getNamedInteger(NodeData, i, "bitRate"    );
                song.track      = getNamedInteger(NodeData, i, "track"      );
                song.year       = getNamedInteger(NodeData, i, "year"       );
                song.genre      = getNamedString (NodeData, i, "genre"      );
                song.size       = getNamedInteger(NodeData, i, "size"       );
                song.suffix     = getNamedString (NodeData, i, "suffix"     );
                song.contentType= getNamedString (NodeData, i, "contentType");
                song.path       = getNamedString (NodeData, i, "path"       );
                song.isVideo    = getNamedBoolean(NodeData, i, "isVideo"    );
                song.albumId    = getNamedInteger(NodeData, i, "albumId"    );
                song.artistId   = getNamedInteger(NodeData, i, "artistId"   );
                
                songs.add(song);
            }
            return songs;
        }
        return null;
    }
    
    public SubsonicSong SubsonicGetSong(int id)
    {
        HTTPRequest = buildHTTPRequest("getSong") + "&id=" + id;
        XMLData = CalcTunesXMLParser.getXmlFromUrl(HTTPRequest);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("subsonic-response");
        if(NodeData.getLength() == 1)
        {
            NodeData = DocData.getElementsByTagName("song");
            
            SubsonicSong song = new SubsonicSong();
            song.id         = getNamedInteger(NodeData, 0, "id"         );
            song.parent     = getNamedInteger(NodeData, 0, "parent"     );
            song.title      = getNamedString (NodeData, 0, "title"      );
            song.album      = getNamedString (NodeData, 0, "album"      );
            song.artist     = getNamedString (NodeData, 0, "artist"     );
            song.isDir      = getNamedBoolean(NodeData, 0, "isDir"      );
            song.coverArt   = getNamedInteger(NodeData, 0, "coverArt"   );
            song.duration   = getNamedInteger(NodeData, 0, "duration"   );
            song.bitRate    = getNamedInteger(NodeData, 0, "bitRate"    );
            song.track      = getNamedInteger(NodeData, 0, "track"      );
            song.year       = getNamedInteger(NodeData, 0, "year"       );
            song.genre      = getNamedString (NodeData, 0, "genre"      );
            song.size       = getNamedInteger(NodeData, 0, "size"       );
            song.suffix     = getNamedString (NodeData, 0, "suffix"     );
            song.contentType= getNamedString (NodeData, 0, "contentType");
            song.path       = getNamedString (NodeData, 0, "path"       );
            song.isVideo    = getNamedBoolean(NodeData, 0, "isVideo"    );
            song.albumId    = getNamedInteger(NodeData, 0, "albumId"    );
            song.artistId   = getNamedInteger(NodeData, 0, "artistId"   );
            return song;
        }
       return null;
    }

    public void SubsonicStream(int id)
    {
        HTTPRequest = buildHTTPRequest("stream") + "&id=" + id;
        XMLData = CalcTunesXMLParser.getFileFromUrl(HTTPRequest, "/storage/sdcard1/calctunes/subsonic_test.ogg");
    }
    
    public void SubsonicStream(int id, String format)
    {
        HTTPRequest = buildHTTPRequest("stream") + "&id=" + id + "&format=" + format;
        XMLData = CalcTunesXMLParser.getFileFromUrl(HTTPRequest, "/storage/sdcard1/calctunes/subsonic_test.ogg");
    }
    
    public void SubsonicStream(int id, int maxBitRate)
    {
        HTTPRequest = buildHTTPRequest("stream") + "&id=" + id + "&maxBitRate=" + maxBitRate;
        XMLData = CalcTunesXMLParser.getFileFromUrl(HTTPRequest, "/storage/sdcard1/calctunes/subsonic_test.ogg");
    }
    
    public void SubsonicStream(int id, int maxBitRate, String format)
    {
        HTTPRequest = buildHTTPRequest("stream") + "&id=" + id + "&maxBitRate=" + maxBitRate + "&format=" + format;
        XMLData = CalcTunesXMLParser.getFileFromUrl(HTTPRequest, "/storage/sdcard1/calctunes/subsonic_test.ogg");
    }
    
    public void SubsonicDownload(int id)
    {
        HTTPRequest = buildHTTPRequest("download") + "&id=" + id;
        XMLData = CalcTunesXMLParser.getFileFromUrl(HTTPRequest, "/storage/sdcard1/calctunes/subsonic_dl_test.flac");
    }
    
    public SubsonicDirectory SubsonicGetMusicDirectory(int id)
    {
        HTTPRequest = buildHTTPRequest("getMusicDirectory") + "&id=" + id;
        XMLData = CalcTunesXMLParser.getXmlFromUrl(HTTPRequest);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("subsonic-response");
        if(NodeData.getLength() == 1)
        {
            SubsonicDirectory directory = new SubsonicDirectory();
            directory.children = new ArrayList<SubsonicDirectoryChild>();
            
            //Get directory name and ID
            NodeData = DocData.getElementsByTagName("directory");
            directory.id   = getNamedInteger(NodeData, 0, "id"  );
            directory.name = getNamedString (NodeData, 0, "name");
            
            //Get children
            NodeData = DocData.getElementsByTagName("child");
            for(int i = 0; i < NodeData.getLength(); i++)
            {
                SubsonicDirectoryChild child = new SubsonicDirectoryChild();
                child.id       = getNamedInteger(NodeData, i, "id"      );
                child.title    = getNamedString (NodeData, i, "title"   );
                child.artist   = getNamedString (NodeData, i, "artist"  );
                child.isDir    = getNamedBoolean(NodeData, i, "isDir"   );
                
                directory.children.add(child);
            }
            return directory;
        }
        return null;
    }
}
