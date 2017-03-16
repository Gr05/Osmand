package net.osmand.plus.traffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.ContextMenuLayer;
import net.osmand.plus.views.OsmandMapLayer;
import net.osmand.plus.views.OsmandMapTileView;

import java.util.ArrayList;
import java.util.List;

/**
 * Class represents a layer which depicts the position of the parked car
 * @author Alena Fedasenka
 * @see TrafficPlugin
 *
 */
public class TrafficLayer extends OsmandMapLayer {
    /**
     * magic number so far
     */
    private static final int radius = 18;

    private DisplayMetrics dm;

    private final MapActivity map;
    private OsmandMapTileView view;

    private Paint bitmapPaint;
    private Paint fluidTrafficPaint;
    private Paint normalTrafficPaint;
    private Paint hardTrafficPaint;

    private Bitmap workIcon;

    ArrayList<TrafficPlugin.Troncon> troncons;

    private TrafficPlugin plugin;

    private ContextMenuLayer contextMenuLayer;

    public TrafficLayer(MapActivity map, TrafficPlugin plugin) {
        this.map = map;
        this.plugin = plugin;
    }

    public LatLon getInWorkPoint() {
        return new LatLon(45.18475, 5.73635);
    }

    @Override
    public void initLayer(OsmandMapTileView view) {
        this.view = view;
        dm = new DisplayMetrics();
        WindowManager wmgr = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        wmgr.getDefaultDisplay().getMetrics(dm);

        bitmapPaint = new Paint();
        bitmapPaint.setDither(true);
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        //workIcon = BitmapFactory.decodeResource(view.getResources(), R.drawable.men_at_work);

        //basic paint
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8.5f * view.getDensity());
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        // fluid traffic paint
        fluidTrafficPaint = new Paint(paint);
        fluidTrafficPaint.setColor(0x8000FF00);

        // normal traffic paint
        normalTrafficPaint = new Paint(paint);
        normalTrafficPaint.setColor(0x80FFFF00);

        // hard traffic paint
        hardTrafficPaint = new Paint(paint);
        hardTrafficPaint.setColor(0x80FF0000);

        contextMenuLayer = view.getLayerByClass(ContextMenuLayer.class);
    }

    public void drawSsTroncon (Canvas canvas, RotatedTileBox tileBox, TrafficPlugin.SsTroncon t){
        float locationX1 = tileBox.getPixXFromLonNoRot(t.getFrom().getLon());
        float locationY1 = tileBox.getPixYFromLatNoRot(t.getFrom().getLat());
        float locationX2 = tileBox.getPixXFromLonNoRot(t.getTo().getLon());
        float locationY2 = tileBox.getPixYFromLatNoRot(t.getTo().getLat());

        switch(t.getCharge()) {
            case 0:
                canvas.drawLine(locationX1, locationY1, locationX2, locationY2, fluidTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 0, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 1:
                canvas.drawLine(locationX1, locationY1, locationX2, locationY2, normalTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 1, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 2:
                canvas.drawLine(locationX1, locationY1, locationX2, locationY2, hardTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 2, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 3:
                canvas.drawLine(locationX1, locationY1, locationX2, locationY2, hardTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 2, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 4:
                canvas.drawLine(locationX1, locationY1, locationX2, locationY2, hardTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 2, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 5:
                canvas.drawLine(locationX1, locationY1, locationX2, locationY2, hardTrafficPaint);
                 //Log.d("DEBUG :", "drawSsTroncon: 2, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            default:
                Log.e("ERROR : ", "DrawSsTroncon default case for :" + t.toString());
        }
    }
    @Override

    public void onDraw(Canvas canvas, RotatedTileBox tileBox, DrawSettings nightMode) {
        Log.d("DEBUG : ", "Dans onDraw()");
        for (int i = 0; i<plugin.getTroncons().size(); i++){
            ArrayList<TrafficPlugin.SsTroncon> etape = plugin.getTroncons().get(i).getEtapes();
            for(int j = 0; j<etape.size(); j++) {
                TrafficPlugin.SsTroncon ssTronconToDraw = etape.get(j);
                drawSsTroncon(canvas, tileBox, ssTronconToDraw);
            }
        }

        /*Bitmap parkingIcon = workIcon;
        int marginX = workIcon.getWidth() / 2;
        int marginY = workIcon.getHeight() / 2;
        canvas.rotate(-view.getRotate(), locationX1, locationY1);
        canvas.drawBitmap(parkingIcon, locationX1 - marginX, locationY1 - marginY, bitmapPaint);*/
    }

    public void onPrepareBufferImage(Canvas canvas, RotatedTileBox tileBox, DrawSettings nightMode){

    }
    @Override
    public void destroyLayer() {
    }

    @Override
    public boolean drawInScreenPixels() {
        return false;
    }

    /**
     * @param latitude1
     * @param longitude1
     * @param latitude2
     * @param longitude2
     * @return true if the parking point is located on a visible part of map
     */
    private boolean isSegmentVisible(RotatedTileBox tb, double latitude1, double longitude1, double latitude2, double longitude2){
        if(getInWorkPoint() == null || view == null){
            return false;
        }
        return tb.containsLatLon(latitude1, longitude1) || tb.containsLatLon(latitude2, longitude2);
    }

    private boolean isLocationVisible(RotatedTileBox tb, double latitude1, double longitude1){
        if(getInWorkPoint() == null || view == null){
            return false;
        }
        return tb.containsLatLon(latitude1, longitude1);
    }
}
