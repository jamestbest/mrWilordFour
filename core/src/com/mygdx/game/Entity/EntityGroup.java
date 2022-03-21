package com.mygdx.game.Entity;

import java.util.ArrayList;
import java.util.Arrays;

public class EntityGroup {
    public ArrayList<Entity> entities;

    public EntityGroup() {
        entities = new ArrayList<>();
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
}
