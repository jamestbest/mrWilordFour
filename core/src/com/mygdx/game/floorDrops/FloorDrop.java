package com.mygdx.game.floorDrops;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;
import java.util.HashMap;

public class FloorDrop {
    private int x;
    private int y;

    private String type;
    private String textureType;

    private int stackSize = 1;
    public static final int maxStackSize = 20;

    public boolean isConsumable;

    public static BitmapFont font;
    private GlyphLayout glyphLayout;

    public boolean shouldBeRemoved;

    public FloorDrop(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        glyphLayout = new GlyphLayout(font, stackSize + "");
        this.isConsumable = getConsumabilityFromType(type);
    }

    public FloorDrop(int x, int y, String type, int amount) {
        this(x, y, type);
        stackSize = amount;
    }

    public FloorDrop(){

    }

    public void draw(SpriteBatch batch, HashMap<String, Texture> textures) {
        glyphLayout = new GlyphLayout(font, stackSize + "");
        batch.draw(textures.get(type), x * GameScreen.TILE_DIMS, y * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);

        glyphLayout.setText(font, stackSize + "");
        float x2 = (GameScreen.TILE_DIMS - glyphLayout.width) / 2 + x * GameScreen.TILE_DIMS;
        float y2 = y * GameScreen.TILE_DIMS + glyphLayout.height;
        font.draw(batch, glyphLayout, x2, y2);
    }

    public boolean notAtMax(){
        return stackSize < maxStackSize;
    }

    public boolean notAtMaxWith(int amount){
        return stackSize + amount <= maxStackSize;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTextureType() {
        return textureType;
    }

    public void setTextureType(String textureType) {
        this.textureType = textureType;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    public void incrementStackSize(int amount){
        stackSize += amount;
    }

    public void incrementStackSize(){
        stackSize++;
    }

    public void decrementStackSize(){
        decrementStackSize(1);
    }

    public void decrementStackSize(int amount){
        stackSize -= amount;
        if (stackSize <= 0){
            shouldBeRemoved = true;
        }
    }

    public static boolean getConsumabilityFromType(String type){
        switch (type){
            case "berry", "fish", "meat":
                return true;
            default:
                return false;
        }
    }
}
