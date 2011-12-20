package com.calcprogrammer1.calctunes;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SourceListAdapter extends BaseAdapter
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
    
    public int getCount()
    {
        return libraryList.size();
    }

    public libraryListElement getItem(int arg0)
    {
        
        return libraryList.get(arg0);
    }

    public long getItemId(int arg0)
    {
        return arg0;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.sourcelistentry, null);
        }
        TextView sourceText = (TextView) convertView.findViewById(R.id.sourcelistentry_text);
        ImageView sourceImage = (ImageView) convertView.findViewById(R.id.sourcelistentry_icon);
        
        sourceText.setText(libraryList.get(position).name);
        if(libraryList.get(position).status == libraryListElement.LIBRARY_OK)
        {
            sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon));
        }
        else if(libraryList.get(position).status == libraryListElement.LIBRARY_UNAVAILABLE)
        {
            sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_library_warning));
        }
        else if(libraryList.get(position).status == libraryListElement.LIBRARY_OFFLINE)
        {
            sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_library_warning));
        }
        else if(libraryList.get(position).status == libraryListElement.LIBRARY_UPDATING)
        {
            sourceImage.setImageDrawable(c.getResources().getDrawable(R.drawable.icon_library_warning));
        }
        return convertView;
    }

}
