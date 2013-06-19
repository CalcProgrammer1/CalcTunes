package com.calcprogrammer1.calctunes.ContentFilesystemFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.calcprogrammer1.calctunes.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContentFilesystemAdapter extends BaseAdapter
{
    private Context con;
    private String now_playing = new String();
    private int length;
    private int interface_color;
    
    public File currentDirectory;
    public ArrayList<File> files;
    
    public ContentFilesystemAdapter(Context c, String startPath)
    {
        con = c;
        currentDirectory = new File(startPath);
        buildFileList();
    }
    
    public void buildFileList()
    {
        files = new ArrayList<File>();
        if(!currentDirectory.getPath().equals("/"))
        {
            files.add(new File(""));
            if(currentDirectory.listFiles() != null)
            {
                length = currentDirectory.listFiles().length + 1;
            }
            else
            {
                length = 1;
            }
        }
        else
        {
            if(currentDirectory.listFiles() != null)
            {
            length = currentDirectory.listFiles().length;
            }
        }
        ArrayList<File> file_list  = new ArrayList<File>();
        if(currentDirectory.listFiles() != null)
        {
            for(int i = 0; i < currentDirectory.listFiles().length; i++)
            {
                if(currentDirectory.listFiles()[i].isDirectory())
                {
                    files.add(currentDirectory.listFiles()[i]);
                }
                else
                {
                    file_list.add(currentDirectory.listFiles()[i]);
                }
            }
        }
        
        Comparator<? super File> filecomparator = new Comparator<File>()
        {
            public int compare(File file1, File file2)
            {
                return String.valueOf(file1.getName()).compareTo(file2.getName());
            }
        };

        Collections.sort(files, filecomparator);
        Collections.sort(file_list, filecomparator);
        files.addAll(file_list);
    }
    
    public void setNowPlaying(String nowPlayingFile)
    {
        now_playing = nowPlayingFile;
    }
    
    public void changeDirectory(String newDirectory)
    {
        currentDirectory = new File(newDirectory);
        notifyDataSetChanged();
    }
    
    public int getCount()
    {
        return length;
    }

    public Object getItem(int position)
    {
        return null;
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = null;
        
        if(convertView != null)
        {
            view = convertView;
        }
        else
        {
            view = LayoutInflater.from(con).inflate(R.layout.contentfilesystementry, parent, false);
        }
        
        ImageView icon = (ImageView) view.findViewById(R.id.contentfilesystementry_icon);
        TextView  text = (TextView)  view.findViewById(R.id.contentfilesystementry_text);
        
        if(position == 0 && !currentDirectory.getPath().equals("/"))
        {
            icon.setImageResource(R.drawable.icon_folder);
            text.setText("..");
        }
        else
        {
            if(files.get(position).isDirectory())
            {
                icon.setImageResource(R.drawable.icon_folder);
            }
            else
            {
                icon.setImageResource(R.drawable.icon);
            }
            text.setText(files.get(position).getName());
            if(files.get(position).getPath().equals(now_playing))
            {
                view.setBackgroundColor(interface_color);
            }
            else
            {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        
        
        return view;
    }

    public void setNowPlayingColor(int interfaceColor)
    {
        interface_color = interfaceColor;
    }

}
