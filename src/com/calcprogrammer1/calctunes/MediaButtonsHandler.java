package com.calcprogrammer1.calctunes;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

interface MediaButtonsHandlerCallback
{
    public void onMediaNextPressed();
    public void onMediaPrevPressed();
    public void onMediaPlayPausePressed();
    public void onMediaStopPressed();
}

public class MediaButtonsHandler
{    
    AudioManager am;
    ComponentName cn;
    
    Context c;
    MediaButtonsHandlerCallback cb;
    
    public MediaButtonsHandler(Context con)
    {
        c = con;
        am = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
        cn = new ComponentName(c.getPackageName(), RemoteControlReceiver.class.getName());
        registerButtons();
    }
    
    public void registerButtons()
    {
        am.registerMediaButtonEventReceiver(new ComponentName(c.getPackageName(), RemoteControlReceiver.class.getName()));
    }
    
    public void setCallback(MediaButtonsHandlerCallback call)
    {
        cb = call;
    }
    
    public class RemoteControlReceiver extends BroadcastReceiver
    {
        public RemoteControlReceiver()
        {
            super();
        }
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) 
            {

                KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if(event == null)
                {
                    return;
                }
                int action = event.getAction();
                if(action == KeyEvent.ACTION_DOWN)
                {
                    if(event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT)
                    {
                        if(cb != null) cb.onMediaNextPressed();
                    }
                }
                abortBroadcast();
            }
        }
    }
}
