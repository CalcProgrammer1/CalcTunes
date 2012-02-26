package com.calcprogrammer1.calctunes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.mp3.MP3File;
import android.content.Context;

public class LibraryOperations
{
    public static void saveLibraryFile(String libName, ArrayList<String> libData, String libPath)
    {
        File libDir = new File(libPath);
        libDir.mkdirs();
        //libDir.setExecutable(true, false);
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
                newLib.status = libraryListElement.LIBRARY_OK;
                
                File file = new File("/data/data/com.calcprogrammer1.calctunes/databases/" + newLib.name + ".db");
                if(!file.exists())
                {
                    newLib.status = libraryListElement.LIBRARY_UNAVAILABLE;
                }
                else
                {
                    ArrayList<String> libDirs = readLibraryFile(newLib.filename);
                    for(int j = 0; j < libDirs.size(); j++)
                    {
                        File directory = new File(libDirs.get(j));
                        if(!directory.exists() || (directory.isDirectory() && (directory.list().length == 0)))
                        {
                            newLib.status = libraryListElement.LIBRARY_OFFLINE;
                        }
                    }
                }
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
    
    public static String formatTime(int duration)
    {
        String time = "";
        int mins = duration / 60;
        int secs = duration % 60;
        time = mins + ":" + String.format("%02d", secs);
        return time;
    }
    
    public static String getLibraryPath(Context c)
    {
        return c.getApplicationContext().getExternalFilesDir(null).getPath() + "/libraries";
    }
    
    public static String getAlbumArtPath(Context c)
    {
        return c.getApplicationContext().getExternalFilesDir(null).getPath() + "/albumart";
    }
    
    public static String makeFilename(String s)
    {
        return s.replaceAll("[^a-zA-Z0-9]", "");
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
}
