package com.mygdx.game.AStar;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Entity.Entity;
import com.mygdx.game.Generation.Noise2D;
import com.mygdx.game.Generation.Tile;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;

import static java.util.Comparator.comparing;

public class AStar {
    public static ArrayList<ArrayList<Node>> grid;
    
    public static ArrayList<Vector2> pathFindForRivers(Vector2 start, Vector2 end,
                                                       int addition, ArrayList<ArrayList<Tile>> map,
                                                       int riverBend, int freq) {
        grid = new ArrayList<>();
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            grid.add(new ArrayList<>());
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                Node temp = new Node(i, j);
                temp.HMP = Noise2D.noise((i / (float) GameScreen.TILES_ON_X) * freq + addition,
                        (j / (float) GameScreen.TILES_ON_X) * freq + addition, 255);
                temp.HMP *= riverBend;
                temp.accessible = map.get(i).get(j).canSpawnOn;
                temp.setDistance(end);
                grid.get(i).add(temp);
            }
        }

        return PathFind(start, end);
    }

    public static ArrayList<Vector2> pathFindForEntities(Vector2 start, Vector2 end, ArrayList<ArrayList<Tile>> map,
                                                         ArrayList<Entity> entities, int entityID){
        if (grid != null) {
            grid.clear();
        }
        grid = new ArrayList<>();
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            grid.add(new ArrayList<>());
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                Node temp = new Node(i, j);
                temp.HMP = 0;
                temp.accessible = map.get(i).get(j).canWalkOn && doesntContainAnEntity(i,j, entities, entityID)
                        && noColonistPathFindingTo(i,j, entities, entityID) && !map.get(i).get(j).hasFireOn;
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

        int counter = 0;

        ArrayList<Node> nodesToCheck = new ArrayList<>();
        nodesToCheck.add(startNode);
        Node currentNode;

        while (nodesToCheck.size() > 0) {
            if (nodesToCheck.get(0).x == end.x && nodesToCheck.get(0).y == end.y) {
                break;
            }

            nodesToCheck.sort(comparing(Node::getGlobal));

            currentNode = nodesToCheck.get(0);
            nodesToCheck.remove(0);

            for (Node n: currentNode.getNeighbours(grid)) {
                if (n.accessible && !n.visited) {
                    nodesToCheck.add(n);
                    counter++;
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

        if (path.size() == 1) { //this prevents colonists from completing tasks that they can't reach
            if (path.get(0).x == end.x && path.get(0).y == end.y) {
                return new ArrayList<>();
            }
        }
        System.out.println(counter);
        return path;
    }

    public static boolean doesntContainAnEntity(int x, int y, ArrayList<Entity> entities, int excludedID) {
        for (Entity e: entities) {
            if (e.getX() == x && e.getY() == y) {
                if (e.getEntityID() != excludedID) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean noColonistPathFindingTo(int x, int y, ArrayList<Entity> entities, int excludedID) {
        for (Entity e : entities) {
            if (e.pathToComplete != null) {
                if (e.pathToComplete.size() > 0) {
                    Vector2 destination = e.pathToComplete.get(e.pathToComplete.size() - 1);
                    if (destination.x == x && destination.y == y) {
                        if (e.getEntityID() != excludedID) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
