package com.mygdx.game.Generation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Math.Math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.mygdx.game.Screens.GameScreen.TILES_ON_X;
import static com.mygdx.game.Screens.GameScreen.TILES_ON_Y;

public class Map {
    ArrayList<ArrayList<Tile>> tiles;
    ArrayList<ArrayList<Thing>> things;

    public int addition;

    int x;
    int y;

    int drawHeight;

    public MapSettings settings;

    ArrayList<Vector2> riverLocs;

    public Map(MapSettings settings){
        this.settings = settings;
    }

    public Map(MapSettings settings, int drawHeight, int x, int y){
        this.settings = settings;
        settings.width = TILES_ON_X;
        settings.height = TILES_ON_Y;
        settings.tileDims = (drawHeight / (float) settings.height);

        this.x = x;
        this.y = y;
    }

    public Map(int drawHeight, int x, int y, String seed){
        this.settings = new MapSettings(seed);
        settings.width = TILES_ON_X;
        settings.height = TILES_ON_Y;
        this.drawHeight = drawHeight;
        settings.tileDims = (drawHeight / (float) settings.height);

        this.x = x;
        this.y = y;
    }

    public Map(){

    }

    public void generateMap(){
        tiles = new ArrayList<>();
        things = new ArrayList<>();
        generateGrass();
        generateStone();

        findRiverLocs();
        generateRiver();
        generateTrees();
    }

    public void generateStone(){
        for(int i = 0; i < settings.width; i++) {
            tiles.add(new ArrayList<>());
            for (int j = 0; j < settings.height; j++) {
                float temp = (float) Noise2D.noise((((float) i / settings.width) * settings.perlinFrequency) + addition,
                        (((float) j / settings.height) * settings.perlinFrequency) + addition, 255);
                if (temp > 0.6f) {
                    Tile tile = new Tile(i, j, "stone");
                    tile.canSpawnOn = false;
                    tiles.get(i).set(j, tile);
                }

            }
        }
    }

    public void generateGrass(){
        for(int i = 0; i < settings.width; i++) {
            tiles.add(new ArrayList<>());
            for (int j = 0; j < settings.height; j++) {
                float temp = (float) Noise2D.noise((((float) i / settings.width) * 40) + addition,
                        (((float) j / settings.height) * 40) + addition, 255);
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
        settings.tileDims = (drawHeight / (float) settings.height);
        getAdditionFromSeed(settings.seed);
        tiles = new ArrayList<>();
        for(int i = 0; i < settings.width; i++){
            tiles.add(new ArrayList<>());
            for(int j = 0; j < settings.height; j++){
                float temp = (float) Noise2D.noise((((float) i / settings.width) * 3) + addition,
                        (((float) j / settings.height) * 3) + addition, 255);
                if(temp > 0.6f){
                    tiles.get(i).add(new Tile(i, j, "stone"));
                }
                else{
                    tiles.get(i).add(new Tile(i, j, "dirt"));
                }
            }
        }
        generateRiver();
    }

    public void generateBlank(){
        tiles = new ArrayList<>();
        things = new ArrayList<>();
        for(int i = 0; i < settings.width; i++){
            tiles.add(new ArrayList<>());
            things.add(new ArrayList<>());
            for(int j = 0; j < settings.height; j++){
                tiles.get(i).add(new Tile(i, j, "dirt"));
                things.get(i).add(new Thing(i, j, (int) settings.tileDims, (int) settings.tileDims, "", (int) settings.tileDims));
            }
        }
    }

    public void drawMap(SpriteBatch batch, HashMap<String, Texture> tileTextures, HashMap<String, TextureAtlas> thingTextures, CameraTwo camera){
        int startX = (int)(Math.highest (((camera.position.x - (camera.width * camera.zoom)/2f) / settings.tileDims) - 5, 0));
        int startY = (int)(Math.highest (((camera.position.y - (camera.height * camera.zoom)/2f) / settings.tileDims) - 5, 0));

        int endX = (int)(Math.lowest((camera.width * camera.zoom / settings.tileDims) + startX + 10, TILES_ON_X));
        int endY = (int)(Math.lowest((camera.height * camera.zoom / settings.tileDims) + startY + 10, TILES_ON_Y));
        for(int i = startX; i < endX; i++){
            for(int j = startY; j < endY; j++){
                batch.draw(tileTextures.get(tiles.get(i).get(j).type), i * settings.tileDims, j * settings.tileDims, settings.tileDims, settings.tileDims);
            }
        }
        for(int i = startX; i < endX; i++) {
            for (int j = endY - 1; j > startY; j--) {
                if (!things.get(i).get(j).type.equals("")) {
                    Thing t = things.get(i).get(j);
                    t.draw(batch, thingTextures.get(t.type));
                }
            }
        }
    }

    public void drawMiniMap(SpriteBatch batch, HashMap<String, Texture> textures){
        for (int i = 0; i < settings.width; i++) {
            for (int j = 0; j < settings.height; j++) {
                batch.draw(textures.get(tiles.get(i).get(j).type), i * settings.tileDims + x, j * settings.tileDims + y, settings.tileDims, settings.tileDims);
            }
        }
    }

    public int getAdditionFromSeed(String seed){
        addition = 0;
        for (char c: seed.toCharArray()
        ) {
            addition += c * 1000;
        }
        return addition;
    }

    public void generateRiver(){
        Vector2 start = riverLocs.get(0);
        Vector2 end = riverLocs.get(1);
        ArrayList<Vector2> path;
        ArrayList<ArrayList<Boolean>> mapTemp = new ArrayList<>();
        for (int i = 0; i < settings.width; i++) {
            mapTemp.add(new ArrayList<>());
            for (int j = 0; j < settings.height; j++) {
                if (tiles.get(i).get(j).type.equals("stone")) {
                    mapTemp.get(i).add(false);
                }
                else {
                    mapTemp.get(i).add(true);
                }
            }
        }
        path = AStar.pathFind(start, end, addition, mapTemp, settings.riverBend, settings.perlinFrequency);
        for (int i = 0; i < path.size(); i++) {
            Vector2 temp = path.get(i);
            if (i + 1 < path.size()) {
                if (path.get(i + 1).y == temp.y) {
                    for (int j = 0; j < 2; j++) {
                        if (temp.y + j < settings.height){
                            Tile tempTile = tiles.get((int) temp.x).get((int) (temp.y + j));
                            tempTile.type = "water";
                            tempTile.canSpawnOn = false;
                        }
                    }
                }

            }
            for (int j = 0; j < 4; j++) {
                if (temp.x + j < settings.width) {
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
        int sx = rand.nextInt(settings.width);
        int ex = rand.nextInt(settings.width);

        while(tiles.get(sx).get(0).type.equals("stone")){
            sx = rand.nextInt(settings.width);
        }
        while(tiles.get(ex).get(0).type.equals("stone")){
            ex = rand.nextInt(settings.width);
        }
        riverLocs.add(new Vector2(sx, 0));
        riverLocs.add(new Vector2(ex, settings.height - 1));
    }

    public void generateTrees(){
        Random random = new Random();
        for(int i = 0; i < settings.width; i++){
            things.add(new ArrayList<>());
            for(int j = 0; j < settings.height; j++){
                if(random.nextInt(100) < settings.treeFreq && tiles.get(i).get(j).canSpawnOn){
                    Thing temp = new Thing(i, j, (int) settings.tileDims, (int) settings.tileDims * 2, "tree", (int) settings.tileDims);
                    things.get(i).add(temp);
                }
                else {
                    things.get(i).add(new Thing(i, j, (int) settings.tileDims, (int) settings.tileDims, "", (int) settings.tileDims));
                }
            }
        }
    }

    public void changeTileType(int x, int y, String type){
        tiles.get(x).get(y).type = type;
    }
}
