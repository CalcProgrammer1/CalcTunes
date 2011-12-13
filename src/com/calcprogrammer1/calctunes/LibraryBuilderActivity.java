package com.calcprogrammer1.calctunes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.calcprogrammer1.calctunes.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class LibraryBuilderActivity extends Activity
{
    ListView librarylist;
    EditText libNameInput;
    ArrayList<String> libraryFolders;
    
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.librarybuilder);
        librarylist = (ListView) findViewById(R.id.libraryListView);
        libNameInput = (EditText) findViewById(R.id.libNameInput);
        libraryFolders = new ArrayList<String>();
    }
    
    public void AddFolderClick(View view)
    {
        Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
        startActivityForResult(intent, 1);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == Activity.RESULT_OK)
        {
            Uri dirname = data.getData();
            String newFolder = dirname.getPath();
            boolean match = false;
            for(int i = 0; i < libraryFolders.size(); i++)
            {
                if(newFolder.equals(libraryFolders.get(i)))
                {
                    match = true;
                }
            }
            if(!match)
            {
                libraryFolders.add(newFolder);
            }
            updateFolderList();
        }
    }
    
    public void updateFolderList()
    {
        librarylist.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, libraryFolders.toArray(new String[libraryFolders.size()])));
        librarylist.setItemsCanFocus(false);
        librarylist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }
    
    public void RemoveSelectedClick(View view)
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
    
    public void DoneClick(View view)
    {
        if(libNameInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(this).create();
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
            AlertDialog ad = new AlertDialog.Builder(this).create();
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
            Intent data = new Intent();
            data.putStringArrayListExtra("libraryFolders", libraryFolders);
            data.putExtra("libraryName", libNameInput.getText().toString());
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }
}
