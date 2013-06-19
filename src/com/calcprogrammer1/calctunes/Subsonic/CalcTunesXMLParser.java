package com.calcprogrammer1.calctunes.Subsonic;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

//This class is mostly a copy of http://www.androidhive.info/2011/11/android-xml-parsing-tutorial/
//but with modifications for downloading non-xml files for Subsonic compatibility
public class CalcTunesXMLParser
{
    public static String getXmlFromFile(String file)
    {
        String xml = null;
        BufferedReader inFile;
        
        try{
            inFile = new BufferedReader(new FileReader(file));
            xml = inFile.readLine();
        }catch(Exception e){}
        
        return xml;
    }
    
    public static String getXmlFromUrl(String url)
    {
        String xml = null;
 
        try
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            Log.d("XMLParser", "Sending XML request: " + url);
            HttpPost httpPost = new HttpPost(url);
 
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);
 
        }catch(Exception e){}

        return xml;
    }
    
    public static String getFileFromUrl(String url, String path)
    {
        try
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            Log.d("XMLParser", "Sending file request: " + url);
            HttpPost httpPost = new HttpPost(url);
 
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            
            InputStream instream = httpEntity.getContent();
            FileOutputStream outstream = new FileOutputStream(path);
            
            int read = 0;
            byte[] bytes = new byte[1024];
            
            while((read = instream.read(bytes)) != -1)
            {
                outstream.write(bytes, 0, read);
            }
 
        }catch(Exception e){}
    
        return "";
    }
    
    public static Document getDomElement(String xml)
    {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);
        }
        catch(Exception e){}

        return doc;
    }

    public static String getValue(Element item, String str)
    {
        NodeList n = item.getElementsByTagName(str);
        return getElementValue(n.item(0));
    }
     
    public static final String getElementValue( Node elem )
    {
         Node child;
         if( elem != null){
             if (elem.hasChildNodes()){
                 for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                     if( child.getNodeType() == Node.TEXT_NODE  ){
                         return child.getNodeValue();
                     }
                 }
             }
         }
         return "";
     }
}
