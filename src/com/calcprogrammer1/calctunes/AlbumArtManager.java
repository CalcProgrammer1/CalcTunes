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

import com.calcprogrammer1.calctunes.SourceList.SourceListOperations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class AlbumArtManager
{
    static public void setImageAsync(final String artist, final String album, final Context c, final boolean thumb, final ImageView view)
    {
        Bitmap artwork = null;

        artwork = getAlbumArt(artist, album, c, thumb, false, false);

        if (view != null && artwork != null)
        {
            view.setImageBitmap(artwork);
        }

        if (artwork == null)
        {
            if (view != null)
            {
                view.setImageBitmap(BitmapFactory.decodeResource(c.getResources(), R.drawable.icon));
            }

            new AsyncTask<Void, Void, Void>()
            {
                Bitmap artwork2;

                @Override
                protected Void doInBackground(Void... arg0)
                {
                    artwork2 = getAlbumArt(artist, album, c, thumb, true, true);
                    return null;
                }

                @Override
                protected void onPostExecute(Void arg0)
                {
                    if (artwork2 != null)
                    {
                        view.setImageBitmap(artwork2);
                    }
                }
            }.execute();
        }
    }

    static public void replaceAlbumArtFile(String artist, String album, Context c, String replaceFilePath)
    {
        String path = getAlbumArtFileName(c, artist, album);
        String extension = FileOperations.getExtension(replaceFilePath);
        if(new File(path + ".png").isFile())
        {
            new File(path + ".png").delete();
        }
        if(new File(path + ".jpg").isFile())
        {
            new File(path + ".jpg").delete();
        }
        FileOperations.copy(replaceFilePath, path + "." + extension);
    }

    static private String getAlbumArtPath(Context c)
    {
        return( SourceListOperations.getAlbumArtPath(c) );
    }

    static private String getAlbumArtFilePath(Context c, String artist)
    {
        return(getAlbumArtPath(c) + File.separator + SourceListOperations.makeFilename(artist) + "/");
    }

    static private String getAlbumArtFileName(Context c, String artist, String album)
    {
        return(getAlbumArtPath(c) + File.separator + SourceListOperations.makeFilename(artist) + "/" + SourceListOperations.makeFilename(album));
    }

    //Checks cache for album art, if it is not found return default icon
    static public Bitmap getAlbumArtFromCache(String artist, String album, Context c)
    {
        Bitmap artwork = null;
        String path = getAlbumArtFileName(c, artist, album);
        String artPath = null;

        if(new File(path + ".png").isFile())
        {
            artPath = path + ".png";
        }
        else if(new File(path + ".jpg").isFile())
        {
            artPath = path + ".jpg";
        }
        Log.d("AlbumArtManager", "Loading cached bitmap: " + artPath);
        if(artPath != null)
        {
            artwork = BitmapFactory.decodeFile(artPath);
        }
        return artwork;
    }

    static private Bitmap getAlbumArtFromLastFM(String artist, String album, Context c)
    {
        Bitmap artwork;
        try
        {
            String apiKey = "4b724a8d125b0c56965ad3e28a51530c";
            String imageSize = "large";
            String method = "album.getinfo";

            String request = "http://ws.audioscrobbler.com/2.0/?method=" + method + "&api_key=" + apiKey + "&artist=" + artist.replaceAll(" ", "%20") + "&album=" + album.replaceAll(" ", "%20");

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
                    if (artwork != null)
                    {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        artwork.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                        File outfile = new File(getAlbumArtFilePath(c, artist));
                        outfile.mkdirs();
                        outfile = new File(getAlbumArtFileName(c, artist, album) + ".png");
                        FileOutputStream fo = new FileOutputStream(outfile);
                        fo.write(bytes.toByteArray());
                        fo.close();
                        return artwork;
                    }
                }
            }

        }
        catch(Exception ex)
        {
            Log.d("AlbumArt Exception", "" + ex);
        }
        return null;
    }

    static private Bitmap getDefaultAlbumArt(Context c)
    {
        try
        {
            return(BitmapFactory.decodeResource(c.getResources(), R.drawable.icon));
        } catch (Exception ex)
        {
        }
        return null;
    }

    //Check cache for album art, if not found then check Last.fm, if still not found then return default icon
    static public Bitmap getAlbumArt(String artist, String album, Context c, boolean thumb, boolean fetch, boolean icon)
    {
        Bitmap artwork = null;

        if (artwork == null && thumb)
        {
            artwork = decodeSampledBitmapFromFile(artist, album, c, 256, 256);
        }
        if(artwork == null && fetch)
        {
            artwork = getAlbumArtFromLastFM(artist, album, c);
        }
        if(artwork == null)
        {
            artwork = getAlbumArtFromCache(artist, album, c);
        }
        if(artwork == null && icon)
        {
            artwork = getDefaultAlbumArt(c);
        }
        return artwork;
    }

    public static Bitmap decodeSampledBitmapFromFile(String artist, String album, Context c, int reqWidth, int reqHeight)
    {
        String path = getAlbumArtFileName(c, artist, album);
        String artPath = null;

        if(new File(path + ".png").isFile())
        {
            artPath = path + ".png";
        }
        else if(new File(path + ".jpg").isFile())
        {
            artPath = path + ".jpg";
        }

        if(artPath != null)
        {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(artPath, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(artPath, options);
        }
        return(null);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}
