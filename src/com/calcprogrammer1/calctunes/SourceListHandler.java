package com.calcprogrammer1.calctunes;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

interface SourceListCallback
{
    void callback(String filename);
}

public class SourceListHandler
{
    ListView sourceList;
    Context c;
    SourceListAdapter adapter;
    ArrayList<libraryListElement> libraryList = new ArrayList<libraryListElement>();
    SourceListCallback cb;
    
    public SourceListHandler(Context con, ListView listv)
    {
        sourceList = listv;
        c = con;
        adapter = new SourceListAdapter(con);
        adapter.attachLibraryList(libraryList);
    }
    
    public void setListView(ListView listv)
    {
        sourceList = listv;
    }
    
    public ArrayList<libraryListElement> getLibraryList()
    {
        return libraryList;
    }
    
    public void setCallback(SourceListCallback callb)
    {
        cb = callb;
    }
    
    public void refreshLibraryList()
    {
        libraryList = new ArrayList<libraryListElement>();
        libraryList = LibraryOperations.readLibraryList(LibraryOperations.getLibraryPath(c));
        updateList();
    }
    
    public void updateList()
    {
        adapter.attachLibraryList(libraryList);
        sourceList.setAdapter(adapter);
        sourceList.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                cb.callback(libraryList.get(arg2).filename);
            }       
        });
    }
}
