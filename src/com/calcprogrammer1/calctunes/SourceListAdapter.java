package com.calcprogrammer1.calctunes;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SourceListAdapter extends BaseExpandableListAdapter
{
    public static final int SOURCE_GROUP_LIBRARY = 0;
    public static final int SOURCE_GROUP_PLAYLIST = 1;
    public static final int SOURCE_GROUP_SYSTEM = 2;
    public static final int TOTAL_SOURCE_GROUP = 3;
    
    LayoutInflater inflater;
    Context c;
    ArrayList<libraryListElement> libraryList = new ArrayList<libraryListElement>();
    
    public SourceListAdapter(Context con)
    {
        c = con;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void attachLibraryList(ArrayList<libraryListElement> libList)
    {
        libraryList = libList;
    }

    public Object getChild(int groupPosition, int childPosition)
    {
        switch(groupPosition)
        {
            case SOURCE_GROUP_LIBRARY:
                return libraryList.get(childPosition);
            
            case SOURCE_GROUP_PLAYLIST:
                return null;
                
            case SOURCE_GROUP_SYSTEM:
                return null;
        }
        return null;
    }

    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.sourcelistlibraryentry, null);
        }
        
        TextView sourceText = (TextView) convertView.findViewById(R.id.sourcelistentry_text);
        ImageView sourceImage = (ImageView) convertView.findViewById(R.id.sourcelistentry_icon);
        
        switch(groupPosition)
        {
            case SOURCE_GROUP_LIBRARY:
                sourceText.setText(libraryList.get(childPosition).name);
                if(libraryList.get(childPosition).status == libraryListElement.LIBRARY_OK)
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon));
                }
                else if(libraryList.get(childPosition).status == libraryListElement.LIBRARY_UNAVAILABLE)
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_library_warning));
                }
                else if(libraryList.get(childPosition).status == libraryListElement.LIBRARY_OFFLINE)
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_library_warning));
                }
                else if(libraryList.get(childPosition).status == libraryListElement.LIBRARY_UPDATING)
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_library_warning));
                }
                break;
                
            case SOURCE_GROUP_SYSTEM:
                if(childPosition == 0)
                {
                    sourceText.setText("Filesystem");
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_folder));
                }
                break;
        }
        return convertView;
    }

    public int getChildrenCount(int groupPosition)
    {
        switch(groupPosition)
        {
            case SOURCE_GROUP_LIBRARY:
                return libraryList.size();
                
            case SOURCE_GROUP_PLAYLIST:
                return 0;
                
            case SOURCE_GROUP_SYSTEM:
                return 1;
        }
        return 0;
    }

    public Object getGroup(int groupPosition)
    {
        return null;
    }

    public int getGroupCount()
    {
        return TOTAL_SOURCE_GROUP;
    }

    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.sourcelistgroupentry, null);
        }
        TextView groupText = (TextView) convertView.findViewById(R.id.sourcelistentry_text);
        ImageView groupImage = (ImageView) convertView.findViewById(R.id.sourcelistentry_icon);
        switch(groupPosition)
        {
            case SOURCE_GROUP_LIBRARY:
                groupText.setText("Libraries");
                groupImage.setImageDrawable(c.getResources().getDrawable(R.drawable.media_play_pause));
                break;
                
            case SOURCE_GROUP_PLAYLIST:
                groupText.setText("Playlists");
                groupImage.setImageDrawable(c.getResources().getDrawable(R.drawable.media_play_pause));
                break;
                
            case SOURCE_GROUP_SYSTEM:
                groupText.setText("System");
                groupImage.setImageDrawable(c.getResources().getDrawable(R.drawable.media_play_pause));
                break;
        }
        return convertView;
    }

    public boolean hasStableIds()
    {
        return false;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

}
