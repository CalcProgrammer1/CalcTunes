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
    SimpleAdapter adapter;
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
        for(int i = 0; i < libraryData.size(); i++)
        {
            HashMap<String, String> amap = new HashMap<String, String>();
            amap.put("artist_index", ""+i);
            amap.put("artist", libraryData.get(i).name);
            amap.put("album", "");
            amap.put("song", "");
            fillMaps.add(amap);
            if(i == artist)
            {
                for(int j = 0; j < libraryData.get(i).albums.size(); j++)
                {
                    HashMap<String, String> bmap = new HashMap<String, String>();
                    bmap.put("artist_index", ""+i);
                    bmap.put("artist", "");
                    bmap.put("album_index", ""+j);
                    bmap.put("album", libraryData.get(i).albums.get(j).name);
                    bmap.put("song", "");
                    fillMaps.add(bmap);
                    for(int k = 0; k < libraryData.get(i).albums.get(j).songs.size(); k++)
                    {
                        HashMap<String, String> cmap = new HashMap<String, String>();
                        cmap.put("artist_index", ""+i);
                        cmap.put("artist", "");
                        cmap.put("album_index", ""+j);
                        cmap.put("album", "");
                        cmap.put("song_index", ""+k);
                        cmap.put("song", libraryData.get(i).albums.get(j).songs.get(k).name);
                        fillMaps.add(cmap); 
                    }
                }
            }
        }
        adapter = new SimpleAdapter(c, fillMaps, R.layout.listentry, from, to);
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
        String songStr = ((HashMap<String, String>) parent.getAdapter().getItem(position)).get("song");
        String artStr =  ((HashMap<String, String>) parent.getAdapter().getItem(position)).get("artist");
        String albStr =  ((HashMap<String, String>) parent.getAdapter().getItem(position)).get("album");
        
        if(!songStr.equals(""))
        {
            int i = Integer.parseInt(((HashMap<String, String>) parent.getAdapter().getItem(position)).get("artist_index"));
            int j = Integer.parseInt(((HashMap<String, String>) parent.getAdapter().getItem(position)).get("album_index"));
            int k = Integer.parseInt(((HashMap<String, String>) parent.getAdapter().getItem(position)).get("song_index"));
            String filePath = libraryData.get(i).albums.get(j).songs.get(k).filename;
            Toast.makeText(c, filePath, Toast.LENGTH_SHORT).show();
            cb.callback(filePath);   
        }
        else if(!albStr.equals(""))
        {
            Toast.makeText(c, "Album Selected", Toast.LENGTH_SHORT).show();
        }
        else if(!artStr.equals(""))
        {
            int i = Integer.parseInt(((HashMap<String, String>) parent.getAdapter().getItem(position)).get("artist_index"));
            drawList(i);
            Toast.makeText(c, "Artist Selected", Toast.LENGTH_SHORT).show();
        }
    }
}
