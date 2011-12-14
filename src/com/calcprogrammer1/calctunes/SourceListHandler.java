package com.calcprogrammer1.calctunes;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    ArrayAdapter<String> adapter;
    ArrayList<libraryListElement> libraryList;
    SourceListCallback cb;
    
    public SourceListHandler(Context con, ListView listv)
    {
        sourceList = listv;
        c = con;
    }
    
    public void setListView(ListView listv)
    {
        sourceList = listv;
    }
    
    public void setLibraryList(ArrayList<libraryListElement> libList)
    {
        libraryList = libList;
    }
    
    public void setCallback(SourceListCallback callb)
    {
        cb = callb;
    }
    
    public void updateList()
    {
        String[] libraryNames = LibraryOperations.getNamesFromList(libraryList);
        
        adapter = new ArrayAdapter<String>(c,android.R.layout.simple_list_item_1,libraryNames);
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
