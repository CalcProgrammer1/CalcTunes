package com.calcprogrammer1.calctunes.MediaPlayer;

import com.calcprogrammer1.calctunes.Interfaces.MediaButtonsHandlerInterface;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver
{    
    AudioManager am;
    ComponentName cn;
    
    Context c;
    MediaButtonsHandlerInterface cb;
    
    //Shared Preferences
    private SharedPreferences appSettings;
    
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
        //Get the application preferences
        appSettings = context.getSharedPreferences("CalcTunes", Context.MODE_PRIVATE);

        boolean car_mode = appSettings.getBoolean("car_mode", false);
        boolean hp_mode  = appSettings.getBoolean("hp_mode", false);
        
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
                Intent broadcast = new Intent();
                broadcast.putExtra("keyEvent", event.getKeyCode());
                broadcast.setAction("com.calcprogrammer1.calctunes.REMOTE_BUTTON_EVENT");
                context.sendBroadcast(broadcast);
            }
            if(action == KeyEvent.KEYCODE_HEADSETHOOK)
            {
                //context.startActivity(new Intent(context, com.calcprogrammer1.calctunes.Activities.CalcTunesActivity.class));    
            }
            abortBroadcast();
        }
        if ("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED".equals(intent.getAction()))
        {
            int state = intent.getIntExtra("android.bluetooth.profile.extra.STATE", -2);
            
            switch(state)
            {
                default:
                case 0:
                    Log.d("RemoteControlReceiver", "Bluetooth A2DP action: disconnected");
                    break;
                
                case 1:
                    Log.d("RemoteControlReceiver", "Bluetooth A2DP action: connecting");
                    break;
                    
                case 2:
                    Log.d("RemoteControlReceiver", "Bluetooth A2DP action: connected");
                    if(car_mode)
                    {
                        Intent activity = new Intent(context, com.calcprogrammer1.calctunes.Activities.CalcTunesActivity.class);
                        activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(activity);
                    }
                    break;
                    
                case 3:
                    Log.d("RemoteControlReceiver", "Bluetooth A2DP action: disconnecting");
                    break;
            }
        }
        if(Intent.ACTION_HEADSET_PLUG.equals(intent.getAction()))
        {
            int state = intent.getIntExtra("state", -1);
            
            switch(state)
            {
                case 0:
                    Log.d("RemoteControlReceiver", "Headset plug action: unplugged");
                    break;
                    
                case 1:
                    Log.d("RemoteControlReceiver", "Headset plug action: plugged");
                    if(hp_mode)
                    {
                        Intent activity = new Intent(context, com.calcprogrammer1.calctunes.Activities.CalcTunesActivity.class);
                        activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(activity);
                    }
                    break;
            }
        }
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
        {
            Log.d("RemoteControlReceiver", "Boot completed");
            Intent serviceIntent = new Intent(context, com.calcprogrammer1.calctunes.MediaPlayer.RemoteControlService.class);
            context.startService(serviceIntent);
        }
    }
}
