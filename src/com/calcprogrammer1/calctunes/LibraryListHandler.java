package com.calcprogrammer1.calctunes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

interface LibraryListCallback
{
    void callback(int position);
}

public class LibraryListHandler
{
    ListView libraryList;
    Context c;
    Cursor cursor;
    LibraryDatabaseAdapter adapter;
    LibraryListCallback cb;
    
    public LibraryListHandler(Context con, ListView lv)
    {
        libraryList = lv;
        c = con;
    }
    
    public void setCallback(LibraryListCallback callback)
    {
        cb = callback;
    }
    
    public void setLibrary(String libName)
    {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.calcprogrammer1.calctunes/databases/" + libName + ".db", null);
        cursor = db.rawQuery("SELECT * FROM MYLIBRARY ORDER BY ARTIST, ALBUM, TRACK", null);// ORDER BY ARTIST, ALBUM, TRACK;", null);
        adapter = new LibraryDatabaseAdapter(c, cursor);
    }
    
    public void setListView(ListView lv)
    {
        libraryList = lv;
        redrawList();
    }
    
    public void drawList(int artist)
    {
        redrawList();
    }
    
    public void redrawList()
    {
        libraryList.setAdapter(adapter);
        libraryList.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                libraryClickHandler(arg0, arg1, arg2, arg3);
            }   
        });
    }

    public void libraryClickHandler(AdapterView<?> parent, View view, int position, long id)
    {
        cb.callback(position);
    }
    
    public String getTrack(int position)
    {
        cursor.moveToPosition(position);
        return cursor.getString(cursor.getColumnIndex("PATH"));
    }
    
    public String getNextTrack(int position)
    {
        cursor.moveToPosition(position+1);
        return cursor.getString(cursor.getColumnIndex("PATH"));
    }
    
    public String getPrevTrack(int position)
    {
        cursor.moveToPosition(position-1);
        return cursor.getString(cursor.getColumnIndex("PATH"));
    }
    
    public void setHighlightedTrack(int position)
    {
        adapter.setNowPlaying(position);
        adapter.notifyDataSetChanged();
    }
}
