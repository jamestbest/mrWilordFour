package com.mygdx.game.Generation.Things;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Entity.Task;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.Tile;
import com.mygdx.game.Screens.GameScreen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Fire {
    private int x;
    private int y;

    private float counter;
    private int animationCounter;
    private int maxAnimationCounter;

    private String name;
    private int id;

    private int powerLevel = 1;
    private final int powerLevelMax = 4;
    private int spreadability;
    public boolean canSpread;

    float totalTime;
    boolean isDying;
    public boolean isDead;
    float timeToDie = 100;

    private static final int[][] neighbours = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

//    private static HashMap<String, ArrayList<Texture>> fireMap;
    private static final Random random = new Random();
    private static int idCounter;
    public static final int DAMAGE = 5;

    public Fire(int x, int y, String name, int maxAnimationCounter) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.id = getNextId();
        counter = 0;
        animationCounter = 0;
        this.maxAnimationCounter = maxAnimationCounter;
    }

    public Fire(){

    }

    public void update(Map map){
        counter += Gdx.graphics.getDeltaTime() * GameScreen.gameSpeed;
        totalTime += Gdx.graphics.getDeltaTime() * GameScreen.gameSpeed;
        float counterMax = 1;
        if(counter >= counterMax){
            counter = 0;
            animationCounter++;
            if(animationCounter >= maxAnimationCounter){
                animationCounter = 0;
            }

            if (random.nextInt(10) < 2){
                if (!isDying){
                    powerLevel++;
                }
                else {
                    powerLevel--;
                }
                if (powerLevel >= powerLevelMax){
                    powerLevel = powerLevelMax;
                }
                if (powerLevel < 0){
                    powerLevel = 0;
                    isDead = true;
                    canSpread = false;
                }
            }
            if (powerLevel == powerLevelMax && !isDying){
                spreadability += random.nextInt(20);
            }
        }
        if (spreadability > 400){
            canSpread = true;
            spreadability = 0;
        }

        if (powerLevel == powerLevelMax){
            if (!Objects.equals(map.tiles.get(x).get(y).type, "dirt")){
                map.tiles.get(x).get(y).type = "dirt";
            }
            if (!Objects.equals(map.things.get(x).get(y).type, "")){
                map.things.get(x).get(y).type = "";
            }
        }

        if (totalTime >= timeToDie){
            isDying = true;
        }
    }

    public void draw(SpriteBatch batch, ArrayList<Texture> fireMap){
        float dims = (GameScreen.TILE_DIMS * ((float)powerLevel / powerLevelMax));
        float x2 = GameScreen.TILE_DIMS * this.x + (GameScreen.TILE_DIMS - dims) / 2;
        float y2 = GameScreen.TILE_DIMS * this.y + (GameScreen.TILE_DIMS - dims) / 2;
        batch.draw(fireMap.get(animationCounter), x2, y2, dims, dims);
    }

    public Fire spread(ArrayList<ArrayList<Tile>> tiles, Map map){
        for (int[] neighbour : neighbours) {
            int x = this.x + neighbour[0];
            int y = this.y + neighbour[1];
            if (map.isWithinBounds(x, y) && tiles.get(x).get(y).canWalkOn && noFireHere(tiles, x, y)){
                tiles.get(x).get(y).hasFireOn = true;
                map.tasks.add(new Task("FireFight", "", x, y));
                return new Fire(x, y, name, this.maxAnimationCounter);
            }
        }
        return null;
    }

    public boolean noFireHere(ArrayList<ArrayList<Tile>> tiles, int x, int y){
        return !tiles.get(x).get(y).hasFireOn;
    }

    public static HashMap<String, ArrayList<Texture>> setupFireMap(){
        File dir = new File("core/assets/Textures/Fire");
        File[] files = dir.listFiles();
        HashMap<String, ArrayList<Texture>> fireMap;
        fireMap = new HashMap<>();
        assert files != null;
        for (File file : files) {
            String[] frames = file.list();
            ArrayList<Texture> textures = new ArrayList<>();
            assert frames != null;
            for (String frame : frames) {
                textures.add(new Texture("core/assets/Textures/Fire/" + file.getName() + "/" + frame));
            }
            fireMap.put(file.getName(), textures);
        }
        return fireMap;
    }

    public int getNextId(){
        return idCounter++;
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

    public float getCounter() {
        return counter;
    }

    public void setCounter(float counter) {
        this.counter = counter;
    }

    public int getAnimationCounter() {
        return animationCounter;
    }

    public void setAnimationCounter(int animationCounter) {
        this.animationCounter = animationCounter;
    }

    public int getMaxAnimationCounter() {
        return maxAnimationCounter;
    }

    public void setMaxAnimationCounter(int maxAnimationCounter) {
        this.maxAnimationCounter = maxAnimationCounter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
