package com.calcprogrammer1.calctunes;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LibraryDatabaseAdapter extends CursorAdapter
{
    static int NO_CHANGE = 0;
    static int NEW_ALBUM = 1;
    static int NEW_ARTIST = 2;
    String now_playing = new String();
    int highlightColor = Color.DKGRAY;
    Context con;
    
    public LibraryDatabaseAdapter(Context context, Cursor c)
    {
        super(context, c);
        con = context;
    }

    @Override
    public int getViewTypeCount()
    {
        return 3;
    }
    
    @Override
    public int getItemViewType(int position)
    {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        return isNewSection(cursor, position);
    }
    
    public void setNowPlaying(String nowPlayingFile)
    {
        now_playing = nowPlayingFile;
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        final TextView artistName = (TextView) view.findViewById(R.id.librarylistartist_text);
        if(artistName != null)
        {
            final String artist = cursor.getString(cursor.getColumnIndex("ARTIST"));
            artistName.setText(artist);
            int[] colors = {Color.BLACK, highlightColor};
            GradientDrawable back = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
            back.setShape(GradientDrawable.RECTANGLE);
            artistName.setBackgroundDrawable(back);
        }
        
        final TextView albumName = (TextView) view.findViewById(R.id.librarylistalbum_text);
        if(albumName != null)
        {
            ((ImageView)view.findViewById(R.id.librarylistalbum_artwork)).setImageBitmap(AlbumArtManager.getAlbumArt(cursor.getString(cursor.getColumnIndex("ARTIST")), cursor.getString(cursor.getColumnIndex("ALBUM")), context));
            final String album = cursor.getString(cursor.getColumnIndex("ALBUM"));
            albumName.setText(album);
            ((TextView)view.findViewById(R.id.librarylistalbum_artist)).setText(cursor.getString(cursor.getColumnIndex("ARTIST")));
            ((TextView)view.findViewById(R.id.librarylistalbum_year)).setText(cursor.getString(cursor.getColumnIndex("YEAR")));
        }
        
        if(cursor.getString(cursor.getColumnIndex("PATH")).equals(now_playing))
        {
            int[] colors = {Color.BLACK, highlightColor, Color.BLACK};
            GradientDrawable back = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
            back.setShape(GradientDrawable.RECTANGLE);
            view.findViewById(R.id.librarylistsong_frame).setBackgroundDrawable(back);
        }
        else
        {
            view.findViewById(R.id.librarylistsong_frame).setBackgroundColor(Color.BLACK);
        }
        
        final String name = cursor.getString(cursor.getColumnIndex("TITLE"));
        final String time = LibraryOperations.formatTime(cursor.getInt(cursor.getColumnIndex("TIME")));
        final String num = "" + cursor.getInt(cursor.getColumnIndex("TRACK"));
        
        final TextView songName = (TextView) view.findViewById(R.id.librarylistsong_text);
        final TextView songTime = (TextView) view.findViewById(R.id.librarylistsong_time);
        final TextView songNum = (TextView) view.findViewById(R.id.librarylistsong_num);
        
        songName.setText(name);
        songTime.setText(time);
        songNum.setText(num);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        int position = cursor.getPosition();
        int newViewType = isNewSection(cursor, position);

        final View view;
        if(newViewType == NEW_ARTIST)
        {
            view = LayoutInflater.from(con).inflate(R.layout.librarylistentryartist, parent, false);
        }
        else if(newViewType == NEW_ALBUM)
        {
            view = LayoutInflater.from(con).inflate(R.layout.librarylistentryalbum, parent, false);
        }
        else
        {
            view = LayoutInflater.from(con).inflate(R.layout.librarylistentrysong, parent, false);
        }
        return view;
    }
    
    private int isNewSection(Cursor cursor, int position)
    {
        if(position == 0)
        {
            return NEW_ARTIST;
        }
        else
        {
            String currentAlbum = cursor.getString(cursor.getColumnIndex("ALBUM"));
            String currentArtist = cursor.getString(cursor.getColumnIndex("ARTIST"));
            
            cursor.moveToPrevious();
            
            String previousAlbum = cursor.getString(cursor.getColumnIndex("ALBUM"));
            String previousArtist = cursor.getString(cursor.getColumnIndex("ARTIST"));
            
            cursor.moveToNext();
            
            if(currentArtist.equals(previousArtist))
            {
                if(!currentAlbum.equals(previousAlbum))
                {
                    return NEW_ALBUM;
                }
                else
                {
                    return NO_CHANGE;
                }
            }
            else
            {
                return NEW_ARTIST;
            }
        }
    }
    
    public void setNowPlayingColor(int color)
    {
        highlightColor = color;
    }
}
