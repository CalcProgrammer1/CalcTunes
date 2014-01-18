package com.calcprogrammer1.calctunes.ContentPlaylistFragment;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.Subsonic.CalcTunesXMLParser;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class PlaylistEditor
{
    private Context context;

    private String playlistName   = "";
    private String playlistAuthor = "";
    private String playlistInfo   = "";

    private ArrayList<String> playlistData = new ArrayList<String>();

    public PlaylistEditor(Context c)
    {
        context = c;
    }

    public PlaylistEditor(Context c, String playlistName)
    {
        context = c;
        this.playlistName = playlistName;
    }

    public void setPlaylistName(String playlistName)
    {
        this.playlistName = playlistName;
    }

    public void setPlaylistAuthor(String playlistAuthor)
    {
        this.playlistAuthor = playlistAuthor;
    }

    public void setPlaylistInfo(String playlistInfo)
    {
        this.playlistInfo = playlistInfo;
    }

    public void readPlaylistFile(String filename)
    {
        if(filename == null)
        {
            filename = SourceListOperations.getPlaylistPath(context) + "/" + SourceListOperations.getFilenameXspf(playlistName);
        }

        String XMLData;
        Document DocData;
        NodeList NodeData;

        //Read XML file
        XMLData = CalcTunesXMLParser.getXmlFromFile(filename);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        NodeData = DocData.getElementsByTagName("playlist");

        if(NodeData.getLength() == 1)
        {
            //Get Name
            NodeData = DocData.getElementsByTagName("title");
            if(NodeData.item(0).getChildNodes().getLength() > 0)
            {
                playlistName = NodeData.item(0).getChildNodes().item(0).getNodeValue();
            }

            //Get Author
            NodeData = DocData.getElementsByTagName("creator");
            if(NodeData.item(0).getChildNodes().getLength() > 0)
            {
                playlistAuthor = NodeData.item(0).getChildNodes().item(0).getNodeValue();
            }

            //Get Info
            NodeData = DocData.getElementsByTagName("info");
            if(NodeData.item(0).getChildNodes().getLength() > 0)
            {
                playlistInfo = NodeData.item(0).getChildNodes().item(0).getNodeValue();
            }

            //Get Track List
            NodeData = DocData.getElementsByTagName("trackList");
            if(NodeData.item(0).getChildNodes().getLength() > 0)
            {
                //Get Tracks
                NodeList TrackData = DocData.getElementsByTagName("track");
            }
        }
    }

    public void writePlaylistFile(String filename)
    {
        if(filename == null)
        {
            filename = SourceListOperations.getPlaylistPath(context) + "/" + SourceListOperations.getFilenameXspf(playlistName);
        }

        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer     = new StringWriter();
        try
        {
            //Start document
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);

            //Start main playlist tag
            serializer.startTag("", "playlist");
            serializer.attribute("", "version", "1");
            serializer.attribute("", "xmlns", "http://xspf.org/ns/0");

            //Name
            serializer.startTag("", "title");
            serializer.text(playlistName);
            serializer.endTag("", "title");

            if(!playlistAuthor.equals(""))
            {
                //Author
                serializer.startTag("", "creator");
                serializer.text(playlistAuthor);
                serializer.endTag("", "creator");
            }

            if(!playlistInfo.equals(""));
            {
                //Info
                serializer.startTag("", "info");
                serializer.text(playlistInfo);
                serializer.endTag("", "info");
            }

            //Track List
            serializer.startTag("", "trackList");

            //Tracks
            for(int i = 0; i < playlistData.size(); i++)
            {
                //Start Track
                serializer.startTag("", "track");

                //Location
                serializer.startTag("", "location");
                serializer.text(playlistData.get(i));
                serializer.endTag("", "location");

                //End Track
                serializer.endTag("", "track");
            }

            //End Track List
            serializer.endTag("", "trackList");

            //End document
            serializer.endTag("", "playlist");
            serializer.endDocument();

            //Make sure library directory exists before writing
            File outdir = new File(filename);
            outdir.getParentFile().mkdirs();

            Log.d("PlaylistEditor", "Writing playlist file " + filename);

            //Write output file
            PrintWriter out = new PrintWriter(filename);
            out.println(writer.toString());
            out.close();
        }
        catch(Exception e){}
    }
}
