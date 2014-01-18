package com.calcprogrammer1.calctunes.ContentLibraryFragment;

import java.util.ArrayList;

import com.calcprogrammer1.calctunes.AlbumArtManager;
import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ContentListAdapter extends BaseAdapter
{
    ArrayList<ContentListElement> listData = new ArrayList<ContentListElement>();

    LayoutInflater inflater;
    Context c;
    
    String now_playing = new String();
    
    public ContentListAdapter(Context con)
    {
        c = con;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void attachList(ArrayList<ContentListElement> list)
    {
        listData = list;
    }
    
    public int getCount()
    {
        if( listData != null )
        {
            return listData.size();
        }
        else
        {
            return 0;
        }
    }

    public ContentListElement getItem(int position)
    {
        return listData.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ContentListElement viewData = listData.get(position);
        
        if(viewData.type == ContentListElement.LIBRARY_LIST_TYPE_HEADING)
        {
            convertView = inflater.inflate(R.layout.librarylistentryartist, null);
            TextView artistText = (TextView) convertView.findViewById(R.id.librarylistartist_text);
            artistText.setText(listData.get(position).artist);
        }
        else if(viewData.type == ContentListElement.LIBRARY_LIST_TYPE_ALBUM)
        {
            convertView = inflater.inflate(R.layout.librarylistentryalbum, null);
            TextView albumText  = (TextView) convertView.findViewById(R.id.librarylistalbum_text);
            TextView artistText = (TextView) convertView.findViewById(R.id.librarylistalbum_artist);
            TextView yearText   = (TextView) convertView.findViewById(R.id.librarylistalbum_year);
            ImageView artwork   = (ImageView)convertView.findViewById(R.id.librarylistalbum_artwork);
            
            albumText.setText(listData.get(position).album);
            artistText.setText(listData.get(position).artist);
            yearText.setText(listData.get(position).year);

            AlbumArtManager.setImageAsync(listData.get(position).artist, listData.get(position).album, c, true, artwork);
        }
        else if(viewData.type == ContentListElement.LIBRARY_LIST_TYPE_TRACK)
        {
            convertView = inflater.inflate(R.layout.librarylistentrysong, null);
            TextView songText = (TextView) convertView.findViewById(R.id.librarylistsong_text);
            TextView songNum = (TextView) convertView.findViewById(R.id.librarylistsong_num);
            TextView songTime = (TextView) convertView.findViewById(R.id.librarylistsong_time);
            ImageView songIcon = (ImageView) convertView.findViewById(R.id.librarylistsong_icon);
            songText.setText(listData.get(position).song);
            songNum.setText(""+listData.get(position).track);
            songTime.setText(SourceListOperations.formatTime(listData.get(position).time));
            if(listData.get(position).cache == ContentListElement.CACHE_DOWNLOADING)
            {
                songIcon.setImageResource(R.drawable.downloading_to_sdcard_icon);
            }
            if(listData.get(position).cache == ContentListElement.CACHE_SDCARD)
            {
                songIcon.setImageResource(R.drawable.cached_to_sdcard_icon);
            }
            if(listData.get(position).path.equals(now_playing))
            {
                TypedValue typedvalue = new TypedValue();
                try{ c.getTheme().resolveAttribute(R.attr.highlight_color, typedvalue, true); } catch(Exception e){}
                int color = typedvalue.resourceId;
                convertView.findViewById(R.id.librarylistsong_frame).setBackgroundResource(color);
            }
            else
            {
                convertView.findViewById(R.id.librarylistsong_frame).setBackgroundColor(Color.TRANSPARENT);
            }
        }

        return convertView;
    }

    public void setNowPlaying(String nowPlayingFile)
    {
        now_playing = nowPlayingFile;
    }
}
