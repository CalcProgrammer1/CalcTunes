package com.calcprogrammer1.calctunes.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.calcprogrammer1.calctunes.ContentPlaylistFragment.PlaylistEditor;
import com.calcprogrammer1.calctunes.R;

public class PlaylistBuilderDialog extends Dialog implements View.OnClickListener
{
    EditText nameText;
    EditText authorText;
    EditText infoText;

    Button   doneButton;

    public PlaylistBuilderDialog(Activity act)
    {
        super(act);
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.playlistbuilder);
        setTitle("Create New Playlist");

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.FILL_PARENT;
        params.width = ViewGroup.LayoutParams.FILL_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        nameText = (EditText) findViewById(R.id.editTextPlaylistName);
        authorText = (EditText) findViewById(R.id.editTextPlaylistAuthor);
        infoText = (EditText) findViewById(R.id.editTextPlaylistInfo);

        doneButton = (Button) findViewById(R.id.buttonPlaylistDone);
        doneButton.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.buttonPlaylistDone:
                PlaylistEditor newEditor = new PlaylistEditor(getContext(), nameText.getText().toString());
                newEditor.setPlaylistName(nameText.getText().toString());
                newEditor.setPlaylistAuthor(authorText.getText().toString());
                newEditor.setPlaylistInfo(infoText.getText().toString());
                newEditor.writePlaylistFile(null);
                dismiss();
                break;
        }
    }
}