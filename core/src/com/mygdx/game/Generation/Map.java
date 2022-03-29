package com.mygdx.game.Generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Game.Task;
import com.mygdx.game.Generation.Things.AnimatedThings;
import com.mygdx.game.Generation.Things.ConnectedThings;
import com.mygdx.game.Generation.Things.Thing;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.Entity.Colonist;
import com.mygdx.game.Math.Math;
import com.mygdx.game.Saving.RLE;
import com.mygdx.game.Screens.GameScreen;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

@SuppressWarnings("unchecked")
public class Map {
    public ArrayList<ArrayList<Tile>> tiles;
    public ArrayList<ArrayList<Thing>> things;
    public ArrayList<Task> tasks = new ArrayList<>();

    public int addition;

    int x;
    int y;

    public int drawHeight;

    public MapSettings settings;

    public static HashMap<String, TileInformation> tileInformationHashMap;

    static Json json = new Json();

    public Map(MapSettings settings){
        this.settings = settings;
    }

    public Map(MapSettings settings, int drawHeight, int x, int y){
        this.settings = settings;

        this.x = x;
        this.y = y;
    }

    public Map(int drawHeight, int x, int y, String seed){
        this.settings = new MapSettings(seed);
        this.drawHeight = drawHeight;

        this.x = x;
        this.y = y;
    }

    public Map(){

    }

    public void generateMap(){
        setup();
        tiles = new ArrayList<>();
        things = new ArrayList<>();
        generateGrass();
        generateStone();

        if (settings.riverToggle){
            findRiverLocs();
            generateRiver(findRiverLocs());
        }

        generateTrees();
    }

    public void setup(){
        setTileInfoHashMap();
    }


    public void generateStone(){
        for(int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                double temp = Noise2D.noise((((float) i / GameScreen.TILES_ON_X) * settings.perlinFrequency) + addition,
                        (((float) j / GameScreen.TILES_ON_X) * settings.perlinFrequency) + addition, 255);
                if (temp > 0.65f) {
                    Tile tile = new Tile(i, j, "stone");
                    tile.updateWalkAndSpawn(tileInformationHashMap);
                    tiles.get(i).set(j, tile);
                }
            }
        }
    }

    public void generateGrass(){
        for(int i = 0; i < GameScreen.TILES_ON_X; i++) {
            tiles.add(new ArrayList<>());
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                float temp = Noise2D.noise((((float) i / GameScreen.TILES_ON_X) * 40) + addition,
                        (((float) j / GameScreen.TILES_ON_X) * 40) + addition, 255);
                Tile tile;
                if (temp > 0.55f) {
                    tile = new Tile(i, j, "grass");
                }
                else{
                    tile = new Tile(i, j, "dirt");
                }
                tile.updateWalkAndSpawn(tileInformationHashMap);
                tiles.get(i).add(tile);
            }
        }
    }

    public void updateMap(){
        getAdditionFromSeed(settings.seed);
        tiles.clear();
        things.clear();
        generateMap();
    }

    public void generateBlank(){
        setup();
        tiles = new ArrayList<>();
        things = new ArrayList<>();
        for(int i = 0; i < GameScreen.TILES_ON_X; i++){
            tiles.add(new ArrayList<>());
            things.add(new ArrayList<>());
            for(int j = 0; j < GameScreen.TILES_ON_X; j++){
                tiles.get(i).add(new Tile(i, j, "dirt"));
                things.get(i).add(new Thing(i, j, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS, "", (int) GameScreen.TILE_DIMS));
            }
        }
    }

    public void drawMap(SpriteBatch batch, HashMap<String, Texture> tileTextures, CameraTwo camera){
        int startX = (int)(Math.highest (((camera.position.x - (camera.width * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));
        int startY = (int)(Math.highest (((camera.position.y - (camera.height * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));

        int endX = (int)(Math.lowest((camera.width * camera.zoom / GameScreen.TILE_DIMS) + startX + 10, GameScreen.TILES_ON_X));
        int endY = (int)(Math.lowest((camera.height * camera.zoom / GameScreen.TILE_DIMS) + startY + 10, GameScreen.TILES_ON_X));

        Texture temp;
        String[] types = tileTextures.keySet().toArray(new String[0]);

        for (int k = 0; k < tileTextures.size(); k++) {
            temp = tileTextures.get(types[k]);
            for(int i = startX; i < endX; i++){
                for(int j = startY; j < endY; j++){
                    if (tiles.get(i).get(j).type.equals(types[k])){
                        batch.draw(temp, i * GameScreen.TILE_DIMS, j * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
                    }
                }
            }
        }
    }

    public void drawThings(SpriteBatch batch, HashMap<String, TextureAtlas> thingTextures, CameraTwo camera){
        int startX = (int)(Math.highest (((camera.position.x - (camera.width * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));
        int startY = (int)(Math.highest (((camera.position.y - (camera.height * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));

        int endX = (int)(Math.lowest((camera.width * camera.zoom / GameScreen.TILE_DIMS) + startX + 10, GameScreen.TILES_ON_X));
        int endY = (int)(Math.lowest((camera.height * camera.zoom / GameScreen.TILE_DIMS) + startY + 10, GameScreen.TILES_ON_X));
        for (int k = 0; k < 3; k++) {
            for(int i = startX; i < endX; i++) {
                for (int j = endY - 1; j > startY - 1; j--) {
                    if (!things.get(i).get(j).type.equals("")) {
                        Thing t = things.get(i).get(j);
                        t.draw(batch, thingTextures.get(t.type), k);
                    }
                }
            }
        }
    }

    public void drawMiniMap(SpriteBatch batch, HashMap<String, Texture> textures, HashMap<String, TextureAtlas> thingTextures){
        float miniMapDims = drawHeight / (float) GameScreen.TILES_ON_X;

        for(int k = 0; k < 3; k++) {
            for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
                for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                    batch.draw(textures.get(tiles.get(i).get(j).type), i * miniMapDims + x, j * miniMapDims + y, miniMapDims, miniMapDims);

                    Thing t = things.get(i).get(j);
                    if (!t.type.equals("")) {
                        Vector2 mults = GameScreen.getMultiplierFromThings(t.type);
                        t.drawMini(batch, thingTextures.get(t.type), ((i * miniMapDims) + x), ((j * miniMapDims) + y),
                                (miniMapDims * mults.x), (miniMapDims * mults.y), k);
                    }
                }
            }
        }
    }

    public void getAdditionFromSeed(String seed){
        addition = 0;
        for (char c: seed.toCharArray()
        ) {
            addition += c * 10;
        }
    }

    public void generateRiver(ArrayList<Vector2> riverLocs){
        Vector2 start = riverLocs.get(0);
        Vector2 end = riverLocs.get(1);
        ArrayList<Vector2> path;
//        updateBooleanMap();
        path = AStar.pathFindForRivers(start, end, addition, tiles, settings.riverBend, settings.perlinFrequency);
        for (int i = 0; i < path.size(); i++) {
            Vector2 temp = path.get(i);
            if (i + 1 < path.size()) {
                if (path.get(i + 1).y == temp.y) {
                    for (int j = 0; j < 2; j++) {
                        if (temp.y + j < GameScreen.TILES_ON_X){
                            Tile tempTile = tiles.get((int) temp.x).get((int) (temp.y + j));
                            tempTile.type = "water";
                            tempTile.canSpawnOn = false;
                        }
                    }
                }

            }
            for (int j = 0; j < 4; j++) {
                if (temp.x + j < GameScreen.TILES_ON_X) {
                    Tile tempTile = tiles.get((int) (temp.x + j)).get((int) temp.y);
                    tempTile.type = "water";
                    tempTile.canSpawnOn = false;
                }
            }
        }
    }

    public ArrayList<Vector2> findRiverLocs(){
        ArrayList<Vector2> riverLocs = new ArrayList<>();
        Random rand = new Random();
        int sx = rand.nextInt(GameScreen.TILES_ON_X);
        int ex = rand.nextInt(GameScreen.TILES_ON_X);

        while(!tiles.get(sx).get(0).canSpawnOn){
            sx = rand.nextInt(GameScreen.TILES_ON_X);
        }
        while(!tiles.get(ex).get(GameScreen.TILES_ON_X - 1).canSpawnOn){
            ex = rand.nextInt(GameScreen.TILES_ON_X);
        }
        riverLocs.add(new Vector2(sx, 0));
        riverLocs.add(new Vector2(ex, GameScreen.TILES_ON_X - 1));
        return riverLocs;
    }

    public void generateTrees(){
        Random random = new Random();
        for(int i = 0; i < GameScreen.TILES_ON_X; i++){
            things.add(new ArrayList<>());
            for(int j = 0; j < GameScreen.TILES_ON_X; j++){
                if(random.nextInt(100) < settings.treeFreq && tiles.get(i).get(j).canSpawnOn){
                    AnimatedThings temp = new AnimatedThings(i, j, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS * 2, "tree", (int) GameScreen.TILE_DIMS);
                    tiles.get(i).get(j).canSpawnOn = tileInformationHashMap.get("tree").canSpawnOn;
                    tiles.get(i).get(j).canWalkOn = tileInformationHashMap.get("tree").canWalkOn;
                    things.get(i).add(temp);
                }
                else {
                    things.get(i).add(new Thing(i, j, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS, "", (int) GameScreen.TILE_DIMS));
                }
            }
        }
        System.out.println("Trees generated");
    }

    public void addThing(Thing thing, int x, int y){
        things.get(x).set(y, thing);
        things.get(x).get(y).update(things);
        tiles.get(x).get(y).updateWalkAndSpawn(tileInformationHashMap, thing.type);
        updateThingNeighbours(x, y);
    }

    public void updateThingNeighbours(int x, int y){
        if (isWithinBounds(x + 1, y)) {
            things.get(x + 1).get(y).update(things);
        }
        if (isWithinBounds(x - 1, y)) {
            things.get(x - 1).get(y).update(things);
        }
        if (isWithinBounds(x, y + 1)) {
            things.get(x).get(y + 1).update(things);
        }
        if (isWithinBounds(x, y - 1)) {
            things.get(x).get(y - 1).update(things);
        }
    }

    public void changeTileType(int x, int y, String type){
        if (isWithinBounds(x, y)){
            Tile temp = tiles.get(x).get(y);
            temp.type = type;
            temp.updateWalkAndSpawn(tileInformationHashMap);
        }
    }

    public void changeThingType(int x, int y, String type, int height){
        if (isWithinBounds(x, y)){
            Thing temp2 = new Thing(things.get(x).get(y));
            temp2.type = type;
            temp2.height = height;
            things.get(x).set(y, temp2);
            Tile tempTile = tiles.get(x).get(y);
            tempTile.updateWalkAndSpawn(tileInformationHashMap);
        }
    }

    public boolean isWithinBounds(int newX, int newY){
        return newX >= 0 && newX < GameScreen.TILES_ON_X && newY >= 0 && newY < GameScreen.TILES_ON_X;
    }

    public static void setTileInfoHashMap(){
        tileInformationHashMap = json.fromJson(HashMap.class, TileInformation.class, Gdx.files.internal("info/tileInfo/tileInfo.txt"));
        System.out.println("");
    }

    public ArrayList<String> packageTiles(){
        ArrayList<String> output = new ArrayList<>();
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                output.add(tiles.get(i).get(j).type);
            }
        }
        return output;
    }

    public ArrayList<String> packageThings(){
        ArrayList<String> output = new ArrayList<>();
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                output.add(things.get(i).get(j).type);
            }
        }
        return output;
    }

    public void unPackageTiles(ArrayList<String> input){
        setup();
        ArrayList<ArrayList<Tile>> tempArray = new ArrayList<>();
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            tempArray.add(new ArrayList<>());
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                Tile temp = new Tile(i, j, input.get(i * GameScreen.TILES_ON_X + j));
                temp.updateWalkAndSpawn(tileInformationHashMap);
                tempArray.get(i).add(temp);
            }
        }
        tiles = tempArray;
    }

    public void unPackageThings(ArrayList<String> input){
        setup();
        ArrayList<ArrayList<Thing>> tempArray = new ArrayList<>();
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            tempArray.add(new ArrayList<>());
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                String s = input.get(i * GameScreen.TILES_ON_X + j);
                Vector2 multiplier = GameScreen.getMultiplierFromThings(s);
                Thing temp = new Thing(i, j, (int) (multiplier.x * GameScreen.TILE_DIMS),
                        (int) (multiplier.y  * GameScreen.TILE_DIMS), s, (int) GameScreen.TILE_DIMS);
                tempArray.get(i).add(temp);
            }
        }
        things = tempArray;
    }

    public static boolean loadMap(String saveName, Map map){
        try{
            String[] save = getSaveString(saveName);

            String mapDims = "250";

            for (String s : save) {
                switch (s.split(" ")[0]) {
                    case "date:":
                        String loadedDate = s.split(" ")[1];
                        break;
                    case "tiles:":
                        String loadedTiles = s.split(" ")[1];
                        map.tiles = RLE.decodeTiles(loadedTiles, Integer.parseInt(mapDims));
                        break;
                    case "things:":
                        String loadedThings = s.split(" ")[1];
                        map.things = RLE.decodeThings(loadedThings, Integer.parseInt(mapDims));
                        map.updateAllTilesWithNewThings(map.things);
                        break;
                    case "mapInfo:":
                        String loadedMapInfo = s.split(" ")[1];
                        break;
                    case "mapDims:":
                        mapDims = s.split(" ")[1];
                        GameScreen.TILES_ON_X = Integer.parseInt(mapDims);
                        break;
                    case "resources:":
                        String loadedResources = s.split(" ")[1];
                        break;
                    default:
                        break;
                }
            }
            return true;
        }catch(Exception e){
            System.out.println("Error loading map");
            return false;
        }
    }

    public void updateAllTilesWithNewThings(ArrayList<ArrayList<Thing>> things){
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                tiles.get(i).get(j).updateWalkAndSpawn(tileInformationHashMap);
            }
        }
    }

    public static ArrayList<Colonist> loadColonists(String saveName){
        try {
            String[] save = getSaveString(saveName);

            for (String s : save) {
                if ("colonists:".equals(s.split(" ")[0])) {
                    String loadedColonists = s.split(": ")[1];
                    System.out.println("showing the colonists' json: " + loadedColonists);
                    return json.fromJson(ArrayList.class, Colonist.class, loadedColonists);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getSaveString(String saveName) throws IOException {
        File file = new File("core/assets/Saves/" + saveName + "/save.sve");
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String[] save = new String[6]; //needs to be the size equal to the number of lines in the save file
        int count = 0;
        String temp = br.readLine();
        while (temp != null){
            System.out.println(temp);
            save[count] = temp;
            count ++;
            temp = br.readLine();
        }
        br.close();
        System.out.println(Arrays.toString(save));
        return save;
    }

    public void clearThing(int x, int y){
        things.get(x).get(y).type = "";
        things.get(x).get(y).canConnect = false;
        tiles.get(x).get(y).updateWalkAndSpawn(tileInformationHashMap);
    }
}
