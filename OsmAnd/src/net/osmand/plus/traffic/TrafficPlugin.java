package net.osmand.plus.traffic;

import android.app.Activity;
import android.util.Log;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.render.RendererRegistry;
import net.osmand.plus.views.OsmandMapTileView;

import java.util.ArrayList;

public class TrafficPlugin extends OsmandPlugin {

	public static final String ID = "traffic.plugin";
	public static final String COMPONENT = "net.osmand.TrafficPlugin";
	private OsmandApplication app;
	private String previousRenderer = RendererRegistry.DEFAULT_RENDER;
	private TrafficLayer trafficLayer;
	// troncons is the set of roads
	private static ArrayList<Troncon> troncons = new ArrayList<Troncon>();

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

    public static ArrayList<Troncon> getTroncons(){
        return troncons;
    }

	public void printTroncons(){
		for (int i = 0; i < this.troncons.size(); i++){
			this.troncons.get(i).printTroncon();
		}
	}

	@Override
	public boolean init(final OsmandApplication app, final Activity activity) {
		if(activity != null) {
			// called from UI
			Log.d("DEBUG : ", "dans la méthode INIT");
			previousRenderer = app.getSettings().RENDERER.get();
			app.getSettings().RENDERER.set(RendererRegistry.TRAFFIC_RENDER);
			BackgroundHandler.startRepeatingGetTrafficInfo(app);
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
		activity.getMapView().addLayer(trafficLayer, 12f);
	}

	@Override
	public void updateLayers(OsmandMapTileView mapView, MapActivity activity) {
		if (isActive()) {
			// Starts to loop to get traffic info every 2 minutes or so.
			// Specify the timer in BackgroundHandler
			BackgroundHandler.startRepeatingGetTrafficInfo(app);
			if (trafficLayer == null) {
				registerLayers(activity);
			}
		} else {
			// Stop looping to get traffic info.
			BackgroundHandler.stopRepeatingGetTrafficInfo();
			if (trafficLayer != null) {
				activity.getMapView().removeLayer(trafficLayer);
				trafficLayer = null;
			}
		}
	}
}

