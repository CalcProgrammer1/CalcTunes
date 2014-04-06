package com.calcprogrammer1.calctunes.Dialogs;

import java.io.File;
import java.util.ArrayList;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.Library.LibraryScannerTask;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.SourceTypes.LibrarySource;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class LibraryBuilderDialog extends Dialog implements View.OnClickListener
{
    Activity activity;
    ListView librarylist;
    EditText libNameInput;
    ArrayList<String> libraryFolders;
    Intent i;
    String editFilename = null;

    public LibraryBuilderDialog(Activity act)
    {
        super(act);
    }

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.librarybuilder);
        setTitle("Create New Library");

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.FILL_PARENT;
        params.width = ViewGroup.LayoutParams.FILL_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        Button AddFolder = (Button) findViewById(R.id.buttonLibAddFolder);
        Button RemoveSelected = (Button) findViewById(R.id.buttonLibRemoveSelected);
        Button Done = (Button) findViewById(R.id.buttonLibDone);

        AddFolder.setOnClickListener(this);
        RemoveSelected.setOnClickListener(this);
        Done.setOnClickListener(this);

        librarylist = (ListView) findViewById(R.id.libraryListView);
        libNameInput = (EditText) findViewById(R.id.libNameInput);
        

        libraryFolders = new ArrayList<String>();
        if(editFilename != null)
        {
            setTitle("Edit Existing Library");
            LibrarySource EditLib = SourceListOperations.readLibraryFile(editFilename);
            String name = EditLib.name;
            libraryFolders = EditLib.folders;
            libNameInput.setText(name);
            updateFolderList();
        }
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.buttonLibAddFolder:
                AddFolderClick();
                break;

            case R.id.buttonLibRemoveSelected:
                RemoveSelectedClick();
                break;

            case R.id.buttonLibDone:
                DoneClick();
                break;
        }
    }

    public void EditExistingLibrary(String libFilename)
    {
        editFilename = libFilename;
    }

    public void AddFolderClick()
    {
        FolderSelectionDialog dialog = new FolderSelectionDialog(getContext());
        dialog.show();
        dialog.setFolderSelectionDialogCallback(new FolderSelectionDialog.FolderSelectionDialogCallback()
        {
            @Override
            public void onCompleted(String folderPath)
            {
                boolean match = false;

                for(int i = 0; i < libraryFolders.size(); i++)
                {
                    if(folderPath.equals(libraryFolders.get(i)))
                    {
                        match = true;
                    }
                }
                if(!match)
                {
                    libraryFolders.add(folderPath);
                }
                updateFolderList();
            }
        });
    }
    
    public void updateFolderList()
    {
        librarylist.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_multiple_choice, libraryFolders.toArray(new String[libraryFolders.size()])));
        librarylist.setItemsCanFocus(false);
        librarylist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }
    
    public void RemoveSelectedClick()
    {
        ArrayList<Integer> indicesToRemove = new ArrayList<Integer>();
        for(int i = 0; i < libraryFolders.size(); i++)
        {
            if(librarylist.isItemChecked(i))
            {
                indicesToRemove.add(i);
            }
        }
        for(int i = 0; i < indicesToRemove.size(); i++)
        {
            libraryFolders.remove(indicesToRemove.get(i).intValue()-i);
        }
        updateFolderList();
    }
    
    @SuppressWarnings("deprecation")
    public void DoneClick()
    {
        if(libNameInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a library name to continue.");
            ad.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else if(libraryFolders.size() == 0)
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please select at least one folder to continue.");
            ad.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else
        {
            LibrarySource lib = new LibrarySource();
            lib.name = libNameInput.getText().toString();
            lib.filename = SourceListOperations.getLibraryPath(getContext()) + "/" + SourceListOperations.getFilenameXml(lib.name);
            lib.folders = libraryFolders;
            
            if(editFilename != null)
            {
                File deleteFile = new File(editFilename);
                deleteFile.delete();
            }
            
            SourceListOperations.writeLibraryFile(lib);

            LibraryScannerTask task = new LibraryScannerTask(getContext());
            task.execute(lib.name);
            Intent broadcast = new Intent();
            broadcast.setAction("com.calcprogrammer1.calctunes.SOURCE_REFRESH_EVENT");
            getContext().sendBroadcast(broadcast);
            dismiss();
        }
    }
}
