package com.mygdx.game.Math.DStar;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.Math.sortingAlgorithms.QuickSort;
import com.mygdx.game.Screens.GameScreen;
import com.mygdx.game.Terrain.Tile;

import java.util.ArrayList;

public class DStar {
    public static ArrayList<ArrayList<NodeD>> nodes;
    public ArrayList<Vector2> pathFind(Vector2 start, Vector2 end, ArrayList<ArrayList<Tile>> map) {
        float startTime = TimeUtils.nanoTime();
        nodes = new ArrayList<>();
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            nodes.add(new ArrayList<NodeD>());
            for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                nodes.get(i).add(new NodeD(i, j));
            }
        }

        NodeD startNode = new NodeD((int) start.x, (int) start.y);
        startNode.global = startNode.HMP;
        startNode.local = 0;
        NodeD endNode = new NodeD((int) end.x, (int) end.y);
        ArrayList<NodeD> toCheck = new ArrayList<NodeD>();
        toCheck.add(startNode);
        NodeD current = startNode;

        while (toCheck.size() > 0) {
//            toCheck = QuickSort.sortNodes(toCheck);
            current = toCheck.get(0);
            toCheck.remove(0);
            if (!(current.x == endNode.x && current.y == endNode.y)) {
                for (NodeD node : current.getNeighbors(nodes)) {
                    if (!node.visited && !(node.HMP > 10)) {
                        node.visited = true;
                        toCheck.add(node);

                        float temp = 0;
//                        float temp = 10;
//                        if (node.x != current.x && node.y != current.y) {
//                            temp = 14f;
//                        }
                        if (current.local + node.HMP + temp < node.local) {
                            node.parent = current;
                            node.local = current.local + node.HMP + temp;
                            node.global = node.local + node.getDTE(end);
                        }
                    }
                }
            }
        }
        ArrayList<Vector2> output = new ArrayList<>();
        current = nodes.get((int) end.x).get((int) end.y);
        while (current != null) {
            output.add(new Vector2(current.x, current.y));
            current = current.parent;
        }
        float endTime = TimeUtils.nanoTime();

        int counter = 0;
        for (int i = 0; i < 250; i++) {
            for (int j = 0; j < 250; j++) {
                if (nodes.get(i).get(j).visited) {
                    counter++;
                }
            }
        }
        System.out.println(counter);
        System.out.println((endTime - startTime) / 1E9);
        System.out.println(output);
        return output;
    }
}
