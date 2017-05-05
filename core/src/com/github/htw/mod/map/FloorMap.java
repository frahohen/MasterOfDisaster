package com.github.htw.mod.map;

import java.util.ArrayList;

import com.github.htw.mom.point.FloorPoint;

public class FloorMap {

    private ArrayList<FloorPoint> map;

    public FloorMap(){
        this.map = new ArrayList<FloorPoint>();
    }

    public int getSize(){
        return map.size();
    }

    public void add(float x, float y) {
        map.add(new FloorPoint(x,y));
    }

    public FloorPoint getFloorPoint(int index){
        return map.get(index);
    }
}
