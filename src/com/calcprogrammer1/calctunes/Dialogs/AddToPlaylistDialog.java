package com.calcprogrammer1.calctunes.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.calcprogrammer1.calctunes.ContentPlaylistFragment.PlaylistEditor;
import com.calcprogrammer1.calctunes.ContentPlaylistFragment.PlaylistElement;
import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.SourceTypes.PlaylistSource;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.ArrayList;

public class AddToPlaylistDialog extends Dialog implements ListView.OnItemClickListener
{
    private ListView rootView;
    private ArrayList<String> fileList = new ArrayList<String>();
    private ArrayList<PlaylistSource> playlistList;

    public AddToPlaylistDialog(Activity act)
    {
        super(act);
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.addtoplaylistdialog);
        setTitle("Add To Playlist");

        rootView = (ListView) findViewById(R.id.AddToPlaylistDialogListView);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.FILL_PARENT;
        params.width = ViewGroup.LayoutParams.FILL_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        playlistList = SourceListOperations.readPlaylistList(SourceListOperations.getPlaylistPath(getContext()));

        ArrayList<String> playlistNames = new ArrayList<String>();

        for(int i = 0; i < playlistList.size(); i++)
        {
            playlistNames.add(playlistList.get(i).name);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_list_item_1,
                playlistNames );

        rootView.setAdapter(arrayAdapter);
        rootView.setOnItemClickListener(this);
    }

    public void addFileList(ArrayList<String> files)
    {
        fileList = files;
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int pos, long arg3)
    {
        PlaylistEditor editor = new PlaylistEditor(getContext());
        editor.readPlaylistFile(playlistList.get(pos).filename);
        for(int i = 0; i < fileList.size(); i++)
        {
            PlaylistElement newElement = new PlaylistElement();
            newElement.filename = fileList.get(i);

            File file = new File(newElement.filename);
            AudioFile f = SourceListOperations.readAudioFileReadOnly(file);
            Tag tag = f.getTag();
            newElement.artist = tag.getFirst(FieldKey.ARTIST);
            newElement.album = tag.getFirst(FieldKey.ALBUM);
            newElement.title = tag.getFirst(FieldKey.TITLE);
            newElement.year = tag.getFirst(FieldKey.YEAR);

            editor.playlistData.add(newElement);
        }
        editor.writePlaylistFile(null);
        dismiss();
    }
}
