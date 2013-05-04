package com.calcprogrammer1.calctunes.MediaInfo;

import com.calcprogrammer1.calctunes.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class MediaInfoView extends LinearLayout
{
    public MediaInfoView(Context context)
    {
        super(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.trackinfoview, this, true);
    }
    
    public MediaInfoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.trackinfoview, this, true);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        if(parentHeight < parentWidth)
        {
            this.findViewById(R.id.track_artwork).getLayoutParams().width=parentHeight;
            this.findViewById(R.id.track_artwork).getLayoutParams().height=parentHeight;
        }
        else
        {
            this.findViewById(R.id.track_artwork).getLayoutParams().width=parentWidth;
            this.findViewById(R.id.track_artwork).getLayoutParams().height=parentWidth;
        }
        this.setMeasuredDimension(parentWidth, parentHeight);
    }
}
