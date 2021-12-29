package com.mygdx.game.Math.DStar;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.Noise2D;
import com.mygdx.game.Math.CStar.NodeC;

import java.util.ArrayList;

import static com.mygdx.game.Screens.GameScreen.TILES_ON_X;
import static com.mygdx.game.Screens.GameScreen.TILES_ON_Y;

public class NodeD {

    int x;
    int y;

    public float global = Float.MAX_VALUE;
    public float local = Float.MAX_VALUE;

    public float HMP;

    public NodeD parent;

    public boolean visited = false;

    public NodeD(int x, int y){
        this.x = x;
        this.y = y;
        HMP = (float) Noise2D.noise((((float) x / TILES_ON_X) * 3) + 932000, (((float) y / TILES_ON_Y) * 3) + 932000, 255);
        if (HMP > 0.6){
            HMP *= 1000f;
        }
        HMP *= HMP;
    }

    public float getDTE(Vector2 end){
        return (float) Math.sqrt(Math.pow(end.x - x, 2) + Math.pow(end.y - y, 2));
    }

    public ArrayList<NodeD> getNeighbors(ArrayList<ArrayList<NodeD>> nodes){
        ArrayList<NodeD> neighbors = new ArrayList<NodeD>();
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

}
