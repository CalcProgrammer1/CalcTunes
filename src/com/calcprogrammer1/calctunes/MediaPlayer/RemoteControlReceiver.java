package com.calcprogrammer1.calctunes.MediaPlayer;

import com.calcprogrammer1.calctunes.ContentPlaybackService;
import com.calcprogrammer1.calctunes.Interfaces.MediaButtonsHandlerInterface;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

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
        appSettings = PreferenceManager.getDefaultSharedPreferences(context);

        boolean car_mode   = appSettings.getBoolean("car_mode", false);
        boolean hp_mode    = appSettings.getBoolean("hp_mode", false);
        boolean auto_close = appSettings.getBoolean("auto_close", false);
        boolean bkgd_only  = appSettings.getBoolean("bkgd_only", false);

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
                    if(car_mode)
                    {
                        if(auto_close)
                        {
                            closeApplication(context, bkgd_only);
                        }
                        else
                        {

                        }
                    }
                    break;
                
                case 1:
                    Log.d("RemoteControlReceiver", "Bluetooth A2DP action: connecting");
                    break;
                    
                case 2:
                    Log.d("RemoteControlReceiver", "Bluetooth A2DP action: connected");
                    if(car_mode)
                    {
                        openApplication(context, bkgd_only);
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
            if(isInitialStickyBroadcast())
            {
                Log.d("RemoteControlReceiver", "Headset state: " + state);
            }
            else
            {
                switch(state)
                {
                    case 0:
                        Log.d("RemoteControlReceiver", "Headset plug action: unplugged");
                        if(auto_close)
                        {
                            closeApplication(context, bkgd_only);
                        }
                        break;
                        
                    case 1:
                        Log.d("RemoteControlReceiver", "Headset plug action: plugged");
                        if(hp_mode)
                        {
                            openApplication(context, bkgd_only);
                        }
                        break;
                }
            }
        }
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
        {
            Log.d("RemoteControlReceiver", "Boot completed");
            Intent serviceIntent = new Intent(context, com.calcprogrammer1.calctunes.MediaPlayer.RemoteControlService.class);
            context.startService(serviceIntent);
        }
    }
    
    private void openApplication(Context context, boolean bkgd_only)
    {
        if(bkgd_only)
        {
            Intent intent = new Intent(context, ContentPlaybackService.class);
            intent.putExtra("auto_start", 1);
            context.startService(intent);
        }
        else
        {
            Intent activity = new Intent(context, com.calcprogrammer1.calctunes.Activities.CalcTunesActivity.class);
            activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activity);
        }
    }
    
    private void closeApplication(Context context, boolean bkgd_only)
    {
        //if(bkgd_only)
        //{
            context.stopService(new Intent(context, ContentPlaybackService.class));   
        //}
        //else
        //{
            Intent broadcast = new Intent();
            broadcast.setAction("com.calcprogrammer1.calctunes.CLOSE_APP_EVENT");
            context.sendBroadcast(broadcast);   
        //}
    }
}
