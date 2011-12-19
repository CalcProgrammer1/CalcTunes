package com.calcprogrammer1.calctunes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.*;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Xml;

public class LibraryOperations
{
    //saveLibraryFile
    //  Saves a library playlist to a file in the application's data directory
    //  libName - name of library
    //  libData - ArrayList containing all the paths included in the library
    //  libPath - Path to store the library files
    public static void saveLibraryFile(String libName, ArrayList<String> libData, String libPath)
    {
        File libDir = new File(libPath);
        libDir.mkdirs();
        libDir.setExecutable(true, false);
        try
        {
            FileWriter outFile = new FileWriter(libPath+"/"+getLibraryFilename(libName));
            PrintWriter out = new PrintWriter(outFile);
            out.println(libName);
            for(int i=0; i < libData.size(); i++)
            {
                out.println(libData.get(i));
            }
            out.close();
        }catch(Exception e){}
    }
    
    public static ArrayList<String> readLibraryFile(String libFilePath)
    {
        ArrayList<String> libraryFolders = new ArrayList<String>();
        BufferedReader inFile = null;
        
        //Create BufferedReader to read file, include useless try/catch to make Java happy
        try{
            inFile = new BufferedReader(new FileReader(libFilePath));
        }catch(Exception e){}
        
        //First line of library file is name
        try{
            inFile.readLine();
        }catch (Exception e){}
        
        //Loop through reading folder names until EOF
        while(true)
        {
            try
            {
                String filename = inFile.readLine();
                if(filename != null)
                {
                    libraryFolders.add(filename);
                }
                else
                {
                    break;
                }
            }
            catch(IOException e)
            {
                break;
            }
        }
        return libraryFolders;
    }
    
    public static String readLibraryName(String libFilePath)
    {
        BufferedReader inFile = null;
        String name = null;
        
        try{
            
            inFile = new BufferedReader(new FileReader(libFilePath));
            name = inFile.readLine();
            inFile.close();
            
        }catch (Exception e){}
        
        return name;
    }
    
    public static void scanMediaIntoDatabase(Context c, String libFilePath)
    {
        ArrayList<String> libraryFolders = readLibraryFile(libFilePath);
        String libName = readLibraryName(libFilePath);
        ArrayList<File> libFiles = new ArrayList<File>();
        
        for(int i = 0; i < libraryFolders.size(); i++)
        {
            File f = new File(libraryFolders.get(i));
            FileOperations.addFilesRecursively(f, libFiles);
        }
        
        writeMediaDatabase(c, libFiles.toArray(new File[libFiles.size()]), libName);
    }
    
    public static ArrayList<libraryElementArtist> readLibraryData(Context c, String libFilePath)
    {
        ArrayList<libraryElementArtist> libData = new ArrayList<libraryElementArtist>();
        String libName = readLibraryName(libFilePath);
        libData = readMediaDatabase(c, libName);
        return libData;
    }
    
    public static ArrayList<libraryListElement> readLibraryList(String libPath)
    {
        try
        {
            File libDir = new File(libPath);
            File[] libFiles = FileOperations.selectFilesOnly(libDir.listFiles());
            ArrayList<libraryListElement> libData = new ArrayList<libraryListElement>();
            for(int i = 0; i < libFiles.length; i++)
            {
                BufferedReader inFile = new BufferedReader(new FileReader(libFiles[i].getAbsolutePath()));
                String name = inFile.readLine();
                libraryListElement newLib = new libraryListElement();
                newLib.name = name;
                newLib.filename = libFiles[i].getAbsolutePath();
                libData.add(newLib);
            }
            return libData;
        }
        catch(Exception e)
        {
            return new ArrayList<libraryListElement>();
        }
    }
    
    public static String[] getNamesFromList(ArrayList<libraryListElement> libraryList)
    {
        ArrayList<String> names = new ArrayList<String>();
        
        for(int i = 0; i < libraryList.size(); i++)
        {
            names.add(libraryList.get(i).name);
        }
        return names.toArray(new String[names.size()]);
    }
    
    public static AudioFile readAudioFileReadOnly(File inFile)
    {
        AudioFile f = null;
        try
        {

                f = AudioFileIO.read(inFile);
        }
        catch(Exception e)
        {
                try
                {
                    f = new MP3File(inFile, MP3File.LOAD_IDV1TAG, true);
                }catch (Exception ex){}
        }
        return f;
    }
    
    public static ArrayList<libraryElementArtist> scanMedia(File[] files)
    {
        ArrayList<libraryElementArtist> libraryData = new ArrayList<libraryElementArtist>();
        for(int i = 0; i < files.length; i++)
        {
            AudioFile f;
            try
            {
                f = readAudioFileReadOnly(files[i]);
                Tag tag = f.getTag();
                int song_length = f.getAudioHeader().getTrackLength();
                String song_artist = tag.getFirstArtist();
                String song_album = tag.getFirstAlbum();
                String song_title = tag.getFirstTitle();
                String song_year = tag.getFirstYear();
                String song_num = tag.getFirstTrack();
                
                int artistIndex = -1;
                int newArtistIndex = 0;
                int j = 0;
                
                //check if artist exists
                for(; j < libraryData.size(); j++)
                {
                    if(libraryData.get(j).name.equals(song_artist))
                    {
                        artistIndex = j;
                    }
                    else if(song_artist.compareTo(libraryData.get(j).name) > 0)
                    {
                        newArtistIndex = j+1;
                    }
                }
                //if artist not in list
                if(artistIndex == -1)
                {
                    libraryElementArtist newEntry = new libraryElementArtist();
                    newEntry.name = song_artist;
                    newEntry.albums = new ArrayList<libraryElementAlbum>();
                    libraryData.add(newArtistIndex, newEntry);
                    artistIndex = newArtistIndex;
                }
                
                //check if album exists
                int albumIndex = -1;
                int newAlbumIndex = 0;
                j = 0; 
                for(; j < libraryData.get(artistIndex).albums.size(); j++)
                {
                    if(libraryData.get(artistIndex).albums.get(j).name.equals(song_album))
                    {
                        albumIndex = j;
                    }
                    else if(song_album.compareTo(libraryData.get(artistIndex).albums.get(j).name) > 0)
                    {
                        newAlbumIndex = j+1;
                    }
                }
                if(albumIndex == -1)
                {
                    libraryElementAlbum newEntry = new libraryElementAlbum();
                    newEntry.name = song_album;
                    newEntry.year = song_year;
                    newEntry.songs = new ArrayList<libraryElementSong>();
                    libraryData.get(artistIndex).albums.add(newAlbumIndex, newEntry);
                    albumIndex = newAlbumIndex;
                }
                
                //check if song exists
                int songIndex = -1;
                int newSongIndex = 0;
                j = 0;
                for(; j < libraryData.get(artistIndex).albums.get(albumIndex).songs.size(); j++)
                {
                    if(libraryData.get(artistIndex).albums.get(albumIndex).songs.get(j).name.equals(song_title))
                    {
                        songIndex = j;
                    }
                    if(Integer.parseInt(song_num) > libraryData.get(artistIndex).albums.get(albumIndex).songs.get(j).num)
                    {
                        newSongIndex = j+1;
                    }
                }
                if(songIndex == -1)
                {
                    libraryElementSong newEntry = new libraryElementSong();
                    newEntry.name = song_title;
                    newEntry.filename = files[i].getAbsolutePath();
                    newEntry.num = Integer.parseInt(song_num);
                    newEntry.length = song_length;
                    libraryData.get(artistIndex).albums.get(albumIndex).songs.add(newSongIndex, newEntry);
                }
            }
            catch(Exception e)
            {
                
            }
        }
        return libraryData;
    }
    
    public static void organizeFiles(String rootDir, ArrayList<libraryElementArtist> libraryData)
    {
        for(int i = 0; i < libraryData.size(); i++)
        {
            //create artist folder
            File artistDir = new File(rootDir + File.separator + libraryData.get(i).name);
            artistDir.mkdir();
            artistDir.setExecutable(true, false);
            for(int j = 0; j < libraryData.get(i).albums.size(); j++)
            {
                //create album folder
                File albumDir = new File(rootDir + File.separator + libraryData.get(i).name + File.separator + libraryData.get(i).albums.get(j).name);
                albumDir.mkdir();
                albumDir.setExecutable(true, false);
                for(int k = 0; k < libraryData.get(i).albums.get(j).songs.size(); k++)
                {
                    //move song into folder
                    FileOperations.moveFile(libraryData.get(i).albums.get(j).songs.get(k).filename, rootDir + "/" + libraryData.get(i).name + "/" + libraryData.get(i).albums.get(j).name);
                }
            }
        }
    }
    
    public static String formatTime(int duration)
    {
        String time = "";
        int mins = duration / 60;
        int secs = duration % 60;
        time = mins + ":" + String.format("%02d", secs);
        return time;
    }

    public static libraryElementGeneric getNextSong(libraryElementGeneric currentSong, ArrayList<libraryElementArtist> currentLibrary)
    {
        libraryElementGeneric nextSong = currentSong;
        if(currentSong.songIndex < (currentLibrary.get(currentSong.artistIndex).albums.get(currentSong.albumIndex).songs.size()-1))
        {
            nextSong = new libraryElementGeneric();
            nextSong.type = "song";
            nextSong.artistIndex = currentSong.artistIndex;
            nextSong.albumIndex = currentSong.albumIndex;
            nextSong.songIndex = currentSong.songIndex + 1;
            nextSong.song = currentLibrary.get(nextSong.artistIndex).albums.get(nextSong.albumIndex).songs.get(nextSong.songIndex);
        }
        else if(currentSong.albumIndex < (currentLibrary.get(currentSong.artistIndex).albums.size()-1))
        {
            nextSong = new libraryElementGeneric();
            nextSong.type = "song";
            nextSong.artistIndex = currentSong.artistIndex;
            nextSong.albumIndex = currentSong.albumIndex + 1;
            nextSong.songIndex = 0;
            nextSong.song = currentLibrary.get(nextSong.artistIndex).albums.get(nextSong.albumIndex).songs.get(nextSong.songIndex);
        }
        else if(currentSong.artistIndex < (currentLibrary.size()-1))
        {
            nextSong = new libraryElementGeneric();
            nextSong.type = "song";
            nextSong.artistIndex = currentSong.artistIndex + 1;
            nextSong.albumIndex = 0;
            nextSong.songIndex = 0;
            nextSong.song = currentLibrary.get(nextSong.artistIndex).albums.get(nextSong.albumIndex).songs.get(nextSong.songIndex);
        }
        return nextSong;
    }
    
    public static libraryElementGeneric getPrevSong(libraryElementGeneric currentSong, ArrayList<libraryElementArtist> currentLibrary)
    {
        libraryElementGeneric prevSong = currentSong;
        if(currentSong.songIndex > 0)
        {
            prevSong = new libraryElementGeneric();
            prevSong.type = "song";
            prevSong.artistIndex = currentSong.artistIndex;
            prevSong.albumIndex = currentSong.albumIndex;
            prevSong.songIndex = currentSong.songIndex - 1;
            prevSong.song = currentLibrary.get(prevSong.artistIndex).albums.get(prevSong.albumIndex).songs.get(prevSong.songIndex);
        }
        else if(currentSong.albumIndex > 0)
        {
            prevSong = new libraryElementGeneric();
            prevSong.type = "song";
            prevSong.artistIndex = currentSong.artistIndex;
            prevSong.albumIndex = currentSong.albumIndex - 1;
            prevSong.songIndex = currentLibrary.get(prevSong.artistIndex).albums.get(prevSong.albumIndex).songs.size() - 1;
            prevSong.song = currentLibrary.get(prevSong.artistIndex).albums.get(prevSong.albumIndex).songs.get(prevSong.songIndex);
        }
        else if(currentSong.artistIndex > 0)
        {
            prevSong = new libraryElementGeneric();
            prevSong.type = "song";
            prevSong.artistIndex = currentSong.artistIndex -1;
            prevSong.albumIndex = currentLibrary.get(prevSong.artistIndex).albums.size() - 1;
            prevSong.songIndex = currentLibrary.get(prevSong.artistIndex).albums.get(prevSong.albumIndex).songs.size() - 1;
            prevSong.song = currentLibrary.get(prevSong.artistIndex).albums.get(prevSong.albumIndex).songs.get(prevSong.songIndex);
        }
        return prevSong;
    }
    
    public static String getLibraryPath(Context c)
    {
        return c.getApplicationContext().getExternalFilesDir(null).getPath() + "/libraries";
    }
    
    public static String getLibraryFilename(String libName)
    {
        String fileName = "";
        
        try{
            fileName = URLEncoder.encode(libName, "UTF-8");
        }catch(Exception e){}
        
        return fileName + ".txt";
    }
    
    public static String getLibraryFullPath(Context c, String libName)
    {
        return getLibraryPath(c) + "/" + getLibraryFilename(libName);
    }
 
    public static void writeMediaDatabase(Context c, File[] files, String libName)
    {
       LibraryDatabaseHelper db = new LibraryDatabaseHelper(c, libName + ".db");
       db.startDatabase();
       for(int i = 0; i < files.length; i++)
       {
           db.addFileToDatabase(files[i]);
       }
    }
    
    public static ArrayList<libraryElementArtist> readMediaDatabase(Context c, String libName)
    {
        ArrayList<libraryElementArtist> libraryData = new ArrayList<libraryElementArtist>();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.calcprogrammer1.calctunes/databases/" + libName + ".db", null);
        
        Cursor artists = db.rawQuery("SELECT DISTINCT ARTIST FROM MYLIBRARY;", null);

        for(int i = 0; i < artists.getCount(); i++)
        {
            artists.moveToNext();
            
            libraryElementArtist artistData = new libraryElementArtist();
            artistData.name = artists.getString(0);
            Cursor albums = db.rawQuery("SELECT DISTINCT ALBUM FROM MYLIBRARY WHERE ARTIST = '" + artists.getString(0).replaceAll("'", "''") + "';", null);
            for(int j = 0; j < albums.getCount(); j++)
            {
                albums.moveToNext();
                
                libraryElementAlbum albumData = new libraryElementAlbum();
                albumData.name = albums.getString(0);
                Cursor songs = db.rawQuery("SELECT DISTINCT TITLE, PATH, YEAR, TRACK, TIME FROM MYLIBRARY WHERE ARTIST = '" + artists.getString(0).replaceAll("'", "''") + "' AND ALBUM = '" + albums.getString(0).replaceAll("'","''") +"' ORDER BY TRACK;", null);
                for(int k = 0; k < songs.getCount(); k++)
                {
                    songs.moveToNext();
                    
                    libraryElementSong songData = new libraryElementSong();
                    songData.name = songs.getString(0);
                    songData.filename = songs.getString(1);
                    songData.year = songs.getString(2);
                    songData.num = songs.getInt(3);
                    songData.length = songs.getInt(4);
                    albumData.songs.add(songData);
                }
                artistData.albums.add(albumData);
                songs.close();
            }
            libraryData.add(artistData);
            albums.close();
        }
        artists.close();
        return libraryData;
    }
}
