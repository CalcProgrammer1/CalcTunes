package com.calcprogrammer1.calctunes;

import android.app.Activity;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.media.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.content.*;
import android.content.res.Configuration;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.*;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.LibraryOperations;
import com.calcprogrammer1.calctunes.libraryElementArtist;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalcTunesActivity extends Activity
{

	//Class Variables
	MediaPlayerHandler mediaplayer;
	TextView artisttext;
	TextView albumtext;
	TextView tracktext;
	
	SeekBar trackseek;
	SeekHandler trackseekhandler;
	
	ListView sourcelist;
	SourceListHandler sourcelisthandler;
	
	ListView mainlist;
    LibraryListHandler mainlisthandler;
	
    ArrayList<libraryElementArtist> currentLibrary;
    ArrayList<libraryListElement> libraryList;
    
    libraryElementGeneric currentTrack;

    MediaButtonsHandler buttons;
    
    boolean sidebarHidden = false;
    
	public void updateGuiElements()
	{
    	artisttext = (TextView) findViewById(R.id.text_artistname);
    	albumtext = (TextView) findViewById(R.id.text_albumname);
    	tracktext = (TextView) findViewById(R.id.text_trackname);
    	
        artisttext.setText(mediaplayer.current_artist);
        albumtext.setText(mediaplayer.current_album);
        tracktext.setText(mediaplayer.current_title);
        
    	trackseek = (SeekBar) findViewById(R.id.seekBar_track);
        trackseekhandler = new SeekHandler(trackseek, mediaplayer);
        
    	sourcelist = (ListView) findViewById(R.id.listView1);
    	sourcelisthandler.setListView(sourcelist);
    	sourcelisthandler.updateList();
    	
    	mainlist = (ListView) findViewById(R.id.listView2);
    	mainlisthandler.setListView(mainlist);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        buttons = new MediaButtonsHandler(this);
        buttons.setCallback(new MediaButtonsHandlerCallback(){

            public void onMediaNextPressed()
            {
                tracktext.setText("Button Pressed");
                ButtonNextClick(null);
            }

            public void onMediaPrevPressed()
            { 
            }

            public void onMediaPlayPausePressed()
            {
            }

            public void onMediaStopPressed()
            {
            }    
        });
        
        mediaplayer = new MediaPlayerHandler();
        mediaplayer.setCallback(new MediaPlayerHandlerCallback(){

            public void onSongFinished()
            {
                media_initialize(LibraryOperations.getNextSong(currentTrack, currentLibrary));
                mediaplayer.startPlayback(true);
            }

            public void onStop()
            {
                artisttext.setText(mediaplayer.current_artist);
                albumtext.setText(mediaplayer.current_album);
                tracktext.setText(mediaplayer.current_title);
            }
        });
    	
        mainlisthandler = new LibraryListHandler(this, mainlist);
    	mainlisthandler.setCallback(new LibraryListCallback(){

            public void callback(libraryElementGeneric song)
            {
                media_initialize(song);
            }
    	    
    	});

    	sourcelisthandler = new SourceListHandler(this, sourcelist);
    	sourcelisthandler.setCallback(new SourceListCallback(){

            public void callback(String filename)
            {
                currentLibrary = LibraryOperations.readLibraryFile(filename);
                mainlisthandler.setLibrary(currentLibrary);
                mainlisthandler.drawList(-1);
                
            }
    	    
    	});
    	
        updateGuiElements();
 
    	libraryList = new ArrayList<libraryListElement>();
    	libraryList = LibraryOperations.readLibraryList(getApplicationContext().getExternalFilesDir(null).getPath());

    	sourcelisthandler.setLibraryList(libraryList);
    	sourcelisthandler.updateList();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        buttons.registerButtons();
    }
    
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.main);
        updateGuiElements();
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.mainoptionsmenu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.createLibrary:
                startActivityForResult(new Intent(this, LibraryBuilderActivity.class), 1);
                break;
                
            case R.id.exitApplication:
                this.finish();
                break;
                
            case R.id.collapseSidebar:
                if(sidebarHidden)
                {
                    sourcelist.setVisibility(View.VISIBLE);
                    sidebarHidden = false;
                }
                else
                {
                    sourcelist.setVisibility(View.GONE);
                    sidebarHidden = true;
                }
                break;
        }
        return true;
    }
        
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == Activity.RESULT_OK)
        {
            ArrayList<String> libraryFolders = data.getStringArrayListExtra("libraryFolders");
            String libraryName = data.getStringExtra("libraryName");
            LibraryOperations.saveLibraryFile(libraryName, libraryFolders, getApplicationContext().getExternalFilesDir(null).getPath());
            currentLibrary = LibraryOperations.readLibraryFile(getApplicationContext().getExternalFilesDir(null).getPath()+"/"+libraryName+".txt");
            mainlisthandler.setLibrary(currentLibrary);
            mainlisthandler.drawList(-1);
        }
    }
    
    public void media_initialize(libraryElementGeneric song)
    {	
            currentTrack = song;
            String filePath = currentTrack.song.filename;
            mediaplayer.initialize(filePath);
			artisttext.setText(mediaplayer.current_artist);
			albumtext.setText(mediaplayer.current_album);
			tracktext.setText(mediaplayer.current_title);
    }
   
    public void ButtonStopClick(View view)
    {
    	mediaplayer.stopPlayback();
    }
    
    public void ButtonPlayPauseClick(View view)
    {
        if(mediaplayer.isPlaying())
        {
            mediaplayer.pausePlayback();
        }
        else if(mediaplayer.prepared)
        {
            mediaplayer.startPlayback(true);
        }
    }
    
    public void ButtonNextClick(View view)
    {
        mediaplayer.stopPlayback();
        media_initialize(LibraryOperations.getNextSong(currentTrack, currentLibrary));
        mediaplayer.startPlayback(true);
    }
    
    public void ButtonPrevClick(View view)
    {
        mediaplayer.stopPlayback();
        media_initialize(LibraryOperations.getPrevSong(currentTrack, currentLibrary));
        mediaplayer.startPlayback(true);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_MEDIA_NEXT)
        {
            ButtonNextClick(null);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        {
            ButtonPrevClick(null);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        {
            ButtonPlayPauseClick(null);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_MEDIA_STOP)
        {
            ButtonStopClick(null);
        }
        return false;       
    }

}