package com.calcprogrammer1.calctunes.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.calcprogrammer1.calctunes.FileOperations;
import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.audio.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class FolderReorganizeDialog extends Dialog implements View.OnClickListener
{
    String sourcePath = "";
    String destPath   = "";

    EditText sourcePathText;
    EditText destPathText;
    TextView FileText;

    ProgressBar progress;
    CheckBox checkRecursive;
    Button sourcePathButton;
    Button destPathButton;
    Button startButton;

    boolean recursive;

    public FolderReorganizeDialog(Activity act, String reorganizePath)
    {
        super(act);
        sourcePath = reorganizePath;
        destPath = reorganizePath;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.reorganizedialog);
        setTitle("Reorganize Music Folder");

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.FILL_PARENT;
        params.width = ViewGroup.LayoutParams.FILL_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        sourcePathText = (EditText) findViewById(R.id.editTextOrgSourceFolder);
        destPathText   = (EditText) findViewById(R.id.editTextOrgDestFolder);
        sourcePathButton = (Button) findViewById(R.id.buttonOrgSource);
        destPathButton = (Button)   findViewById(R.id.buttonOrgDest);
        startButton    = (Button)   findViewById(R.id.buttonOrgStart);
        FileText       = (TextView) findViewById(R.id.textViewOrgFile);
        progress    = (ProgressBar) findViewById(R.id.progressBarOrg);
        checkRecursive = (CheckBox) findViewById(R.id.checkBoxOrgRecursive);

        sourcePathText.setText(sourcePath);
        destPathText.setText(destPath);

        sourcePathButton.setOnClickListener(this);
        destPathButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.buttonOrgSource:
                {
                    FolderSelectionDialog dialog = new FolderSelectionDialog(getContext());
                    dialog.show();
                    dialog.setFolderSelectionDialogCallback(new FolderSelectionDialog.FolderSelectionDialogCallback()
                    {
                        @Override
                        public void onCompleted(String folderPath)
                        {
                            sourcePathText.setText(folderPath);
                        }
                    });
                }
                break;

            case R.id.buttonOrgDest:
                {
                    FolderSelectionDialog dialog = new FolderSelectionDialog(getContext());
                    dialog.show();
                    dialog.setFolderSelectionDialogCallback(new FolderSelectionDialog.FolderSelectionDialogCallback()
                    {
                        @Override
                        public void onCompleted(String folderPath)
                        {
                            destPathText.setText(folderPath);
                        }
                    });
                }
                break;

            case R.id.buttonOrgStart:
                {
                recursive = checkRecursive.isChecked();
                setCancelable(false);
                startButton.setEnabled(false);
                ReorganizeTask task = new ReorganizeTask();
                task.execute();
                }
                break;
        }
    }

    private class ReorganizeTask extends AsyncTask<Void, String, Void>
    {
        private int i;
        private int j;
        protected Void doInBackground(Void... v)
        {
            ArrayList<File> reorganizeFiles = new ArrayList<File>();

            if(recursive)
            {
                FileOperations.addFilesRecursively(new File(sourcePath), reorganizeFiles);
            }
            else
            {
                FileOperations.addFiles(new File(sourcePath), reorganizeFiles);
            }

            j = reorganizeFiles.size();

            for(i = 0; i < j; i++)
            {
                if(reorganizeFiles.get(i).isFile())
                {
                    String filename = reorganizeFiles.get(i).getAbsolutePath().toString();

                    publishProgress(filename);

                    AudioFile f = SourceListOperations.readAudioFileReadOnly(reorganizeFiles.get(i));
                    Tag tag = f.getTag();

                    String extension = filename.substring(filename.lastIndexOf('.')+1);
                    String artist = tag.getFirst(FieldKey.ARTIST);
                    String album  = tag.getFirst(FieldKey.ALBUM);
                    String song   = tag.getFirst(FieldKey.TITLE);
                    int track     = new Scanner( tag.getFirst(FieldKey.TRACK) ).useDelimiter("\\D+").nextInt();

                    String newPath = destPath + "/" + SourceListOperations.makeFilename(artist) + "/" + SourceListOperations.makeFilename(album) + "/";

                    new File(newPath).mkdirs();

                    newPath += String.format("%02d", track) + " " + SourceListOperations.makeFilename(song) + "." + extension;

                    new File(filename).renameTo(new File(newPath));
                }
            }
            return(null);
        }

        protected void onProgressUpdate(String... filename)
        {
            FileText.setText(filename[0]);
            progress.setMax(j);
            progress.setProgress(i);
        }

        protected void onPostExecute(Void v)
        {
            setCancelable(true);
            startButton.setEnabled(true);
            FileText.setText("Done!");
        }
    }
}
