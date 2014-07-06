package com.calcprogrammer1.calctunes.Subsonic;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

public class SubsonicDownloaderService extends Service
{
    private Handler handler;
    private class downloadJob
    {
        public String   url;
        public String   user;
        public String   password;
        public String   downloadPath;
        public int      id;
        public boolean  transcode;
        public String   format;
        public int      bitRate;
    }

    private ArrayList<downloadJob> downloadQueue = new ArrayList<downloadJob>();

    private boolean isDownloading;

    public void queueDownload(downloadJob job)
    {
        Log.d("SubsonicDownloaderService", "Queueing new job " + job.id + " from URL " + job.url);
        downloadQueue.add(job);
        if(!isDownloading)
        {
            startDownloaderThread();
        }
    }

    private void processDownloading()
    {
        isDownloading = true;

        //If queue is empty, return
        if(downloadQueue.isEmpty())
        {
            return;
        }

        //Pop first job off of queue
        downloadJob job = downloadQueue.get(0);
        downloadQueue.remove(job);

        SubsonicAPI sub = new SubsonicAPI(job.url, job.user, job.password);

        Log.d("SubsonicDownloaderService", "Downloading job " + job.id + " from URL " + job.url);

        if(sub.SubsonicPing() && sub.SubsonicGetLicense())
        {
            if (job.transcode)
            {
                String downloadUrl = sub.SubsonicStreamURL(job.id, job.format, job.bitRate);
                CalcTunesXMLParser.getFileFromUrl(downloadUrl, job.downloadPath);
            } else
            {
                String downloadUrl = sub.SubsonicDownloadURL(job.id);
                CalcTunesXMLParser.getFileFromUrl(downloadUrl, job.downloadPath);
            }
        }

        Log.d("SubsonicDownloaderService", "Download job " + job.id + " completed");

        sendBroadcast(job, true);

        isDownloading = false;
    }

    private void sendBroadcast(downloadJob job, boolean finished)
    {
        Intent broadcast = new Intent();
        broadcast.putExtra("id", job.id);
        broadcast.putExtra("transcode", job.transcode);
        broadcast.putExtra("finished", finished);
        broadcast.setAction("com.calcprogrammer1.calctunes.SUBSONIC_DOWNLOADED_EVENT");
        getApplicationContext().sendBroadcast(broadcast);
    }

    private void startDownloaderThread()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                Log.d("SubsonicDownloaderThread", "Downloader thread started");
                while(!downloadQueue.isEmpty())
                {
                    try
                    {
                        processDownloading();
                        //handler.sendEmptyMessage(0);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                Log.d("SubsonicDownloaderThread", "Downloader thread ended");
            }
        }).start();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////Service Functions//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final IBinder mBinder = new ContentPlaybackBinder();

    public class ContentPlaybackBinder extends Binder
    {
        public SubsonicDownloaderService getService()
        {
            return SubsonicDownloaderService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.d("SubsonicDownloaderService", "onStartCommand");
        Bundle extras = intent.getExtras();

        downloadJob job = new downloadJob();

        job.url             = extras.getString("url");
        job.user            = extras.getString("user");
        job.password        = extras.getString("password");
        job.id              = extras.getInt("id");
        job.transcode       = extras.getBoolean("transcode");
        job.bitRate         = extras.getInt("bitRate");
        job.format          = extras.getString("format");
        job.downloadPath    = extras.getString("downloadPath");

        sendBroadcast(job, false);

        queueDownload(job);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate()
    {

    }

    @Override
    public void onDestroy()
    {
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return mBinder;
    }

}
