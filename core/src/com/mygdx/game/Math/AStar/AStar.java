package com.mygdx.game.Math.AStar;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.Noise2D;
import com.mygdx.game.Math.sortingAlgorithms.QuickSort;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;

public class AStar {
    ArrayList<ArrayList<Node>> arrayOfNodes = new ArrayList<>();
    ArrayList<Node> nodesToTest = new ArrayList<>();
    public static final float ROOT_2 = (float) StrictMath.sqrt(2);

    public static ArrayList<ArrayList<Float>> infoOnSearch = new ArrayList<>();

    public AStar(){

    }

    public void setupWithPerlin(int perlinNoiseAdditionX, int perlinNoiseAdditionY, Vector2 endCords, Vector2 startCords){
        int freq = 3;
        double value = Noise2D.noise(((endCords.x) / (float)GameScreen.TILES_ON_X) * freq + perlinNoiseAdditionX,((endCords.y) / (float)GameScreen.TILES_ON_Y) * freq + perlinNoiseAdditionY, 255);
//        value += 1;
//        value /= 2;
        Node end = new Node((int)endCords.x, (int)endCords.y, (float) value);
        end.isEnd = true;

        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            arrayOfNodes.add(new ArrayList<Node>());
            for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                if ((endCords.x == i) && (endCords.y == j)){
                    arrayOfNodes.get(i).add(end);
                }
                else {
                    value = Noise2D.noise((i / (float)GameScreen.TILES_ON_X) * freq + perlinNoiseAdditionX,(j / (float)GameScreen.TILES_ON_Y) * freq + perlinNoiseAdditionY, 255);
//                    value += 1;
//                    value /= 2;
                    Node temp = new Node(i,j,(float) value, end);
                    if (value > 0.6){
                        temp.accessible = false;
                    }
                    arrayOfNodes.get(i).add(temp);
                }
            }
        }

        Node start = arrayOfNodes.get((int) startCords.x).get((int) startCords.y);
        start.isStart = true;
        start.local = 0;
        start.global = start.DFE;

        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                arrayOfNodes.get(i).get(j).setNeighbours(arrayOfNodes);
            }
        }
        nodesToTest.add(start);
    }

    public void setup(Vector2 endCords, Vector2 startCords, ArrayList<ArrayList<Float>> HMP){
        float value = HMP.get((int)endCords.x).get((int)endCords.y);
        Node end = new Node((int)endCords.x, (int)endCords.y, value);
        end.isEnd = true;
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            arrayOfNodes.add(new ArrayList<Node>());
            for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                if ((endCords.x == i) && (endCords.y == j)){
                    arrayOfNodes.get(i).add(end);
                }
                else {
                    value = HMP.get(i).get(j);
                    arrayOfNodes.get(i).add(new Node(i,j,value, end));
                }
            }
        }
        Node start = arrayOfNodes.get((int) startCords.x).get((int) startCords.y);
        start.isStart = true;
        start.local = 0;
        start.global = start.DFE;
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                arrayOfNodes.get(i).get(j).setNeighbours(arrayOfNodes);
            }
        }
        nodesToTest.add(start);
    }

    public ArrayList<Vector2> pathFind(boolean forRivers){
        ArrayList<Vector2> output = new ArrayList<>();
        float count  = 0;

        if (forRivers){
            for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
                infoOnSearch.add(new ArrayList<Float>());
                for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                    infoOnSearch.get(i).add(0f);
                }
            }
        }
//        while (nodesToTest.size() != 0) {
            while (!nodesToTest.get(0).isEnd) {
//                if (forRivers){
//                    nodesToTest = QuickSort.sortNodes(nodesToTest);
//                }
                Node currentTest = nodesToTest.get(0);
                for (Node n : currentTest.neighbours
                ) {
                    float temp = currentTest.local + (n.HMP);
                    if (!forRivers){
                        if (n.x != currentTest.x && n.y != currentTest.y){
                            temp += ROOT_2;
                        }
                        else {
                            temp += 1;
                        }
                    }


                    if (temp < n.local) {
                        if (!n.visited){
                            nodesToTest.add(n);
                            count ++;
                            n.visited = true;
                        }
                        n.connection = currentTest;
                        n.local = temp;
                        n.global = n.DFE + n.local;
                    }
                }
                if (forRivers){
                    float tempVal = infoOnSearch.get(nodesToTest.get(0).x).get(nodesToTest.get(0).y);
                    infoOnSearch.get(nodesToTest.get(0).x).set(nodesToTest.get(0).y, (float) (tempVal + 0.05));
                }
                nodesToTest.remove(0);

//            }

        }
//        System.out.println("end");
        Node n = (arrayOfNodes.get(0).get(0).end);
        while (!n.isStart){
            n = n.connection;
            output.add(new Vector2(n.x, n.y));
        }
        System.out.println("count: " + count);
        return output;
    }
}
