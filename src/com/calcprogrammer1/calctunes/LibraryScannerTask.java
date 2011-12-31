package com.calcprogrammer1.calctunes;

import java.io.File;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

interface LibraryScannerTaskCallback
{
    void onScanComplete();
}

public class LibraryScannerTask extends AsyncTask<String, Integer, Long>
{
    private Context con;
    private String libraryName;
    private int notificationId = 1;
    private RemoteViews notificationView;
    private Notification notification;
    private NotificationManager notificationManager;
    private LibraryScannerTaskCallback cb;
    
    public LibraryScannerTask(Context c)
    {
        con = c;
        cb = null;
    }
    
    public void setCallback(LibraryScannerTaskCallback call)
    {
        cb = call;
    }
    
    @Override
    protected Long doInBackground(String... libName)
    {
        libraryName = libName[0];
        ArrayList<String> libraryFolders = LibraryOperations.readLibraryFile(LibraryOperations.getLibraryFullPath(con, libraryName));
        ArrayList<File> libraryFiles = new ArrayList<File>();
        
        for(int i = 0; i < libraryFolders.size(); i++)
        {
            File f = new File(libraryFolders.get(i));
            FileOperations.addFilesRecursively(f, libraryFiles);
        }
        
        initializeNotification(libraryFiles.size());
        
        LibraryDatabaseHelper db = new LibraryDatabaseHelper(con, libraryName + ".db");
        db.startDatabase();
        for(int i = 0; i < libraryFiles.size(); i++)
        {
            db.addFileToDatabase(libraryFiles.get(i));
            if(i % 20 == 0)
            {
                updateNotification(libraryFiles.size(), i);
            }
        }
        db.closeDatabase();
        updateNotification(libraryFiles.size(), libraryFiles.size());
        notificationManager.cancel(notificationId);
        return null;
    }

    @Override
    protected void onPostExecute(Long result)
    {
        if(cb != null)
        {
            cb.onScanComplete();
        }
    }
    
    private void initializeNotification(int length)
    {
        int icon = R.drawable.icon;
        CharSequence notificationText = "Updating Library \"" + libraryName + "\"";
        long when = System.currentTimeMillis();
        
        notificationView = new RemoteViews(con.getPackageName(), R.layout.libraryupdatingnotification);
        notificationView.setImageViewResource(R.id.libupdatenotification_image, R.drawable.icon);
        notificationView.setTextViewText(R.id.libupdatenotification_title, "CalcTunes: Updating \"" + libraryName + "\" - 0/" + length);
        notificationView.setProgressBar(R.id.libupdatenotification_progress, length, 0, false);
        
        notification = new Notification(icon, notificationText, when);
        notification.contentView = notificationView;
        notification.contentIntent = PendingIntent.getActivity(con.getApplicationContext(), 0, new Intent(), 0);
        
        
        notificationManager = (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }
    
    private void updateNotification(int length, int progress)
    {
        notificationView.setTextViewText(R.id.libupdatenotification_title, "CalcTunes: Updating \"" + libraryName + "\" - " + progress + "/" + length);
        notificationView.setProgressBar(R.id.libupdatenotification_progress, length, progress, false);
        notificationManager.notify(notificationId, notification);
    }
}
