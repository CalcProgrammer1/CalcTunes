package com.calcprogrammer1.calctunes.Activities;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.SourceTypes.SubsonicSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class CalcTunesSubsonicBuilderActivity extends Activity
{
    EditText srvNameInput;
    EditText srvAddrInput;
    EditText srvPortInput;
    EditText srvUserInput;
    EditText srvPassInput;
    
    Intent i;
    
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.subsonicbuilder);
        
        srvNameInput = (EditText) findViewById(R.id.srvNameInput);
        srvAddrInput = (EditText) findViewById(R.id.srvAddrInput);
        srvPortInput = (EditText) findViewById(R.id.srvPortInput);
        srvUserInput = (EditText) findViewById(R.id.srvUserInput);
        srvPassInput = (EditText) findViewById(R.id.srvPassInput);
        
        //If ICS, make the title border match the ICS Holo theme
        if(Integer.valueOf(android.os.Build.VERSION.SDK) > 10)
        {
            findViewById(R.id.title_border).setBackgroundResource(android.R.color.holo_blue_light);
        }
        
        i = getIntent();
        Bundle extras = i.getExtras();
        if(extras != null)
        {
            String filename = extras.getString("EditFilename");
            SubsonicSource EditSub = SourceListOperations.readSubsonicFile(filename);
            srvNameInput.setText(EditSub.name);
            srvAddrInput.setText(EditSub.address);
            srvPortInput.setText(""+EditSub.port);
            srvUserInput.setText(EditSub.username);
            srvPassInput.setText(EditSub.password);
        }

    }
    
    @SuppressWarnings("deprecation")
    public void DoneClick(View view)
    {
        if(srvNameInput.getText().toString().equals(""))
        {
            AlertDialog ad = new AlertDialog.Builder(this).create();
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
            AlertDialog ad = new AlertDialog.Builder(this).create();
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
            sub.name     = srvNameInput.getText().toString();
            sub.address  = srvAddrInput.getText().toString();
            sub.port     = Integer.parseInt(srvPortInput.getText().toString());
            sub.username = srvUserInput.getText().toString();
            sub.password = srvPassInput.getText().toString();
            sub.filename = SourceListOperations.getSubsonicPath(this) + "/" + SourceListOperations.getFilename(sub.name);
            SourceListOperations.writeSubsonicFile(sub);
            setResult(Activity.RESULT_OK, i);
            finish();
        }
    }
}
