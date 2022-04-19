package com.mygdx.game.Lighting;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Generation.MapComponent;
import com.mygdx.game.Generation.Things.Thing;

import java.util.ArrayList;

public class EdgeController {
    ArrayList<Edge> edges = new ArrayList<>();
    int nextEdgeId = 0;

    int[][] tileAdds = new int[][]{{-1,0},{0,1},{1,0},{0,-1}};
    int[][] positionAdds = new int[][]{{0,0,0,1},{0,1,1,0},{1,0,0,1},{0,0,1,0}};

    public void update(ArrayList<ArrayList<Thing>> map){
        setupEdgePool(map);
    }

    public void setupEdgePool(ArrayList<ArrayList<Thing>> map){
        nextEdgeId = 0;
        edges = new ArrayList<>();
        clearAllCellInfo(map);
        for (int j = map.size() - 1; j >= 0; j--) {
            for (int i = 0; i < map.get(j).size(); i++) {
                Thing c = map.get(i).get(j);
                if (c.doIExist() && MapComponent.doIInteractWithLight(c)) {
                    for (int k = 0; k < tileAdds.length; k++) {
                        if (isWithinBounds(i + tileAdds[k][0], j + tileAdds[k][1], map)) {
                            if (c.doINotHaveANeighbourAtThisLocation(map, i + tileAdds[k][0], j + tileAdds[k][1])) {
                                if (isWithinBounds(i + tileAdds[(k + 1) % 2][0], j + tileAdds[(k + 1) % 2][1], map)) {
                                    if (c.doINotHaveANeighbourAtThisLocation(map, i + tileAdds[(k + 1) % 2][0], j + tileAdds[(k + 1) % 2][1])) {
                                        addNewEdge(i, j, c, k);
                                    }
                                    else {
                                        Thing neighbour = map.get(i + tileAdds[(k + 1) % 2][0]).get(j + tileAdds[(k + 1) % 2][1]);
                                        if (neighbour.edgeID[k] != 0) {
                                            c.edgeID[k] = neighbour.edgeID[k];
                                            if (neighbour.edgeID[k] - 1 < edges.size()) {
                                                Edge e = edges.get(neighbour.edgeID[k] - 1);
                                                if (k % 2 == 1) {
                                                    e.endX++;
                                                } else {
                                                    e.startY--;
                                                }
                                            }
                                        }
                                        else {
                                            addNewEdge(i, j, c, k);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isWithinBounds(int x, int y, ArrayList<ArrayList<Thing>> map){
        return x < map.size() && y < map.size() && x >= 0 && y >= 0;
    }

    public void clearAllCellInfo(ArrayList<ArrayList<Thing>> map){
        for (int i = 0; i < map.size(); i++) {
            for (int j = 0; j < map.size(); j++) {
                Thing c = map.get(i).get(j);
                c.clearInfo();
            }
        }
    }

    private void addNewEdge(int i, int j, Thing c, int k) {
        int eId = getNextEdgeId();
        c.edgeID[k] = eId;
        int startX = i + positionAdds[k][0];
        int startY = j + positionAdds[k][1];
        int endX = startX + positionAdds[k][2];
        int endY = startY + positionAdds[k][3];
        Edge e = new Edge(startX, startY, endX, endY, eId);
        edges.add(e);
    }

    public int getNextEdgeId(){
        nextEdgeId++;
        return nextEdgeId;
    }

    public void drawEdges(ShapeRenderer shapeRenderer, float cellDims){
        for (Edge e : edges) {
            e.draw(shapeRenderer, (int) cellDims);
        }
    }

    public void setupEdgeBouncers(ArrayList<ArrayList<Thing>> map){
        for (int i = 0; i < map.size(); i++) {
            for (int j = 0; j < map.get(i).size(); j++) {
                if (i == 0 || j == 0 || i == map.size() - 1 || j == map.size() - 1) {
                    map.get(i).get(j).type = "edgeBouncer";
                }
            }
        }
    }
}
