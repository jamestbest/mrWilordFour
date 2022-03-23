package com.mygdx.game.Generation;

import com.mygdx.game.Game.Task;

import java.util.HashMap;

public class Tile extends MapComponent{
    public boolean canWalkOn = true;
    public boolean canSpawnOn = true;

    public Task task;

    public Tile(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public Tile(){

    }

    public void setTask(String task, String subType){
    	this.task = new Task(task);
    	this.task.subType = subType;
    }

    public void updateWalkAndSpawn(HashMap<String, TileInformation> tileInformationHashMap){
        canSpawnOn = tileInformationHashMap.get(type).canSpawnOn;
        canWalkOn = tileInformationHashMap.get(type).canWalkOn;
    }

    public void updateWalkAndSpawn(HashMap<String, TileInformation> tileInformation, String thingType){
        canSpawnOn = tileInformation.get(thingType).canSpawnOn && tileInformation.get(type).canSpawnOn;
        canWalkOn = tileInformation.get(thingType).canWalkOn && tileInformation.get(type).canWalkOn;
    }
}
