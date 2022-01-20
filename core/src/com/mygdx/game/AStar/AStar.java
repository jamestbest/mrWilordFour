package com.mygdx.game.AStar;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.Noise2D;

import java.util.ArrayList;

import static java.util.Comparator.comparing;

public class AStar {
    public static ArrayList<ArrayList<Node>> grid;
    
    public static ArrayList<Vector2> pathFindForRivers(Vector2 start, Vector2 end, int addition, ArrayList<ArrayList<Boolean>> map, int riverBend, int freq) {
        grid = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            grid.add(new ArrayList<>());
            for (int j = 0; j < 250; j++) {
                Node temp = new Node(i, j);
                temp.HMP = (float) Noise2D.noise((i / 250f) * freq + addition, (j / 250f) * freq + addition, 255);
                temp.HMP *= riverBend;
                temp.accessible = map.get(i).get(j);
                temp.setDistance(end);
                grid.get(i).add(temp);
            }
        }

        return PathFind(start, end);
    }

    public static ArrayList<Vector2> pathFindForColonist(Vector2 start, Vector2 end, int addition, ArrayList<ArrayList<Boolean>> map){
        grid.clear();
        grid = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            grid.add(new ArrayList<>());
            for (int j = 0; j < 250; j++) {
                Node temp = new Node(i, j);
                temp.HMP = 0;
                temp.accessible = map.get(i).get(j);
                temp.setDistance(end);
                grid.get(i).add(temp);
            }
        }

        return PathFind(start, end);
    }

    private static ArrayList<Vector2> PathFind(Vector2 start, Vector2 end) {
        Node startNode = grid.get((int) start.x).get((int) start.y);

        startNode.local = 0;
        startNode.global = 0;

        ArrayList<Node> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(startNode);
        Node currentNode;

        int count = 0;

        while (nodesToCheck.size() > 0) {
//            nodesToCheck = QuickSort.sortNodes(nodesToCheck); //too slow
            nodesToCheck.sort(comparing(Node::getGlobal));

            currentNode = nodesToCheck.get(0);
            nodesToCheck.remove(0);

            count ++;

            for (Node n: currentNode.getNeighbours(grid)) {
                if (n.accessible && !n.visited) {
                    nodesToCheck.add(n);
                    n.visited = true;
                    float temp = 10;
                    if (currentNode.x != n.x && currentNode.y != n.y) {
                        temp = 14f;
                    }
                    if (currentNode.local + n.HMP + temp < n.local) {
                        n.local = currentNode.local + n.HMP + temp;
                        n.parent = currentNode;
                        n.global = n.local + n.DTE;
                    }
                }
            }
        }
        ArrayList<Vector2> pathRev = new ArrayList<>();
        ArrayList<Vector2> path = new ArrayList<>();
        currentNode = grid.get((int) end.x).get((int) end.y);
        while (currentNode != null){
            pathRev.add(new Vector2(currentNode.x, currentNode.y));
            currentNode = currentNode.parent;
        }

        for (int i = 0; i < pathRev.size(); i++) {
            path.add(pathRev.get(pathRev.size() - i - 1));
        }

        System.out.println(count + " iterations");

        return path;
    }
}
