package net.osmand.plus.traffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
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
import java.util.Iterator;
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

    private int canvas_save = -10;

    private DisplayMetrics dm;

    private final MapActivity map;
    private OsmandMapTileView view;

    private Paint bitmapPaint;
    private Paint fluidTrafficPaint;
    private Paint normalTrafficPaint;
    private Paint hardTrafficPaint;
    private Paint noTrafficPaint;

    private Bitmap workIcon;
    
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
        workIcon = BitmapFactory.decodeResource(view.getResources(), R.drawable.map_pin_avoid_road);

        //basic paint
        Paint paint = new Paint();
        paint.setShadowLayer((float) 2.0, (float) 2.0, (float) 2.0, Color.BLACK);
        paint.setStrokeWidth(2.5f * view.getDensity());               // set the size
        paint.setDither(true);                    // set the dither to true
        paint.setStyle(Paint.Style.STROKE);       // set to STOKE
        paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        paint.setPathEffect(new CornerPathEffect(10) );   // set the path effect when they join.
        paint.setAntiAlias(true);

        // fluid traffic paint
        fluidTrafficPaint = new Paint(paint);
        fluidTrafficPaint.setColor(0xFF00FF00);


        // normal traffic paint
        normalTrafficPaint = new Paint(paint);
        normalTrafficPaint.setColor(0xFFFFFF00);

        // hard traffic paint
        hardTrafficPaint = new Paint(paint);
        hardTrafficPaint.setColor(0xFFFF0000);

        // no trafic data paint
        noTrafficPaint = new Paint(paint);
        noTrafficPaint.setColor(0xFFFFFFFF);

        contextMenuLayer = view.getLayerByClass(ContextMenuLayer.class);
    }

    public void drawTroncon (Canvas canvas, RotatedTileBox tileBox, Troncon troncon){

        Path pathToDraw = new Path();
        ArrayList<SsTroncon> etapes = troncon.getEtapes();
        Iterator<SsTroncon> iterator = etapes.iterator();

        while (iterator.hasNext()){
            SsTroncon ssTroncon = iterator.next();
            float locationX1 = tileBox.getPixXFromLonNoRot(ssTroncon.getFrom().getLon());
            float locationY1 = tileBox.getPixYFromLatNoRot(ssTroncon.getFrom().getLat());
            pathToDraw.moveTo(locationX1, locationY1);
            float locationX2 = tileBox.getPixXFromLonNoRot(ssTroncon.getTo().getLon());
            float locationY2 = tileBox.getPixYFromLatNoRot(ssTroncon.getTo().getLat());
            pathToDraw.lineTo(locationX2, locationY2);
        }

        Log.d("TRONCON : ", "charge : " + troncon.getCharge());
        switch(troncon.getCharge()) {
            case -1:
                canvas.drawPath(pathToDraw, noTrafficPaint);
                break;
            case 0:
                canvas.drawPath(pathToDraw, noTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 0, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 1:
                canvas.drawPath(pathToDraw, fluidTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 1, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 2:
                canvas.drawPath(pathToDraw, normalTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 2, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 3:
                canvas.drawPath(pathToDraw, hardTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 2, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 4:
                canvas.drawPath(pathToDraw, hardTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 2, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            case 5:
                canvas.drawPath(pathToDraw, hardTrafficPaint);
                //Log.d("DEBUG :", "drawSsTroncon: 2, lon :" + t.getFrom().getLon() + " lat : " + t.getFrom().getLat());
                break;
            default:
                Log.e("ERROR : ", "Draw troncon default case for :" + troncon.toString());
        }
    }
    @Override

    public void onDraw(Canvas canvas, RotatedTileBox tileBox, DrawSettings nightMode) {
        if( canvas_save  != -10 ){
            canvas.restore();
        }
        this.canvas_save = canvas.save();
        refresh();
        Log.d("DEBUG : ", "Dans onDraw()");
        Log.d("DEBUG : ", "Taille du plugin.getTroncons : " + plugin.getTroncons().size());
        for (int i = 0; i<plugin.getTroncons().size(); i++){
            drawTroncon(canvas, tileBox, plugin.getTroncons().get(i));
        }

        for (int i = 0; i < plugin.getEvents().size(); i ++){
            Log.d("DEBUG : ", "Taille du plugin.getEvents : " + plugin.getEvents().size());
            Event evt = plugin.getEvents().get(i);

            switch (evt.getType()){
                case "chantier":

                    LatLon ppmPos = new LatLon(evt.getLocation().getLat(), evt.getLocation().getLon());

                    float locationX = tileBox.getPixXFromLonNoRot(ppmPos.getLongitude());
                    float locationY = tileBox.getPixYFromLatNoRot(ppmPos.getLatitude());
                    Bitmap parkingIcon = workIcon;
                    int marginX = workIcon.getWidth() / 2;
                    int marginY = workIcon.getHeight();
                    canvas.rotate(-view.getRotate(), locationX, locationY);
                    canvas.drawBitmap(parkingIcon, locationX - marginX, locationY - marginY, bitmapPaint);
                    break;

            }
        }

    }

    public void refresh(){
        this.view.refreshMap();
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
