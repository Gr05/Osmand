package net.osmand.plus.traffic;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by vial-grelier on 25/03/2017.
 */

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