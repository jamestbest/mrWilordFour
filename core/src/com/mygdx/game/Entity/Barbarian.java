package com.mygdx.game.Entity;

import com.mygdx.game.Generation.Map;

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
}

