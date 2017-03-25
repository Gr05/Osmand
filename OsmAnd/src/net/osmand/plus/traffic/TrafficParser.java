package net.osmand.plus.traffic;

import android.os.StrictMode;
import android.util.Log;

import net.osmand.plus.OsmandApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by vial-grelier on 25/03/2017.
 */

public class TrafficParser {

    public static void parseTrafficData(JSONObject dataTroncon, JSONObject dataTraffic, ArrayList<Troncon> troncons){
        Log.d("DEBUG : ", "Dans le parseur");
        Log.d("DEBUG : ", String.valueOf(dataTraffic.length()));
        JSONArray features = null;
        JSONArray traffic = null;
        TrafficPlugin.getTroncons().clear();
        try {
            features = dataTroncon.getJSONArray("features");
            traffic = null;
        } catch (JSONException e) {
            Log.e("ERREUR", e.getMessage(), e);
        }
        for(int i=0;i<features.length();i++){
            try {
                JSONObject feature = features.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");
                JSONObject geometry = feature.getJSONObject("geometry");
                String identifiant = properties.getString("id");
                int niveau = properties.getInt("NIVEAU");
                int nsv_id = properties.getInt("NSV_ID");
                if (niveau == 1 ) {
                    Troncon troncon = new Troncon(identifiant);
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    for (int j = 0; j < coordinates.length() - 1; j++) {
                        JSONArray coordinateFrom = coordinates.getJSONArray(j);
                        float coordianteXFrom = Float.parseFloat(coordinateFrom.getString(0));
                        float coordianteYFrom = Float.parseFloat(coordinateFrom.getString(1));
                        Point pointFrom = new Point(coordianteXFrom, coordianteYFrom);
                        JSONArray coordinateTo = coordinates.getJSONArray(j + 1);
                        float coordianteXTo = Float.parseFloat(coordinateTo.getString(0));
                        float coordianteYTo = Float.parseFloat(coordinateTo.getString(1));
                        Point pointTo = new Point(coordianteXTo, coordianteYTo);
                        int trafficValue = 0;
                        try {
                            trafficValue = dataTraffic.getJSONArray(identifiant).getJSONObject(0).getInt("nsv_id");
                        } catch (JSONException e) {
                            Log.e("ERREUR : ", e.getMessage(), e);
                        }
                        Log.d("TEST : ", String.valueOf(trafficValue));
                        SsTroncon subTronc = new SsTroncon(
                                pointFrom, pointTo, trafficValue);
                        troncon.addEtape(subTronc);
                    }
                    TrafficPlugin.getTroncons().add(troncon);
                }
            } catch (JSONException e) {
                Log.e("ERREUR", e.getMessage(), e);
            }
        }
    }

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

    /* Retrieving of traffic informations either on web or cache file */
    public static void getTrafficInfo(OsmandApplication app){
        try{
            String tronconUrl = "http://data.metromobilite.fr/api/troncons/json";
            String trafficUrl = "http://data.metromobilite.fr/api/dyn/trr/json";
            String tronconFilename = "tronconCache.json";
            String trafficFilename = "trafficCache.json";
            String tronconData = getInfoFromWeb(tronconUrl, tronconFilename, app);
            Log.d("Debug : ", tronconData);
            JSONObject tronconJson = new JSONObject(tronconData);
            String trafficData = getInfoFromWeb(trafficUrl, trafficFilename, app);
            JSONObject trafficJson = new JSONObject(trafficData);
            parseTrafficData(tronconJson, trafficJson, TrafficPlugin.getTroncons());
        } catch (JSONException e) {
            Log.e("ERROR : ", e.getMessage(), e);
        }
    }
}
