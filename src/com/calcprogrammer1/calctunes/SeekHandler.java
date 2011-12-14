package com.calcprogrammer1.calctunes;

import android.media.MediaPlayer;
import android.widget.SeekBar;

public class SeekHandler implements Runnable
{
    SeekBar sb;
    MediaPlayer mp;
    Thread t;
    boolean running = false;
    
    public SeekHandler(SeekBar seekb, MediaPlayer mediap)
    {
        sb = seekb;
        mp = mediap;
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                if(mp != null)
                {
                    mp.seekTo(sb.getProgress());
                }
            }
            
            public void onStartTrackingTouch(SeekBar seekBar)
            {           
            }
            
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {           
            }
        });
        resume();
    }
    
    public void updateMediaPlayer(MediaPlayer mediap)
    {
        mp = mediap;
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
                    sb.setMax(mp.getDuration());
                    int currentPosition = mp.getCurrentPosition();
                    sb.setProgress(currentPosition);
                    Thread.sleep(250);
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