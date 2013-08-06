package com.calcprogrammer1.calctunes.MediaInfo;

import java.io.File;
import java.util.ArrayList;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.calcprogrammer1.calctunes.AlbumArtManager;
import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.Interfaces.MediaInfoViewInterface;
import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MediaInfoFragment extends Fragment
{
    private MediaInfoView                   view;
    private ImageView                       track_artwork;
    private ListView                        track_info_list;
    
    private MediaInfoAdapter                adapter;
    private ArrayList<MediaInfoListType>    adapter_data;
    
    private Bitmap                          artwork_image;
    
    // Shared Preferences
    private SharedPreferences appSettings;
    
    // Interface Color
    private int interfaceColor;
    
    OnSharedPreferenceChangeListener appSettingsListener = new OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
            interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
            setTrackInfo();
        }
    };
    
    private MediaInfoViewInterface viewcallback = new MediaInfoViewInterface(){
        @Override
        public void onLayoutReloaded()
        {
            setTrackInfo();
        }
    };
    
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //Get the application preferences
        appSettings = getActivity().getSharedPreferences("CalcTunes", Activity.MODE_PRIVATE);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);
        interfaceColor = appSettings.getInt("InterfaceColor", Color.DKGRAY);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        view = new MediaInfoView(getActivity());
        view.registerCallback(viewcallback);
        Log.d("MediaInfoFragment", "OnCreateView");
        return view;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        Log.d("MediaInfoFragment", "OnConfigurationChanged");
    }
    
    public void setTrackInfoFromFile(String track_info_path)
    {
        File file               = new File(track_info_path);
        AudioFile f             = SourceListOperations.readAudioFileReadOnly(file);
        Tag tag                 = f.getTag();
        AudioHeader header      = f.getAudioHeader();
        
        //Album Art
        artwork_image   = AlbumArtManager.getAlbumArt(tag.getFirst(FieldKey.ARTIST), tag.getFirst(FieldKey.ALBUM), getActivity(), false);
        
        //Create new adapter and adapter data
        adapter         = new MediaInfoAdapter(getActivity());
        adapter_data    = new ArrayList<MediaInfoListType>();
        
        //Title
        adapter_data.add(new MediaInfoListType( "Title",        tag.getFirst(FieldKey.TITLE)        ));
        
        //Artist
        adapter_data.add(new MediaInfoListType( "Artist",       tag.getFirst(FieldKey.ARTIST)       ));
        
        //Album
        adapter_data.add(new MediaInfoListType( "Album",        tag.getFirst(FieldKey.ALBUM)        ));
        
        //Track Number
        adapter_data.add(new MediaInfoListType( "Track Number", tag.getFirst(FieldKey.TRACK)        ));
        
        //Track Total
        adapter_data.add(new MediaInfoListType( "Track Total",  tag.getFirst(FieldKey.TRACK_TOTAL)  ));
        
        //Disc Number
        adapter_data.add(new MediaInfoListType( "Disc Number",  tag.getFirst(FieldKey.DISC_NO)      ));
        
        //Disc Total
        adapter_data.add(new MediaInfoListType( "Total Discs",  tag.getFirst(FieldKey.DISC_TOTAL)   ));
        
        //Track Year
        adapter_data.add(new MediaInfoListType( "Year",         tag.getFirst(FieldKey.YEAR)         ));
        
        //Album Artist
        adapter_data.add(new MediaInfoListType( "Album Artist", tag.getFirst(FieldKey.ALBUM_ARTIST) ));
        
        //Composer
        adapter_data.add(new MediaInfoListType( "Composer",     tag.getFirst(FieldKey.COMPOSER)     ));
        
        //Conductor
        adapter_data.add(new MediaInfoListType( "Conductor",    tag.getFirst(FieldKey.CONDUCTOR)    ));
        
        //Duration
        adapter_data.add(new MediaInfoListType( "Track Length", "" + header.getTrackLength()        ));
        
        //Format
        adapter_data.add(new MediaInfoListType( "File Format",  header.getFormat()                  ));
        
        //Sample Rate
        adapter_data.add(new MediaInfoListType( "Sample Rate",  "" + header.getSampleRateAsNumber() ));
        
        //Encoder
        adapter_data.add(new MediaInfoListType( "Encoder",      tag.getFirst(FieldKey.ENCODER)      ));
        
        adapter.setData(adapter_data);
        setTrackInfo();
    }
    
    public void setTrackInfo()
    {
        track_artwork   = (ImageView) view.findViewById(R.id.track_artwork);
        track_info_list = (ListView)  view.findViewById(R.id.track_info_list);
        View separator  = (View)      view.findViewById(R.id.separator);
        
        track_info_list.setDivider(null);
        track_info_list.setDividerHeight(0);
        
        track_artwork.setImageBitmap(artwork_image);
        track_info_list.setAdapter(adapter);
    }
    

}
