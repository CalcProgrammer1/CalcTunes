package com.calcprogrammer1.calctunes;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class LibraryListAdapter extends BaseAdapter
{
    ArrayList<libraryElementGeneric> listData = new ArrayList<libraryElementGeneric>();
    LayoutInflater inflater;
    Context c;
    
    public LibraryListAdapter(Context con)
    {
        c = con;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void addArtist(libraryElementArtist artist)
    {
        libraryElementGeneric newData = new libraryElementGeneric();
        newData.type = "artist";
        newData.artist = artist;
        newData.album = null;
        newData.song = null;
        listData.add(newData);
    }
    
    public void addAlbum(libraryElementAlbum album)
    {
        libraryElementGeneric newData = new libraryElementGeneric();
        newData.type = "album";
        newData.artist = null;
        newData.album = album;
        newData.song = null;
        listData.add(newData);
    }
    
    public void addSong(libraryElementSong song)
    {
        libraryElementGeneric newData = new libraryElementGeneric();
        newData.type = "song";
        newData.artist = null;
        newData.album = null;
        newData.song = song;
        listData.add(newData);
    }
    
    public int getCount()
    {
        return listData.size();
    }

    public libraryElementGeneric getItem(int position)
    {
        return listData.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        libraryElementGeneric viewData = listData.get(position);
        
        if(true)//convertView == null)
        {
            if(viewData.type.equals("artist"))
            {
                convertView = inflater.inflate(R.layout.librarylistentryartist, null);
                TextView artistText = (TextView) convertView.findViewById(R.id.librarylistartist_text);
                artistText.setText(listData.get(position).artist.name);
            }
            else if(viewData.type.equals("album"))
            {
                convertView = inflater.inflate(R.layout.librarylistentryalbum, null);
                TextView albumText = (TextView) convertView.findViewById(R.id.librarylistalbum_text);
                albumText.setText(listData.get(position).album.name);
            }
            else if(viewData.type.equals("song"))
            {
                convertView = inflater.inflate(R.layout.librarylistentrysong, null);
                TextView songText = (TextView) convertView.findViewById(R.id.librarylistsong_text);
                songText.setText(listData.get(position).song.name);
            }
        }
        else
        {

        }
        return convertView;
    }

}
