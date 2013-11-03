package com.calcprogrammer1.calctunes.MediaPlayer;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class RemoteControlService extends Service
{

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent intent, int startid)
    {
        //Register receiver
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        RemoteControlReceiver receiver = new RemoteControlReceiver();
        registerReceiver(receiver, receiverFilter);
    }
}
