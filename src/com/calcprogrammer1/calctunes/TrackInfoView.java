package com.calcprogrammer1.calctunes;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrackInfoView extends LinearLayout
{
    private ImageView track_artwork;
    private TextView  track_title;
    private TextView  track_artist;
    private TextView  track_album;
    private TextView  track_number;
    private TextView  track_disc;
    Context con;
    
    public TrackInfoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        con = context;
        LayoutInflater  inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.trackinfoview, this, true);

        track_artwork = (ImageView) findViewById(R.id.track_artwork);
        track_title   = (TextView)  findViewById(R.id.track_title);
        track_artist  = (TextView)  findViewById(R.id.track_album);
        track_album   = (TextView)  findViewById(R.id.track_artist);
        track_number  = (TextView)  findViewById(R.id.track_number);
        track_disc    = (TextView)  findViewById(R.id.track_disc);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        if(parentHeight < parentWidth)
        {
            track_artwork.getLayoutParams().width=parentHeight;
            track_artwork.getLayoutParams().height=parentHeight;
        }
        else
        {
            track_artwork.getLayoutParams().width=parentWidth;
            track_artwork.getLayoutParams().height=parentWidth;
        }
        this.setMeasuredDimension(parentWidth, parentHeight);
    }
    
    public void setTrackInfoFromFile(String track_info_path)
    {
        File file = new File(track_info_path);
        AudioFile f = SourceListOperations.readAudioFileReadOnly(file);
        Tag tag = f.getTag();
        AudioHeader header = f.getAudioHeader();
        
        track_artwork.setImageBitmap(AlbumArtManager.getAlbumArt(tag.getFirst(FieldKey.ARTIST), tag.getFirst(FieldKey.ALBUM), con));
        track_title.setText(tag.getFirst(FieldKey.TITLE));
        track_artist.setText(tag.getFirst(FieldKey.ARTIST));
        track_album.setText(tag.getFirst(FieldKey.ALBUM));
        track_number.setText(tag.getFirst(FieldKey.TRACK));
        track_disc.setText(tag.getFirst(FieldKey.DISC_NO));
    }
    

}
