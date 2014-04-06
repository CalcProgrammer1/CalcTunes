package com.calcprogrammer1.calctunes.ContentPlaylistFragment;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;
import com.calcprogrammer1.calctunes.Subsonic.CalcTunesXMLParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

    public ArrayList<PlaylistElement> playlistData = new ArrayList<PlaylistElement>();

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

    public int maxIndex()
    {
        return playlistData.size() - 1;
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

        Log.d("PlaylistEditor", "Opening playlist file " + filename);

        String XMLData;
        Document DocData;
        Element DocElement;
        NodeList NodeData;

        //Read XML file
        XMLData = CalcTunesXMLParser.getXmlFromFile(filename);
        DocData = CalcTunesXMLParser.getDomElement(XMLData);
        DocElement = DocData.getDocumentElement();
        NodeData = DocElement.getChildNodes();

        if(NodeData != null && NodeData.getLength() > 0)
        {
            for(int i = 0; i < NodeData.getLength(); i++)
            {
                if(NodeData.item(i).getNodeType() == Node.ELEMENT_NODE)
                {
                    Element NodeElement = (Element) NodeData.item(i);
                    if(NodeElement.getNodeName().equals("title"))
                    {
                        playlistName = NodeElement.getTextContent();
                    }
                    else
                    if(NodeElement.getNodeName().equals("creator"))
                    {
                        playlistAuthor = NodeElement.getTextContent();
                    }
                    else
                    if(NodeElement.getNodeName().equals("info"))
                    {
                        playlistInfo = NodeElement.getTextContent();
                    }
                    else
                    if(NodeElement.getNodeName().equals("trackList"))
                    {
                        NodeList TrackNodeData = NodeElement.getChildNodes();
                        if(TrackNodeData != null && TrackNodeData.getLength() > 0)
                        {
                            for(int j = 0; j < TrackNodeData.getLength(); j++)
                            {
                                if(TrackNodeData.item(j).getNodeType() == Node.ELEMENT_NODE)
                                {
                                    Element TrackElement = (Element) TrackNodeData.item(j);
                                    if(TrackElement.getNodeName().equals("track"))
                                    {
                                        PlaylistElement newElement = new PlaylistElement();
                                        newElement.filename = TrackElement.getElementsByTagName("location").item(0).getTextContent();
                                        newElement.artist   = TrackElement.getElementsByTagName("creator").item(0).getTextContent();
                                        newElement.album    = TrackElement.getElementsByTagName("album").item(0).getTextContent();
                                        newElement.title    = TrackElement.getElementsByTagName("title").item(0).getTextContent();
                                        playlistData.add(newElement);
                                    }
                                }
                            }
                        }
                    }
                }
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
                serializer.text(playlistData.get(i).filename);
                serializer.endTag("", "location");

                if(!playlistData.get(i).artist.equals(""))
                {
                    //Artist
                    serializer.startTag("", "creator");
                    serializer.text(playlistData.get(i).artist);
                    serializer.endTag("", "creator");
                }

                if(!playlistData.get(i).album.equals(""))
                {
                    //Album
                    serializer.startTag("", "album");
                    serializer.text(playlistData.get(i).album);
                    serializer.endTag("", "album");
                }

                if(!playlistData.get(i).album.equals(""))
                {
                    //Title
                    serializer.startTag("", "title");
                    serializer.text(playlistData.get(i).title);
                    serializer.endTag("", "title");
                }

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

            //If playlist file already exists, delete before writing
            if(outdir.exists())
            {
                outdir.delete();
            }

            Log.d("PlaylistEditor", "Writing playlist file " + filename);

            //Write output file
            PrintWriter out = new PrintWriter(filename);
            out.println(writer.toString());
            out.close();
        }
        catch(Exception e){}
    }
}
