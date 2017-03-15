package net.osmand.plus.traffic;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.parkingpoint.ParkingPositionLayer;
import net.osmand.plus.render.RendererRegistry;
import net.osmand.plus.views.MapInfoLayer;
import net.osmand.plus.views.OsmandMapTileView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TrafficPlugin extends OsmandPlugin {

	public static final String ID = "traffic.plugin";
	public static final String COMPONENT = "net.osmand.TrafficPlugin";
	private OsmandApplication app;
	private String previousRenderer = RendererRegistry.DEFAULT_RENDER;
	private TrafficLayer trafficLayer;
	private ArrayList<Troncon> troncons = new ArrayList<Troncon>();

	public TrafficPlugin(OsmandApplication app) {
		this.app = app;
	}

	@Override
	public String getDescription() {
		return app.getString(net.osmand.plus.R.string.plugin_traffic_descr);
	}

	@Override
	public String getName() {return app.getString(net.osmand.plus.R.string.plugin_traffic_name);}

	@Override
	public int getLogoResourceId() {
		return R.drawable.ic_car_traffic;
	}
	
	@Override
	public int getAssetResourceName() {
		return R.drawable.traffic_map;
	}


	@Override
	public String getHelpFileName() {
		return "feature_articles/traffic_plugin.html";
	}

	public class Point {
		private float x;
		private float y;

		public Point(float coorX, float coorY){
			this.x = coorX;
			this.y = coorY;
		}

		public float getX() {
			return this.x;
		}

		public float getY() {
			return this.y;
		}
	}

	public class SsTroncon {
		private Point from;
		private Point to;
		private int charge;

		public SsTroncon (Point from, Point to, int charge) {
			this.charge = charge;
			this.from = from;
			this.to = to;
		}

		public Point getFrom(){
			return this.from;
		}

		public Point getTo(){
			return this.to;
		}

		public int getCharge(){
			return this.charge;
		}

		@Override
		public String toString(){
			return "Tronçon qui va de " + getFrom().getX() + " || " + getFrom().getY() +
					" -- à -- " + getTo().getX() + " || " + getTo().getY() +
					" ##### CHARGE : ####### " + getCharge();
		}
	}

	public class Troncon {
		private ArrayList<SsTroncon> etapes = new ArrayList<SsTroncon>();
		private String identifiant;

		public Troncon(String identifiant){
			this.identifiant = identifiant;
		}

		public void addEtape(SsTroncon ssTrc){
			this.etapes.add(ssTrc);
		}

		public void printTroncon(){
			Log.d("DEBUG : ", "Troncon");
			for (int i = 0; i < this.etapes.size(); i ++){
				Log.d("DEBUG : ", etapes.get(i).toString());
			}
		}

		public ArrayList<SsTroncon> getEtapes(){
			return this.etapes;
		}
	}

	public void printTroncons(){
		for (int i = 0; i < this.troncons.size(); i++){
			this.troncons.get(i).printTroncon();
		}
	}

	public void parseTrafficData(JSONObject dataRout){
		Log.d("DEBUG : ", "Dans le parseur");
		JSONArray features = null;
		JSONArray traffic = null;
		this.troncons.clear();
		try {
			features = dataRout.getJSONArray("features");
		} catch (JSONException e) {
			Log.e("ERREUR", e.getMessage(), e);
		}
		for(int i=0;i<features.length();i++){
			try {
				JSONObject feature = features.getJSONObject(i);
				JSONObject properties = feature.getJSONObject("properties");
				JSONObject geometry = feature.getJSONObject("geometry");
				String identifiant = properties.getString("id");
				String niveau = properties.getString("NIVEAU");
				Troncon troncon = new Troncon(identifiant);
				JSONArray coordinates = geometry.getJSONArray("coordinates");
				for (int j=0; j < coordinates.length() - 1; j++){
					JSONArray coordinateFrom = coordinates.getJSONArray(j);
					float coordianteXFrom = Float.parseFloat(coordinateFrom.getString(0));
					float coordianteYFrom = Float.parseFloat(coordinateFrom.getString(1));
					Point pointFrom = new Point (coordianteXFrom, coordianteYFrom);
					JSONArray coordinateTo = coordinates.getJSONArray(j+1);
					float coordianteXTo = Float.parseFloat(coordinateTo.getString(0));
					float coordianteYTo = Float.parseFloat(coordinateTo.getString(1));
					Point pointTo = new Point (coordianteXTo, coordianteYTo);
					SsTroncon subTronc = new SsTroncon(
							pointFrom, pointTo, Integer.parseInt(niveau));
					troncon.addEtape(subTronc);
				}
				this.troncons.add(troncon);
			} catch (JSONException e) {
				Log.e("ERREUR", e.getMessage(), e);
			}
		}
	}

	public boolean writeCacheFile(String data){
		String filename = "trafficCache.json";
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

	public String getCacheFileContent(){
		String filename = "trafficCache.json";
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

	@Override
	public boolean init(final OsmandApplication app, final Activity activity) {
		if(activity != null) {
			// called from UI
			Log.d("DEBUG : ", "dans la méthode INIT");
			previousRenderer = app.getSettings().RENDERER.get();
			app.getSettings().RENDERER.set(RendererRegistry.TRAFFIC_RENDER);
			startRepeatingGetTrafficInfo();
		}
		return true;
	}
	
	@Override
	public void disable(OsmandApplication app) {
		super.disable(app);
		if(app.getSettings().RENDERER.get().equals(RendererRegistry.TRAFFIC_RENDER)) {
			app.getSettings().RENDERER.set(previousRenderer);
		}
	}

	private final static int INTERVAL = 1000 * 60 * 2; //2 minutes (ms * s * min)
	Handler mHandler = new Handler();

	Runnable mHandlerTask = new Runnable() {
		@Override
		public void run() {
			getTrafficInfo();
			mHandler.postDelayed(mHandlerTask, INTERVAL);
		}
	};

	void startRepeatingGetTrafficInfo()
	{
		mHandlerTask.run();
	}

	void stopRepeatingGetTrafficInfo()
	{
		mHandler.removeCallbacks(mHandlerTask);
	}

	/* Retrieving of traffic informations either on web or cache file */
	public void getTrafficInfo(){
		String result = null;
        try {
			Log.d("DEBUG", "Dans le premier try");
            URL url = new URL("http://data.metromobilite.fr/api/troncons/json");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			JSONObject jObject = null;
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
					try {
						Log.d("DEBUG", "Dans le troisième try");
						// Log.d("DEBUG : ", result);
						writeCacheFile(result);
						jObject = new JSONObject(result);
						// Log.d("DEBUG : ", jObject.toString(4));
						parseTrafficData(jObject);
						printTroncons();
					} catch (JSONException e) {
						Log.e("ERREUR (3e try) : ", e.getMessage(), e);
					}
					in.close();
				}
            } catch (Exception e) {
				Log.e("ERREUR (2e try) : ", e.getMessage(), e);
				/*
					Traitement à partir du fichier cache
				 */
				result = getCacheFileContent();
				jObject = new JSONObject(result);
				parseTrafficData(jObject);
				printTroncons();
				// Log.d("DEBUG : HORS-LIGNE", jObject.toString(4));
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
			Log.e("ERREUR (1er try) : ", e.getMessage(), e);
        }
    }



    @Override
	public String getId() {
		return ID;
	}

	@Override
	public Class<? extends Activity> getSettingsActivity() {
		return null;
	}

	@Override
	public void registerLayers(MapActivity activity) {
		// remove old if existing after turn
		if(trafficLayer != null) {
			activity.getMapView().removeLayer(trafficLayer);
		}
		trafficLayer = new TrafficLayer(activity, this);
		activity.getMapView().addLayer(trafficLayer, 5.5f);
	}

	@Override
	public void updateLayers(OsmandMapTileView mapView, MapActivity activity) {
		if (isActive()) {
			startRepeatingGetTrafficInfo();
			if (trafficLayer == null) {
				registerLayers(activity);
			}
		} else {
			stopRepeatingGetTrafficInfo();
			if (trafficLayer != null) {
				activity.getMapView().removeLayer(trafficLayer);
				trafficLayer = null;
			}
		}
	}
}

