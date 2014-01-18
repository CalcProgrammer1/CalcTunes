package com.calcprogrammer1.calctunes.Dialogs;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.SourceTypes.SubsonicSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

public class SubsonicBuilderDialog extends Dialog implements View.OnClickListener
{
    EditText srvNameInput;
    EditText srvAddrInput;
    EditText srvPortInput;
    EditText srvUserInput;
    EditText srvPassInput;
    EditText srvTransInput;
    EditText srvOrigInput;
    Button   buttonDone;
    Button   buttonTrans;
    Button   buttonOrig;

    String editFilename = null;

    public SubsonicBuilderDialog(Activity act)
    {
        super(act);
    }

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subsonicbuilder);
        setTitle("Create New Subsonic Connection");

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.FILL_PARENT;
        params.width = ViewGroup.LayoutParams.FILL_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        srvNameInput  = (EditText) findViewById(R.id.srvNameInput);
        srvAddrInput  = (EditText) findViewById(R.id.srvAddrInput);
        srvPortInput  = (EditText) findViewById(R.id.srvPortInput);
        srvUserInput  = (EditText) findViewById(R.id.srvUserInput);
        srvPassInput  = (EditText) findViewById(R.id.srvPassInput);
        srvTransInput = (EditText) findViewById(R.id.srvTransInput);
        srvOrigInput  = (EditText) findViewById(R.id.srvOrigInput);
        buttonDone    = (Button)   findViewById(R.id.buttonSubsonicDone);
        buttonTrans   = (Button)   findViewById(R.id.buttonSubsonicTrans);
        buttonOrig    = (Button)   findViewById(R.id.buttonSubsonicOrig);

        buttonDone.setOnClickListener(this);
        buttonTrans.setOnClickListener(this);
        buttonOrig.setOnClickListener(this);

        if(editFilename != null)
        {
            setTitle("Edit Subsonic Connection");
            SubsonicSource EditSub = SourceListOperations.readSubsonicFile(editFilename);
            srvNameInput.setText(EditSub.name);
            srvAddrInput.setText(EditSub.address);
            srvPortInput.setText(""+EditSub.port);
            srvUserInput.setText(EditSub.username);
            srvPassInput.setText(EditSub.password);
            srvTransInput.setText(EditSub.transPath);
            srvOrigInput.setText(EditSub.origPath);
        }

    }

    public void EditExistingSubsonic(String subFilename)
    {
        editFilename = subFilename;
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.buttonSubsonicDone:
                DoneClick();
                break;

            case R.id.buttonSubsonicTrans:
                {
                    FolderSelectionDialog dialog = new FolderSelectionDialog(getContext());
                    dialog.show();
                    dialog.setFolderSelectionDialogCallback(new FolderSelectionDialog.FolderSelectionDialogCallback()
                    {
                        @Override
                        public void onCompleted(String folderPath)
                        {
                            srvTransInput.setText(folderPath + "/");
                        }
                    });
                }
                break;

            case R.id.buttonSubsonicOrig:
                {
                    FolderSelectionDialog dialog = new FolderSelectionDialog(getContext());
                    dialog.show();
                    dialog.setFolderSelectionDialogCallback(new FolderSelectionDialog.FolderSelectionDialogCallback()
                    {
                        @Override
                        public void onCompleted(String folderPath)
                        {
                            srvOrigInput.setText(folderPath + "/");
                        }
                    });
                }
                break;
        }
    }

    @SuppressWarnings("deprecation")
    public void DoneClick()
    {
        if(srvNameInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a server name to continue.");
            ad.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else if(srvAddrInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a server address to continue.");
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
            SubsonicSource sub = new SubsonicSource();
            sub.name      = srvNameInput.getText().toString();
            sub.address   = srvAddrInput.getText().toString();
            sub.port      = Integer.parseInt(srvPortInput.getText().toString());
            sub.username  = srvUserInput.getText().toString();
            sub.password  = srvPassInput.getText().toString();
            sub.transPath = srvTransInput.getText().toString();
            sub.origPath  = srvOrigInput.getText().toString();

            if(!sub.transPath.endsWith("/")) sub.transPath += "/";
            if(!sub.origPath.endsWith("/")) sub.origPath += "/";

            sub.filename = SourceListOperations.getSubsonicPath(getContext()) + "/" + SourceListOperations.getFilenameXml(sub.name);
            if(editFilename != null)
            {
                File deleteFile = new File(editFilename);
                deleteFile.delete();
            }

            SourceListOperations.writeSubsonicFile(sub);
            dismiss();
        }
    }
}
