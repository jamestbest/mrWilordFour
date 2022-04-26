package com.mygdx.game.floorDrops;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Entity.Entity;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;
import java.util.HashMap;

public class Zone {
    private int x;
    private int y;

    private int width;
    private int height;

    private Color color;

    ArrayList<FloorDrop> drops;

    int zoneId;

    public static BitmapFont font;
    private GlyphLayout glyphLayout;

    public Zone(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new Color(0.5f, 0.5f, 0.5f, 1);
        this.drops = new ArrayList<>();
    }

    public Zone(int x, int y, int width, int height, Color color) {
        this(x, y, width, height);
        this.color = color;
    }

    public Zone(){
        
    }

    public void setup(int zoneId){
        glyphLayout = new GlyphLayout();
        this.zoneId = zoneId;
        glyphLayout.setText(font, "Zone " + zoneId);
    }

    public void draw(ShapeRenderer shapeRenderer, Map map){
        shapeRenderer.setColor(color.r, color.g, color.b, 0.5f);
        int dims = (int) GameScreen.TILE_DIMS;
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (map.tiles.get(i).get(j).canSpawnOn){
                    shapeRenderer.rect(i * dims, j * dims, dims, dims);
                }
            }
        }
    }

    public void drawText(SpriteBatch batch){
        float x2 = (x + width / 2f) * GameScreen.TILE_DIMS - (int) glyphLayout.width / 2f;
        float y2 = (y + height / 2f) * GameScreen.TILE_DIMS + (int) glyphLayout.height / 2f;
        font.draw(batch, glyphLayout, x2, y2);
    }

    public void drawDrops(SpriteBatch batch, HashMap<String, Texture> textures){
        for (FloorDrop f : drops) {
            f.draw(batch, textures);
        }
        checkForRemoval();
    }

    public void checkForRemoval(){
        drops.removeIf(f -> f.shouldBeRemoved);
    }

    public Vector2 getBestTile(FloorDrop drop, Map map, int eX, int eY){
        Vector2 best = null;
        float lowestDist = Integer.MAX_VALUE;

        for (FloorDrop f : drops) {
            if (f.getType().equals(drop.getType()) && f.notAtMaxWith(drop.getStackSize())){
                float dist = distance(new Vector2(eX, eY), new Vector2(f.getX(), f.getY()));
                if (dist < lowestDist){
                    lowestDist = dist;
                    best = new Vector2(f.getX(), f.getY());
                }
            }
        }

        if (best != null){
            return best;
        }

        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (map.tiles.get(i).get(j).canSpawnOn){
                    if (!hasDropHere(i,j)){
                        if (distance(new Vector2(eX, eY), new Vector2(i, j)) < lowestDist){
                            lowestDist = distance(new Vector2(eX, eY), new Vector2(i, j));
                            best = new Vector2(i, j);
                        }
                    }
                }
            }
        }
        return best;
    }

    public float distance(Vector2 v, Vector2 v2){
        return (float) Math.sqrt(Math.pow(v.x - v2.x, 2) + Math.pow(v.y - v2.y, 2));
    }

    public float distance(Vector2 v){
        int x2 = x + width / 2;
        int y2 = y + height / 2;
        return (float) Math.sqrt(Math.pow(v.x - x2, 2) + Math.pow(v.y - y2, 2));
    }

    public boolean hasRoom(String type, Map map, int amount){
        int totalTiles = numberOfStacksAvailible(map);
        if (totalTiles > 0){
            for (FloorDrop f : drops) {
                if (f.getType().equals(type)){
                    if (f.notAtMaxWith(amount)){
                        return true;
                    }
                }
            }
        }
        return totalTiles > drops.size() - 1;
    }

    public int numberOfStacksAvailible(Map map){
        int amount = 0;
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (map.tiles.get(i).get(j).canSpawnOn){
                    amount++;
                }
            }
        }
        return amount;
    }

    public boolean isInZone(int x, int y, Map map){
        if (map.isWithinBounds(x, y)) {
            boolean isInArea = (x >= this.x && x < this.x + width && y >= this.y && y < this.y + height);
            boolean isAllowed = map.tiles.get(x).get(y).canSpawnOn;
            return isInArea && isAllowed;
        }
        return false;
    }

    public void addFloorDrop(FloorDrop fadd, Map map, int eX, int eY){
        Vector2 best = getBestTile(fadd, map, eX, eY);
        if (best != null){
            if (!hasSameTypeHere((int) best.x, (int) best.y, fadd.getType())){
                fadd.setX((int) best.x);
                fadd.setY((int) best.y);
                drops.add(fadd);
            }
            else {
                getDropAt((int) best.x, (int) best.y).incrementStackSize(fadd.getStackSize());
            }
            map.removeFloorDrop(fadd);
        }
    }

    public boolean hasSameTypeHere(int x, int y, String type){
        for (FloorDrop f : drops) {
            if (f.getX() == x && f.getY() == y && f.getType().equals(type)){
                return true;
            }
        }
        return false;
    }

    public FloorDrop getDropAt(int x, int y){
        for (FloorDrop f : drops) {
            if (f.getX() == x && f.getY() == y){
                return f;
            }
        }
        return null;
    }

    public boolean hasDropHere(int x, int y){
        return getDropAt(x, y) != null;
    }

    public void calculateResources(HashMap<String, Integer> resources){
        for (FloorDrop f : drops) {
            resources.replace(f.getType(), resources.get(f.getType()) + f.getStackSize());
        }
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public ArrayList<FloorDrop> getDrops(){
        return drops;
    }

    public int getAmountOfResource(String type){
        int amount = 0;
        for (FloorDrop f : drops) {
            if (f.getType().equals(type)){
                amount += f.getStackSize();
            }
        }
        return amount;
    }

    public void decrementResource(String resource, int amount){
        for (FloorDrop f : drops) {
            if (f.getType().equals(resource)){
                int removedAmount = f.decrementStackSize(amount);
                amount -= removedAmount;
                if (amount <= 0){
                    return;
                }
            }
        }
    }

    public void decrementResource(String resource){
        decrementResource(resource, 1);
    }

    public boolean hasAnyThing(){
        return !drops.isEmpty();
    }

    public ArrayList<String> getResources(){
        ArrayList<String> resources = new ArrayList<>();
        for (FloorDrop f : drops) {
            if (!resources.contains(f.getType())){
                resources.add(f.getType());
            }
        }
        return resources;
    }
}
