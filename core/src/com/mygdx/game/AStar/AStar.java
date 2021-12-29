package com.mygdx.game.AStar;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.Noise2D;

import java.util.ArrayList;

import static java.util.Comparator.comparing;

public class AStar {
    public static ArrayList<ArrayList<Node>> grid;
    
    public static ArrayList<Vector2> pathFind(Vector2 start, Vector2 end, int addition, ArrayList<ArrayList<Boolean>> map) {
        grid = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            grid.add(new ArrayList<>());
            for (int j = 0; j < 250; j++) {
                Node temp = new Node(i, j);
                temp.HMP = (float) Noise2D.noise((i / 250f) * 3 + addition, (j / 250f) * 3 + addition, 255);
                temp.HMP *= 10000;
                temp.accessible = map.get(i).get(j);
                temp.setDistance(end);
                grid.get(i).add(temp);
            }
        }

        Node startNode = grid.get((int) start.x).get((int) start.y);
        Node endNode = grid.get((int) end.x).get((int) end.y);

        startNode.local = 0;
        startNode.global = 0;

        ArrayList<Node> nodesToCheck = new ArrayList<Node>();
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
                    float temp = 1;
                    if (currentNode.x != n.x && currentNode.y != n.y) {
                        temp = 1.4f;
                    }
                    if (currentNode.local + n.HMP + temp < n.local) {
                        n.local = currentNode.local + n.HMP + temp;
                        n.parent = currentNode;
                        n.global = n.local + n.DTE;
                    }
                }
            }
        }
        ArrayList<Vector2> path = new ArrayList<>();
        currentNode = grid.get((int) end.x).get((int) end.y);
        while (currentNode != null){
            path.add(new Vector2(currentNode.x, currentNode.y));
            currentNode = currentNode.parent;
        }

        System.out.println(count + " iterations");

        return path;
    }
}
