package net.osmand.plus.traffic;

import android.os.StrictMode;
import android.util.Log;

import net.osmand.plus.OsmandApplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by vial-grelier on 29/03/2017.
 */

public class InfoFromWeb {

    /*
        Retrieving information on web url specified. If impossible to get it, then finds the file specified in $filename and reads the data
        in it. If the data can be accessed from web then wirtes in the cache file so last data is stored.
        Returns the data read.
         */
    public static String getInfoFromWeb(String _url, String filename, OsmandApplication app){
        String result = null;
        try {
            Log.d("DEBUG", "Dans le premier try");
            URL url = new URL(_url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                Log.d("DEBUG", "Dans le deuxième try");
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8){
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        Log.d("DEBUG", "Dans le while");
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                    in.close();
                    return result;
                }
            } catch (Exception e) {
                Log.e("ERREUR (2e try) : ", e.getMessage(), e);
				/*
					Traitement à partir du fichier cache
				 */
                Log.d("DEBUG : ", "Accès au fichier cache");
                result = CacheFile.getCacheFileContent(filename, app);
                return result;
                // Log.d("DEBUG : HORS-LIGNE", jObject.toString(4));
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERREUR (1er try) : ", e.getMessage(), e);
        }
        return result;
    }
}
