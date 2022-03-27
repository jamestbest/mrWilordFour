package com.mygdx.game.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.HashMap;

public class Mob extends Entity {
    public Mob(int x, int y, String entityType, int width, int height) {
        super(x, y, entityType, width, height);
    }

    private String aggroType;

    public String getAggroType() {
        return aggroType;
    }

    public void setAggroType(String aggroType) {
        this.aggroType = aggroType;
    }

    public void draw(SpriteBatch batch, float tileDims, HashMap<String, TextureAtlas> clothes) {
        batch.draw(clothes.get(entityType).findRegion(direction), (x + ((nextX - x) * timer)) * tileDims, (y + ((nextY - y) * timer)) * tileDims, tileDims, tileDims);
        updateTimer();
    }
}
