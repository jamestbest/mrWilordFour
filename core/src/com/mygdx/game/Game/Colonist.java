package com.mygdx.game.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.Tile;
import com.mygdx.game.Screens.GameScreen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Colonist {
    public int x;
    public int y;

    int nextX;
    int nextY;

    public int health;
    public HashMap<String, Integer> skills;

    public String profession;
    public String backstory;

    public String clotheName;
    String direction = "front";

    public String firstName;
    public String lastName;

    static Random random = new Random();

    ArrayList<Vector2> pathToComplete = new ArrayList<>();

    HashMap<String, Integer> priorityFromType;

    boolean movingAcrossPath = false;
    public boolean completingTask = false;
    public boolean doingTaskAnimation = false;

    Vector2 currentTaskLoc;

    float timer = 0f;
    float timerMax = 1f;

    int randomMoveRadius = 25;

    public Colonist() {
        this.health = 100;
    }

    public void setup() {
        getRandomName();
        System.out.println(firstName + " " + lastName + " is a " + profession);
        setupPriorities();
    }

    public void copyTemplate(Colonist template) {
        this.skills = template.skills;
        this.profession = template.profession;
        this.backstory = template.backstory;
    }

    public void getRandomName() {
        ArrayList<String> firstNames = makeArrayOfNames("ColonistFirstNames");
        ArrayList<String> lastNames = makeArrayOfNames("ColonistLastNames");
        firstName = firstNames.get(random.nextInt(firstNames.size()));
        lastName = lastNames.get(random.nextInt(lastNames.size()));
    }

    public ArrayList<String> makeArrayOfNames(String nameOfFile) {
        try {
            FileReader fileReader = new FileReader("core/assets/ColonistInformation/" + nameOfFile);

            BufferedReader br = new BufferedReader(fileReader);

            ArrayList<String> temp = new ArrayList<>();

            String line = br.readLine();
            while (line != null) {
                temp.add(line);
                line = br.readLine();
            }

            fileReader.close();
            br.close();
            return temp;
        } catch (IOException e) {
            Gdx.app.log("Colonist", "Error reading file");
        }

        return null;
    }

    public void draw(SpriteBatch batch, float tileDims, HashMap<String, TextureAtlas> clothes) {
        timer += Gdx.graphics.getDeltaTime() * GameScreen.gameSpeed;
        if (timer >= timerMax) {
            x = nextX;
            y = nextY;
            timer = 0f;
        }
        updateDirection();
        batch.draw(clothes.get(clotheName).findRegion(direction), (x + ((nextX - x) * timer)) * tileDims, (y + ((nextY - y) * timer)) * tileDims, tileDims, tileDims);
    }

    public void moveRandomly(Map map) {
        int randomX = random.nextInt(3) - 1;
        int randomY = random.nextInt(3) - 1;

        if (map.isWithinBounds(randomX + x, randomY + y)) {
            if (map.tiles.get(x + randomX).get(y + randomY).canWalkOn) {
                nextX = x + randomX;
                nextY = y + randomY;
            }
        }
    }

    public void setMoveToPos(int x, int y, Map map) {
        pathToComplete = AStar.pathFindForColonist(new Vector2(this.x, this.y), new Vector2(x, y), map.tiles);
        movingAcrossPath = pathToComplete.size() > 0;
    }

    public void getRandomPosition(Map map) {
        int count = 0;
        Vector2 randomPos = getPosInRange(map);
        int randomX = (int) randomPos.x;
        int randomY = (int) randomPos.y;

        while (!map.tiles.get(x + randomX).get(y + randomY).canWalkOn) {
            randomPos = getPosInRange(map);
            randomX = (int) randomPos.x;
            randomY = (int) randomPos.y;
            count++;
            if (count > 100) {
                break;
            }
        }

        pathToComplete = AStar.pathFindForColonist(new Vector2(x, y), new Vector2(randomX + x, randomY + y), map.tiles);

        movingAcrossPath = pathToComplete.size() > 0;
    }

    public Vector2 getPosInRange(Map map) {
        int randomX = random.nextInt(randomMoveRadius * 2);
        int randomY = random.nextInt(randomMoveRadius * 2);

        randomX -= randomMoveRadius;
        randomY -= randomMoveRadius;
        while (!map.isWithinBounds(x + randomX, y + randomY)) {
            randomX = random.nextInt(randomMoveRadius * 2);
            randomY = random.nextInt(randomMoveRadius * 2);

            randomX -= randomMoveRadius;
            randomY -= randomMoveRadius;
        }
        return new Vector2(randomX, randomY);
    }

    public void moveAlongPath() {
        if (pathToComplete.size() > 0) {
            Vector2 nextTile = pathToComplete.get(0);
            if (nextTile.x == x && nextTile.y == y) {
                pathToComplete.remove(0);
            }
        } else {
            movingAcrossPath = false;
            if (completingTask){
                doingTaskAnimation = true;
            }
        }
        if (pathToComplete.size() >= 1) {
            nextX = (int) pathToComplete.get(0).x;
            nextY = (int) pathToComplete.get(0).y;
        }
    }

    public void moveColonist(Map map, HashMap<String, Integer> resources) {
        if (doingTaskAnimation){
            doTaskAnimation(map, resources);
        }
        else if (movingAcrossPath) {
            moveAlongPath();
        } else {
            if (getNextTask(map.tiles)) {
                movingAcrossPath = true;
                // TODO: 04/02/2022 BUG: colonists will sometimes move randomly before going to next task
            }
            int choice = random.nextInt(10);
            if (choice <= -1) {
                getRandomPosition(map);
            } else {
                System.out.println("moving randomly");
                moveRandomly(map);
            }
        }
    }

    public void updateDirection() {
        if (nextX > x) {
            direction = "right";
        } else if (nextX < x) {
            direction = "left";
        }
        if (nextY > y) {
            direction = "back";
        } else if (nextY < y) {
            direction = "front";
        }
    }

    public void drawPathOutline(ShapeRenderer shapeRenderer) {
        for (Vector2 v : pathToComplete) {
            shapeRenderer.rect(v.x * GameScreen.TILE_DIMS, v.y * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
        }
    }

    public void setupPriorities() {
        priorityFromType = new HashMap<>();
        priorityFromType.put("Mine", 1);
        priorityFromType.put("Plant", 2);
        priorityFromType.put("CutDown", 3);
        priorityFromType.put("Harvest", 4);
    }

    public boolean getNextTask(ArrayList<ArrayList<Tile>> tiles) {
        float minDistance = Integer.MAX_VALUE;
        int maxPriority = 0;
        String maxPriorityTaskType = "";
        Vector2 bestTask = null;

        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                Task task = tiles.get(i).get(j).task;
                if (task != null){
                    boolean canGetToTask = task.getNeighbour(tiles, i, j) != null || tiles.get(i).get(j).canWalkOn;
                    if (priorityFromType.get(task.type) > maxPriority && canGetToTask && !task.reserved) {
                        maxPriority = priorityFromType.get(task.type);
                        maxPriorityTaskType = task.type;
                    }
                }
            }
        }

        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                Task task = tiles.get(i).get(j).task;
                if (task != null) {
                    boolean canGetToTask = task.getNeighbour(tiles, i, j) != null || tiles.get(i).get(j).canWalkOn;
                    if (task.type.equals(maxPriorityTaskType) && canGetToTask && !task.reserved) {
                        float distance = getDistance(i, j);
                        if (distance < minDistance) {
                            minDistance = distance;
                            bestTask = new Vector2(i, j);
                        }
                    }
                }
            }
        }
        if (bestTask != null) {
            Vector2 neighbour = tiles.get((int) bestTask.x).get((int) bestTask.y).task.getNeighbour(tiles, (int) bestTask.x, (int) bestTask.y);
            if (tiles.get((int) bestTask.x).get((int) bestTask.y).canWalkOn) {
                pathToComplete = AStar.pathFindForColonist(new Vector2(x, y), bestTask, tiles);
            }
            else {
                pathToComplete = AStar.pathFindForColonist(new Vector2(x, y), neighbour, tiles);
            }
            completingTask = true;
            tiles.get((int) bestTask.x).get((int) bestTask.y).task.reserved = true;
            currentTaskLoc = new Vector2(bestTask.x, bestTask.y);
        }
        return bestTask != null;
    }

    public float getDistance(int x, int y){
        return (float) Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
    }

    public void doTaskAnimation(Map map, HashMap<String, Integer> resources) {
        System.out.println("doing task animation");
        Task task = map.tiles.get((int) currentTaskLoc.x).get((int) currentTaskLoc.y).task;
        Random random = new Random();
        if (random.nextInt(100) <= 90){
            doingTaskAnimation = true;
        }
        else {
            doingTaskAnimation = false;
            task.completeTask((int) currentTaskLoc.x, (int) currentTaskLoc.y, map, resources);
            map.tiles.get((int) currentTaskLoc.x).get((int) currentTaskLoc.y).task = null;
        }
        completingTask = doingTaskAnimation;
    }
}
