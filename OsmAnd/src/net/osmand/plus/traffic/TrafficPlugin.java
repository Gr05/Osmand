package net.osmand.plus.traffic;

import android.app.Activity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class TrafficPlugin extends OsmandPlugin {

	/*test CI*/
	public static final String ID = "traffic.plugin";
	public static final String COMPONENT = "net.osmand.TrafficPlugin";
	private OsmandApplication app;
	private String previousRenderer = RendererRegistry.DEFAULT_RENDER;
	private TrafficLayer trafficLayer;

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

	@Override
	public boolean init(final OsmandApplication app, final Activity activity) {
		if(activity != null) {
			// called from UI
			Log.d("DEBUG : ", "dans la méthode INIT");
			previousRenderer = app.getSettings().RENDERER.get();
			app.getSettings().RENDERER.set(RendererRegistry.TRAFFIC_RENDER);
			// traficParser();
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

	/*
	Timer timer = new Timer ();
	TimerTask hourlyTask = new TimerTask () {
		@Override
		public void run () {
			// your code here...
		}
	};

	// schedule the task to run starting now and then every hour...
	timer.schedule (hourlyTask, 0l, 1000*60*60);   // 1000*10*60 every 10 minut
	 */

	private final static int INTERVAL = 1000 * 60 * 2; //2 minutes
	Handler mHandler = new Handler();

	Runnable mHandlerTask = new Runnable() {
		@Override
		public void run() {
			traficParser();
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



	/* Parser */
	public void traficParser(){
		String result = null;
        try {
			Log.d("DEBUG", "Dans le premier try");
            URL url = new URL("http://data.metromobilite.fr/api/troncons/json");
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
					try {
						Log.d("DEBUG", "Dans le troisième try");
						Log.d("DEBUG : ", result);
						JSONObject jObject = new JSONObject(result);
						Log.d("DEBUG : ", jObject.toString(4));
					} catch (JSONException e) {
						Log.e("ERREUR (3e try) : ", e.getMessage(), e);
					}
				}
            } catch (Exception e) {
				Log.e("ERREUR (2e try) : ", e.getMessage(), e);
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
			if (trafficLayer != null) {
				activity.getMapView().removeLayer(trafficLayer);
				trafficLayer = null;
			}
		}
	}
}

