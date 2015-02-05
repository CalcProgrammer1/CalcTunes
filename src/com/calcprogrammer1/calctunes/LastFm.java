package com.calcprogrammer1.calctunes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class LastFm
{
    static String apiKey = "";
    static String apiSecret = "";
    static String sessionKey = "";

    String userName = "";
    String userPass = "";

    //Shared Preferences
    private SharedPreferences appSettings;

    SharedPreferences.OnSharedPreferenceChangeListener appSettingsListener = new SharedPreferences.OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
        {
            appSettings = arg0;
            userName = appSettings.getString("lastfm_username", "");
            userPass = appSettings.getString("lastfm_password", "");
            if(!userName.equals("") && !userPass.equals(""))
            {
                authenticate();
            }
        }
    };

    public LastFm(Context c)
    {
        //Get the application preferences
        appSettings = PreferenceManager.getDefaultSharedPreferences(c);
        appSettings.registerOnSharedPreferenceChangeListener(appSettingsListener);

        userName = appSettings.getString("lastfm_username", "");
        userPass = appSettings.getString("lastfm_password", "");

        if(!userName.equals("") && !userPass.equals(""))
        {
            authenticate();
        }
    }

    private class LastFmParameter
    {
        public LastFmParameter(){}
        public LastFmParameter(String n, String v)
        {
            name = n;
            value = v;
        }

        public String name;
        public String value;
    }
    
    private class LastFmParameterComparator implements Comparator<LastFmParameter>
    {
        public int compare(LastFmParameter first, LastFmParameter second)
        {
            return first.name.compareTo(second.name);
        }
    }
    
    public LastFmParameter getSignature(ArrayList<LastFmParameter> params)
    {
        LastFmParameter returnval = new LastFmParameter();
        returnval.name = "api_sig";
        String paramlist = "";
        Collections.sort(params, new LastFmParameterComparator());
        for(int i = 0; i < params.size(); i++)
        {
            paramlist += params.get(i).name + params.get(i).value;
        }
        returnval.value = md5(paramlist + apiSecret);
        return returnval;
    }
    
    public String getParameterString(ArrayList<LastFmParameter> params)
    {
        String returnval = "";
        Collections.sort(params, new LastFmParameterComparator());
        for(int i = 0; i < params.size(); i++)
        {
            returnval += params.get(i).name + "=" + params.get(i).value;
            if(i != params.size()-1)
            {
                returnval += "&";
            }
        }
        return returnval;
    }
    
    public String sendRequest(ArrayList<LastFmParameter> params)
    {
        try
        {
            String request = "https://ws.audioscrobbler.com/2.0/?";
            LastFmParameter signature = getSignature(params);
            request += "&" + signature.name + "=" + signature.value;
            Log.d("LastFm", "Request: " + request);

            HttpPost httpPost = new HttpPost(request);

            ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();

            for(int i = 0; i < params.size(); i++)
            {
                list.add(new BasicNameValuePair(params.get(i).name, params.get(i).value));
            }
            list.add(new BasicNameValuePair(signature.name, signature.value));

            httpPost.setEntity(new UrlEncodedFormEntity(list));

            HttpResponse response = createHttpClient().execute(httpPost);

            String resp = EntityUtils.toString(response.getEntity());

            Log.d("LastFm", "Response: " + resp);

            return resp;
        }catch(Exception e){}
        return null;
    }
    
    public void authenticate()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                ArrayList<LastFmParameter> params = new ArrayList<LastFmParameter>();
                params.add(new LastFmParameter("method", "auth.getMobileSession"));
                params.add(new LastFmParameter("username", userName));
                params.add(new LastFmParameter("password", userPass));
                params.add(new LastFmParameter("api_key", apiKey));

                String XMLData    = sendRequest(params);
                Document DocData  = CalcTunesXMLParser.getDomElement(XMLData);
                if( DocData != null )
                {
                    NodeList NodeData = DocData.getElementsByTagName("lfm");
                    String status = getNamedString(NodeData, 0, "status");
                    String name = DocData.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                    String key = DocData.getElementsByTagName("key").item(0).getFirstChild().getNodeValue();
                    String subscriber = DocData.getElementsByTagName("subscriber").item(0).getFirstChild().getNodeValue();

                    Log.d("LastFm", "Authenticate results: status=" + status + " name=" + name + " key=" + key + " subscriber=" + subscriber);

                    sessionKey = key;

                    if (key != null && !key.equals(""))
                    {

                    }
                }
            }
        }).start();
    }

    public void updateNowPlaying(final String artist, final String album, final String track, final int duration)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                ArrayList<LastFmParameter> params = new ArrayList<LastFmParameter>();
                params.add(new LastFmParameter("method", "track.updateNowPlaying"));
                params.add(new LastFmParameter("artist", artist));
                params.add(new LastFmParameter("track",  track));
                params.add(new LastFmParameter("duration", ""+duration));
                if(album != null)
                {
                    params.add(new LastFmParameter("album", album));
                }
                params.add(new LastFmParameter("api_key", apiKey));
                params.add(new LastFmParameter("sk", sessionKey));

                String XMLData = sendRequest(params);
                if(XMLData != null)
                {
                    Log.d("LastFm", XMLData);
                }
            }
        }).start();
    }

    public void scrobble(final String artist, final String album, final String track, final int duration, final long timestamp)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                ArrayList<LastFmParameter> params = new ArrayList<LastFmParameter>();
                params.add(new LastFmParameter("method", "track.scrobble"));
                params.add(new LastFmParameter("artist", artist));
                params.add(new LastFmParameter("track",  track));
                params.add(new LastFmParameter("duration", ""+duration));
                params.add(new LastFmParameter("timestamp", ""+timestamp));
                if(album != null)
                {
                    params.add(new LastFmParameter("album", album));
                }
                params.add(new LastFmParameter("api_key", apiKey));
                params.add(new LastFmParameter("sk", sessionKey));

                String XMLData = sendRequest(params);
                if(XMLData != null)
                {
                    Log.d("LastFm", XMLData);
                }
            }
        }).start();
    }

    private HttpClient createHttpClient()
    {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);

        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

        return new DefaultHttpClient(conMgr, params);
    }

    private String md5(String data)
    {
        MessageDigest m = null;
        try
        {
            m = MessageDigest.getInstance("MD5");
        }catch (Exception e){}
        m.update(data.getBytes(), 0, data.length());
        String hash = new BigInteger(1, m.digest()).toString(16);
        return hash;
    }

    //return a boolean value from a node list
    public Boolean getNamedBoolean(NodeList data, int id, String item)
    {
        Boolean retVal = false;
        try
        {
            retVal = Boolean.parseBoolean(data.item(id).getAttributes().getNamedItem(item).getNodeValue());
        }
        catch(Exception e)
        {
            Log.d("SubsonicAPI", "Warning: no node " + item + " at node ID " + id + "!");
        }
        return retVal;
    }

    //return an integer value from a node list
    public int getNamedInteger(NodeList data, int id, String item)
    {
        int retVal = 0;
        try
        {
            retVal = Integer.parseInt(data.item(id).getAttributes().getNamedItem(item).getNodeValue());
        }
        catch(Exception e)
        {
            Log.d("SubsonicAPI", "Warning: no node " + item + " at node ID " + id + "!");
        }
        return retVal;
    }

    //return a string value from a node list
    public String getNamedString(NodeList data, int id, String item)
    {
        String retVal = "";
        try
        {
            retVal = data.item(id).getAttributes().getNamedItem(item).getNodeValue();
        }
        catch(Exception e)
        {
            Log.d("SubsonicAPI", "Warning: no node " + item + " at node ID " + id + "!");
        }
        return retVal;
    }
}
