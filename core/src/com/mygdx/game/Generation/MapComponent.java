package com.mygdx.game.Generation;

import com.mygdx.game.Generation.Things.Thing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MapComponent {
    public int x;
    public int y;

    public String type;

    public boolean[] edgeExists = new boolean[4];
    public int[] edgeID = new int[4];

    static ArrayList<String> canInteract = new ArrayList<>(Arrays.asList("stoneWall", "woodWall", "stoneDoor", "edgeBouncer"));

    public boolean doINotHaveANeighbourAtThisLocation(ArrayList<ArrayList<Thing>> things, int x, int y){
        return Objects.equals(things.get(x).get(y).type, "") || !doIInteractWithLight(things.get(x).get(y));
    }

    public boolean doINotHaveANeighbourAtThisLocation(ArrayList<ArrayList<Tile>> tiles, float x, int y){
        return Objects.equals(tiles.get((int) x).get(y).type, "") || !doIInteractWithLight(tiles.get((int) x).get(y));
    }

    public void clearInfo(){
        for (int i = 0; i < 4; i++){
            edgeExists[i] = false;
            edgeID[i] = 0;
        }
    }

    public boolean doIExist(){
        return (!type.equals(""));
    }

    public static boolean doIInteractWithLight(MapComponent c){
        return canInteract.contains(c.type);
    }
}
