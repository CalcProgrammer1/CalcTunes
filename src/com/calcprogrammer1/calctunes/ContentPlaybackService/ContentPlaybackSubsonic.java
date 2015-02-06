package com.calcprogrammer1.calctunes.ContentPlaybackService;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.calcprogrammer1.calctunes.ContentLibraryFragment.ContentListElement;
import com.calcprogrammer1.calctunes.Subsonic.SubsonicAPI;
import com.calcprogrammer1.calctunes.Subsonic.SubsonicConnection;

import java.util.ArrayList;

public class ContentPlaybackSubsonic implements ContentPlaybackService.ContentPlaybackType
{
    private SubsonicConnection subcon;
    private int position;
    private String nowPlayingUri;
    private String contentString;
    private boolean nowPlayingStream;
    private Context context;

    private ArrayList<SubsonicAPI.SubsonicArtist> artistList;
    private SubsonicAPI.SubsonicArtist  nowPlayingArtist;
    private SubsonicAPI.SubsonicAlbum   nowPlayingAlbum;
    private SubsonicAPI.SubsonicSong    nowPlayingSong;
    private int                         nowPlayingId;

    public ContentPlaybackSubsonic(SubsonicConnection sc, int pos)
    {
        subcon = sc;
        position = pos;

        if(subcon.listData.get(position).cache == ContentListElement.CACHE_SDCARD_ORIGINAL)
        {
            nowPlayingUri = subcon.listData.get(position).origPath + "." + subcon.listData.get(position).origExt;
            nowPlayingStream = false;
        }
        else if( subcon.listData.get(position).cache == ContentListElement.CACHE_SDCARD_TRANSCODED)
        {
            nowPlayingUri = subcon.listData.get(position).transPath + "." + subcon.listData.get(position).transExt;
            nowPlayingStream = false;
        }
        else
        {
            // Android versions < API 10 (Android 3.0) cannot stream HTTP sources with MediaPlayer
            // On these devices, download the transcoded file before playback
            // On API 10 or greater, open the mediaplayer for streaming directly
            if (Build.VERSION.SDK_INT < 10)
            {
                subcon.downloadTranscoded(position, context);
                nowPlayingUri = subcon.listData.get(position).transPath + "." + subcon.listData.get(position).transExt;
                nowPlayingStream = false;
            }
            else
            {
                nowPlayingId     = subcon.listData.get(position).id;
                nowPlayingSong   = subcon.subsonicapi.SubsonicGetSong(nowPlayingId);
                nowPlayingAlbum  = subcon.subsonicapi.SubsonicGetAlbum(nowPlayingSong.albumId);
                nowPlayingArtist = subcon.subsonicapi.SubsonicGetArtist(nowPlayingAlbum.artistId);
                artistList       = subcon.subsonicapi.SubsonicGetArtists();

                nowPlayingUri = subcon.streamUrlTranscodedId(nowPlayingId);
                nowPlayingStream = true;
            }
        }
    }

    @Override
    public void NextTrack()
    {
        if(Build.VERSION.SDK_INT < 10)
        {
        }
        else
        {
            //Find current song in album
            for(int i = 0; i < nowPlayingAlbum.songs.size(); i++)
            {
                if(nowPlayingAlbum.songs.get(i).id == nowPlayingId)
                {
                    //If last ID
                    if(i >= nowPlayingAlbum.songs.size() - 1)
                    {
                        for(int j = 0; j < nowPlayingArtist.albums.size(); j++)
                        {
                            if(nowPlayingArtist.albums.get(j).id == nowPlayingAlbum.id)
                            {
                                if(j >= nowPlayingArtist.albums.size() - 1)
                                {
                                    for(int k = 0; k < artistList.size(); k++)
                                    {
                                        if(nowPlayingArtist.id == artistList.get(k).id)
                                        {
                                            if(k >= artistList.size() - 1)
                                            {
                                                nowPlayingArtist = subcon.subsonicapi.SubsonicGetArtist(artistList.get(0).id);
                                                nowPlayingAlbum  = subcon.subsonicapi.SubsonicGetAlbum(nowPlayingArtist.albums.get(0).id);
                                                nowPlayingId     = nowPlayingAlbum.songs.get(0).id;
                                                nowPlayingUri    = subcon.streamUrlTranscodedId(nowPlayingId);
                                                nowPlayingStream = true;
                                            }
                                            else
                                            {
                                                nowPlayingArtist = subcon.subsonicapi.SubsonicGetArtist(artistList.get(k + 1).id);
                                                nowPlayingAlbum  = subcon.subsonicapi.SubsonicGetAlbum(nowPlayingArtist.albums.get(0).id);
                                                nowPlayingId     = nowPlayingAlbum.songs.get(0).id;
                                                nowPlayingUri    = subcon.streamUrlTranscodedId(nowPlayingId);
                                                nowPlayingStream = true;
                                            }
                                            break;
                                        }
                                    }
                                }
                                else
                                {
                                    nowPlayingAlbum = subcon.subsonicapi.SubsonicGetAlbum(nowPlayingArtist.albums.get(j + 1).id);
                                    nowPlayingId    = nowPlayingAlbum.songs.get(0).id;
                                    nowPlayingUri   = subcon.streamUrlTranscodedId(nowPlayingId);
                                    nowPlayingStream = true;
                                }
                                break;
                            }
                        }
                    }
                    else
                    {
                        nowPlayingId = nowPlayingAlbum.songs.get(i + 1).id;
                        nowPlayingUri = subcon.streamUrlTranscodedId(nowPlayingId);
                        nowPlayingStream = true;
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void PrevTrack()
    {
        if(Build.VERSION.SDK_INT < 10)
        {
        }
        else
        {
            //Find current song in album
            for(int i = 0; i < nowPlayingAlbum.songs.size(); i++)
            {
                if(nowPlayingAlbum.songs.get(i).id == nowPlayingId)
                {
                    //If first ID
                    if(i <= 0)
                    {
                        for(int j = 0; j < nowPlayingArtist.albums.size(); j++)
                        {
                            if(nowPlayingArtist.albums.get(j).id == nowPlayingAlbum.id)
                            {
                                if(j <= 0)
                                {
                                    for(int k = 0; k < artistList.size(); k++)
                                    {
                                        if(nowPlayingArtist.id == artistList.get(k).id)
                                        {
                                            if(k <= 0)
                                            {
                                                nowPlayingArtist = subcon.subsonicapi.SubsonicGetArtist(artistList.get(artistList.size() - 1).id);
                                                nowPlayingAlbum  = subcon.subsonicapi.SubsonicGetAlbum(nowPlayingArtist.albums.get(nowPlayingArtist.albums.size() - 1).id);
                                                nowPlayingId     = nowPlayingAlbum.songs.get(nowPlayingAlbum.songs.size() - 1).id;
                                                nowPlayingUri    = subcon.streamUrlTranscodedId(nowPlayingId);
                                                nowPlayingStream = true;
                                            }
                                            else
                                            {
                                                nowPlayingArtist = subcon.subsonicapi.SubsonicGetArtist(artistList.get(k - 1).id);
                                                nowPlayingAlbum  = subcon.subsonicapi.SubsonicGetAlbum(nowPlayingArtist.albums.get(nowPlayingArtist.albums.size() - 1).id);
                                                nowPlayingId     = nowPlayingAlbum.songs.get(nowPlayingAlbum.songs.size() - 1).id;
                                                nowPlayingUri    = subcon.streamUrlTranscodedId(nowPlayingId);
                                                nowPlayingStream = true;
                                            }
                                            break;
                                        }
                                    }
                                }
                                else
                                {
                                    nowPlayingAlbum = subcon.subsonicapi.SubsonicGetAlbum(nowPlayingArtist.albums.get(j - 1).id);
                                    nowPlayingId    = nowPlayingAlbum.songs.get(nowPlayingAlbum.songs.size() - 1).id;
                                    nowPlayingUri   = subcon.streamUrlTranscodedId(nowPlayingId);
                                    nowPlayingStream = true;
                                }
                                break;
                            }
                        }
                    }
                    else
                    {
                        nowPlayingId = nowPlayingAlbum.songs.get(i - 1).id;
                        nowPlayingUri = subcon.streamUrlTranscodedId(nowPlayingId);
                        nowPlayingStream = true;
                    }
                    break;
                }
            }
        }

    }

    @Override
    public void RandomTrack()
    {

    }

    @Override
    public void NextArtist()
    {

    }

    @Override
    public void PrevArtist()
    {

    }

    @Override
    public String getNowPlayingUri()
    {
        return nowPlayingUri;
    }

    @Override
    public boolean getNowPlayingStream()
    {
        return nowPlayingStream;
    }

    @Override
    public String getContentString()
    {
        return contentString;
    }

    @Override
    public int getContentType()
    {
        return ContentPlaybackService.CONTENT_TYPE_SUBSONIC;
    }

    @Override
    public void setContext(Context con)
    {
        context = con;
    }

    @Override
    public void CleanUp()
    {
        nowPlayingUri = "";
    }
}
