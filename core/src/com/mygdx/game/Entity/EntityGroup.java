package com.mygdx.game.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EntityGroup {
    public ArrayList<Entity> entities;

    int x = 50;
    int y = 50;
    int radius = 10;

    String entityType;

    public EntityGroup() {
        entities = new ArrayList<>();
    }

    public EntityGroup(String entityType) {
        entities = new ArrayList<>();
        this.entityType = entityType;
    }

    public void add(Entity entity) {
        entities.add(entity);
    }

    public void add(Entity... entities) {
        this.entities.addAll(Arrays.asList(entities));
    }

    public void remove(Entity entity) {
        entities.remove(entity);
    }

    public void moveGroup(Map map){
        for(Entity entity : entities){
            entity.updateMovement(this, map);
        }
    }

    public void draw(SpriteBatch batch, HashMap<String, TextureAtlas> TAs) {
        for (Entity entity : entities) {
            entity.draw(batch, GameScreen.TILE_DIMS, TAs);
        }
    }
}
