package com.mygdx.game.Entity;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Math.Math;
import com.mygdx.game.Screens.GameScreen;
import com.mygdx.game.Weapons.Weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Entity {
    protected int x;
    protected int y;
    protected int nextX;
    protected int nextY;
    public ArrayList<Vector2> pathToComplete = new ArrayList<>();

    protected float timer = 0f;
    protected float timerMax = 1f;

    protected String clotheName;
    protected String direction = "front";

    protected String entityType;

    protected int width;
    protected int height;

    protected int health;

    protected Weapon weapon;

    boolean movingAcrossPath = false;
    int randomMoveRadius = 25;

    public static HashMap<String, Integer> typeToHealth = new HashMap<>();
    public static Random random = new Random();

    boolean isAttacking;
    Entity defender;
    ArrayList<Entity> attackers = new ArrayList<>();

    public Entity(int x, int y, String entityType, int width, int height) {
        this.x = x;
        this.y = y;
        this.nextX = x;
        this.nextY = y;
        this.entityType = entityType;
        this.width = width;
        this.height = height;
        this.health = getHealthFromType(entityType);
    }

    public Entity(){

    }

    public void update(float delta) {

    }

    public void draw(SpriteBatch batch, float tileDims, HashMap<String, TextureAtlas> clothes) {
        batch.draw(clothes.get(entityType).findRegion(direction), (x + ((nextX - x) * timer)) * tileDims, (y + ((nextY - y) * timer)) * tileDims, tileDims, tileDims);
        updateTimer();
    }


    public void drawMini(SpriteBatch batch, int x, int y, int dims, HashMap<String, TextureAtlas> clothes) {
        batch.draw(clothes.get(clotheName).findRegion("front"), x, y, dims, dims);
    }

    public void move(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public void updateTimer(){
        weapon.updateTimers(Gdx.graphics.getDeltaTime() * GameScreen.gameSpeed);
        timer += Gdx.graphics.getDeltaTime() * GameScreen.gameSpeed;
        if (timer >= timerMax) {
            x = nextX;
            y = nextY;
            timer = 0f;
        }
        updateDirection();
    }

    public void updateDirection() {
        if (nextX > x) {
            direction = "right";
        } else if (nextX < x) {
            direction = "left";
        }
        if (nextY > y) {
            direction = "back";
        } else if (nextY < y) {
            direction = "front";
        }
    }

    public void updateMovement(EntityGroup eg, Map map){
        if (isAlive()) {
            if (isAttacking && defender == null) {
                defender = findClosestAttacker();
            }
            if (isAttacking && !isNeighbouringDefender() && !isInRange()) {
                setMoveToPos(defender.x, defender.y, map);
            }
            if (isAttacking && isInRange() && Entity.haveLineOfSight(this, defender, map)) {
                attack();
            }
            else if (movingAcrossPath) {
                moveAcrossPath();
            } else {
                setMoveToPos(eg.x + (random.nextInt(eg.radius * 2) - eg.radius), eg.y + (random.nextInt(eg.radius * 2) - eg.radius), map);
            }
        }
    }

    public void moveAcrossPath(){
        if (pathToComplete.size() > 0) {
            Vector2 nextTile = pathToComplete.get(0);
            if (nextTile.x == x && nextTile.y == y) {
                pathToComplete.remove(0);
            }
        } else {
            movingAcrossPath = false;
        }
        if (pathToComplete.size() >= 1) {
            nextX = (int) pathToComplete.get(0).x;
            nextY = (int) pathToComplete.get(0).y;
        }
    }

    public void moveRandomly(Map map) {
        int randomX = random.nextInt(3) - 1;
        int randomY = random.nextInt(3) - 1;

        if (map.isWithinBounds(randomX + x, randomY + y)) {
            if (map.tiles.get(x + randomX).get(y + randomY).canWalkOn) {
                nextX = x + randomX;
                nextY = y + randomY;
            }
        }
    }

    public void setMoveToPos(int x, int y, Map map) {
        pathToComplete = AStar.pathFindForColonist(new Vector2(this.x, this.y), new Vector2(x, y), map.tiles);
        movingAcrossPath = pathToComplete.size() > 0;
    }

    public void getRandomPosition(Map map) {
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

        pathToComplete = AStar.pathFindForColonist(new Vector2(x, y), new Vector2(randomX + x, randomY + y), map.tiles);

        movingAcrossPath = pathToComplete.size() > 0;
    }

    public Vector2 getPosInRange(Map map) {
        int randomX = random.nextInt(randomMoveRadius * 2);
        int randomY = random.nextInt(randomMoveRadius * 2);

        randomX -= randomMoveRadius;
        randomY -= randomMoveRadius;
        while (!map.isWithinBounds(x + randomX, y + randomY)) {
            randomX = random.nextInt(randomMoveRadius * 2);
            randomY = random.nextInt(randomMoveRadius * 2);

            randomX -= randomMoveRadius;
            randomY -= randomMoveRadius;
        }
        return new Vector2(randomX, randomY);
    }

    public static Integer getHealthFromType(String entityType) {
        return typeToHealth.get(entityType);
    }

    public static void setHealthFromType() {
        typeToHealth.put("barbarian", 90);
        typeToHealth.put("colonist", 150);
        typeToHealth.put("pig", 20);
        typeToHealth.put("sheep", 25);
        typeToHealth.put("wolf", 50);
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

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getNextX() {
        return nextX;
    }

    public void setNextX(int nextX) {
        this.nextX = nextX;
    }

    public int getNextY() {
        return nextY;
    }

    public void setNextY(int nextY) {
        this.nextY = nextY;
    }

    public float getTimer() {
        return timer;
    }

    public void setTimer(float timer) {
        this.timer = timer;
    }

    public float getTimerMax() {
        return timerMax;
    }

    public void setTimerMax(float timerMax) {
        this.timerMax = timerMax;
    }

    public String getClotheName() {
        return clotheName;
    }

    public void setClotheName(String clotheName) {
        this.clotheName = clotheName;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public boolean attack(Entity defender, Entity attacker) {
        return weapon.attack(defender, attacker);
    }

    public boolean isAlive(){
        return health > 0;
    }

    public static boolean haveLineOfSight(Entity one, Entity two, Map map) {
        if (one.getX() == two.getX() && one.getY() != two.getY()) {
            for (int i = java.lang.Math.min(two.getY(), one.getY()); i < two.getY() - one.getY(); i++) {
                if (!map.tiles.get(one.getX()).get(one.getY() + i).canWalkOn) {
                    return false;
                }
            }
        }
        if (one.getY() == two.getY() && one.getX() != two.getX()) {
            for (int i = java.lang.Math.min(two.getX(), one.getX()); i < two.getX() - one.getX(); i++) {
                if (!map.tiles.get(one.getX() + i).get(one.getY()).canWalkOn) {
                    return false;
                }
            }
        }
        if (one.getX() != two.getX() && one.getY() != two.getY()) {
            int x = one.getX();
            int y = one.getY();
            int x2 = two.getX();
            int y2 = two.getY();
            int xDiff = java.lang.Math.abs(x2 - x);
            int yDiff = java.lang.Math.abs(y2 - y);
            int xDir = x2 - x;
            int yDir = y2 - y;
            int xInc = xDir / xDiff;
            int yInc = yDir / yDiff;
            for (int i = 0; i < xDiff; i++) {
                x += xInc;
                y += yInc;
                if (map.isWithinBounds(x, y)) {
                    if (!map.tiles.get(x).get(y).canWalkOn) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void setDefender(Entity defender){
        this.defender = defender;
        defender.addAttacker(this);
        isAttacking = true;
    }

    public void addAttacker(Entity e){
        isAttacking = true;
        attackers.add(e);
    }

    public void removeAttacker(Entity e){
        attackers.remove(e);
    }

    public boolean isInRange(){
        return Math.abs(defender.x - x) <= weapon.getRange() && Math.abs(defender.y - y) <= weapon.getRange();
    }

    public boolean isNeighbouringDefender(){
        return Math.abs(defender.x - x) <= 1 && Math.abs(defender.y - y) <= 1;
    }

    public boolean attack() {
        if (!defender.isAlive()) {
            isAttacking = false;
            defender = null;
            return false;
        }
        return weapon.attack(defender, this);
    }

    public void drawPathOutline(ShapeRenderer shapeRenderer) {
        for (Vector2 v : pathToComplete) {
            shapeRenderer.rect(v.x * GameScreen.TILE_DIMS, v.y * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
        }
    }

    public Entity findClosestAttacker(){
        float minDist = Float.MAX_VALUE;
        Entity closest = null;
        for (Entity e : attackers){
            float dist = Math.abs(e.x - x) + Math.abs(e.y - y);
            if (dist < minDist){
                minDist = dist;
                closest = e;
            }
        }
        this.isAttacking = closest != null;
        return closest;
    }
}
