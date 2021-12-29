package com.mygdx.game.Math.CStar;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.Noise2D;

import java.util.ArrayList;

import static com.mygdx.game.Screens.GameScreen.TILES_ON_X;
import static com.mygdx.game.Screens.GameScreen.TILES_ON_Y;

public class NodeC {
    int x;
    int y;

    NodeC parent;

    public float Global;
    public float Local;

    public float HMP;

    public boolean visited = false;

    public NodeC(int x, int y, int addition) {
        this.x = x;
        this.y = y;

        Global = Float.POSITIVE_INFINITY;
        Local = Float.POSITIVE_INFINITY;

        HMP = (float) Noise2D.noise((((float) x / (float) TILES_ON_X) * 3) + addition, (((float) y / (float) TILES_ON_Y) * 3) + addition, 255);

        if (HMP > 0.6f){
            HMP *= 100f;
        }
    }

    public ArrayList<NodeC> getNeighbors(ArrayList<ArrayList<NodeC>> nodes){
        ArrayList<NodeC> neighbors = new ArrayList<NodeC>();
        if (x > 0) {
            neighbors.add(nodes.get(x - 1).get(y));
            if (y > 0) {
                neighbors.add(nodes.get(x - 1).get(y - 1));
            }
            if (y < nodes.get(0).size() - 1) {
                neighbors.add(nodes.get(x - 1).get(y + 1));
            }
        }
        if (x < nodes.size() - 1) {
            neighbors.add(nodes.get(x + 1).get(y));
            if (y > 0) {
                neighbors.add(nodes.get(x + 1).get(y - 1));
            }
            if (y < nodes.get(0).size() - 1) {
                neighbors.add(nodes.get(x + 1).get(y + 1));
            }
        }
        if (y > 0) {
            neighbors.add(nodes.get(x).get(y - 1));
        }
        if (y < nodes.get(0).size() - 1) {
            neighbors.add(nodes.get(x).get(y + 1));
        }
        return neighbors;
    }

    public float getDistanceToEnd(Vector2 end){
        return (float) Math.sqrt(Math.pow(end.x - x, 2) + Math.pow(end.y - y, 2));
    }

//    public void setNodeWeight(int addition){
//        NodeWeight = (float) noise2D.noise((float)(x / GameScreen.TILES_ON_X) * 4 + addition,(float)(y / GameScreen.TILES_ON_Y) * 4 + addition, 255);
//    }
}
