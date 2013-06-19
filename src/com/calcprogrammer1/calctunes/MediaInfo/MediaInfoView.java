package com.calcprogrammer1.calctunes.MediaInfo;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.Interfaces.MediaInfoViewInterface;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class MediaInfoView extends FrameLayout
{
    private int  current = 0;
    private View layout = null;
    private Context con;
    private MediaInfoViewInterface callback;
    
    public MediaInfoView(Context context)
    {
        super(context);
        con = context;
    }
    
    public MediaInfoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        con = context;
    }
    
    public void registerCallback(MediaInfoViewInterface callb)
    {
        callback = callb;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {        
        LayoutInflater inflater = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("MediaInfoView", "OnMeasure: " + parentWidth + "x" + parentHeight);
        if(parentHeight < parentWidth)
        {
            if(current != 1)
            {
                removeView(layout);
                layout = inflater.inflate(R.layout.mediainfoview_landscape, null, false);
                addView(layout);
                Log.d("MediaInfoView", "Inflating landscape layout");
                this.findViewById(R.id.track_artwork).getLayoutParams().width=parentHeight;
                this.findViewById(R.id.track_artwork).getLayoutParams().height=parentHeight;
                invalidate();
                if(callback != null)
                {
                    callback.onLayoutReloaded();
                }
                current = 1;
            }
        }
        else
        {
            if(current != 2)
            {
                removeView(layout);
                layout = inflater.inflate(R.layout.mediainfoview_portrait, null, false);
                addView(layout);
                Log.d("MediaInfoView", "Inflating portrait layout");
                findViewById(R.id.track_artwork).getLayoutParams().width=parentWidth;
                findViewById(R.id.track_artwork).getLayoutParams().height=parentWidth;
                invalidate();
                if(callback != null)
                {
                    callback.onLayoutReloaded();
                }
                current = 2;
            }
        }
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
