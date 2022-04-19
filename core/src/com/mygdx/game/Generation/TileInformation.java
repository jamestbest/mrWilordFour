package com.mygdx.game.Generation;

public class TileInformation {
    public String type;

    public boolean canWalkOn;
    public boolean canSpawnOn;

    public int drawLayer;

    public TileInformation(String type, boolean canWalkOn, boolean canSpawnOn, int drawLayer){
        this.type = type;

        this.canWalkOn = canWalkOn;
        this.canSpawnOn = canSpawnOn;
        this.drawLayer = drawLayer;
    }

    public TileInformation(){
    }
}
