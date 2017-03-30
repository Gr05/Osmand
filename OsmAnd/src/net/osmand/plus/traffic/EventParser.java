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
import java.util.Iterator;

/**
 * Created by vial-grelier on 29/03/2017.
 */

public class EventParser {

    /*
    This is purely the parser. No library used only native android. This is the function that will need to modified once the data will be opened.

     */
    public static void parseEventData(JSONObject dataEvents, ArrayList<Event> events){
        Log.d("DEBUG : ", "Dans le parseur des évènements");
        Log.d("DEBUG : ", String.valueOf(dataEvents.length()));
        TrafficPlugin.getEvents().clear();
        Iterator<String> it = dataEvents.keys();
        while ( it.hasNext() ){
            try {
                String current_obj = it.next();
                JSONObject event = dataEvents.getJSONObject(current_obj);
                String type = event.getString("type");
                if (type.equals("chantier")) {
                    int id = event.getInt("id");
                    String startDate = event.getString("dateDebut");
                    String endDate = event.getString("dateFin");
                    String lon = event.getString("longitude");
                    String lat = event.getString("latitude");
                    int weekend = event.getInt("weekEnd");
                    String description = event.getString("texte");
                    Event evt = new Event(type, id, startDate, endDate, lon, lat, weekend, description);
                    TrafficPlugin.getEvents().add(evt);
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
    public static void getEventsInfo (OsmandApplication app){
        try{
            String eventUrl = "http://data.metromobilite.fr/api/dyn/evt/json";
            String eventFilename = "eventCache.json";
            String eventData = InfoFromWeb.getInfoFromWeb(eventUrl, eventFilename, app);
            Log.d("Debug : ", eventData);
            JSONObject eventJson = new JSONObject(eventData);
            parseEventData(eventJson, TrafficPlugin.getEvents());
        } catch (JSONException e) {
            Log.e("ERROR : ", e.getMessage(), e);
        }
    }


}
