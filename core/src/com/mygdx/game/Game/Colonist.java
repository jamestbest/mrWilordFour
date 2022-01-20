package com.mygdx.game.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Generation.Map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Colonist {

    public int x;
    public int y;

    public int health;
    public HashMap<String, Integer> skills;

    public String profession;
    public String backstory;

    public String clotheName;
    public TextureAtlas textureAtlas;
    String direction = "front";

    public String firstName;
    public String lastName;

    static Random random = new Random();
    static ShapeRenderer shapeRenderer = new ShapeRenderer();

    ArrayList<Vector2> pathToComplete;

    Map map;

    boolean movingAcrossPath = false;

    public Colonist() {
        this.health = 100;
    }

    public void setup(){
        generateTextureAtlas();
        getRandomName();
        System.out.println(firstName + " " + lastName + " is a " + profession);
    }

    public void setMap(Map map){
        this.map = map;
    }

    public void copyTemplate(Colonist template){
        this.skills = template.skills;
        this.profession = template.profession;
        this.backstory = template.backstory;
    }

    public void generateTextureAtlas(){
        textureAtlas = new TextureAtlas("Textures/TAResources/" + clotheName + ".atlas");
    }

    public void getRandomName() {
        ArrayList<String> firstNames = makeArrayOfNames("ColonistFirstNames");
        ArrayList<String> lastNames = makeArrayOfNames("ColonistLastNames");
        firstName = firstNames.get(random.nextInt(firstNames.size()));
        lastName = lastNames.get(random.nextInt(lastNames.size()));
    }

    public ArrayList<String> makeArrayOfNames(String nameOfFile) {
        try {
            FileReader fileReader = new FileReader("core/assets/ColonistInformation/" + nameOfFile);

            BufferedReader br = new BufferedReader(fileReader);

            ArrayList<String> temp = new ArrayList<>();

            String line = br.readLine();
            while(line != null) {
                temp.add(line);
                line = br.readLine();
            }

            fileReader.close();
            br.close();
            return temp;
        }
        catch (IOException e) {
            Gdx.app.log("Colonist", "Error reading file");
        }

        return null;
    }

    public void draw(SpriteBatch batch, float tileDims){
        batch.draw(textureAtlas.findRegion(direction), x * tileDims, y * tileDims, tileDims, tileDims);
    }

    public void moveRandomly(){
        int randomX = random.nextInt(3) - 1;
        int randomY = random.nextInt(3) - 1;

        if (map.isWithinBounds(randomX + x, randomY + y)) {
            if (map.tiles.get(x + randomX).get(y + randomY).canWalkOn) {
                x += randomX;
                y += randomY;
            }
        }
    }

    public void getRandomPosition(){
        int randomX = random.nextInt(map.settings.width);
        int randomY = random.nextInt(map.settings.height);

        if (!map.tiles.get(randomX).get(randomY).canWalkOn) {
            getRandomPosition();
        }

        pathToComplete = AStar.pathFindForColonist(new Vector2(x, y), new Vector2(randomX, randomY), map.addition, map.booleanMap);

        if (pathToComplete.size() > 0) {
            movingAcrossPath = true;
        }
    }

    public void moveAlongPath(){
        if(pathToComplete.size() > 0){
            Vector2 nextTile = pathToComplete.get(0);
            if(nextTile.x == x && nextTile.y == y){
                pathToComplete.remove(0);
            }
            else{
                if(nextTile.x > x){
                    direction = "right";
                }
                else if(nextTile.x < x){
                    direction = "left";
                }
                else if(nextTile.y > y){
                    direction = "front";
                }
                else if(nextTile.y < y){
                    direction = "back";
                }
                x = (int) nextTile.x;
                y = (int) nextTile.y;
            }
        }
    }

    public void drawPathOutline(CameraTwo cameraTwo){
        for (Vector2 v : pathToComplete) {
            Gdx.gl.glEnable(GL30.GL_BLEND);
            Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(cameraTwo.projViewMatrix);
            shapeRenderer.setColor(0, 0, 1, 0.5f);
            shapeRenderer.rect(v.x * map.settings.tileDims, v.y * map.settings.tileDims, map.settings.tileDims, map.settings.tileDims);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL30.GL_BLEND);
        }
    }
}
