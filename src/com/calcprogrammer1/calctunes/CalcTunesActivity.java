package com.calcprogrammer1.calctunes;

import android.app.Activity;
import android.view.*;
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
	
	String rootDirectory;
    ArrayList<libraryElementArtist> myLibrary;
    ArrayList<libraryListElement> libraryList;
    
	public void updateGuiElements()
	{
    	artisttext = (TextView) findViewById(R.id.text_artistname);
    	albumtext = (TextView) findViewById(R.id.text_albumname);
    	tracktext = (TextView) findViewById(R.id.text_trackname);
    	
    	trackseek = (SeekBar) findViewById(R.id.seekBar_track);
        trackseekhandler = new SeekHandler(trackseek, mediaplayer);
        
    	sourcelist = (ListView) findViewById(R.id.listView1);
    	sourcelisthandler.setListView(sourcelist);
    	sourcelisthandler.updateList();
    	
    	mainlist = (ListView) findViewById(R.id.listView2);
    	mainlisthandler.setListView(mainlist);
	}
	
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	
        mediaplayer = new MediaPlayerHandler();
    	
        mainlisthandler = new LibraryListHandler(this, mainlist);
    	mainlisthandler.setCallback(new LibraryListCallback(){

            public void callback(String filename)
            {
                media_initialize(filename);
            }
    	    
    	});

    	sourcelisthandler = new SourceListHandler(this, sourcelist);
    	sourcelisthandler.setCallback(new SourceListCallback(){

            public void callback(String filename)
            {
                myLibrary = LibraryOperations.readLibraryFile(filename);
                mainlisthandler.setLibrary(myLibrary);
                mainlisthandler.drawList(-1);
                
            }
    	    
    	});
    	
        updateGuiElements();
 
    	libraryList = new ArrayList<libraryListElement>();
    	libraryList = LibraryOperations.readLibraryList(getApplicationContext().getExternalFilesDir(null).getPath());

    	sourcelisthandler.setLibraryList(libraryList);
    	sourcelisthandler.updateList();
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
            myLibrary = LibraryOperations.readLibraryFile(getApplicationContext().getExternalFilesDir(null).getPath()+"/"+libraryName+".txt");
            mainlisthandler.setLibrary(myLibrary);
            mainlisthandler.drawList(-1);
        }
    }
    
    public void media_initialize(String filePath)
    {	
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
            mediaplayer.startPlayback();
        }
    }
 
}