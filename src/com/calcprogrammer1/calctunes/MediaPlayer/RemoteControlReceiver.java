package com.calcprogrammer1.calctunes.MediaPlayer;

import com.calcprogrammer1.calctunes.Interfaces.MediaButtonsHandlerInterface;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver
{    
    AudioManager am;
    ComponentName cn;
    
    Context c;
    MediaButtonsHandlerInterface cb;
    
    public void setCallback(MediaButtonsHandlerInterface call)
    {
        cb = call;
    }
    
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
