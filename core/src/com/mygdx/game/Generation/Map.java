package com.mygdx.game.Generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Math.Math;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.mygdx.game.Screens.GameScreen.TILES_ON_X;
import static com.mygdx.game.Screens.GameScreen.TILES_ON_Y;

public class Map {
    public ArrayList<ArrayList<Tile>> tiles;
    ArrayList<ArrayList<Thing>> things;
    public ArrayList<ArrayList<Boolean>> booleanMap;

    public int addition;

    int x;
    int y;

    int drawHeight;

    public MapSettings settings;

    ArrayList<Vector2> riverLocs;

    public static HashMap<String, TileInformation> tileInformationHashMap;

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
        setTileInfoHashMap();
        tiles = new ArrayList<>();
        things = new ArrayList<>();
        generateGrass();
        generateStone();

        if (settings.riverToggle){
            findRiverLocs();
            generateRiver();
        }

        generateTrees();
    }

    public void generateStone(){
        for(int i = 0; i < TILES_ON_X; i++) {
            for (int j = 0; j < TILES_ON_Y; j++) {
                double temp = Noise2D.noise((((float) i / TILES_ON_X) * settings.perlinFrequency) + addition,
                        (((float) j / TILES_ON_Y) * settings.perlinFrequency) + addition, 255);
                if (temp > 0.65f) {
                    Tile tile = new Tile(i, j, "stone");
                    tile.canSpawnOn = false;
                    tile.canWalkOn = false;
                    tiles.get(i).set(j, tile);
                }

            }
        }
    }

    public void generateGrass(){
        for(int i = 0; i < TILES_ON_X; i++) {
            tiles.add(new ArrayList<>());
            for (int j = 0; j < TILES_ON_Y; j++) {
                float temp = (float) Noise2D.noise((((float) i / TILES_ON_X) * 40) + addition,
                        (((float) j / TILES_ON_Y) * 40) + addition, 255);
                Tile tile;
                if (temp > 0.55f) {
                    tile = new Tile(i, j, "grass");
                }
                else{
                    tile = new Tile(i, j, "dirt");
                }
                tile.canSpawnOn = true;
                tiles.get(i).add(tile);
            }
        }
    }

    public void updateMap(){
//        GameScreen.TILE_DIMS = (drawHeight / (float) TILES_ON_Y);
        getAdditionFromSeed(settings.seed);
        tiles.clear();
        things.clear();
        generateMap();
    }

    public void updateBooleanMap(){
        booleanMap = new ArrayList<>();
        for (int i = 0; i < TILES_ON_X; i++) {
            booleanMap.add(new ArrayList<>());
            for (int j = 0; j < TILES_ON_Y; j++) {
                if (tiles.get(i).get(j).canWalkOn) {
                    booleanMap.get(i).add(true);
                }
                else {
                    booleanMap.get(i).add(false);
                }
            }
        }
    }

    public void generateBlank(){
        tiles = new ArrayList<>();
        things = new ArrayList<>();
        for(int i = 0; i < TILES_ON_X; i++){
            tiles.add(new ArrayList<>());
            things.add(new ArrayList<>());
            for(int j = 0; j < TILES_ON_Y; j++){
                tiles.get(i).add(new Tile(i, j, "dirt"));
                things.get(i).add(new Thing(i, j, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS, "", (int) GameScreen.TILE_DIMS));
            }
        }
    }

    public void drawMap(SpriteBatch batch, HashMap<String, Texture> tileTextures, CameraTwo camera){
        int startX = (int)(Math.highest (((camera.position.x - (camera.width * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));
        int startY = (int)(Math.highest (((camera.position.y - (camera.height * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));

        int endX = (int)(Math.lowest((camera.width * camera.zoom / GameScreen.TILE_DIMS) + startX + 10, TILES_ON_X));
        int endY = (int)(Math.lowest((camera.height * camera.zoom / GameScreen.TILE_DIMS) + startY + 10, TILES_ON_Y));
        for(int i = startX; i < endX; i++){
            for(int j = startY; j < endY; j++){
                batch.draw(tileTextures.get(tiles.get(i).get(j).type), i * GameScreen.TILE_DIMS, j * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
            }
        }

    }

    public void drawThings(SpriteBatch batch, HashMap<String, TextureAtlas> thingTextures, CameraTwo camera){
        int startX = (int)(Math.highest (((camera.position.x - (camera.width * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));
        int startY = (int)(Math.highest (((camera.position.y - (camera.height * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));

        int endX = (int)(Math.lowest((camera.width * camera.zoom / GameScreen.TILE_DIMS) + startX + 10, TILES_ON_X));
        int endY = (int)(Math.lowest((camera.height * camera.zoom / GameScreen.TILE_DIMS) + startY + 10, TILES_ON_Y));
        for(int i = startX; i < endX; i++) {
            for (int j = endY - 1; j > startY; j--) {
                if (!things.get(i).get(j).type.equals("")) {
                    Thing t = things.get(i).get(j);
                    t.draw(batch, thingTextures.get(t.type));
                }
            }
        }
    }

    public void drawMiniMap(SpriteBatch batch, HashMap<String, Texture> textures, HashMap<String, TextureAtlas> thingTextures){
        float miniMapDims = drawHeight / (float) TILES_ON_Y;
        
        for (int i = 0; i < TILES_ON_X; i++) {
            for (int j = 0; j < TILES_ON_Y; j++) {
                batch.draw(textures.get(tiles.get(i).get(j).type), i * miniMapDims + x, j * miniMapDims + y, miniMapDims, miniMapDims);

                if (!things.get(i).get(j).type.equals("")) {
                    batch.draw(thingTextures.get(things.get(i).get(j).type).findRegion("0"), i * miniMapDims + x, j * miniMapDims + y, miniMapDims, miniMapDims);
                }
            }
        }
    }

    public void getAdditionFromSeed(String seed){
        addition = 0;
        for (char c: seed.toCharArray()
        ) {
            addition += c * 1000;
        }
    }

    public void generateRiver(){
        Vector2 start = riverLocs.get(0);
        Vector2 end = riverLocs.get(1);
        ArrayList<Vector2> path;
        updateBooleanMap();
        path = AStar.pathFindForRivers(start, end, addition, booleanMap, settings.riverBend, settings.perlinFrequency);
        for (int i = 0; i < path.size(); i++) {
            Vector2 temp = path.get(i);
            if (i + 1 < path.size()) {
                if (path.get(i + 1).y == temp.y) {
                    for (int j = 0; j < 2; j++) {
                        if (temp.y + j < TILES_ON_Y){
                            Tile tempTile = tiles.get((int) temp.x).get((int) (temp.y + j));
                            tempTile.type = "water";
                            tempTile.canSpawnOn = false;
                        }
                    }
                }

            }
            for (int j = 0; j < 4; j++) {
                if (temp.x + j < TILES_ON_X) {
                    Tile tempTile = tiles.get((int) (temp.x + j)).get((int) temp.y);
                    tempTile.type = "water";
                    tempTile.canSpawnOn = false;
                }
            }
        }
    }

    public void findRiverLocs(){
        riverLocs = new ArrayList<>();
        Random rand = new Random();
        int sx = rand.nextInt(TILES_ON_X);
        int ex = rand.nextInt(TILES_ON_X);

        while(!tiles.get(sx).get(0).canSpawnOn){
            sx = rand.nextInt(TILES_ON_X);
        }
        while(!tiles.get(ex).get(TILES_ON_Y - 1).canSpawnOn){
            ex = rand.nextInt(TILES_ON_X);
        }
        riverLocs.add(new Vector2(sx, 0));
        riverLocs.add(new Vector2(ex, TILES_ON_Y - 1));
    }

    public void generateTrees(){
        Random random = new Random();
        for(int i = 0; i < TILES_ON_X; i++){
            things.add(new ArrayList<>());
            for(int j = 0; j < TILES_ON_Y; j++){
                if(random.nextInt(100) < settings.treeFreq && tiles.get(i).get(j).canSpawnOn){
                    Thing temp = new Thing(i, j, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS * 2, "tree", (int) GameScreen.TILE_DIMS);
                    things.get(i).add(temp);
                }
                else {
                    things.get(i).add(new Thing(i, j, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS, "", (int) GameScreen.TILE_DIMS));
                }
            }
        }
        System.out.println("Trees generated");
    }

    public void changeTileType(int x, int y, String type){
        if (isWithinBounds(x, y)){
            Tile temp = tiles.get(x).get(y);
            temp.type = type;
            temp.canSpawnOn = tileInformationHashMap.get(type).canSpawnOn;
            temp.canWalkOn = tileInformationHashMap.get(type).canWalkOn;
        }
    }

    public boolean isWithinBounds(int newX, int newY){
        return newX >= 0 && newX < TILES_ON_X && newY >= 0 && newY < TILES_ON_Y;
    }

    public static void setTileInfoHashMap(){
        Json json = new Json();
        tileInformationHashMap = json.fromJson(HashMap.class, TileInformation.class, Gdx.files.internal("TileInfo/tileInfo.txt"));
        System.out.println("");
    }
}
