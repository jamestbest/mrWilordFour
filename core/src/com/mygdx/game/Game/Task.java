package com.mygdx.game.Game;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.ConnectedThings;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.Tile;
import com.mygdx.game.Screens.GameScreen;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Task {
    public String type;
    public String subType;

    public boolean reserved = false;

    public Task(String type) {
        this.type = type;
    }

    public Vector2 getNeighbour (ArrayList<ArrayList<Tile>> tileMap, int x, int y) {
        if (x + 1 < tileMap.size()){
            if (tileMap.get(x + 1).get(y).canWalkOn) {
                return new Vector2(x + 1, y);
            }
            if (y + 1 < tileMap.size()){
                if (tileMap.get(x + 1).get(y + 1).canWalkOn){
                    return new Vector2(x + 1, y + 1);
                }
            }
            if (y - 1 > 0){
                if (tileMap.get(x + 1).get(y - 1).canWalkOn){
                    return new Vector2(x + 1, y - 1);
                }
            }
        }
        if (x - 1 > 0){
            if (tileMap.get(x - 1).get(y).canWalkOn){
                return new Vector2(x - 1, y);
            }
            if (y + 1 < tileMap.size()){
                if (tileMap.get(x - 1).get(y + 1).canWalkOn){
                    return new Vector2(x - 1, y + 1);
                }
            }
            if (y - 1 > 0){
                if (tileMap.get(x - 1).get(y - 1).canWalkOn){
                    return new Vector2(x - 1, y - 1);
                }
            }
        }
        if (y + 1 < tileMap.size()){
            if (tileMap.get(x).get(y + 1).canWalkOn){
                return new Vector2(x, y + 1);
            }
        }
        if (y - 1 > 0){
            if (tileMap.get(x).get(y - 1).canWalkOn){
                return new Vector2(x, y - 1);
            }
        }
        return null;
    }

    public void completeTask(int x, int y, Map map, HashMap<String, Integer> resources, Socket socket) {
        switch (type) {
            case "Mine" -> {
                map.changeTileType(x, y, "dirt");
                emitTileChange(socket, x, y, "dirt");
                changeResources(resources, "Mine", "");
            }
            case "CutDown" -> {
                map.clearThing(x, y);
                emitThingChange("", x, y, 1, socket);
                changeResources(resources, "CutDown", "");
            }
            case "Plant" -> {
                map.changeThingType(x, y, "tree", (int) (GameScreen.TILE_DIMS * 2));
                emitThingChange("tree", x, y, (int) (GameScreen.TILE_DIMS * 2), socket);
                changeResources(resources, "Plant", "");
            }
            case "Demolish" -> {
                map.clearThing(x, y);
                map.updateThingNeighbours(x, y);
                emitThingChange("", x, y, 1, socket);
            }
            case "Build" -> {
                ConnectedThings temp = new ConnectedThings(map.things.get(x).get(y), subType);
                map.addThing(temp, x, y);
//                emitThingChange(subType, x, y, 1, socket);
                changeResources(resources, "Build", subType);
            }
        }
    }

    public static void changeResources(HashMap<String, Integer> resources, String type, String subType){
        switch (type){
            case "Mine" -> {
                resources.replace("stone", resources.get("stone") + 1);
            }
            case "CutDown" -> {
                resources.replace("wood", resources.get("wood") + 1);
            }
            case "Plant" -> {
                resources.replace("wood", resources.get("wood") - 1);
            }
            case "Demolish" -> {

            }
            case "Build" -> {
                switch (subType){
                    case "stoneWall" -> {
                        resources.replace("stone", resources.get("stone") - 1);
                    }
                    case "woodWall" -> {
                        resources.replace("wood", resources.get("wood") - 1);
                    }
                }
            }
        }
    }

    public void addToResource(String res, int addition, HashMap<String, Integer> resources){
        resources.replace(res, resources.get(res) + addition);
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

    public void emitThingChange(String type, int x, int y, int height, Socket socket) {
        if (socket != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("x", x);
                jsonObject.put("y", y);
                jsonObject.put("type", type);
                jsonObject.put("height", height);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("changeThingType", jsonObject);
        }
    }
}
