package com.mygdx.game.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
}
