package com.calcprogrammer1.calctunes.MediaInfo;

import java.util.ArrayList;

import com.calcprogrammer1.calctunes.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MediaInfoAdapter extends BaseAdapter
{
    Context context;
    ArrayList<MediaInfoListType> values = new ArrayList<MediaInfoListType>();
    
    public MediaInfoAdapter(Context con)
    {
        super();
        context = con;
    }
    
    public void setData(ArrayList<MediaInfoListType> data)
    {
        values = data;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.mediainfoview_entry, parent, false);
        TextView label = (TextView) rowView.findViewById(R.id.label);
        TextView value = (TextView) rowView.findViewById(R.id.value);
        label.setText(values.get(position).label);
        value.setText(values.get(position).value);
        if(position % 2 != 0)
        {
            rowView.setBackgroundResource(R.color.holo_dark_background);
        }
        return rowView;
    }

    @Override
    public int getCount()
    {
        return values.size();
    }

    @Override
    public Object getItem(int arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0)
    {
        // TODO Auto-generated method stub
        return arg0;
    }

}
