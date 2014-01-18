package com.calcprogrammer1.calctunes.SourceList;

import java.util.ArrayList;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceTypes.LibrarySource;
import com.calcprogrammer1.calctunes.SourceTypes.PlaylistSource;
import com.calcprogrammer1.calctunes.SourceTypes.SubsonicSource;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SourceListAdapter extends BaseExpandableListAdapter
{
    public static final int SOURCE_GROUP_LIBRARY  = 0;
    public static final int SOURCE_GROUP_PLAYLIST = 1;
    public static final int SOURCE_GROUP_SYSTEM   = 2;
    public static final int SOURCE_GROUP_SUBSONIC = 3;
    public static final int TOTAL_SOURCE_GROUP    = 4;
    
    private int interfaceColor;
    private int selectedGroup = -1;
    private int selectedChild = -1;
    
    LayoutInflater inflater;
    Context c;
    ArrayList<LibrarySource> libraryList = new ArrayList<LibrarySource>();
    ArrayList<PlaylistSource> playlistList = new ArrayList<PlaylistSource>();
    ArrayList<SubsonicSource> subsonicList = new ArrayList<SubsonicSource>();
    
    public SourceListAdapter(Context con)
    {
        c = con;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void attachLibraryList(ArrayList<LibrarySource> libList)
    {
        libraryList = libList;
    }

    public void attachPlaylistList(ArrayList<PlaylistSource> playList)
    {
        playlistList = playList;
    }

    public void attachSubsonicList(ArrayList<SubsonicSource> subList)
    {
        subsonicList = subList;
    }
    
    public Object getChild(int groupPosition, int childPosition)
    {
        switch(groupPosition)
        {
            case SOURCE_GROUP_LIBRARY:
                return libraryList.get(childPosition);
            
            case SOURCE_GROUP_PLAYLIST:
                return playlistList.get(childPosition);
                
            case SOURCE_GROUP_SYSTEM:
                return null;
            
            case SOURCE_GROUP_SUBSONIC:
                return subsonicList.get(childPosition);
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
        
        TextView sourceText = (TextView)      convertView.findViewById(R.id.sourcelistentry_text);
        ImageView sourceImage = (ImageView)   convertView.findViewById(R.id.sourcelistentry_icon);

        if(groupPosition == selectedGroup && childPosition == selectedChild)
        {
            TypedValue typedvalue = new TypedValue();
            try{ c.getTheme().resolveAttribute(R.attr.highlight_color, typedvalue, true); } catch(Exception e){}
            int color = typedvalue.resourceId;
            convertView.setBackgroundResource(color);
        }
        else
        {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        
        switch(groupPosition)
        {
            case SOURCE_GROUP_LIBRARY:
                sourceText.setText(libraryList.get(childPosition).name);
                if(libraryList.get(childPosition).status == LibrarySource.LIBRARY_OK)
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon));
                }
                else if(libraryList.get(childPosition).status == LibrarySource.LIBRARY_UNAVAILABLE)
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_library_warning));
                }
                else if(libraryList.get(childPosition).status == LibrarySource.LIBRARY_OFFLINE)
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_library_warning));
                }
                else if(libraryList.get(childPosition).status == LibrarySource.LIBRARY_UPDATING)
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_library_warning));
                }
                break;

            case SOURCE_GROUP_PLAYLIST:
                sourceText.setText(playlistList.get(childPosition).name);
                sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.cached_to_sdcard_icon));
                break;
                
            case SOURCE_GROUP_SYSTEM:
                if(childPosition == 0)
                {
                    sourceText.setText("Filesystem");
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_folder));
                }
                break;
                
            case SOURCE_GROUP_SUBSONIC:
                sourceText.setText(subsonicList.get(childPosition).name);
                if(subsonicList.get(childPosition).status == SubsonicSource.SUBSONIC_OK)
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_server_available));
                }
                else
                {
                    sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_server_unavailable));
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
                return playlistList.size();
                
            case SOURCE_GROUP_SYSTEM:
                return 1;
            
            case SOURCE_GROUP_SUBSONIC:
                return subsonicList.size();
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
        
        switch(groupPosition)
        {
            case SOURCE_GROUP_LIBRARY:
                groupText.setText("Libraries");
                break;
                
            case SOURCE_GROUP_PLAYLIST:
                groupText.setText("Playlists");
                break;
                
            case SOURCE_GROUP_SYSTEM:
                groupText.setText("System");
                break;
           
            case SOURCE_GROUP_SUBSONIC:
                groupText.setText("Subsonic Servers");
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
    
    public void setSelected(int group, int child)
    {
        selectedGroup = group;
        selectedChild = child;
        notifyDataSetChanged();
    }
}
