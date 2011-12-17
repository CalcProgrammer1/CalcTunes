/*---------------------------------------------------------------------------------------------*\
| CalcTunes - A desktop-like media player for Android!                                          |
|                                                                                               |
| CalcTunes features a two-panel GUI that supports multiple media sources (libraries) as well   |
| as playlists.  Libraries are defined by groups of folders which are scanned for media and     |
| organized by artist, album, and track tags.  Playlists are lists of individual media files.   |
|                                                                                               |
| Created by Adam Honse (CalcProgrammer1), calcprogrammer1@gmail.com, 12/16/2011                |
\*---------------------------------------------------------------------------------------------*/
package com.calcprogrammer1.calctunes;

import android.app.Activity;
import android.view.*;
import android.os.Bundle;
import android.widget.*;
import android.content.*;
import android.content.res.Configuration;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.LibraryOperations;
import com.calcprogrammer1.calctunes.libraryElementArtist;

import java.io.File;
import java.util.ArrayList;

public class CalcTunesActivity extends Activity
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Class Variables////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Callbacks//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    MediaButtonsHandlerCallback buttonsCallback = new MediaButtonsHandlerCallback(){
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
    };
    
    MediaPlayerHandlerCallback mediaplayerCallback = new MediaPlayerHandlerCallback(){
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
    };
    
    LibraryListCallback mainlisthandlerCallback = new LibraryListCallback(){
        public void callback(libraryElementGeneric song)
        {
            media_initialize(song);
        }
    };
    
    SourceListCallback sourcelisthandlerCallback = new SourceListCallback(){
        public void callback(String filename)
        {
            currentLibrary = LibraryOperations.readLibraryData(filename);
            mainlisthandler.setLibrary(currentLibrary);
            mainlisthandler.drawList(-1);
            
        }
    };
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Class Overrides////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        buttons = new MediaButtonsHandler(this);
        buttons.setCallback(buttonsCallback);
        
        mediaplayer = new MediaPlayerHandler();
        mediaplayer.setCallback(mediaplayerCallback);
    	
        mainlisthandler = new LibraryListHandler(this, mainlist);
    	mainlisthandler.setCallback(mainlisthandlerCallback);

    	sourcelisthandler = new SourceListHandler(this, sourcelist);
    	sourcelisthandler.setCallback(sourcelisthandlerCallback);
    	
        updateGuiElements();
 
        refreshLibraryList();
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
            
            if(data.getStringExtra("EditFilename") != null)
            {
                File deleteFile = new File(data.getStringExtra("EditFilename"));
                deleteFile.delete();
            }
            
            String libraryName = data.getStringExtra("libraryName");
            
            LibraryOperations.saveLibraryFile(libraryName, libraryFolders, LibraryOperations.getLibraryPath(this));
            
            refreshLibraryList();
            currentLibrary = LibraryOperations.readLibraryData(LibraryOperations.getLibraryFullPath(this, libraryName));
            mainlisthandler.setLibrary(currentLibrary);
            mainlisthandler.drawList(-1);
        }
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
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        if(v == findViewById(R.id.listView1))
        {
            menu.add(1, 1, Menu.NONE, "Edit Library");
            menu.add(1, 2, Menu.NONE, "Delete Library");
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = (int) info.id;

        switch(item.getItemId())
        {
            case 1:
                Intent intent = new Intent(getBaseContext(), LibraryBuilderActivity.class);
                intent.putExtra("EditFilename", libraryList.get(id).filename);
                intent.putExtra("EditName", libraryList.get(id).name);
                startActivityForResult(intent, 1);
                break;
            
            case 2:
                File libraryToDelete = new File(libraryList.get(id).filename);
                libraryToDelete.delete();
                refreshLibraryList();
                Toast.makeText(this, "Library Deleted", Toast.LENGTH_SHORT).show();
                break;
        }
        
        return(super.onOptionsItemSelected(item));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Other Activity Functions///////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void refreshLibraryList()
    {
        libraryList = new ArrayList<libraryListElement>();
        libraryList = LibraryOperations.readLibraryList(LibraryOperations.getLibraryPath(this));

        sourcelisthandler.setLibraryList(libraryList);
        sourcelisthandler.updateList();
    }
    
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
        registerForContextMenu(sourcelist);
        if(sidebarHidden)
        {
            sourcelist.setVisibility(View.GONE);
        }
        
        mainlist = (ListView) findViewById(R.id.listView2);
        mainlisthandler.setListView(mainlist);
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
    
}