package com.mygdx.game.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Generation.Things.Fire;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.Things.Door;
import com.mygdx.game.Generation.Tile;
import com.mygdx.game.Screens.GameScreen;
import com.mygdx.game.floorDrops.FloorDrop;
import com.mygdx.game.floorDrops.Zone;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class Colonist extends Entity {
    public HashMap<String, Integer> skills;
    public HashMap<String, Float> skillsPartial;

    public String profession;
    public String backstory;

    public String firstName;
    public String lastName;

    public int age;

    public HashMap<String, Integer> priorityFromType;

    public static Texture deanTexture;

    public int colonistID;

    static ArrayList<String> firstNames;
    static ArrayList<String> lastNames;

    FloorDrop carrying;
    boolean hasDrop;

    public static final int MAX_PRIORITY = 4;

    public Colonist() {
        this.health = 100;
    }

    public void setup() {
        getRandomName();
        this.health = getHealthFromType("colonist");
        this.maxHealth = health;
        System.out.println(firstName + " " + lastName + " is a " + profession);
        setupPriorities();
    }

    public void copyTemplate(Colonist template) {
        this.skills = template.skills;
        this.skillsPartial = new HashMap<>();
        setupPartialSkills();
        this.profession = template.profession;
        this.backstory = template.backstory;
        this.entityType = "colonist";
    }

    public void getRandomName() {
        firstName = firstNames.get(random.nextInt(firstNames.size()));
        lastName = lastNames.get(random.nextInt(lastNames.size()));
        age = random.nextInt(100) + 1;
    }

    public static void setupNames(){
        firstNames = makeArrayOfNames("ColonistFirstNames");
        lastNames = makeArrayOfNames("ColonistLastNames");
    }

    public static ArrayList<String> makeArrayOfNames(String nameOfFile) {
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
        if (isAlive()) {
            batch.draw(clothes.get(clotheName).findRegion("front"), x, y, dims, dims);
        }
        else {
            batch.draw(clothes.get(clotheName).findRegion("front"), x, y, dims / 2f, dims / 2f, dims, dims, 1, 1, 90);
        }
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

    public void moveColonist(Map map, Socket socket, ArrayList<Entity> entities, boolean isHost) {
        if (isAlive()) {
            if (isAttacking && defender == null) {
                defender = findClosestAttacker();
            }
            if (isAttacking && isNeighbouringNotDefender() && !isInRange()) {
                for (Vector2 v : getNeighbours(defender.x, defender.y, map, entities)) {
                    if (setMoveToPos((int) v.x, (int) v.y, map, entities)){
                        break;
                    }
                }
            }
            if (isAttacking && isInRange() && Entity.haveLineOfSight(this, defender, map)) {
                attack(socket, isHost);
            }
            if (isAttacking){
                if (currentTask != null) {
                    removeCurrentTask();
                }
            }
            else if (doingTaskAnimation) {
                doTaskAnimation(map, socket, isHost);
            } else if (movingAcrossPath) {
                moveAlongPath(map);
            }
            else if (currentTask != null) {
                doingTaskAnimation = true;
                currentTask.startTaskSound(socket, isHost);
            } else {
                if (hasDrop) {
                    Zone z = map.findNearestZone(x,y, carrying.getType(), carrying.getStackSize());
                    if (z != null) {
                        if (moveToNearestZone(z, map.tiles, entities, map)){
                            z.addFloorDrop(carrying, map, x, y);
                            if (isHost && socket != null) {
                                socket.emit("addDropToZone", x, y, carrying.getType(), carrying.getStackSize());
                            }
                            carrying = null;
                            hasDrop = false;
                        }
                    }
                }
                if (!movingAcrossPath) {
                    if (getNextTask(map.tiles, map.tasks, entities, map)) {
                        movingAcrossPath = true;
                        if (socket != null) {
                            GameScreen.sendColonistTask(socket, currentTask, colonistID);
                        }
                        // FIXED: 04/02/2022 BUG: colonists will sometimes move randomly before going to next task
                        // FIXED: 29/03/2022 BUG: the colonists will start completing a tasks that they have access to but cannot pathfind to
                    } else {
                        int choice = random.nextInt(10);
                        if (choice <= 3) {
                            getRandomPosition(map, entities);
                        } else {
                            System.out.println("moving randomly");
                            moveRandomly(map);
                        }
                    }
                }
            }
            if (map.tiles.get(x).get(y).hasFireOn) {
                health -= Fire.DAMAGE;
            }
        }
    }

    public void setupPriorities() {
        priorityFromType = new HashMap<>();
        priorityFromType.put("Mine", 1);
        priorityFromType.put("Plant", 1);
        priorityFromType.put("CutDown", 1);
        priorityFromType.put("Build", 2);
        priorityFromType.put("Demolish", 2);
        priorityFromType.put("FireFight", 4);
        priorityFromType.put("PickUp", 3);
        priorityFromType.put("PickBerries", 3);
        priorityFromType.put("Heal", 4);
        priorityFromType.put("Fishing", 1);
    }

    public void incrementPriority(String type) {
        if (priorityFromType.containsKey(type)) {
            priorityFromType.put(type, priorityFromType.get(type) + 1);
            if (priorityFromType.get(type) > MAX_PRIORITY) {
                priorityFromType.put(type, 0);
            }
        }
    }

    public boolean getNextTask(ArrayList<ArrayList<Tile>> tiles, ArrayList<Task> tasks, ArrayList<Entity> entities, Map map) {
        float minDistance = Integer.MAX_VALUE;
        int maxPriority = 0;
        Task bestTask = null;

        for (Task task : tasks) {
            boolean canGetToTask = Task.getBestNeighbour(tiles, task.getX(), task.getY(), map, entities, this) != null
                    || (tiles.get(task.getX()).get(task.getY()).canWalkOn && !tiles.get(task.getX()).get(task.getY()).hasFireOn);
            if (!checkForPickUp(task, map)) {
                continue;
            }
            if (priorityFromType.get(task.type) > maxPriority && canGetToTask && !task.reserved) {
                maxPriority = priorityFromType.get(task.type);
            }
        }

        for (Task task : tasks) {
            if (priorityFromType.get(task.type) == maxPriority && !task.reserved) {
                boolean canGetToTask = Task.getBestNeighbour(tiles, task.getX(), task.getY(), map, entities, this) != null
                        || (tiles.get(task.getX()).get(task.getY()).canWalkOn && !tiles.get(task.getX()).get(task.getY()).hasFireOn);
                if (!checkForPickUp(task, map)) {
                    continue;
                }
                if (canGetToTask) {
                    float distance = getDistance(task.getX(), task.getY());
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestTask = task;
                    }
                }
            }
        }

        if (bestTask != null) {
            return pathFindToTask(bestTask, tiles, entities, map);
        }
        return false;
    }

    public void gotTask(Task t){
        completingTask = true;
        t.reserved = true;
        currentTask = t;
    }

    public boolean pathFindToTask(Task bestTask, ArrayList<ArrayList<Tile>> tiles, ArrayList<Entity> entities, Map map){
        Vector2 neighbour = Task.getBestNeighbour(tiles, bestTask.getX(), bestTask.getY(), map, entities, this);
        if (neighbour != null) {
            if (neighbour.x == x && neighbour.y == y) {
                gotTask(bestTask);
                return true;
            }
            pathToComplete = AStar.pathFindForEntities(new Vector2(x, y), neighbour, tiles, entities, entityID);
            if (pathToComplete.size() != 0) {
                gotTask(bestTask);
            }
            return pathToComplete.size() != 0;
        }
        else {
            boolean canGoToTaskDirect = (tiles.get(bestTask.getX()).get(bestTask.getY()).canWalkOn && !tiles.get(bestTask.getX()).get(bestTask.getY()).hasFireOn);
            if (canGoToTaskDirect) {
                pathToComplete = AStar.pathFindForEntities(new Vector2(x, y), new Vector2(bestTask.getX(), bestTask.getY()), tiles, entities, entityID);
                if (pathToComplete.size() != 0) {
                    gotTask(bestTask);
                }
                return pathToComplete.size() != 0;
            }
        }
        return false;
    }

    public float getDistance(int x, int y){
        return (float) java.lang.Math.sqrt(java.lang.Math.pow(x - this.x, 2) + java.lang.Math.pow(y - this.y, 2));
    }

    public boolean checkForPickUp(Task task, Map map){
        if (task.type.equals("PickUp")){
            if (carrying != null) {
                return map.getFloorDropAt(task.getX(), task.getY()).getStackSize() + carrying.getStackSize() < FloorDrop.maxStackSize;
            }
        }
        return true;
    }

    public boolean moveToNearestZone(Zone z, ArrayList<ArrayList<Tile>> tiles, ArrayList<Entity> entities, Map map) {
        if (z.isInZone(x,y,map)){
            return true;
        }
        Vector2 bestLocation = findBestLocationInZone(z, entities);
        pathToComplete = AStar.pathFindForEntities(new Vector2(x, y), bestLocation, tiles, entities, entityID);
        if(pathToComplete.size() != 0){
            movingAcrossPath = true;
        }
        return false;
    }

    public Vector2 findBestLocationInZone(Zone z, ArrayList<Entity> entities){
        float minDistance = Integer.MAX_VALUE;
        Vector2 bestLocation = null;
        for (int i = z.getX(); i < z.getX() + z.getWidth(); i++) {
            for (int j = z.getY(); j < z.getY() + z.getHeight(); j++) {
                boolean NoOtherEntities = hasNoEntitiesHere(i, j, entities, this.entityID);
                if (NoOtherEntities && getDistance(i, j) < minDistance) {
                    minDistance = getDistance(i, j);
                    bestLocation = new Vector2(i, j);
                }
            }
        }
        return bestLocation;
    }

    public boolean hasNoEntitiesHere(int x, int y, ArrayList<Entity> entities, int excludeID){
        for (Entity e : entities){
            if (e.x == x && e.y == y && e.entityID != excludeID){
                return false;
            }
        }
        return true;
    }

    public void doTaskAnimation(Map map, Socket socket, boolean isHost) {
        System.out.println("doing task animation");
        faceTask();
        currentTask.incrementPercentage(this);
        if (currentTask.getPercentageComplete() < currentTask.getMaxPercentage()) {
            doingTaskAnimation = true;
            if (socket != null) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("x", currentTask.getX());
                    json.put("y", currentTask.getY());
                    json.put("type", currentTask.type);
                    json.put("percentage", currentTask.getPercentageComplete());

                    socket.emit("updateTaskPercentage", json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            doingTaskAnimation = false;
            currentTask.completeTask(currentTask.getX(), currentTask.getY(), map, socket, this, isHost);
            map.tasks.remove(currentTask);
            currentTask = null;
            updateLevel();
            checkPartialSkills();
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
        currentTask.reserved = false;
        currentTask = null;
    }

    public void checkPartialSkills(){
        Set<String> keys = skillsPartial.keySet();
        for (String key : keys){
            if (skillsPartial.get(key) >= 1){
                skillsPartial.put(key, 0f);
                skills.put(key, skills.get(key) + 1);
            }
        }
    }

    public void setupPartialSkills(){
        Set<String> keys = skills.keySet();
        for (String key : keys){
            skillsPartial.put(key, 0f);
        }
    }

    public String getFullName(){
        return firstName + " " + lastName;
    }

    public void stopWhatYoureDoing(Map map){
        super.stopWhatYoureDoing(map);
        if (completingTask){
            if (currentTask != null){
                if (currentTask.type.equals("Heal")){
                    map.tasks.remove(currentTask);
                }
                else {
                    currentTask.reserved = false;
                }
                currentTask = null;
            }
            completingTask = false;
        }
        if (doingTaskAnimation){
            doingTaskAnimation = false;
        }
    }

    public void setTask(Task task, Map map, ArrayList<Entity> entities){
        currentTask = task;
        if (pathFindToTask(task, map.tiles, entities, map)){
            movingAcrossPath = true;
        }
    }

    public void updateLevel(){
        if (xp >= 10 + level * 10){
            level++;
            xp = 0;
        }
    }

    public void setPriorityValue(String priority, int value){
        priorityFromType.put(priority, value);
    }

    public int getPriorityValue(String priority){
        return priorityFromType.get(priority);
    }
}
