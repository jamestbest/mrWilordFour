package com.mygdx.game.Math.CStar;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Math.sortingAlgorithms.QuickSort;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;

public class CStar {
    ArrayList<NodeC> nodesToCheck = new ArrayList<NodeC>();

    public static ArrayList<ArrayList<NodeC>> arrayOfNodes = new ArrayList<ArrayList<NodeC>>();

    public ArrayList<Vector2> pathFind(Vector2 start, Vector2 end, int addition){
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            arrayOfNodes.add(new ArrayList<NodeC>());
            for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                NodeC temp = new NodeC(i, j, addition);
                arrayOfNodes.get(i).add(temp);
            }
        }

        NodeC startNodeC = arrayOfNodes.get((int)start.x).get((int)start.y);
        NodeC endNodeC = arrayOfNodes.get((int)end.x).get((int)end.y);

        startNodeC.Global = startNodeC.getDistanceToEnd(end);
        startNodeC.Local = 0;

        nodesToCheck = new ArrayList<>();
        nodesToCheck.add(startNodeC);
        NodeC currentNodeC = nodesToCheck.get(0);
        while(!currentNodeC.equals(endNodeC)){
//            nodesToCheck = QuickSort.sortNodes(nodesToCheck);
//            System.out.println(nodesToCheck.size());
//
//            int counter = 0;
//            for (int i = 0; i < 250; i++) {
//                for (int j = 0; j < 250; j++) {
//                    if (arrayOfNodes.get(i).get(j).visited){
//                        counter++;
//                    }
//                }
//            }
//            System.out.println(counter);

//            nodesToCheck = quickSort.sortNodes(nodesToCheck);
            currentNodeC = nodesToCheck.get(0);
            nodesToCheck.remove(0);
            currentNodeC.visited = true;
            for(NodeC nodeC : currentNodeC.getNeighbors(arrayOfNodes)){
                if(!nodeC.visited && !(nodeC.HMP > 0.6f)){
                    nodesToCheck.add(nodeC);
                    float temp = 1;
                    if (nodeC.x != currentNodeC.x && nodeC.y != currentNodeC.y){
                        temp = 1.41f;
                    }
                    if (currentNodeC.Local + temp + nodeC.HMP < nodeC.Local){
                        nodeC.parent = currentNodeC;
                        nodeC.Local = currentNodeC.Local + temp + nodeC.HMP;
                        nodeC.Global = nodeC.Local + nodeC.getDistanceToEnd(end);
                        nodeC.visited = true;
                    }
                }
            }
        }
        ArrayList<Vector2> path = new ArrayList<>();
        while (currentNodeC != null){
            path.add(new Vector2(currentNodeC.x, currentNodeC.y));
            currentNodeC = currentNodeC.parent;
        }

        return path;
    }
}
