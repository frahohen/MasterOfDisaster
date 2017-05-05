package com.github.htw.mod.map;

import java.util.ArrayList;

import com.github.htw.mom.point.SpawnPoint;

public class SpawnMap {

    private ArrayList<SpawnPoint> map;

    public SpawnMap(){
        this.map = new ArrayList<SpawnPoint>();
    }

    public void add(float x, float y) {
        map.add(new SpawnPoint(x,y));
    }

    public int getSize(){
        return map.size();
    }

    public SpawnPoint getSpawnPoint(int index){
        return map.get(index);
    }
}