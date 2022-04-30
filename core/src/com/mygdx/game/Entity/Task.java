package com.mygdx.game.Entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Generation.Things.AnimatedThings;
import com.mygdx.game.Generation.Things.ConnectedThings;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.Things.Door;
import com.mygdx.game.Generation.Things.Thing;
import com.mygdx.game.Generation.Tile;
import com.mygdx.game.Screens.GameScreen;
import com.mygdx.game.floorDrops.FloorDrop;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class Task {
    protected int x;
    protected int y;

    public String type;
    public String subType;

    public boolean reserved = false;
    public boolean isIndependent;

    protected float percentageComplete;

    protected static final Random random = new Random();

    public static final int[][] neighbours = new int[][]{
            {-1,+1},{+0,+1},{+1,+1},
            {-1,+0},        {+1,+0},
            {-1,-1},{+0,-1},{+1,-1}
    };

    public Task(String type) {
        this.type = type;
    }

    public Task(){

    }

    public Task(String type, String subType, int x, int y){
        this.type = type;
        this.subType = subType;
        this.x = x;
        this.y = y;
        this.isIndependent = getIndependentValueFromType(type);
    }

    public static Vector2 getBestNeighbour(ArrayList<ArrayList<Tile>> tileMap, int x, int y, Map map, ArrayList<Entity> e, Entity c){
        ArrayList<Vector2> accessibleNeighbours = new ArrayList<>();
        for (int[] n: neighbours) {
            if (map.isWithinBounds(x + n[0], y + n[1])) {
                if (tileMap.get(x + n[0]).get(y + n[1]).canWalkOn) {
                    if (checkIfNoEntities(e, x + n[0], y + n[1], c.getEntityID()) && AStar.noColonistPathFindingTo(x + n[0], y + n[1], e, c.getEntityID())
                        && !map.tiles.get(x + n[0]).get(y + n[1]).hasFireOn) {
                        accessibleNeighbours.add(new Vector2(x + n[0], y + n[1]));
                    }
                }
            }
        }
        if (accessibleNeighbours.size() > 0) {
            float minDist = Float.MAX_VALUE;
            Vector2 best = new Vector2(-1, -1);
            for (Vector2 v: accessibleNeighbours) {
                float dist = v.dst(c.getX(), c.getY());
                if (dist < minDist) {
                    minDist = dist;
                    best = v;
                }
            }
            return best;
        }
        return null;
    }
    
    public static boolean checkIfNoEntities(ArrayList<Entity> entities, int x, int y, int excludedID){
        for (Entity e: entities
             ) {
            if (e.getX() == x && e.getY() == y && e.getEntityID() != excludedID) {
                return false;
            }
        }
        return true;
    }

    public void completeTask(int x, int y, Map map, Socket socket, Colonist c, boolean isHost) {
        increaseSkillLevel(c, type);
        map.totalRaidChanceAffector += map.getDistanceMultiplier(x,y);
        GameScreen.score += getTotalFromTaskType(type);
        if (isHost && socket != null) {
            GameScreen.completeTaskNotifyServer(socket, x, y, type);
        }
        switch (type) {
            case "Mine" -> {
                map.changeTileType(x, y, "dirt");
                emitTileChange(socket, x, y, "dirt");
                map.addFloorDrop(x, y, "stone", getRandomAmount(4), socket, isHost);
            }
            case "CutDown" -> {
                map.clearThing(x, y);
                emitThingChange("", x, y, 1, false, socket);
                map.addFloorDrop(x, y, "wood", getRandomAmount(4), socket, isHost);
            }
            case "Plant" -> {
                map.changeThingType(x, y, "tree", (int) (GameScreen.TILE_DIMS * 2), true);
                emitThingChange("tree", x, y, (int) (GameScreen.TILE_DIMS * 2), false, socket);
            }
            case "Demolish" -> {
                map.addFloorDrop(x, y, GameScreen.getResourceFromBuilding(map.things.get(x).get(y).type), 1, socket, isHost);
                map.clearThing(x, y);
                map.updateThingNeighbours(x, y);
                GameScreen.lightManager.removeLight(x, y);
                map.lightShouldBeUpdated = true;
                emitThingChange("", x, y, 1, false, socket);
            }
            case "Build" -> {
                String s = getThingTypeFromSubType(subType);
                switch (s) {
                    case "connected" -> {
                        ConnectedThings temp = new ConnectedThings(map.things.get(x).get(y), subType);
                        map.addThing(temp, x, y, true, socket, isHost);
                    }
                    case "door" -> {
                        Door temp = new Door(map.things.get(x).get(y), subType);
                        map.addThing(temp, x, y, true, socket, isHost);
                    }
                    case "animated" -> {
                        AnimatedThings t = new AnimatedThings(map.things.get(x).get(y), subType);
                        map.addThing(t, x, y, true, socket, isHost);
                    }
                    default -> {
                        Thing t = new Thing(map.things.get(x).get(y), subType);
                        map.addThing(t, x, y, true, socket, isHost);
                    }
                }
                if (doesEmitLight(subType)) {
                    setupLight(subType, map.things.get(x).get(y), map);
                }
                emitThingChange(subType, x, y, 1, doesEmitLight(subType) ,socket);
            }
            case "FireFight" -> map.removeFire(x, y, socket, isHost);
            case "PickUp" -> {
                FloorDrop f = map.getFloorDropAt(x, y);
                if (c.carrying == null) {
                    c.carrying = f;
                }
                else {
                    c.carrying.incrementStackSize(f.getStackSize());
                }
                c.hasDrop = true;
                map.removeFloorDrop(f.getX(), f.getY(), f.getType());
                if (socket != null && isHost) {
                    socket.emit("removeFloorDrop", f.getX(), f.getY(), f.getType());
                }
            }
            case "PickBerries" -> {
                map.clearThing(x, y);
                emitThingChange("", x, y, 1, false, socket);
                map.addFloorDrop(x, y, "berry", getRandomAmount(5), socket, isHost);
            }
            case "Heal" -> {
                FloorDrop fd = map.getFloorDropAt(x, y);
                if (fd != null) {
                    while (fd.getStackSize() > 0 && c.getHealth() < c.getMaxHealth()) {
                        c.setHealth(c.getHealth() + getHealAmount());
                        fd.decrementStackSize();
                    }
                }
                if (isHost && socket != null) {
                    socket.emit("updateHealth", c.getEntityID(), c.getHealth());
                }
            }
            case "Fishing" -> {
                map.addFloorDrop(x, y, "fish", getRandomAmount(2), socket, isHost);
                map.tiles.get(x).get(y).hasBeenFished = true;
            }
        }
        stopSound(socket, isHost);
    }

    public int getHealAmount(){
        return random.nextInt(10) + 1;
    }

    public int getRandomAmount(int max){
        return random.nextInt(max) + 1;
    }

    public void increaseSkillLevel(Colonist c, String taskType){
        String partialType = getPriorityNameFromType(taskType);
        if (partialType.equals("")) {
            return;
        }
        c.skillsPartial.put(partialType, c.skillsPartial.get(partialType) + 0.1f);
        c.xp += random.nextInt(getTotalFromTaskType(taskType)) + 1;
    }

    public int getTotalFromTaskType(String taskType){
        switch (taskType) {
            case "Mine", "Fishing" -> {
                return 7;
            }
            case "CutDown" -> {
                return 6;
            }
            case "Plant" -> {
                return 4;
            }
            case "Build" -> {
                return 5;
            }
            default -> {
                return 1;
            }
        }
    }

    public static void setupLight(String subType, Thing t, Map map){
        t.emitsLight = true;
        t.lightTextureName = getLightTextureName(subType);
        t.setup();
        map.lightShouldBeUpdated = true;
    }

    public static boolean doesEmitLight(String type){
        switch (type) {
            case "torch", "lamp" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public static String getLightTextureName(String type){
        switch (type) {
            case "torch" -> {
                return "golden";
            }
            case "lamp" -> {
                return "default";
            }
            default -> {
                return "";
            }
        }
    }

    public String getThingTypeFromSubType(String subType) {
        switch (subType) {
            case "woodWall", "stoneWall" -> {
                return "connected";
            }
            case "stoneDoor" -> {
                return "door";
            }
            case "tree", "torch" -> {
                return "animated";
            }
            default -> {
                return "";
            }
        }
    }

    public static void changeResourcesFromBuilding(Map map, String subType){
        switch (subType){
            case "stoneWall", "stoneDoor", "lamp" -> map.decreaseResource("stone", 1);
            case "woodWall", "torch" -> map.decreaseResource("wood", 1);
        }
    }

    public void emitTileChange(Socket socket, int x, int y, String type) {
        if (socket != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("x", x);
                jsonObject.put("y", y);
                jsonObject.put("type", type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("changeTileType", jsonObject);
        }
    }

    public void emitThingChange(String type, int x, int y, int height, boolean emitsLight, Socket socket) {
        if (socket != null) {
            socket.emit("changeThingType", x, y, type, height, emitsLight);
        }
    }

    public static boolean getIndependentValueFromType(String type) {
        switch (type) {
            case "hunt" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getPercentageComplete() {
        return percentageComplete;
    }

    public void setPercentageComplete(float percentageComplete) {
        this.percentageComplete = percentageComplete;
    }

    public void incrementPercentage(Colonist c){
        if (type.equals("Heal")){
            this.percentageComplete += 5;
            return;
        }
        this.percentageComplete += (c.skills.get(getPriorityNameFromType(this.type)) / 2f + c.level / 5f);
    }

    public String getPriorityNameFromType(String type){
        return switch (type) {
            case "Mine" -> "Mining";
            case "Build" -> "Construction";
            case "Demolish" -> "Deconstruction";
            case "PickUp" -> "Moving";
            case "CutDown" -> "Chopping trees";
            case "Plant" -> "Planting";
            case "FireFight" -> "FireFighting";
            case "Fishing" -> "Fishing";
            case "PickBerries" -> "Foraging";
            default -> "";
        };
    }

    public int getMaxPercentage(){
        switch (this.type){
            case "Mine" -> {
                return 200;
            }
            case "CutDown" -> {
                return 150;
            }
            case "Plant" -> {
                return 100;
            }
            case "Demolish" -> {
                return 175;
            }
            case "Build" -> {
                return 190;
            }
            case "Fishing" -> {
                return 350;
            }
            case "PickBerries" -> {
                return 90;
            }
            case "FireFight" -> {
                return 10;
            }
            default -> {
                return 0;
            }
        }
    }

    public void startTaskSound(Socket socket, boolean isHost){
        GameScreen.soundManager.addSound(type, x, y, socket, isHost);
    }

    public void stopSound(Socket socket, boolean isHost){
        GameScreen.soundManager.removeSound(type, x, y, socket, isHost);
    }
}
