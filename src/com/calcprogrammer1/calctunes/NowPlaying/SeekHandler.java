package com.calcprogrammer1.calctunes.NowPlaying;

import com.calcprogrammer1.calctunes.ContentPlaybackService;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.SeekBar;

public class SeekHandler implements Runnable
{
    SeekBar sb;
    ContentPlaybackService mp;
    Thread t;
    Resources res;
    boolean running = false;
    boolean touch = false;
    public SeekHandler(SeekBar seekb, ContentPlaybackService playbackservice, Context con)
    {
        res = con.getResources();
        updateSeekBar(seekb);

        mp = playbackservice;
        sb = seekb;
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                if(mp != null)
                {
                    mp.SeekPlayback(sb.getProgress());
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
    
    public void updateMediaPlayer(ContentPlaybackService playbackservice)
    {
        mp = playbackservice;
    }
    
    private int px_to_dip(int x)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, res.getDisplayMetrics());
    }
    
    @SuppressWarnings("deprecation")
    public void setInterfaceColor(int color)
    {
        int dip20 = px_to_dip(20);
        int dip40 = px_to_dip(40);
        Bitmap thumbbm = Bitmap.createBitmap(dip40, dip40, Bitmap.Config.ARGB_8888);
        Canvas thumb = new Canvas(thumbbm);
        Paint  thumbp = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbp.setColor(Color.argb(Color.alpha(color)/2, Color.red(color), Color.green(color), Color.blue(color)));
        thumb.drawCircle(dip20, dip20, dip20, thumbp);
        thumbp.setColor(color);
        thumb.drawCircle(dip20, dip20, px_to_dip(8), thumbp);
        
        BitmapDrawable thumbdraw = new BitmapDrawable(thumbbm);
        thumbdraw.setBounds(new Rect(0, 0, thumbdraw.getIntrinsicWidth(), thumbdraw.getIntrinsicHeight()));
        sb.setThumb(thumbdraw);
        sb.setThumbOffset(0);
              
        DisplayMetrics metrics = res.getDisplayMetrics();
        sb.measure(metrics.widthPixels, metrics.heightPixels);
        Log.d("test", "screen width " + metrics.widthPixels);
        Log.d("test", "screen height " + metrics.heightPixels);
        Log.d("test", "bar width " + sb.getMeasuredWidth());
        Log.d("test", "bar height " + sb.getMeasuredHeight());
        Bitmap progbm = Bitmap.createBitmap(1, dip40, Bitmap.Config.ARGB_8888);
        Canvas prog = new Canvas(progbm);
        Paint progp = new Paint(Paint.ANTI_ALIAS_FLAG);
        progp.setColor(Color.GRAY);
        prog.drawLine(0, dip20, 1, dip20, progp);
        BitmapDrawable progdraw = new BitmapDrawable(progbm);
        
       
        Rect bounds = sb.getProgressDrawable().getBounds();
        Log.d("test", "rect " + bounds.flattenToString());
        bounds.offset(dip20, 0);
        sb.setProgressDrawable(progdraw);
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
                        sb.setMax(mp.NowPlayingDuration());
                        int currentPosition = mp.NowPlayingPosition();
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
        t = null;
    }
    
    public void resume()
    {
        running = true;
        t = new Thread(this);
        t.start();
    }
}