package net.osmand.plus.traffic;

import android.content.Context;
import android.util.Log;

import net.osmand.plus.OsmandApplication;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by vial-grelier on 25/03/2017.
 */

public class CacheFile {

    public static boolean writeCacheFile(String data, String filename,  OsmandApplication app){
        String JSONdata = data;
        FileOutputStream outputStream;
        try {
            outputStream =  app.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(JSONdata.getBytes());
            outputStream.close();
            Log.d("DEBUG : ", "Fichier normalement écrit");
        } catch (Exception e) {
            Log.e("ERREUR : ", e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getCacheFileContent(String filename,  OsmandApplication app){
        String JSONdata = null;
        try {
            InputStream inputStream = app.openFileInput(filename);
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                JSONdata = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return JSONdata;
    }

}
