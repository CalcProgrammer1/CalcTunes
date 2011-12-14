package com.calcprogrammer1.calctunes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

interface LibraryListCallback
{
    void callback(String filename);
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
    }
    
    public void setCallback(LibraryListCallback callback)
    {
        cb = callback;
    }
    
    public void setLibrary(ArrayList<libraryElementArtist> newLibrary)
    {
        libraryData = newLibrary;
    }
    
    public void setListView(ListView lv)
    {
        libraryList = lv;
        redrawList();
    }
    
    public void rebuildListData(int artist)
    {
        // create the grid item mapping
        String[] from = new String[] {"artist", "album", "song"};
        int[] to = new int[] { R.id.textView1, R.id.textView2, R.id.textView3};

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        adapter = new LibraryListAdapter(c);
        for(int i = 0; i < libraryData.size(); i++)
        {
            adapter.addArtist(libraryData.get(i));
            if(true)//i == artist)
            {
                for(int j = 0; j < libraryData.get(i).albums.size(); j++)
                {
                    adapter.addAlbum(libraryData.get(i).albums.get(j));

                    for(int k = 0; k < libraryData.get(i).albums.get(j).songs.size(); k++)
                    {
                        adapter.addSong(libraryData.get(i).albums.get(j).songs.get(k));
                    }
                }
            }
        }
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
            String filepath = ((libraryElementGeneric) parent.getAdapter().getItem(position)).song.filename;
            cb.callback(filepath);
        }
    }
}
