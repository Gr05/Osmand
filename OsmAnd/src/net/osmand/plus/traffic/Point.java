package net.osmand.plus.traffic;

/**
 * Created by vial-grelier on 25/03/2017.
 */

/*
Basic point representation of a point with LatLong coordinates.
 */
public class Point {
    private float lon; // longitude
    private float lat; // latitude

    public Point(float coorX, float coorY){
        this.lon = coorX;
        this.lat = coorY;
    }

    public float getLon() {
        return this.lon;
    }

    public float getLat() {
        return this.lat;
    }
}
