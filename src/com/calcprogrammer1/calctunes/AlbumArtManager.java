package com.calcprogrammer1.calctunes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AlbumArtManager
{
    //Checks cache for album art, if it is not found return default icon
    static public Bitmap getAlbumArtFromCache(String artist, String album, Context c)
    {
        Bitmap artwork = null;
        File dirfile = new File(LibraryOperations.getAlbumArtPath(c));
        dirfile.mkdirs();
        String artfilepath = LibraryOperations.getAlbumArtPath(c) + File.separator + LibraryOperations.makeFilename(artist) + "_" + LibraryOperations.makeFilename(album) + ".png";
        File infile = new File(artfilepath);
        try
        {
            artwork = BitmapFactory.decodeFile(infile.getAbsolutePath());
        }catch(Exception e){}
        if(artwork == null)
        {
            try
            {
                artwork = BitmapFactory.decodeResource(c.getResources(), R.drawable.icon);
            }catch(Exception ex){}
        }
        return artwork;
    }

    //Check cache for album art, if not found then check Last.fm, if still not found then return default icon
    static public Bitmap getAlbumArt(String artist, String album, Context c)
    {
        Bitmap artwork = null;
        String artfilepath = LibraryOperations.getAlbumArtPath(c) + File.separator + LibraryOperations.makeFilename(artist) + "_" + LibraryOperations.makeFilename(album) + ".png";
        File infile = new File(artfilepath);
        try
        {
            artwork = BitmapFactory.decodeFile(infile.getAbsolutePath());
        }catch(Exception e){}
        if(artwork == null)
        {
            try
            {
                String apiKey = "b25b959554ed76058ac220b7b2e0a026";
                String imageSize = "large";
                String method = "album.getinfo";
                
                String request = "http://ws.audioscrobbler.com/2.0/?method=" + method + "&api_key="+apiKey;
                request += "&artist=" + artist.replaceAll(" ", "%20");
                if (method.equals("album.getinfo"))
                {
                    request += "&album=" + album.replaceAll(" ", "%20");
                }
                
                URL url = new URL(request);
                InputStream is = url.openStream();
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(is);
        
                NodeList nl = doc.getElementsByTagName("image");
                
                for (int i = 0; i < nl.getLength(); i++)
                {
                    Node n = nl.item(i);
                    if (n.getAttributes().item(0).getNodeValue().equals(imageSize))
                    {
                        Node fc = n.getFirstChild();
                        URL imgUrl = new URL(fc.getNodeValue());
                        artwork = BitmapFactory.decodeStream(imgUrl.openStream());
                        
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        artwork.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                        File outfile = new File(artfilepath);
                        FileOutputStream fo = new FileOutputStream(outfile);
                        fo.write(bytes.toByteArray());
                    }
                }
                
            }catch(Exception ex){}
        }
        if(artwork == null)
        {
            try
            {
                artwork = BitmapFactory.decodeResource(c.getResources(), R.drawable.icon);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                artwork.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                File outfile = new File(artfilepath);
                FileOutputStream fo = new FileOutputStream(outfile);
                fo.write(bytes.toByteArray());
            }catch(Exception ex){}
        }
        return artwork;   
    }
}
