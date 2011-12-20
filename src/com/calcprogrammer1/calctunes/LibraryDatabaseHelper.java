package com.calcprogrammer1.calctunes;

import java.io.File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LibraryDatabaseHelper extends SQLiteOpenHelper
{
    private SQLiteDatabase myDataBase;
    
    public LibraryDatabaseHelper(Context context, String name)
    {
        super(context, name, null, 1);
        
        myDataBase = SQLiteDatabase.openOrCreateDatabase("/data/data/com.calcprogrammer1.calctunes/databases/"+name, null);
    }

    public void startDatabase()
    {
        myDataBase.execSQL("DROP TABLE IF EXISTS MYLIBRARY;");
        myDataBase.execSQL("CREATE TABLE MYLIBRARY (ARTIST TEXT, ALBUM TEXT, YEAR TEXT, TRACK INTEGER, TITLE TEXT, PATH TEXT, TIME INTEGER);");
    }
    
    public void addFileToDatabase(File file)
    {
        AudioFile f;
        try
        {
            f = LibraryOperations.readAudioFileReadOnly(file);
            Tag tag = f.getTag();
            int song_length = f.getAudioHeader().getTrackLength();
            int song_num = Integer.parseInt(tag.getFirstTrack());
            String song_artist = tag.getFirstArtist();
            String song_album = tag.getFirstAlbum();
            String song_title = tag.getFirstTitle();
            String song_year = tag.getFirstYear();
            String song_path = file.getPath();
            song_artist = song_artist.replaceAll("'", "''");
            song_album = song_album.replaceAll("'", "''");
            song_title = song_title.replaceAll("'", "''");
            song_year = song_year.replaceAll("'", "''");
            song_path = song_path.replaceAll("'", "''");
            myDataBase.execSQL("INSERT INTO 'MYLIBRARY' VALUES ('" + song_artist + "', '" + song_album + "', '" + song_year + "', " + song_num + ", '" + song_title + "', '" + song_path + "', " + song_length + ");");
        }catch(Exception e){}
    }

    @Override
    public synchronized void close()
    {
        if(myDataBase != null)
        {
            myDataBase.close();
        }
        super.close();
    }
    
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        
    }

}
