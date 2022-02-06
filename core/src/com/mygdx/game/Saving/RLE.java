package com.mygdx.game.Saving;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.Tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RLE {
    static HashMap<String, String> tileNameCode;

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

    public static ArrayList<ArrayList<Tile>> decodeTiles(String input, Vector2 mapDims) {
        setupTileNameCodes();
        HashMap<String, String> reverseTileNameCode = new HashMap<String, String>();
        String[] oldKeyset = tileNameCode.keySet().toArray(new String[0]);
        for (int i = 0; i < tileNameCode.size(); i++) {
            reverseTileNameCode.put(tileNameCode.get(oldKeyset[i]),  oldKeyset[i]);
        }

        ArrayList<ArrayList<Tile>> output = new ArrayList<ArrayList<Tile>>();

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

        int count = 0;
        int lineCount = 0;
        String temp = "";
        for (int i = 0; i < inputSplit.size(); i++) {
            System.out.println(count);

            lineCount++;
            for (int j = 0; j < Integer.parseInt(inputSplit.get(i).substring(1)); j++) {
                temp = inputSplit.get(i);
                if ((count) % mapDims.y == 0) {
                    output.add(new ArrayList<>());
                }
                output.get((count / (int) mapDims.y)).add(new Tile(count / (int) mapDims.y, count % (int) mapDims.y, reverseTileNameCode.get(inputSplit.get(i).substring(0, 1))));
                count++;
            }
            if (i == inputSplit.size() - 3) {
                System.out.println("test");
            }
        }
        System.out.println(lineCount + " line count");

        System.out.println(output.size() + " size info " + output.get(0).size());

        return output;
    }

    public static String encodeThings(Map map){
        HashMap<String, String> thingNameCode = new HashMap<String, String>();
        thingNameCode.put("", "n");
        thingNameCode.put("tree", "t");

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

    public static void setupTileNameCodes(){
        tileNameCode = new HashMap<String, String>();
        tileNameCode.put("grass", "g");
        tileNameCode.put("water", "w");
        tileNameCode.put("stone", "s");
        tileNameCode.put("dirt", "d");
    }
}
