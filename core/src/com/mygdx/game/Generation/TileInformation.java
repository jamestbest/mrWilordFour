package com.mygdx.game.Generation;

public class TileInformation {
    public String type;

    public boolean canWalkOn;
    public boolean canSpawnOn;

    public TileInformation(String type, boolean canWalkOn, boolean canSpawnOn) {
        this.type = type;

        this.canWalkOn = canWalkOn;
        this.canSpawnOn = canSpawnOn;
    }

    public TileInformation(){

    }
}
