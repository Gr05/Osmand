package net.osmand.plus.traffic;

/**
 * Created by vial-grelier on 25/03/2017.
 */

/*
A SsTroncon is a part from Troncon and is composed by two points. The line between two points is the representation of the road.
 */
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
        return "Tronçon qui va de " + getFrom().getLon() + " || " + getFrom().getLat() +
                " -- à -- " + getTo().getLon() + " || " + getTo().getLat() +
                " ##### CHARGE : ####### " + getCharge();
    }
}
