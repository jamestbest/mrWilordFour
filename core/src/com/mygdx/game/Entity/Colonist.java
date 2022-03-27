package com.mygdx.game.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Game.Task;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.Things.Door;
import com.mygdx.game.Generation.Tile;
import io.socket.client.Socket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Colonist extends Entity {
    public HashMap<String, Integer> skills;

    public String profession;
    public String backstory;

    public String firstName;
    public String lastName;

    HashMap<String, Integer> priorityFromType;

    public static Texture deanTexture;

    public boolean completingTask = false;
    public boolean doingTaskAnimation = false;

//    Vector2 currentTaskLoc;
    private Task currentTask;

    public int colonistID;

    public Colonist() {
        this.health = 100;
    }

    public void setup() {
        getRandomName();
        this.health = getHealthFromType("colonist");
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
        updateTimer();
        if (isAlive()) {
            batch.draw(clothes.get(clotheName).findRegion(direction), (x + ((nextX - x) * timer)) * tileDims, (y + ((nextY - y) * timer)) * tileDims, tileDims, tileDims);
        }
        else {
            batch.draw(clothes.get(clotheName).findRegion(direction), (x + ((nextX - x) * timer)) * tileDims, (y + ((nextY - y) * timer)) * tileDims,
                        tileDims / 2f, tileDims / 2f, tileDims, tileDims, 1, 1, 90);
        }
    }

    public void drawMini(SpriteBatch batch, int x, int y, int dims, HashMap<String, TextureAtlas> clothes) {
        batch.draw(clothes.get(clotheName).findRegion("front"), x, y, dims, dims);
    }

    public void drawAsDeanNorris(SpriteBatch batch, float tileDims){
        updateTimer();
        batch.draw(deanTexture, (x + ((nextX - x) * timer)) * tileDims, (y + ((nextY - y) * timer)) * tileDims, tileDims, tileDims);
    }

    public void moveAlongPath(Map map) {
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
            if (pathToComplete.size() >= 2) {
                int nextX2 = (int) pathToComplete.get(1).x;
                int nextY2 = (int) pathToComplete.get(1).y;
                if (map.things.get(nextX2).get(nextY2) instanceof Door) {
                    Door temp = (Door) map.things.get(nextX2).get(nextY2);
                    temp.triggerOpen();
                }
            }
            else{
                if (map.things.get(nextX).get(nextY) instanceof Door) {
                    Door temp = (Door) map.things.get(nextX).get(nextY);
                    temp.triggerOpen();
                }
            }
        }
        movingAcrossPath = pathToComplete.size() > 0;
    }

    public void moveColonist(Map map, HashMap<String, Integer> resources, Socket socket) {
        if (isAlive()) {
            if (isAttacking && defender == null) {
                defender = findClosestAttacker();
            }
            if (isAttacking && !isNeighbouringDefender() && !isInRange()) {
                setMoveToPos(defender.x, defender.y, map);
            }
            if (isAttacking && isInRange() && Entity.haveLineOfSight(this, defender, map)) {
                attack();
            }
            else if (doingTaskAnimation) {
                doTaskAnimation(map, resources, socket);
            } else if (movingAcrossPath) {
                moveAlongPath(map);
            }
            else if (currentTask != null) {
                doingTaskAnimation = true;
            } else {
                if (getNextTask(map.tiles, map.tasks)) {
                    movingAcrossPath = true;
                    // FIXED: 04/02/2022 BUG: colonists will sometimes move randomly before going to next task
                } else {
                    int choice = random.nextInt(10);
                    if (choice <= 3) {
                        getRandomPosition(map);
                    } else {
                        System.out.println("moving randomly");
                        moveRandomly(map);
                    }
                }
            }
        }
    }

    public void setupPriorities() {
        priorityFromType = new HashMap<>();
        priorityFromType.put("Mine", 1);
        priorityFromType.put("Plant", 2);
        priorityFromType.put("CutDown", 3);
        priorityFromType.put("Harvest", 4);
        priorityFromType.put("Build", 5);
        priorityFromType.put("Demolish", 6);
    }

    public boolean getNextTask(ArrayList<ArrayList<Tile>> tiles, ArrayList<Task> tasks) {
        float minDistance = Integer.MAX_VALUE;
        int maxPriority = 0;
        String maxPriorityTaskType = "";
        Task bestTask = null;

//        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
//            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
//                Task task = tiles.get(i).get(j).task;
//                if (task != null){
//                    boolean canGetToTask = task.getNeighbour(tiles, i, j) != null || tiles.get(i).get(j).canWalkOn;
//                    if (priorityFromType.get(task.type) > maxPriority && canGetToTask && !task.reserved) {
//                        maxPriority = priorityFromType.get(task.type);
//                        maxPriorityTaskType = task.type;
//                    }
//                }
//            }
//        }

        for (Task task : tasks) {
            boolean canGetToTask = Task.getNeighbour(tiles, task.getX(), task.getY()) != null || tiles.get(task.getX()).get(task.getY()).canWalkOn;
            if (priorityFromType.get(task.type) > maxPriority && canGetToTask && !task.reserved) {
                maxPriority = priorityFromType.get(task.type);
                maxPriorityTaskType = task.type;
            }
        }

//        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
//            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
//                Task task = tiles.get(i).get(j).task;
//                if (task != null) {
//                    boolean canGetToTask = task.getNeighbour(tiles, i, j) != null || tiles.get(i).get(j).canWalkOn;
//                    if (task.type.equals(maxPriorityTaskType) && canGetToTask && !task.reserved) {
//                        float distance = getDistance(i, j);
//                        if (distance < minDistance) {
//                            minDistance = distance;
//                            bestTask = new Vector2(i, j);
//                        }
//                    }
//                }
//            }
//        }

        for (Task task : tasks) {
            boolean canGetToTask = Task.getNeighbour(tiles, task.getX(), task.getY()) != null || tiles.get(task.getX()).get(task.getY()).canWalkOn;
            if (task.type.equals(maxPriorityTaskType) && !task.reserved && canGetToTask) {
                float distance = getDistance(task.getX(), task.getY());
                if (distance < minDistance) {
                    minDistance = distance;
                    bestTask = task;
                }
            }
        }

        if (bestTask != null) {
            Vector2 neighbour = Task.getNeighbour(tiles, bestTask.getX(), bestTask.getY());
            if (neighbour != null) {
                pathToComplete = AStar.pathFindForColonist(new Vector2(x, y), neighbour, tiles);
                completingTask = true;
                bestTask.reserved = true;
                currentTask = bestTask;
                return true;
//                currentTaskLoc = new Vector2(bestTask.getX(), bestTask.getY());
            }
            else {
                return false;
            }
        }
        return false;
    }

    public float getDistance(int x, int y){
        return (float) java.lang.Math.sqrt(java.lang.Math.pow(x - this.x, 2) + java.lang.Math.pow(y - this.y, 2));
    }

    public void doTaskAnimation(Map map, HashMap<String, Integer> resources, Socket socket) {
        System.out.println("doing task animation");
        currentTask.incrementPercentage();
        if (currentTask.getPercentageComplete() < 100){
            doingTaskAnimation = true;
        }
        else {
            doingTaskAnimation = false;
            currentTask.completeTask(currentTask.getX(), currentTask.getY(), map, resources, socket);
            map.tasks.remove(currentTask);
            currentTask = null;
        }
        completingTask = doingTaskAnimation;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public void removeCurrentTask(){
        pathToComplete = new ArrayList<>();
        movingAcrossPath = false;
        doingTaskAnimation = false;
        completingTask = false;
        currentTask = null;
    }
}
