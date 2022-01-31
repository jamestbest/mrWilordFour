package com.mygdx.game.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.DataStructures.Queue;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.TileInformation;
import com.mygdx.game.Screens.GameScreen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Colonist {

    public int x;
    public int y;

    int nextX;
    int nextY;

    public int health;
    public HashMap<String, Integer> skills;

    public String profession;
    public String backstory;

    public String clotheName;
    String direction = "front";

    public String firstName;
    public String lastName;

    static Random random = new Random();

    ArrayList<Vector2> pathToComplete = new ArrayList<>();

    HashMap<String, Integer> priorityFromType;

    boolean movingAcrossPath = false;

    float timer = 0f;
    float timerMax = 1f;

    int randomMoveRadius = 25;

    public Colonist() {
        this.health = 100;
    }

    public void setup(){
        getRandomName();
        System.out.println(firstName + " " + lastName + " is a " + profession);
        setupPriorities();
    }

    public void copyTemplate(Colonist template){
        this.skills = template.skills;
        this.profession = template.profession;
        this.backstory = template.backstory;
    }

    public void getRandomName() {
        ArrayList<String> firstNames = makeArrayOfNames("ColonistFirstNames");
        ArrayList<String> lastNames = makeArrayOfNames("ColonistLastNames");
        firstName = firstNames.get(random.nextInt(firstNames.size()));
        lastName = lastNames.get(random.nextInt(lastNames.size()));
    }

    public ArrayList<String> makeArrayOfNames(String nameOfFile) {
        try {
            FileReader fileReader = new FileReader("assets/ColonistInformation/" + nameOfFile);

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

    public void draw(SpriteBatch batch, float tileDims, HashMap<String, TextureAtlas> clothes){
        timer += Gdx.graphics.getDeltaTime() * GameScreen.gameSpeed;
        if (timer >= timerMax) {
            x = nextX;
            y = nextY;
            timer = 0f;
        }
        updateDirection();
        batch.draw(clothes.get(clotheName).findRegion(direction),  (x + ((nextX - x) * timer)) * tileDims , (y + ((nextY - y) * timer)) * tileDims, tileDims, tileDims);
    }

    public void moveRandomly(Map map){
        int randomX = random.nextInt(3) - 1;
        int randomY = random.nextInt(3) - 1;

        if (map.isWithinBounds(randomX + x, randomY + y)) {
            if (map.tiles.get(x + randomX).get(y + randomY).canWalkOn) {
                nextX = x + randomX;
                nextY = y + randomY;
            }
        }
    }

    public void setMoveToPos(int x, int y, Map map){
        pathToComplete = AStar.pathFindForColonist(new Vector2(this.x, this.y), new Vector2(x, y), map.addition, map.booleanMap);
        movingAcrossPath = pathToComplete.size() > 0;
    }

    public void getRandomPosition(Map map){
        int count = 0;
        Vector2 randomPos = getPosInRange(map);
        int randomX = (int) randomPos.x;
        int randomY = (int) randomPos.y;

        while (!map.tiles.get(x + randomX).get(y + randomY).canWalkOn) {
            randomPos = getPosInRange(map);
            randomX = (int) randomPos.x;
            randomY = (int) randomPos.y;
            count++;
            if (count > 100) {
                break;
            }
        }

        pathToComplete = AStar.pathFindForColonist(new Vector2(x, y), new Vector2(randomX + x, randomY + y), map.addition, map.booleanMap);

        movingAcrossPath = pathToComplete.size() > 0;
    }

    public Vector2 getPosInRange(Map map){
        int randomX = random.nextInt(randomMoveRadius * 2);
        int randomY = random.nextInt(randomMoveRadius * 2);

        randomX -= randomMoveRadius;
        randomY -= randomMoveRadius;
        while (!map.isWithinBounds(x + randomX, y + randomY)){
            randomX = random.nextInt(randomMoveRadius * 2);
            randomY = random.nextInt(randomMoveRadius * 2);

            randomX -= randomMoveRadius;
            randomY -= randomMoveRadius;
        }
        return new Vector2(randomX, randomY);
    }

    public void moveAlongPath(){
        if(pathToComplete.size() > 0){
            Vector2 nextTile = pathToComplete.get(0);
            if(nextTile.x == x && nextTile.y == y){
                pathToComplete.remove(0);
            }
        }
        else {
            movingAcrossPath = false;
        }
        if (pathToComplete.size() >= 1) {
            nextX = (int) pathToComplete.get(0).x;
            nextY = (int) pathToComplete.get(0).y;
        }
    }

    public void moveColonist(Map map){
        if (movingAcrossPath) {
            moveAlongPath();
        }
        else {
            int choice = random.nextInt(10);
            if (choice <= 3){
                getRandomPosition(map);
            }
            else {
                moveRandomly(map);
            }
        }
    }

    public void updateDirection(){
        if(nextX > x){
            direction = "right";
        }
        else if(nextX < x){
            direction = "left";
        }
        if(nextY > y){
            direction = "back";
        }
        else if(nextY < y){
            direction = "front";
        }
    }

    public void drawPathOutline(CameraTwo cameraTwo, ShapeRenderer shapeRenderer){
        for (Vector2 v : pathToComplete) {
            Gdx.gl.glEnable(GL30.GL_BLEND);
            Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(cameraTwo.projViewMatrix);
            shapeRenderer.setColor(0, 0, 1, 0.5f);
            shapeRenderer.rect(v.x * GameScreen.TILE_DIMS, v.y * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL30.GL_BLEND);
        }
    }

    public void setupPriorities(){
        priorityFromType = new HashMap<>();
        priorityFromType.put("Mine", 1);
        priorityFromType.put("Plant", 2);
        priorityFromType.put("CutDown", 3);
        priorityFromType.put("Harvest", 4);
    }

    public Task getNextTask(ArrayList<Task> availableTasks){
        float minDistance = Integer.MAX_VALUE;
        int maxPriority = 0;
        String maxPriorityTaskType = "";
        Task bestTask = null;

        for(Task task : availableTasks){
            if(priorityFromType.get(task.type) > maxPriority){
                maxPriority = priorityFromType.get(task.type);
                maxPriorityTaskType = task.type;
            }
        }

        for (Task task : availableTasks) {
            if (task.type.equals(maxPriorityTaskType)) {
                float distance = getDistance(task.x, task.y);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestTask = task;
                }
            }
        }
        return bestTask;
    }

    public float getDistance(int x, int y){
        return (float) Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
    }
}
