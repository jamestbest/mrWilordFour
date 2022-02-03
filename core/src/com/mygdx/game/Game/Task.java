package com.mygdx.game.Game;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.Tile;

import java.util.ArrayList;

public class Task {
    public String type;

    public boolean reserved = false;

    public Task(String type) {
        this.type = type;
    }

    public Vector2 getNeighbour (ArrayList<ArrayList<Tile>> tileMap, int x, int y) {
        Vector2 output = null;
        if (x + 1 < tileMap.size()){
            if (tileMap.get(x + 1).get(y).canWalkOn) {
                return new Vector2(x + 1, y);
            }
            if (y + 1 < tileMap.size()){
                if (tileMap.get(x + 1).get(y + 1).canWalkOn){
                    return new Vector2(x + 1, y + 1);
                }
            }
            if (y - 1 > 0){
                if (tileMap.get(x + 1).get(y - 1).canWalkOn){
                    return new Vector2(x + 1, y - 1);
                }
            }
        }
        if (x - 1 > 0){
            if (tileMap.get(x - 1).get(y).canWalkOn){
                return new Vector2(x - 1, y);
            }
            if (y + 1 < tileMap.size()){
                if (tileMap.get(x - 1).get(y + 1).canWalkOn){
                    return new Vector2(x - 1, y + 1);
                }
            }
            if (y - 1 > 0){
                if (tileMap.get(x - 1).get(y - 1).canWalkOn){
                    return new Vector2(x - 1, y - 1);
                }
            }
        }
        if (y + 1 < tileMap.size()){
            if (tileMap.get(x).get(y + 1).canWalkOn){
                return new Vector2(x, y + 1);
            }
        }
        if (y - 1 > 0){
            if (tileMap.get(x).get(y - 1).canWalkOn){
                return new Vector2(x, y - 1);
            }
        }
        return null;
    }
}
