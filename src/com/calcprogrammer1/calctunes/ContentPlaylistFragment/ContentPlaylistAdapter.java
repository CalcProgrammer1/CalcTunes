package com.calcprogrammer1.calctunes.ContentPlaylistFragment;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.calcprogrammer1.calctunes.R;

public class ContentPlaylistAdapter extends BaseAdapter
{
    private Context c;
    private PlaylistEditor playlist;
    private String nowPlaying;

    public ContentPlaylistAdapter(Context c)
    {
        this.c = c;
    }

    public void setPlaylist(PlaylistEditor playlist)
    {
        this.playlist = playlist;
    }

    public void setNowPlaying(String nowPlaying)
    {
        this.nowPlaying = nowPlaying;
    }

    @Override
    public int getCount()
    {
        return playlist.playlistData.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = null;

        if(convertView != null)
        {
            view = convertView;
        }
        else
        {
            view = LayoutInflater.from(c).inflate(R.layout.contentfilesystementry, parent, false);
        }

        ImageView icon = (ImageView) view.findViewById(R.id.imageViewFilesystemEntry);
        TextView text = (TextView)  view.findViewById(R.id.textViewFilesystemEntry);

        text.setText(playlist.playlistData.get(position).title);

        if(playlist.playlistData.get(position).filename.equals(nowPlaying))
        {
            TypedValue typedvalue = new TypedValue();
            try{ c.getTheme().resolveAttribute(R.attr.highlight_color, typedvalue, true); } catch(Exception e){}
            int color = typedvalue.resourceId;
            view.setBackgroundResource(color);
        }
        else
        {
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        return view;
    }
}
