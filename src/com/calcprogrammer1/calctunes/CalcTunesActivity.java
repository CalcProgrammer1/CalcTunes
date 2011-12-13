package com.calcprogrammer1.calctunes;

import android.app.Activity;
import android.view.*;
import android.media.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.content.*;
import android.content.res.Configuration;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.*;

import com.calcprogrammer1.calctunes.R;
import com.calcprogrammer1.calctunes.FileOperations;
import com.calcprogrammer1.calctunes.LibraryOperations;
import com.calcprogrammer1.calctunes.libraryElementArtist;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalcTunesActivity extends Activity
{

	//Class Variables
	MediaPlayer mediaplayer;
	TextView artisttext;
	TextView albumtext;
	TextView tracktext;
	SeekBar trackseek;
	ListView playlist;
	ListView mainlist;
    String rootDirectory;
    ArrayList<libraryElementArtist> myLibrary;
    ArrayList<libraryListElement> libraryList;
    
	public void updateGuiElements()
	{
    	artisttext = (TextView) findViewById(R.id.text_artistname);
    	albumtext = (TextView) findViewById(R.id.text_albumname);
    	tracktext = (TextView) findViewById(R.id.text_trackname);
    	trackseek = (SeekBar) findViewById(R.id.seekBar_track);
    	playlist = (ListView) findViewById(R.id.listView1);
    	mainlist = (ListView) findViewById(R.id.listView2);
	}
	
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	
        mediaplayer = new MediaPlayer();
    	
        updateGuiElements();
    	
    	libraryList = new ArrayList<libraryListElement>();
    	libraryList = LibraryOperations.readLibraryList(getApplicationContext().getExternalFilesDir(null).getPath());
    	String[] libraryNames = LibraryOperations.getNamesFromList(libraryList);
    	playlist.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,libraryNames));
        playlist.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                myLibrary = LibraryOperations.readLibraryFile(libraryList.get(arg2).filename);
                drawList(myLibrary, -1);
            }
            
        });
    	
    	new seekHandler().execute();
    	trackseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
    	{
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				mediaplayer.seekTo(trackseek.getProgress());
			}
			
			public void onStartTrackingTouch(SeekBar seekBar)
			{			
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{			
			}
		});
    }
    
    public void onConfigurationChanged(Configuration newConfig)
    {
    	super.onConfigurationChanged(newConfig);
    	setContentView(R.layout.main);
    	updateGuiElements();
    }
    
    
    public void ButtonNewLibraryClick(View view)
    {
    	startActivityForResult(new Intent(this, LibraryBuilderActivity.class), 1);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == Activity.RESULT_OK)
        {
            ArrayList<String> libraryFolders = data.getStringArrayListExtra("libraryFolders");
            String libraryName = data.getStringExtra("libraryName");
            LibraryOperations.saveLibraryFile(libraryName, libraryFolders, getApplicationContext().getExternalFilesDir(null).getPath());
            myLibrary = LibraryOperations.readLibraryFile(getApplicationContext().getExternalFilesDir(null).getPath()+"/"+libraryName+".txt");
            drawList(myLibrary, -1);
        }
    }
    
    public void media_initialize(String filePath)
    {	
		File song = new File(filePath);
		AudioFile f;
		try
		{
			f = AudioFileIO.read(song);
			Tag tag = f.getTag();
			String song_artist = tag.getFirstArtist();
			String song_album = tag.getFirstAlbum();
			String song_title = tag.getFirstTitle();
			artisttext.setText(song_artist);
			albumtext.setText(song_album);
			tracktext.setText(song_title);
		}
		catch (Exception e)
		{
			
		}
		
		mediaplayer.reset();
		mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try
		{
			mediaplayer.setDataSource(filePath);
		} 
		catch (Exception e)
		{
		}
		
		try
		{
			mediaplayer.prepare();
		}
		catch (Exception e)
		{
		}
		
		trackseek.setMax(mediaplayer.getDuration());
    }
    
    public class seekHandler extends AsyncTask
    {
    	public Object doInBackground(Object... object)
    	{
    		try
    		{
    			while(mediaplayer != null)
    			{
    				int currentPosition = mediaplayer.getCurrentPosition();
    				trackseek.setProgress(currentPosition);
    				Thread.sleep(250);
    			}
    			
    		}
    		catch (InterruptedException e)
    		{
    			e.printStackTrace();
    		}
			return object;
    	}
    }

	
    public void ButtonStopClick(View view)
    {
    	mediaplayer.seekTo(0);
    	mediaplayer.stop();
    	try
    	{
			mediaplayer.prepare();
	    	mediaplayer.seekTo(0);
		}
    	catch (Exception e)
    	{
    	}
    }
    
    public void ButtonPlayPauseClick(View view)
    {
    	mediaplayer.start();
    }
    
    public void drawList(ArrayList<libraryElementArtist> libraryData, int artist)
    {
        
        // create the grid item mapping
        String[] from = new String[] {"artist", "album", "song"};
        int[] to = new int[] { R.id.textView1, R.id.textView2, R.id.textView3};

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < libraryData.size(); i++)
        {
            HashMap<String, String> amap = new HashMap<String, String>();
            amap.put("artist_index", ""+i);
            amap.put("artist", libraryData.get(i).name);
            amap.put("album", "");
            amap.put("song", "");
            fillMaps.add(amap);
            if(i == artist)
            {
                for(int j = 0; j < libraryData.get(i).albums.size(); j++)
                {
                    HashMap<String, String> bmap = new HashMap<String, String>();
                    bmap.put("artist_index", ""+i);
                    bmap.put("artist", "");
                    bmap.put("album_index", ""+j);
                    bmap.put("album", libraryData.get(i).albums.get(j).name);
                    bmap.put("song", "");
                    fillMaps.add(bmap);
                    for(int k = 0; k < libraryData.get(i).albums.get(j).songs.size(); k++)
                    {
                        HashMap<String, String> cmap = new HashMap<String, String>();
                        cmap.put("artist_index", ""+i);
                        cmap.put("artist", "");
                        cmap.put("album_index", ""+j);
                        cmap.put("album", "");
                        cmap.put("song_index", ""+k);
                        cmap.put("song", libraryData.get(i).albums.get(j).songs.get(k).name);
                        fillMaps.add(cmap); 
                    }
                }
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.listentry, from, to);
        mainlist.setAdapter(adapter);
        mainlist.setOnItemClickListener(new OnItemClickListener() 
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                libraryClickHandler(arg0, arg1, arg2, arg3);
            }
            
        });
    }
    

    public void libraryClickHandler(AdapterView<?> parent, View view, int position, long id)
    {
        String songStr = ((HashMap<String, String>) parent.getAdapter().getItem(position)).get("song");
        String artStr =  ((HashMap<String, String>) parent.getAdapter().getItem(position)).get("artist");
        String albStr =  ((HashMap<String, String>) parent.getAdapter().getItem(position)).get("album");
        
        if(!songStr.equals(""))
        {
            int i = Integer.parseInt(((HashMap<String, String>) parent.getAdapter().getItem(position)).get("artist_index"));
            int j = Integer.parseInt(((HashMap<String, String>) parent.getAdapter().getItem(position)).get("album_index"));
            int k = Integer.parseInt(((HashMap<String, String>) parent.getAdapter().getItem(position)).get("song_index"));
            String filePath = myLibrary.get(i).albums.get(j).songs.get(k).filename;
            Toast.makeText(getApplicationContext(), filePath, Toast.LENGTH_SHORT).show();
            media_initialize(filePath);   
        }
        else if(!albStr.equals(""))
        {
            Toast.makeText(getApplicationContext(), "Album Selected", Toast.LENGTH_SHORT).show();
        }
        else if(!artStr.equals(""))
        {
            int i = Integer.parseInt(((HashMap<String, String>) parent.getAdapter().getItem(position)).get("artist_index"));
            drawList(myLibrary, i);
            Toast.makeText(getApplicationContext(), "Artist Selected", Toast.LENGTH_SHORT).show();
        }
    }
    
    
}