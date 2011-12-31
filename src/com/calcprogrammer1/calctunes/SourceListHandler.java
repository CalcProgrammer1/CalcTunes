package com.calcprogrammer1.calctunes;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

interface SourceListCallback
{
    void callback(String filename);
}

public class SourceListHandler
{
    ExpandableListView sourceList;
    Context c;
    SourceListAdapter adapter;
    ArrayList<libraryListElement> libraryList = new ArrayList<libraryListElement>();
    SourceListCallback cb;
    
    public SourceListHandler(Context con, ExpandableListView listv)
    {
        sourceList = listv;
        c = con;
        adapter = new SourceListAdapter(con);
        adapter.attachLibraryList(libraryList);
    }
    
    public void setListView(ExpandableListView listv)
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
        sourceList.setOnChildClickListener(new OnChildClickListener() 
        {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                if(groupPosition == 0)
                {
                    cb.callback(libraryList.get(childPosition).filename);
                }
                return true;
            }       
        });
        sourceList.expandGroup(0);
    }
}
