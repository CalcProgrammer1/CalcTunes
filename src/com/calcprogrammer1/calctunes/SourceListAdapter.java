package com.calcprogrammer1.calctunes;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
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
    
    private int interfaceColor;
    private int selectedGroup = -1;
    private int selectedChild = -1;
    
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
        
        TextView sourceText = (TextView)      convertView.findViewById(R.id.sourcelistentry_text);
        ImageView sourceImage = (ImageView)   convertView.findViewById(R.id.sourcelistentry_icon);

        if(groupPosition == selectedGroup && childPosition == selectedChild)
        {
            int colors1[] = {Color.TRANSPARENT, interfaceColor, Color.TRANSPARENT};
            GradientDrawable back1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors1);
            back1.setShape(GradientDrawable.RECTANGLE);
            int colors2[] = {Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK};
            GradientDrawable back2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors2);
            back2.setShape(GradientDrawable.RECTANGLE);
            LayerDrawable back = new LayerDrawable(new Drawable[] {back1, back2});
            convertView.setBackgroundDrawable(back);
        }
        else
        {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        
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
        final View sourceline1 = convertView.findViewById(R.id.sourcelistentry_line1);
        final View sourceline2 = convertView.findViewById(R.id.sourcelistentry_line2);
        final View sourceline3 = convertView.findViewById(R.id.sourcelistentry_line3);
        
        int[] colors = {Color.TRANSPARENT, interfaceColor};
        GradientDrawable back1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        GradientDrawable back2 = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
        back1.setShape(GradientDrawable.RECTANGLE);
        back2.setShape(GradientDrawable.RECTANGLE);
        sourceline1.setBackgroundDrawable(back1);
        sourceline2.setBackgroundColor(interfaceColor);
        sourceline3.setBackgroundDrawable(back2);
        
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

    public void setNowPlayingColor(int color)
    {
        interfaceColor = color;
        notifyDataSetChanged();
    }
}
