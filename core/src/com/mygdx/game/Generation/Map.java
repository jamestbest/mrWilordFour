package com.mygdx.game.Generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AStar.AStar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Map {

    public int width;
    public int height;

    public float TILE_DIMS;

    ArrayList<ArrayList<Tile>> tiles;

    HashMap<String, Texture> textures = new HashMap<>();

    public Map(int width, int height, float TILE_DIMS){
        this.width = width;
        this.height = height;
        this.TILE_DIMS = TILE_DIMS;

        initialiseTextures();
    }

    public void generateMap(int addition){
        tiles = new ArrayList<>();
        for(int i = 0; i < width; i++){
            tiles.add(new ArrayList<>());
            for(int j = 0; j < height; j++){
                float temp = (float) Noise2D.noise((((float) i / width) * 3) + addition, (((float) j / height) * 3) + addition, 255);
                if(temp > 0.6f){
                    tiles.get(i).add(new Tile(i, j, "stone"));
                }
                else{
                    tiles.get(i).add(new Tile(i, j, "dirt"));
                }
            }
        }
    }

    public void drawMap(SpriteBatch batch){
        for(int i = 0; i < tiles.size(); i++){
            for(int j = 0; j < tiles.get(i).size(); j++){
                switch (tiles.get(i).get(j).type) {
                    case "grass" -> batch.draw(textures.get("grass"), i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                    case "stone" -> batch.draw(textures.get("stone"), i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                    case "water" -> batch.draw(textures.get("water"), i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                    case "dirt" -> batch.draw(textures.get("dirt"), i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                }
            }
        }
    }

    public void initialiseTextures(){
        //loads every texture in the textures map
        File directory= new File("core/assets/Textures");
        String[] files = directory.list();
        assert files != null;
        for ( String fileName : files) {
            String[] temp = fileName.split("\\.");
            textures.put(temp[0], new Texture(Gdx.files.internal("core/assets/Textures/" + fileName)));
        }
    }

    public int getAdditionFromSeed(String seed){
        int addition = 0;
        for (char c: seed.toCharArray()
        ) {
            addition += c * 1000;
        }
        return addition;
    }

    public void generateRiver(int addition){
        ArrayList<Vector2> riverLocs = findRiverLocs();
        Vector2 start = riverLocs.get(0);
        Vector2 end = riverLocs.get(1);
        ArrayList<Vector2> path;
        ArrayList<ArrayList<Boolean>> mapTemp = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            mapTemp.add(new ArrayList<>());
            for (int j = 0; j < height; j++) {
                if (tiles.get(i).get(j).type.equals("stone")) {
                    mapTemp.get(i).add(false);
                }
                else {
                    mapTemp.get(i).add(true);
                }
            }
        }
        path = AStar.pathFind(start, end, addition, mapTemp);
        for (int i = 0; i < path.size(); i++) {
            Vector2 temp = path.get(i);
            if (i + 1 < path.size()) {
                if (path.get(i + 1).y == temp.y) {
                    for (int j = 0; j < 2; j++) {
                        if (temp.y + j < height){
                            tiles.get((int) temp.x).get((int) (temp.y + j)).type = "water";
                        }
                    }
                }

            }
            for (int j = 0; j < 4; j++) {
                if (temp.x + j < width) {
                    tiles.get((int) temp.x + j).get((int) temp.y).type = "water";
                }
            }
        }
    }

    public ArrayList<Vector2> findRiverLocs(){
        ArrayList<Vector2> riverLocs = new ArrayList<>();
        Random rand = new Random();
        int sx = rand.nextInt(width);
        int ex = rand.nextInt(width);

        while(tiles.get(sx).get(0).type.equals("stone")){
            sx = rand.nextInt(width);
        }
        while(tiles.get(ex).get(0).type.equals("stone")){
            ex = rand.nextInt(width);
        }
        riverLocs.add(new Vector2(sx, 0));
        riverLocs.add(new Vector2(ex, height - 1));
        return riverLocs;
    }
}
