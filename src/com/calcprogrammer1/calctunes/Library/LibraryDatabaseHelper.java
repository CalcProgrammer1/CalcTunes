package com.calcprogrammer1.calctunes.Library;

import java.io.File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.calcprogrammer1.calctunes.AlbumArtManager;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LibraryDatabaseHelper extends SQLiteOpenHelper
{
    private SQLiteDatabase myDataBase;
    private Context con;
    public LibraryDatabaseHelper(Context context, String name)
    {
        super(context, name, null, 1);
        con = context;
        File databaseDirectory = new File("/data/data/com.calcprogrammer1.calctunes/databases/");
        databaseDirectory.mkdirs();
        myDataBase = SQLiteDatabase.openOrCreateDatabase("/data/data/com.calcprogrammer1.calctunes/databases/"+name, null);
    }

    public void startDatabase()
    {
        myDataBase.execSQL("DROP TABLE IF EXISTS MYLIBRARY;");
        myDataBase.execSQL("CREATE TABLE MYLIBRARY (_id INTEGER PRIMARY KEY AUTOINCREMENT, ARTIST TEXT, ALBUM TEXT, YEAR TEXT, TRACK INTEGER, TITLE TEXT, PATH TEXT, TIME INTEGER, DISC INTEGER);");
    }
    
    public void closeDatabase()
    {
        myDataBase.close();
    }
    
    public void addFileToDatabase(File file)
    {
        AudioFile f;
        try
        {
            f = SourceListOperations.readAudioFileReadOnly(file);
            Tag tag = f.getTag();
            int song_length = f.getAudioHeader().getTrackLength();
            int song_num = Integer.parseInt(tag.getFirst(FieldKey.TRACK));
            int song_disc = 0;
            if(!tag.getFirst(FieldKey.DISC_NO).equals(""))
            {
                song_disc = Integer.parseInt(tag.getFirst(FieldKey.DISC_NO));
            }
            String song_artist = tag.getFirst(FieldKey.ARTIST);
            String song_album = tag.getFirst(FieldKey.ALBUM);
            String song_title = tag.getFirst(FieldKey.TITLE);
            String song_year = tag.getFirst(FieldKey.YEAR);
            String song_path = file.getPath();
            song_artist = song_artist.replaceAll("'", "''");
            song_album = song_album.replaceAll("'", "''");
            song_title = song_title.replaceAll("'", "''");
            song_year = song_year.replaceAll("'", "''");
            song_path = song_path.replaceAll("'", "''");
            AlbumArtManager.getAlbumArt(song_artist, song_album, con);
            myDataBase.execSQL("INSERT INTO 'MYLIBRARY' VALUES (NULL, '" + song_artist + "', '" + song_album + "', '" + song_year + "', " + song_num + ", '" + song_title + "', '" + song_path + "', " + song_length + ", " + song_disc + ");");
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
