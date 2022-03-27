package com.mygdx.game.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Generation.Map;

import java.util.HashMap;

public class Barbarian extends Entity {
    public Barbarian(int x, int y, String entityType, int width, int height) {
        super(x, y, entityType, width, height);
    }

    public void moveBarbarian(int x, int y) {
        move(x, y);
    }

    public void moveTo(int x, int y, Map map) {
        if (map.isWithinBounds(x, y)) {
            setMoveToPos(x, y, map);
        }
    }

    public void draw(SpriteBatch batch, float tileDims, HashMap<String, TextureAtlas> clothes) {
        updateTimer();
        batch.draw(clothes.get(clotheName).findRegion(direction), (x + ((nextX - x) * timer)) * tileDims, (y + ((nextY - y) * timer)) * tileDims, tileDims, tileDims);
    }
}

