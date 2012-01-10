package com.calcprogrammer1.calctunes;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class LastFm
{
    static String apiKey = "";
    static String apiSecret = "";
    
    String userName = "";
    String userPass = "";
    String userAuth = md5(userName + md5(userPass));
    
    private class LastFmParameter
    {
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
        String returnval = null;
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
    
    public Document sendRequest(ArrayList<LastFmParameter> params)
    {
        Document doc = null;
        try
        {
            String request = "http://ws.audioscrobbler.com/2.0/?" + getParameterString(params);
            LastFmParameter signature = getSignature(params);
            request += "&" + signature.name + "=" + signature.value;
            
            URL url = new URL(request);
            InputStream is = url.openStream();
            
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = db.parse(is);
            
        }catch(Exception e){}
        return doc;
    }
    
    public void authenticate()
    {
        ArrayList<LastFmParameter> params = new ArrayList<LastFmParameter>();
        LastFmParameter param = new LastFmParameter();
        
        param.name = "method";
        param.value = "auth.getMobileSession";
        params.add(param);
        
        param.name = "username";
        param.value = userName;
        params.add(param);
        
        param.name = "authToken";
        param.value = userAuth;
        params.add(param);
        
        sendRequest(params);
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
}
