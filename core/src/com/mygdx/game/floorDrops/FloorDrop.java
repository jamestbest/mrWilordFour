package com.mygdx.game.floorDrops;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.HashMap;

public class FloorDrop {
    protected int x;
    protected int y;

    protected String type;
    protected String textureType;

    public FloorDrop(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void draw(SpriteBatch batch, HashMap<String, TextureAtlas> textures) {
        batch.draw(textures.get(type).findRegion(textureType), x, y);
    }
}
