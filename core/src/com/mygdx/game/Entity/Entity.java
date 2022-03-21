package com.mygdx.game.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Generation.Map;
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

    public Entity(int x, int y, String entityType, int width, int height) {
        this.x = x;
        this.y = y;
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
        batch.draw(clothes.get(clotheName).findRegion(direction), (x + ((nextX - x) * timer)) * tileDims, (y + ((nextY - y) * timer)) * tileDims, tileDims, tileDims);
    }


    public void drawMini(SpriteBatch batch, int x, int y, int dims, HashMap<String, TextureAtlas> clothes) {
        batch.draw(clothes.get(clotheName).findRegion("front"), x, y, dims, dims);
    }

    public void move(int x, int y) {
        this.x += x;
        this.y += y;
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
        typeToHealth.put("Gunner", 90);
        typeToHealth.put("Melee", 150);
        typeToHealth.put("Ranger", 60);
        typeToHealth.put("Pig", 20);
        typeToHealth.put("Sheep", 25);
        typeToHealth.put("Wolf", 50);
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

    public boolean attack(Entity defender) {
        if (weapon.getCurrentCooldown() == 0) {
            weapon.setCurrentCooldown(weapon.getCooldown());
            if (random.nextInt(100) <= weapon.getAccuracy()) {
                defender.setHealth(defender.getHealth() - weapon.getDamage());
                return true;
            }
        }
        return false;
    }
}
