package com.calcprogrammer1.calctunes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.*;

import android.content.Context;

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
            FileWriter outFile = new FileWriter(libPath+"/"+libName+".txt");
            PrintWriter out = new PrintWriter(outFile);
            out.println(libName);
            for(int i=0; i < libData.size(); i++)
            {
                out.println(libData.get(i));
            }
            out.close();
        }catch(Exception e){}
    }
    
    public static ArrayList<libraryElementArtist> readLibraryFile(String libFilePath)
    {
        ArrayList<libraryElementArtist> libData = new ArrayList<libraryElementArtist>();
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
        
        //Recursively loop through directories and build list of files
        ArrayList<File> myFiles = new ArrayList<File>();
        for(int i = 0; i < libraryFolders.size(); i++)
        {
            File f = new File(libraryFolders.get(i));
            FileOperations.addFilesRecursively(f, myFiles);
        }
        
        //Scan resulting file list into media library
        libData = scanMedia(myFiles.toArray(new File[myFiles.size()]));
        
        return libData;
    }
    
    //saveLibraryFile
    //  Reads all library files from library directory
    //  libPath - Path to read libraries from
    public static ArrayList<libraryListElement> readLibraryList(String libPath)
    {
        try
        {
            File libDir = new File(libPath);
            File[] libFiles = FileOperations.selectFilesOnly(libDir.listFiles());
            ArrayList<libraryListElement> libData = new ArrayList<libraryListElement>();
            for(int i = 0; i < libFiles.length; i++)
            {
                String mypath = libFiles[i].getAbsolutePath();
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
                int j = 0;
                
                //check if artist exists
                for(; j < libraryData.size(); j++)
                {
                    if(libraryData.get(j).name.equals(song_artist))
                    {
                        artistIndex = j;
                    }
                }
                //if artist not in list
                if(artistIndex == -1)
                {
                    libraryElementArtist newEntry = new libraryElementArtist();
                    newEntry.name = song_artist;
                    newEntry.albums = new ArrayList<libraryElementAlbum>();
                    libraryData.add(newEntry);
                    artistIndex = j;
                }
                
                //check if album exists
                int albumIndex = -1;
                j = 0; 
                for(; j < libraryData.get(artistIndex).albums.size(); j++)
                {
                    if(libraryData.get(artistIndex).albums.get(j).name.equals(song_album))
                    {
                        albumIndex = j;
                    }
                }
                if(albumIndex == -1)
                {
                    libraryElementAlbum newEntry = new libraryElementAlbum();
                    newEntry.name = song_album;
                    newEntry.year = song_year;
                    newEntry.songs = new ArrayList<libraryElementSong>();
                    libraryData.get(artistIndex).albums.add(newEntry);
                    albumIndex = j;
                }
                
                //check if song exists
                int songIndex = -1;
                j = 0;
                for(; j < libraryData.get(artistIndex).albums.get(albumIndex).songs.size(); j++)
                {
                    if(libraryData.get(artistIndex).albums.get(albumIndex).songs.get(j).name.equals(song_title))
                    {
                        songIndex = j;
                    }
                }
                if(songIndex == -1)
                {
                    libraryElementSong newEntry = new libraryElementSong();
                    newEntry.name = song_title;
                    newEntry.filename = files[i].getAbsolutePath();
                    newEntry.num = Integer.parseInt(song_num);
                    newEntry.length = song_length;
                    libraryData.get(artistIndex).albums.get(albumIndex).songs.add(newEntry);
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
}
