package com.calcprogrammer1.calctunes.ContentLibraryFragment;

import java.util.ArrayList;

import com.calcprogrammer1.calctunes.AlbumArtManager;
import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
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
    
    private int interfaceColor;
    
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
        return listData.size();
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
            final View      artistline1 =    convertView.findViewById(R.id.librarylistartist_line1);
            final View      artistline2 =    convertView.findViewById(R.id.librarylistartist_line2);
            final View      artistline3 =    convertView.findViewById(R.id.librarylistartist_line3);
            int[] colors = {Color.TRANSPARENT, interfaceColor};
            GradientDrawable back1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
            GradientDrawable back2 = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
            back1.setShape(GradientDrawable.RECTANGLE);
            back2.setShape(GradientDrawable.RECTANGLE);
            artistline1.setBackgroundDrawable(back1);
            artistline2.setBackgroundColor(interfaceColor);
            artistline3.setBackgroundDrawable(back2);
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
            
            Bitmap art = AlbumArtManager.getAlbumArtFromCache(listData.get(position).artist, listData.get(position).album, c, true);
            artwork.setImageBitmap(art);
        }
        else if(viewData.type == ContentListElement.LIBRARY_LIST_TYPE_TRACK)
        {
            convertView = inflater.inflate(R.layout.librarylistentrysong, null);
            TextView songText = (TextView) convertView.findViewById(R.id.librarylistsong_text);
            TextView songNum = (TextView) convertView.findViewById(R.id.librarylistsong_num);
            TextView songTime = (TextView) convertView.findViewById(R.id.librarylistsong_time);
            songText.setText(listData.get(position).song);
            songNum.setText(""+listData.get(position).track);
            songTime.setText(SourceListOperations.formatTime(listData.get(position).time));
            
            if(listData.get(position).path.equals(now_playing))
            {
                int colors1[] = {Color.TRANSPARENT, interfaceColor, Color.TRANSPARENT};
                GradientDrawable back1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors1);
                back1.setShape(GradientDrawable.RECTANGLE);
                int colors2[] = {Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK};
                GradientDrawable back2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors2);
                back2.setShape(GradientDrawable.RECTANGLE);
                LayerDrawable back = new LayerDrawable(new Drawable[] {back1, back2});
                convertView.findViewById(R.id.librarylistsong_frame).setBackgroundDrawable(back);
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

    public void setNowPlayingColor(int new_color)
    {
        interfaceColor = new_color;
    }

}
