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
    ArrayList<libraryElementArtist> libData = new ArrayList<libraryElementArtist>();
    LayoutInflater inflater;
    Context c;
    
    public LibraryListAdapter(Context con)
    {
        c = con;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void attachLibrary(ArrayList<libraryElementArtist> library)
    {
        libData = library;
    }
    
    public void rebuildData()
    {
        for(int i = 0; i < libData.size(); i++)
        {
            addArtist(i);
            if(true)
            {
                for(int j = 0; j < libData.get(i).albums.size(); j++)
                {
                    addAlbum(i, j);

                    for(int k = 0; k < libData.get(i).albums.get(j).songs.size(); k++)
                    {
                        addSong(i, j, k);
                    }
                }
            }
        }
    }
    
    public void addArtist(int artistIndex)
    {
        libraryElementGeneric newData = new libraryElementGeneric();
        newData.artistIndex = artistIndex;
        newData.type = "artist";
        newData.artist = libData.get(artistIndex);
        newData.album = null;
        newData.song = null;
        listData.add(newData);
    }
    
    public void addAlbum(int artistIndex, int albumIndex)
    {
        libraryElementGeneric newData = new libraryElementGeneric();
        newData.type = "album";
        newData.artistIndex = artistIndex;
        newData.albumIndex = albumIndex;
        newData.artist = null;
        newData.album = libData.get(artistIndex).albums.get(albumIndex);
        newData.song = null;
        listData.add(newData);
    }
    
    public void addSong(int artistIndex, int albumIndex, int songIndex)
    {
        libraryElementGeneric newData = new libraryElementGeneric();
        newData.type = "song";
        newData.artistIndex = artistIndex;
        newData.albumIndex = albumIndex;
        newData.songIndex = songIndex;
        newData.artist = null;
        newData.album = null;
        newData.song = libData.get(artistIndex).albums.get(albumIndex).songs.get(songIndex);
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
                TextView songNum = (TextView) convertView.findViewById(R.id.librarylistsong_num);
                TextView songTime = (TextView) convertView.findViewById(R.id.librarylistsong_time);
                songText.setText(listData.get(position).song.name);
                songNum.setText(""+listData.get(position).song.num);
                songTime.setText(LibraryOperations.formatTime(listData.get(position).song.length));
            }
        }
        else
        {

        }
        return convertView;
    }

}
