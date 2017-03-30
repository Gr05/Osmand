package net.osmand.plus.traffic;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vial-grelier on 29/03/2017.
 */

public class Event {

    private String type;
    private int id;
    private Date startDate;
    private Date endDate;
    private Point location;
    private int weekend; // 1 if only during week end  2 if during week AND week end 0 if only during week
    private String description;

    public Event (String type,
                  int id,
                  String startDate,
                  String endDate,
                  String lon,
                  String lat,
                  int weekend,
                  String description){

        this.type = type;
        this.id = id;
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Log.d("DEBUG : ", "LATITUDE : " + lat);
        Log.d("DEBUG : ", "LONGITUDE : " + lon);
        try {
            this.startDate = formatter.parse(startDate);
            this.endDate = formatter.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] lon_arr = lon.split("\\.");
        String[] lat_arr = lat.split("\\.");
        this.location = new Point(Float.parseFloat(lon_arr[0] + "." + lon_arr[1]),
                Float.parseFloat(lat_arr[0] + "." + lat_arr[1]));
        this.weekend = weekend;
        this.description = description;
    }

    public Point getLocation(){
        return this.location;
    }

    public String getType() {
        return this.type;
    }
}
