package com.mygdx.game.Generation;

public class Tile {

    public int x;
    public int y;

    public String type;

    public boolean canWalkOn = false;
    public boolean canSpawnOn = true;

    public Tile(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public Tile(){

    }
}
