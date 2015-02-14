package com.calcprogrammer1.calctunes.Dialogs;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.SourceTypes.SubsonicSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.File;

public class SubsonicBuilderDialog extends Dialog implements View.OnClickListener
{
    EditText srvNameInput;
    EditText srvAddrInput;
    EditText srvPortInput;
    EditText srvUserInput;
    EditText srvPassInput;
    EditText srvFrmtInput;
    EditText srvBtrtInput;
    EditText srvTransInput;
    EditText srvOrigInput;
    Button   buttonDone;
    Button   buttonTrans;
    Button   buttonOrig;
    CheckBox checkTrans;

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
        srvFrmtInput  = (EditText) findViewById(R.id.srvFormatInput);
        srvBtrtInput  = (EditText) findViewById(R.id.srvBitrateInput);
        srvTransInput = (EditText) findViewById(R.id.srvTransInput);
        srvOrigInput  = (EditText) findViewById(R.id.srvOrigInput);
        buttonDone    = (Button)   findViewById(R.id.buttonSubsonicDone);
        buttonTrans   = (Button)   findViewById(R.id.buttonSubsonicTrans);
        buttonOrig    = (Button)   findViewById(R.id.buttonSubsonicOrig);
        checkTrans    = (CheckBox) findViewById(R.id.srvTranscodeStream);

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
            srvFrmtInput.setText(EditSub.streamingFormat);
            srvBtrtInput.setText(EditSub.streamingBitrate);
            srvTransInput.setText(EditSub.transPath);
            srvOrigInput.setText(EditSub.origPath);
            checkTrans.setChecked(EditSub.transcodeStream);
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
        else if(srvPortInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a server port to continue.  The default Subsonic port is 4040.");
            ad.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else if(srvUserInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a username to continue.");
            ad.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else if(srvPassInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a password to continue.");
            ad.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else if(srvFrmtInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a transcoding format to continue.  The default Subsonic format is mp3.");
            ad.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else if(srvBtrtInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a transcoding bit rate to continue.  If unsure, set to 160.");
            ad.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else if(srvTransInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a path for transcoded files.  If unsure, /sdcard/Music should work for most Android versions.");
            ad.setButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else if(srvOrigInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false);
            ad.setMessage("Please input a path for original files.  If unsure, /sdcard/Music should work for most Android versions.");
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
            sub.streamingFormat = srvFrmtInput.getText().toString();
            sub.streamingBitrate = srvBtrtInput.getText().toString();
            sub.transPath = srvTransInput.getText().toString();
            sub.origPath  = srvOrigInput.getText().toString();
            sub.transcodeStream = checkTrans.isChecked();

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
            Intent broadcast = new Intent();
            broadcast.setAction("com.calcprogrammer1.calctunes.SOURCE_REFRESH_EVENT");
            getContext().sendBroadcast(broadcast);
        }
    }
}
