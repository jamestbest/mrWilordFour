package com.mygdx.game.Math.AStar;

import java.util.ArrayList;

public class Node {
    public int x;
    public int y;

    public float DFE; //distance from end //this is stored as c^2
    public float HMP; //height map value

    public float global;
    public float local;

    public boolean isStart = false;
    public boolean isEnd = false;

    ArrayList<Node> neighbours;

    Node end;
    Node connection;

    boolean visited = false;
    boolean accessible = true;

    public Node(int x, int y, float heightMapValue, Node end){
        this.x = x;
        this.y = y;

        this.HMP = heightMapValue;
        if (HMP > 0.65f){
            HMP = 100;
        }
        HMP *= HMP;
        this.end = end;

        setDFE();

        global = Float.POSITIVE_INFINITY;
        local = Float.POSITIVE_INFINITY;
    }

    public Node(int x, int y, float heightMapValue){
        this.x = x;
        this.y = y;

        this.HMP = heightMapValue;
        HMP *= HMP;

        this.end = this;

        setDFE();

        global = Float.POSITIVE_INFINITY;
        local = Float.POSITIVE_INFINITY;
    }

    public void setNeighbours(ArrayList<ArrayList<Node>> arrayOfNodes){
        neighbours = new ArrayList<>();
        if (x > 0){
            if (arrayOfNodes.get(x - 1).get(y).accessible){
                neighbours.add(arrayOfNodes.get(x - 1).get(y));
            }
            if (y > 0){
                if (arrayOfNodes.get(x - 1).get(y - 1).accessible){
                    neighbours.add(arrayOfNodes.get(x - 1).get(y - 1));
                }
            }
            if (y < arrayOfNodes.get(0).size() - 1){
                if (arrayOfNodes.get(x - 1).get(y + 1).accessible){
                    neighbours.add(arrayOfNodes.get(x - 1).get(y + 1));
                }
            }
        }
        if (x < arrayOfNodes.size() - 1){
            if (arrayOfNodes.get(x + 1).get(y).accessible){
                neighbours.add(arrayOfNodes.get(x + 1).get(y));
            }
            if (y > 0){
                if (arrayOfNodes.get(x + 1).get(y - 1).accessible){
                    neighbours.add(arrayOfNodes.get(x + 1).get(y - 1));
                }
            }
            if (y < arrayOfNodes.get(0).size() - 1){
                if (arrayOfNodes.get(x + 1).get(y + 1).accessible){
                    neighbours.add(arrayOfNodes.get(x + 1).get(y + 1));
                }
            }
        }
        if (y > 0){
            if (arrayOfNodes.get(x).get(y - 1).accessible){
                neighbours.add(arrayOfNodes.get(x).get(y - 1));
            }
        }
        if (y < arrayOfNodes.get(0).size() - 1){
            if (arrayOfNodes.get(x).get(y + 1).accessible){
                neighbours.add(arrayOfNodes.get(x).get(y + 1));
            }
        }
    }

    public void setDFE(){
        float tempX = this.x - end.x;
        float tempY = this.y - end.y;
        DFE = (float) Math.sqrt((tempX * tempX) + (tempY * tempY));
    }
}
