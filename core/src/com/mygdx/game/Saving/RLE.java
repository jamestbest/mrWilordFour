package com.mygdx.game.Saving;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.*;
import com.mygdx.game.Generation.Things.AnimatedThings;
import com.mygdx.game.Generation.Things.ConnectedThings;
import com.mygdx.game.Generation.Things.Thing;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static com.mygdx.game.Generation.Map.tileInformationHashMap;

public class RLE {
    static HashMap<String, String> tileNameCode;
    static HashMap<String, String> thingNameCode;
    static HashMap<String, String> thingClassType;

    public static String encodeTiles(Map map) {
        setupTileNameCodes();

        String lastType = "";
        int count = 0;
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < map.tiles.size(); i++) {
            for (int j = 0; j < map.tiles.get(i).size(); j++) {
                if (map.tiles.get(i).get(j).type.equals(lastType)) {
                    count++;
                }
                else {
                    if (count != 0){
                        output.append(tileNameCode.get(lastType));
                        output.append(count);
                    }
                    lastType = map.tiles.get(i).get(j).type;
                    count = 1;
                }
            }
        }
        output.append(tileNameCode.get(lastType));
        output.append(count);
        return output.toString();
    }

    public static ArrayList<ArrayList<Tile>> decodeTiles(String input, int mapDims) {
        setupTileNameCodes();
        HashMap<String, String> reverseTileNameCode = reverseHashmap(tileNameCode);

        ArrayList<ArrayList<Tile>> output = new ArrayList<>();

        ArrayList<String> inputSplit = getSplitArray(input);

        int count = 0;
        for (String s : inputSplit) {
            for (int j = 0; j < Integer.parseInt(s.substring(1)); j++) {
                if ((count) % mapDims == 0) {
                    output.add(new ArrayList<>());
                }
                Tile tempTile = new Tile(count / mapDims, count % mapDims, reverseTileNameCode.get(s.substring(0, 1)));
                tempTile.updateWalkAndSpawn(tileInformationHashMap);
                output.get((count / mapDims)).add(tempTile);
                count++;
            }
        }
        return output;
    }

    public static String encodeThings(Map map){
        setThingNameCode();
        String lastType = "";
        int count = 0;
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < map.things.size(); i++) {
            for (int j = 0; j < map.things.get(i).size(); j++) {
                if (map.things.get(i).get(j).type.equals(lastType)) {
                    count++;
                }
                else {
                    if (count != 0){
                        output.append(thingNameCode.get(lastType));
                        output.append(count);
                    }
                    lastType = map.things.get(i).get(j).type;
                    count = 1;
                }
            }
        }
        output.append(thingNameCode.get(lastType));
        output.append(count);
        return output.toString();
    }

    public static ArrayList<ArrayList<Thing>> decodeThings(String input, int mapDims) {
        setThingNameCode();
        HashMap<String, String> reverseThingNameCode = reverseHashmap(thingNameCode);
        ArrayList<String> inputSplit = getSplitArray(input);
        ArrayList<ArrayList<Thing>> output = new ArrayList<>();

        int count = 0;
        for (String s : inputSplit) {
            for (int j = 0; j < Integer.parseInt(s.substring(1)); j++) {
                if ((count) % mapDims == 0) {
                    output.add(new ArrayList<>());
                }
                String type = reverseThingNameCode.get(s.substring(0, 1));
                Vector2 dims = GameScreen.getMultiplierFromThings(type);
                Vector2 dimsComplete = new Vector2(dims.x * GameScreen.TILE_DIMS, dims.y * GameScreen.TILE_DIMS);
                if (Objects.equals(thingClassType.get(type), "Thing")) {
                    output.get((count / mapDims)).add(new Thing(count / mapDims, count % mapDims, (int) dimsComplete.x, (int) dimsComplete.y, type, (int) GameScreen.TILE_DIMS));
                } else if (Objects.equals(thingClassType.get(type), "AnimatedThing")) {
                    output.get((count / mapDims)).add(new AnimatedThings(count / mapDims, count % mapDims, (int) dimsComplete.x, (int) dimsComplete.y, type, (int) GameScreen.TILE_DIMS));
                } else if (Objects.equals(thingClassType.get(type), "ConnectedThing")) {
                    output.get((count / mapDims)).add(new ConnectedThings(count / mapDims, count % mapDims, (int) dimsComplete.x, (int) dimsComplete.y, type, (int) GameScreen.TILE_DIMS));
                }
                count++;
            }
        }

        for (int i = 0; i < output.size(); i++) {
            for (int j = 0; j < output.get(i).size(); j++) {
                Thing tempThing = output.get(i).get(j);
                if (tempThing != null) {
                    tempThing.update(output);
                }
            }
        }
        return output;
    }

    public static HashMap<String, String> reverseHashmap(HashMap<String, String> input){
        HashMap<String, String> reverseTileNameCode = new HashMap<>();
        String[] oldKeyset = input.keySet().toArray(new String[0]);
        for (int i = 0; i < input.size(); i++) {
            reverseTileNameCode.put(input.get(oldKeyset[i]),  oldKeyset[i]);
        }
        return reverseTileNameCode;
    }

    public static void setupTileNameCodes(){
        tileNameCode = new HashMap<>();
        tileNameCode.put("grass", "g");
        tileNameCode.put("water", "w");
        tileNameCode.put("stone", "s");
        tileNameCode.put("dirt", "d");
    }

    public static void setThingNameCode(){
        thingNameCode = new HashMap<>();
        thingNameCode.put("", "n");
        thingNameCode.put("tree", "t");
        thingNameCode.put("stoneWall", "s");

        thingClassType = new HashMap<>();
        thingClassType.put("", "Thing");
        thingClassType.put("tree", "AnimatedThing");
        thingClassType.put("stoneWall", "ConnectedThing");
    }

    public static ArrayList<String> getSplitArray(String input){
        ArrayList<String> inputSplit = new ArrayList<>();
        ArrayList<Character> nums = new ArrayList<>(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));
        System.out.println(nums);

        int lastPos = 0;
        for (int i = 0; i < input.length(); i++) {
            if (!nums.contains(input.charAt(i)) && i != 0) {
                inputSplit.add(input.substring(lastPos, i));
                lastPos = i;
            }
        }
        inputSplit.add(input.substring(lastPos));
        return inputSplit;
    }
}
