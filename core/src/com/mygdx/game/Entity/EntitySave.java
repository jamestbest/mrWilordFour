package com.mygdx.game.Entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Weapons.Weapon;

public class EntitySave extends Entity {
    public int defenderID = -1;
    public int[] attackers = null;

    public boolean hasPath = false;
    public Vector2 pathEnd = null;


}
