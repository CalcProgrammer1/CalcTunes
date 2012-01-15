package com.calcprogrammer1.calctunes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.SeekBar;

public class SeekHandler implements Runnable
{
    SeekBar sb;
    MediaPlayerHandler mp;
    Thread t;
    boolean running = false;
    boolean touch = false;
    public SeekHandler(SeekBar seekb, MediaPlayerHandler mediap)
    {
        updateSeekBar(seekb);
        mp = mediap;
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                if(mp != null)
                {
                    mp.seekPlayback(sb.getProgress());
                }
                touch = false;
            }
            
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                touch = true;
            }
            
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {           
            }
        });
        resume();
    }
    
    public void updateMediaPlayer(MediaPlayerHandler mediap)
    {
        mp = mediap;
    }
    
    public void setInterfaceColor(int color)
    {
        RadialGradient thumbgr = new RadialGradient(20, 20, 20, Color.BLACK, color, android.graphics.Shader.TileMode.CLAMP);
        Bitmap thumbbm = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        Canvas thumb = new Canvas(thumbbm);
        Paint  thumbp = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbp.setColor(Color.BLACK);
        thumb.drawCircle(10, 10, 10, thumbp);
        thumbp.setShader(thumbgr);
        thumb.drawCircle(10, 10, 8, thumbp);
        
        BitmapDrawable thumbdraw = new BitmapDrawable(thumbbm);
        thumbdraw.setBounds(new Rect(0, 0, thumbdraw.getIntrinsicWidth(), thumbdraw.getIntrinsicHeight()));
        sb.setThumb(thumbdraw);
        sb.setThumbOffset(0);
              
        int colors[] = {Color.BLACK, Color.BLACK, color, Color.BLACK, Color.BLACK};
        GradientDrawable progback1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        progback1.setShape(GradientDrawable.RECTANGLE);
        progback1.setCornerRadius(10);
        
        int colors2[] = {Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK};
        GradientDrawable progback2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors2);
        progback2.setShape(GradientDrawable.RECTANGLE);
        progback2.setCornerRadius(10);
        
        LayerDrawable prog = new LayerDrawable(new Drawable[] {progback1, progback2});
        
        Rect bounds = sb.getProgressDrawable().getBounds();
        sb.setProgressDrawable(prog);
        sb.getProgressDrawable().setBounds(bounds);
        sb.refreshDrawableState();
    }
    
    public void updateSeekBar(SeekBar seekb)
    {
        sb = seekb;
    }
    
    public void run()
    {
        while(running)
        {
            try
            {
                while(mp != null)
                {
                    if(!touch)
                    {
                        sb.setMax(mp.getDuration());
                        int currentPosition = mp.getCurrentPosition();
                        sb.setProgress(currentPosition);
                        Thread.sleep(250);
                    }
                }
                
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void pause()
    {
        running = false;
        while(true)
        {
            try
            {
                t.join();
            } catch (Exception e){}
            break;
        }
        t = null;
    }
    
    public void resume()
    {
        running = true;
        t = new Thread(this);
        t.start();
    }
}