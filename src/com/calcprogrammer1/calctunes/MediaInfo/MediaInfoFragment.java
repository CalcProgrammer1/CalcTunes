package com.calcprogrammer1.calctunes.MediaInfo;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.calcprogrammer1.calctunes.AlbumArtManager;
import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MediaInfoFragment extends Fragment
{
    private ImageView track_artwork;
    private TextView  track_title;
    private TextView  track_artist;
    private TextView  track_album;
    private TextView  track_number;
    private TextView  track_disc;
    
    public void onCreate(Context context, AttributeSet attrs)
    {

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        View view = new MediaInfoView(getActivity());
        track_artwork = (ImageView) view.findViewById(R.id.track_artwork);
        track_title   = (TextView)  view.findViewById(R.id.track_title);
        track_artist  = (TextView)  view.findViewById(R.id.track_album);
        track_album   = (TextView)  view.findViewById(R.id.track_artist);
        track_number  = (TextView)  view.findViewById(R.id.track_number);
        track_disc    = (TextView)  view.findViewById(R.id.track_disc);
        return view;
    }
    
    public void setTrackInfoFromFile(String track_info_path)
    {
        File file = new File(track_info_path);
        AudioFile f = SourceListOperations.readAudioFileReadOnly(file);
        Tag tag = f.getTag();
        //AudioHeader header = f.getAudioHeader();
        
        track_artwork.setImageBitmap(AlbumArtManager.getAlbumArt(tag.getFirst(FieldKey.ARTIST), tag.getFirst(FieldKey.ALBUM), getActivity()));
        track_title.setText(tag.getFirst(FieldKey.TITLE));
        track_artist.setText(tag.getFirst(FieldKey.ARTIST));
        track_album.setText(tag.getFirst(FieldKey.ALBUM));
        track_number.setText(tag.getFirst(FieldKey.TRACK));
        track_disc.setText(tag.getFirst(FieldKey.DISC_NO));
    }
    

}
