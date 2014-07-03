package com.calcprogrammer1.calctunes.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.calcprogrammer1.calctunes.R;

import java.io.File;
import java.util.ArrayList;

public class FolderSelectionDialog extends Dialog implements View.OnClickListener
{
    String FolderPath = "/";
    EditText FolderText;
    ListView FolderListView;

    ArrayList<String> dir_list;
    ArrayList<String> lbl_list;

    boolean fileMode;

    public interface FolderSelectionDialogCallback
    {
        void onCompleted(String folderPath);
    }

    FolderSelectionDialogCallback callback = null;

    public FolderSelectionDialog(Context context)
    {
        super(context);
        fileMode = false;
    }

    public FolderSelectionDialog(Context context, boolean file)
    {
        super(context);
        fileMode = file;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.folderdialog);

        if(fileMode)
        {
            setTitle("Select File");
        }
        else
        {
            setTitle("Select Folder");
        }

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.FILL_PARENT;
        params.width = ViewGroup.LayoutParams.FILL_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        Button SelectFolder = (Button) findViewById(R.id.buttonSelectFolder);
        Button UpFolder = (Button) findViewById(R.id.buttonUpFolder);
        FolderText = (EditText) findViewById(R.id.editTextFolderPath);
        FolderListView = (ListView) findViewById(R.id.listViewFolderDialog);

        SelectFolder.setOnClickListener(this);
        UpFolder.setOnClickListener(this);

        updateFolderList();

        FolderListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                FolderPath = dir_list.get(position);
                updateFolderList();
            }
        });
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.buttonSelectFolder:
                FolderPath = FolderText.getText().toString();
                if(callback != null)
                {
                    callback.onCompleted(FolderPath);
                }
                dismiss();
                break;

            case R.id.buttonUpFolder:
                File parent = new File(FolderPath);
                if(parent.getParent() != null)
                {
                    FolderPath = parent.getParent();
                    updateFolderList();
                }
                break;
        }
    }

    public void updateFolderList()
    {
        File currentDirectory = new File(FolderPath);
        lbl_list = new ArrayList<String>();
        dir_list = new ArrayList<String>();
        if(currentDirectory.listFiles() != null)
        {
            for(int i = 0; i < currentDirectory.listFiles().length; i++)
            {
                if(currentDirectory.listFiles()[i].isDirectory())
                {
                    lbl_list.add(currentDirectory.listFiles()[i].getPath());
                    dir_list.add(currentDirectory.listFiles()[i].getAbsolutePath());
                }
                if(fileMode && currentDirectory.listFiles()[i].isFile())
                {
                    lbl_list.add(currentDirectory.listFiles()[i].getPath());
                    dir_list.add(currentDirectory.listFiles()[i].getAbsolutePath());
                }
            }
        }
        FolderListView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, lbl_list.toArray(new String[lbl_list.size()])));
        FolderText.setText(FolderPath);
    }

    public void setFolderSelectionDialogCallback(FolderSelectionDialogCallback call)
    {
        callback = call;
    }
}
