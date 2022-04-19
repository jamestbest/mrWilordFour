package com.mygdx.game.Generation;

import java.util.HashMap;

public class Tile extends MapComponent{
    public boolean canWalkOn = true;
    public boolean canSpawnOn = true;

    public boolean hasFireOn;
    public boolean hasFloorDropOn;

    public boolean hasBeenFished;

    public Tile(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public Tile(){

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
