package com.calcprogrammer1.calctunes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LibraryDatabaseAdapter extends BaseAdapter
{
    private Context con;
    private String query;
    private SQLiteDatabase db;
    
    static int NO_CHANGE = 0;
    static int NEW_ALBUM = 1;
    static int NEW_ARTIST = 2;
    String now_playing = new String();
    
    private static int QUERY_LIMIT = 200;
    
    private int total_rows = 0;
    
    private Cursor cur0;
    private Cursor cur1;
    private Cursor cur2;
    
    private boolean cur1_ready;
    private boolean cur2_ready;
    
    private int cur1_offset;
    private int cur2_offset;
    
    private int highlightColor;

    public LibraryDatabaseAdapter (Context context, SQLiteDatabase data, String dbquery)
    {
        con = context;
        db = data;
        query = dbquery;
        
        Cursor tmp = db.rawQuery("SELECT COUNT(*) FROM MYLIBRARY", null);
        tmp.moveToPosition(0);
        total_rows = Integer.parseInt(tmp.getString(0));
        tmp.close();
        
        cur0 = db.rawQuery(query.replace(";", "") + " LIMIT " + QUERY_LIMIT + " OFFSET 0;", null);
        cur0.moveToPosition(0);
        
        cur1_offset = QUERY_LIMIT;
        cur2_offset = QUERY_LIMIT*2;
        setupCursor(1);
        setupCursor(2);
    }
    
    public void setNowPlayingColor(int color)
    {
        highlightColor = color;
    }

    private class CursorUpdateTask extends Thread
    {
        public void run(int x)
        {
            setupCursor(x);
        }
    }
    
    public void setupCursor(int cursor)
    {
        if(cursor == 1)
        {
            if(cur1 != null)
            {
                cur1.close();
                cur1_ready = false;
            }
            cur1 = db.rawQuery(query.replace(";", "") + " LIMIT " + QUERY_LIMIT + " OFFSET " + cur1_offset + ";", null);
            cur1_ready = true;
            cur1.moveToPosition(0);
        }
        else
        {
            if(cur2 != null)
            {
                cur2.close();
                cur2_ready = false;
            }
            cur2 = db.rawQuery(query.replace(";", "") + " LIMIT " + QUERY_LIMIT + " OFFSET " + cur2_offset + ";", null);
            cur2_ready = true;
            cur2.moveToPosition(0);
        }
    }
    
    public Cursor getCursorForPosition(int position)
    {
        if(position < QUERY_LIMIT)
        {
            cur0.moveToPosition(position);
            return cur0;
        }
        else
        {
            //If cursor 2 is after cursor 1
            if(cur1_offset < cur2_offset)
            {
                //If position is within cursor 1
                if(position >= cur1_offset && position < cur2_offset)
                {
                    if(position < cur1_offset + (0.25*QUERY_LIMIT))
                    {
                        if(cur2_ready)
                        {
                            cur2_offset = cur1_offset - QUERY_LIMIT;
                            cur2_ready = false;
                            new CursorUpdateTask().run(2);
                        }
                    }
                }
                else
                {
                    if(position > cur2_offset + (0.75*QUERY_LIMIT))
                    {
                        if(cur1_ready)
                        {
                            cur1_offset = cur2_offset + QUERY_LIMIT;
                            cur1_ready = false;
                            new CursorUpdateTask().run(1);
                        }
                    }
                }
            }
            else
            {
                //If position is within cursor 2
                if(position >= cur2_offset && position < cur1_offset)
                {
                    if(position < cur2_offset + (0.25*QUERY_LIMIT))
                    {
                        if(cur1_ready)
                        {
                            cur1_offset = cur2_offset - QUERY_LIMIT;
                            cur1_ready = false;
                            new CursorUpdateTask().run(1);
                        }
                    }
                }
                else
                {
                    if(position > cur1_offset + (0.75*QUERY_LIMIT))
                    {
                        if(cur2_ready)
                        {
                            cur2_offset = cur1_offset + QUERY_LIMIT;
                            cur2_ready = false;
                            new CursorUpdateTask().run(2);
                        }
                    }
                }
            }
            
            if(cur1_ready && (position >= cur1_offset) && (position < (cur1_offset + QUERY_LIMIT)))
            {
                cur1.moveToPosition(position-(cur1_offset));
                return cur1;
            }
            else if(cur2_ready && (position >= cur2_offset) && (position < (cur2_offset + QUERY_LIMIT)))
            {
                cur2.moveToPosition(position-(cur2_offset));
                return cur2;
            }
            else
            {
                cur1_offset = (position / QUERY_LIMIT) * QUERY_LIMIT;
                setupCursor(1);
                cur1.moveToPosition(position-(cur1_offset));
                return cur1;
            }
        }
    }
    
    public int getCount()
    {
        return total_rows;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        int newViewType = isNewSection(position);
        
        View view = null;
        if(newViewType == NEW_ARTIST)
        {
            if((convertView != null) && (convertView.findViewById(R.id.librarylistartist_text) != null))
            {
                view = convertView;
            }
            else
            {
               view = LayoutInflater.from(con).inflate(R.layout.librarylistentryartist, parent, false);
            }
        }
        else if(newViewType == NEW_ALBUM)
        {
            if((convertView != null) && (convertView.findViewById(R.id.librarylistalbum_text) != null) && (convertView.findViewById(R.id.librarylistartist_text) == null))
            {
                view = convertView;
            }
            else
            {
                view = LayoutInflater.from(con).inflate(R.layout.librarylistentryalbum, parent, false);
            }
        }
        else
        {
            if(convertView != null && (convertView.findViewById(R.id.librarylistalbum_text) == null))
            {
                view = convertView;
            }
            else
            {
                view = LayoutInflater.from(con).inflate(R.layout.librarylistentrysong, parent, false);
            }
        }
        
        Cursor cursor = getCursorForPosition(position);
        
        final TextView  artistName  = (TextView)  view.findViewById(R.id.librarylistartist_text);
        final View      artistline1 =             view.findViewById(R.id.librarylistartist_line1);
        final View      artistline2 =             view.findViewById(R.id.librarylistartist_line2);
        final View      artistline3 =             view.findViewById(R.id.librarylistartist_line3);
        final TextView  albumName   = (TextView)  view.findViewById(R.id.librarylistalbum_text);
        final TextView  albumArtist = (TextView)  view.findViewById(R.id.librarylistalbum_artist);
        final TextView  albumYear   = (TextView)  view.findViewById(R.id.librarylistalbum_year);
        final ImageView albumArt    = (ImageView) view.findViewById(R.id.librarylistalbum_artwork);
        
        final String name = cursor.getString(cursor.getColumnIndex("TITLE"));
        final String path = cursor.getString(cursor.getColumnIndex("PATH"));
        final String time = LibraryOperations.formatTime(cursor.getInt(cursor.getColumnIndex("TIME")));
        final String num  = "" + cursor.getInt(cursor.getColumnIndex("TRACK"));
        
        final TextView songName = (TextView) view.findViewById(R.id.librarylistsong_text);
        final TextView songTime = (TextView) view.findViewById(R.id.librarylistsong_time);
        final TextView songNum = (TextView) view.findViewById(R.id.librarylistsong_num);
        
        songName.setText(name);
        songTime.setText(time);
        songNum.setText(num);
        
        if(albumName != null)
        {
            final String artist = cursor.getString(cursor.getColumnIndex("ARTIST"));
            final String album  = cursor.getString(cursor.getColumnIndex("ALBUM"));
            final String year   = cursor.getString(cursor.getColumnIndex("YEAR"));
            final Bitmap art    = AlbumArtManager.getAlbumArt(artist, album, con);
            
            albumName.setText(album);
            albumArtist.setText(artist);
            albumYear.setText(year);
            albumArt.setImageBitmap(art);
        
            if(artistName != null)
            {   
                artistName.setText(artist);
                int[] colors = {Color.BLACK, highlightColor};
                GradientDrawable back1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                GradientDrawable back2 = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
                back1.setShape(GradientDrawable.RECTANGLE);
                back2.setShape(GradientDrawable.RECTANGLE);
                artistline1.setBackgroundDrawable(back1);
                artistline2.setBackgroundColor(highlightColor);
                artistline3.setBackgroundDrawable(back2);
            }
        }
        
        if(path.equals(now_playing))
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
        
        return view;
    }

    public void setNowPlaying(String nowPlayingFile)
    {
        now_playing = nowPlayingFile;
    }

    private int isNewSection(int position)
    {
        if(position == 0)
        {
            return NEW_ARTIST;
        }
        else
        {
            Cursor cursor = getCursorForPosition(position);
            String currentAlbum = cursor.getString(cursor.getColumnIndex("ALBUM"));
            String currentArtist = cursor.getString(cursor.getColumnIndex("ARTIST"));
            
            cursor = getCursorForPosition(position-1);
            
            String previousAlbum = cursor.getString(cursor.getColumnIndex("ALBUM"));
            String previousArtist = cursor.getString(cursor.getColumnIndex("ARTIST"));
            
            cursor = getCursorForPosition(position);
            
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
    
    public long getItemId(int position)
    {
        return position;
    }
    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
