package com.calcprogrammer1.calctunes;

import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

interface LibraryListCallback
{
    void callback(libraryElementGeneric song);
}

public class LibraryListHandler
{
    ArrayList<libraryElementArtist> libraryData;
    ListView libraryList;
    Context c;
    LibraryListAdapter adapter;
    LibraryListCallback cb;
    
    public LibraryListHandler(Context con, ListView lv)
    {
        libraryList = lv;
        c = con;
        adapter = new LibraryListAdapter(c);
    }
    
    public void setCallback(LibraryListCallback callback)
    {
        cb = callback;
    }
    
    public void setLibrary(ArrayList<libraryElementArtist> newLibrary)
    {
        libraryData = newLibrary;
        adapter.attachLibrary(libraryData);
    }
    
    public void setListView(ListView lv)
    {
        libraryList = lv;
        redrawList();
    }
    
    public void rebuildListData(int artist)
    {
        adapter.rebuildData();
    }
    
    public void drawList(int artist)
    {
        rebuildListData(artist);
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
        String type = ((libraryElementGeneric) parent.getAdapter().getItem(position)).type;
        
        if(type.equals("artist"))
        {
            Toast.makeText(c, "artist selected", Toast.LENGTH_SHORT).show();
        }
        else if (type.equals("album"))
        {
            Toast.makeText(c, "album selected", Toast.LENGTH_SHORT).show();
        }
        else if (type.equals("song"))
        {
            cb.callback((libraryElementGeneric) parent.getAdapter().getItem(position));
        }
    }
}
