package com.mygdx.game.Generation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
        riverLocs = findRiverLocs();
        generateRiver();
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
        for(int i = 0; i < settings.width; i++){
            tiles.add(new ArrayList<>());
            for(int j = 0; j < settings.height; j++){
                tiles.get(i).add(new Tile(i, j, "dirt"));
            }
        }
    }

    public void drawMap(SpriteBatch batch, HashMap<String, Texture> textures, CameraTwo camera){
        int startX = (int)(Math.highest (((camera.position.x - (camera.width * camera.zoom)/2f) / settings.tileDims) - 5, 0));
        int startY = (int)(Math.highest (((camera.position.y - (camera.height * camera.zoom)/2f) / settings.tileDims) - 5, 0));

        int endX = (int)(Math.lowest((camera.width * camera.zoom / settings.tileDims) + startX + 10, TILES_ON_X));
        int endY = (int)(Math.lowest((camera.height * camera.zoom / settings.tileDims) + startY + 10, TILES_ON_Y));
        for(int i = startX; i < endX; i++){
            for(int j = startY; j < endY; j++){
                batch.draw(textures.get(tiles.get(i).get(j).type), i * settings.tileDims, j * settings.tileDims, settings.tileDims, settings.tileDims);
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
                            tiles.get((int) temp.x).get((int) (temp.y + j)).type = "water";
                        }
                    }
                }

            }
            for (int j = 0; j < 4; j++) {
                if (temp.x + j < settings.width) {
                    tiles.get((int) temp.x + j).get((int) temp.y).type = "water";
                }
            }
        }
    }

    public ArrayList<Vector2> findRiverLocs(){
        ArrayList<Vector2> riverLocs = new ArrayList<>();
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
        return riverLocs;
    }
}
