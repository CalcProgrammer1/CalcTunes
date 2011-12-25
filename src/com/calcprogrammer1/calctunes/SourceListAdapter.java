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
        if(groupPosition == 0)
        {
            return libraryList.get(childPosition);
        }
        else
        {
            return null;
        }
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
        return convertView;
    }

    public int getChildrenCount(int groupPosition)
    {
        if(groupPosition == 0)
        {
            return libraryList.size();
        }
        else
        {
            return 0;
        }
    }

    public Object getGroup(int groupPosition)
    {
        return null;
    }

    public int getGroupCount()
    {
        return 2;
    }

    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        if(groupPosition == 0)
        {
            if(convertView == null)
            {
                convertView = inflater.inflate(R.layout.sourcelistgroupentry, null);
            }
            TextView groupText = (TextView) convertView.findViewById(R.id.sourcelistentry_text);
            ImageView groupImage = (ImageView) convertView.findViewById(R.id.sourcelistentry_icon);
            groupText.setText("Libraries");
            groupImage.setImageDrawable(c.getResources().getDrawable(R.drawable.media_play_pause));
            return convertView;
        }
        else
        {
            if(convertView == null)
            {
                convertView = inflater.inflate(R.layout.sourcelistgroupentry, null);
            }
            TextView groupText = (TextView) convertView.findViewById(R.id.sourcelistentry_text);
            ImageView groupImage = (ImageView) convertView.findViewById(R.id.sourcelistentry_icon);
            groupText.setText("Playlists");
            groupImage.setImageDrawable(c.getResources().getDrawable(R.drawable.media_play_pause));
            return convertView;
        }
    }

    public boolean hasStableIds()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }

}
