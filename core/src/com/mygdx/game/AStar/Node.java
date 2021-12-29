package com.mygdx.game.AStar;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Node {
    int x;
    int y;

    public float global = Float.POSITIVE_INFINITY;
    public float local = Float.POSITIVE_INFINITY;

    float HMP;
    float DTE;

    Node parent = null;

    boolean visited = false;

    boolean accessible;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        accessible = true;
    }

    public ArrayList<Node> getNeighbours(ArrayList<ArrayList<Node>> nodes){
        ArrayList<Node> neighbors = new ArrayList<Node>();
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

    public void setDistance(Vector2 end){
        DTE = (float) Math.sqrt(Math.pow(end.x - x, 2) + Math.pow(end.y - y, 2));
    }

    public float getGlobal(){
        return global;
    }
}
