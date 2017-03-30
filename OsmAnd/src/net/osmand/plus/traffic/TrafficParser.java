package net.osmand.plus.traffic;

import android.util.Log;

import net.osmand.plus.OsmandApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by vial-grelier on 25/03/2017.
 */

public class TrafficParser {

    /*
    This is purely the parser. No library used only native android. This is the function that will need to modified once the data will be opened.
     dataTronon is road information (path) but no trafic. If the trafic data comes from another ressource, then use the second parameter to give it to the parser
     then link both informations based on the ID.
     */
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
                    Troncon troncon = new Troncon(identifiant, -1);
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
                            troncon.setCharge(trafficValue);
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

    /*
    Retrieving of traffic informations either on web or cache file
    Specifies urls and filenames to manage the data
    */
    public static void getTrafficInfo(OsmandApplication app){
        try{
            String tronconUrl = "http://data.metromobilite.fr/api/troncons/json";
            String trafficUrl = "http://data.metromobilite.fr/api/dyn/trr/json";
            String tronconFilename = "tronconCache.json";
            String trafficFilename = "trafficCache.json";
            String tronconData = InfoFromWeb.getInfoFromWeb(tronconUrl, tronconFilename, app);
            Log.d("Debug : ", tronconData);
            JSONObject tronconJson = new JSONObject(tronconData);
            String trafficData = InfoFromWeb.getInfoFromWeb(trafficUrl, trafficFilename, app);
            JSONObject trafficJson = new JSONObject(trafficData);
            parseTrafficData(tronconJson, trafficJson, TrafficPlugin.getTroncons());
        } catch (JSONException e) {
            Log.e("ERROR : ", e.getMessage(), e);
        }
    }
}
