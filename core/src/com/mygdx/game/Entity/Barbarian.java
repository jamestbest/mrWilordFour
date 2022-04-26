package com.mygdx.game.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Screens.GameScreen;
import com.mygdx.game.floorDrops.Zone;
import io.socket.client.Socket;

import java.util.ArrayList;
import java.util.HashMap;

public class Barbarian extends Colonist {
    private BarbarianTask currentTask;

    public Barbarian(int x, int y, String entityType, int width, int height) {
        super(x, y, entityType, width, height);
    }

    public Barbarian(){

    }

    public void drawMini(SpriteBatch batch, int x, int y, int dims, HashMap<String, TextureAtlas> clothes) {
        super.drawMini(batch, x, y, dims, clothes, clotheName);
    }

    public void updateMovement(EntityGroup eg, Map map, ArrayList<Entity> entities, Socket socket, boolean isHost) {
        if (isAlive()) {
            checkIfCanFindAttacker(map, entities);
            if (isAttacking && isInRange() && Entity.haveLineOfSight(this, defender, map)) {
                attack(socket, isHost);
            }
            else if (doingTaskAnimation) {
                doTaskAnimation(map, socket, isHost);
            }
            else if (movingAcrossPath) {
                moveAcrossPath();
            }
            else if (currentTask != null) {
                doingTaskAnimation = true;
                currentTask.startTaskSound(socket, isHost);
            }
            else {
                int choice = random.nextInt(2);
                if (choice == 0) {
                    sabotage(map, entities, socket, isHost);
                }
                else {
                    int x = eg.x + (random.nextInt(eg.radius * 2) - eg.radius);
                    int y = eg.y + (random.nextInt(eg.radius * 2) - eg.radius);
                    if (map.isWithinBounds(x, y)) {
                        if (map.tiles.get(x).get(y).canWalkOn){
                            setMoveToPos(x, y, map, entities);
                        }
                    }
                }
            }
        }
    }

    public void doTaskAnimation(Map map, Socket socket, boolean isHost) {
        System.out.println("doing task animation");
        faceTask();
        currentTask.incrementPercentage();
        if (currentTask.getPercentageComplete() < currentTask.getMaxPercentage()) {
            doingTaskAnimation = true;
        }
        else {
            doingTaskAnimation = false;
            currentTask.completeTask(map, socket);
            currentTask = null;
        }
        completingTask = doingTaskAnimation;
    }

    public void drawTaskPercentageFillBox(ShapeRenderer shapeRenderer){
        if (currentTask != null) {
            if (currentTask.getPercentageComplete() > 0) {
                shapeRenderer.rect(currentTask.getX() * GameScreen.TILE_DIMS + GameScreen.TILE_DIMS * 0.05f + 1,
                        currentTask.getY() * GameScreen.TILE_DIMS + 1 + GameScreen.TILE_DIMS * 0.05f,
                        (GameScreen.TILE_DIMS * 0.9f - 2) * ((currentTask.getPercentageComplete() / currentTask.getMaxPercentage())), GameScreen.TILE_DIMS * 0.2f - 2);
            }
        }
    }

    public void drawTaskPercentageBoundBox(ShapeRenderer shapeRenderer){
        if (currentTask != null) {
            if (currentTask.getPercentageComplete() > 0) {
                shapeRenderer.rect(currentTask.getX() * GameScreen.TILE_DIMS + GameScreen.TILE_DIMS * 0.05f,
                        currentTask.getY() * GameScreen.TILE_DIMS + GameScreen.TILE_DIMS * 0.05f,
                        GameScreen.TILE_DIMS * 0.9f, GameScreen.TILE_DIMS * 0.2f);
            }
        }
    }

    public void sabotage(Map map, ArrayList<Entity> allEntities, Socket socket, boolean isHost) {
        int choice = random.nextInt(101);
        if (choice <= 10){
            map.changeTileType(x, y, "dirt");
        }
        else if (choice <= 21){
            if (!map.tiles.get(x).get(y).hasFireOn){
                map.addFire(x, y, socket, isHost);
            }
        }
        else if (choice <= 31){
            setDefender(findClosestEntity(allEntities, "barbarian"));
        }
        else if (choice <= 41){
            Vector2 loc = findClosestBuilding(map);
            if (loc != null){
                currentTask = new BarbarianTask(this, (int) loc.x, (int) loc.y, "Demolish");
                Vector2 neighbour = Task.getBestNeighbour(map.tiles, (int) loc.x, (int) loc.y, map, allEntities, this);
                pathToComplete = AStar.pathFindForEntities(new Vector2(x, y), neighbour, map.tiles, allEntities, this.entityID);
                movingAcrossPath = pathToComplete.size() > 0;
            }
        }
        else if (choice <= 51){
            Zone zone = map.findNearestZoneToStealFrom(x, y);
            if (zone != null){
                currentTask = new BarbarianTask(this, zone.getX(), zone.getY(), "Steal");
                pathToComplete = AStar.pathFindForEntities(new Vector2(x, y), new Vector2(zone.getX(), zone.getY()), map.tiles, allEntities, this.entityID);
                movingAcrossPath = pathToComplete.size() > 0;
                if (!movingAcrossPath){
                    currentTask = null;
                }
            }
        }
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(BarbarianTask currentTask) {
        this.currentTask = currentTask;
    }

    public void died(){
        if (currentTask != null){
            currentTask = null;
        }
    }
}

