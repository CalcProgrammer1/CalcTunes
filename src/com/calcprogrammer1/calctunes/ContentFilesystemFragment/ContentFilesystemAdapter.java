package com.calcprogrammer1.calctunes.ContentFilesystemFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.calcprogrammer1.calctunes.Interfaces.ContentFilesystemAdapterInterface;
import com.calcprogrammer1.calctunes.R;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ContentFilesystemAdapter extends BaseAdapter implements View.OnClickListener
{
    private ContentFilesystemAdapterInterface callback;
    private Context c;
    private String now_playing = new String();
    private int length;
    
    public File currentDirectory;
    public ArrayList<File> files;
    public ArrayList<String> checkList;

    public ContentFilesystemAdapter(Context c, String startPath)
    {
        this.c = c;
        currentDirectory = new File(startPath);
        buildFileList();
    }

    public void setCallback(ContentFilesystemAdapterInterface callback)
    {
        this.callback = callback;
    }

    public void setCheckList(ArrayList<String> checkList)
    {
        this.checkList = checkList;
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
            view = LayoutInflater.from(c).inflate(R.layout.contentfilesystementry, parent, false);
        }
        
        ImageView icon = (ImageView) view.findViewById(R.id.imageViewFilesystemEntry);
        TextView  text = (TextView)  view.findViewById(R.id.textViewFilesystemEntry);
        CheckBox check = (CheckBox)  view.findViewById(R.id.checkBoxFilesystemEntry);

        if(position == 0 && !currentDirectory.getPath().equals("/"))
        {
            icon.setImageResource(R.drawable.icon_folder);
            check.setEnabled(false);
            check.setVisibility(View.GONE);
            check.setOnClickListener(null);
            text.setText("..");
        }
        else
        {
            if(files.get(position).isDirectory())
            {
                icon.setImageResource(R.drawable.icon_folder);
                check.setEnabled(false);
                check.setVisibility(View.GONE);
                check.setOnClickListener(null);
            }
            else
            {
                icon.setImageResource(R.drawable.icon);
                check.setEnabled(true);
                check.setVisibility(View.VISIBLE);
                check.setTag(position);
                check.setOnClickListener(this);

                if(checkList.contains(files.get(position).getAbsolutePath()))
                {
                    check.setChecked(true);
                }
                else
                {
                    check.setChecked(false);
                }
            }
            text.setText(files.get(position).getName());
            if(files.get(position).getPath().equals(now_playing))
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
        }
        return view;
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.checkBoxFilesystemEntry:
                int position = ((Integer)v.getTag()).intValue();
                if(callback != null)
                {
                    callback.onCheckboxClicked(position, files.get(position).getAbsolutePath(), ((CheckBox)v).isChecked());
                }
                break;
        }
    }
}
