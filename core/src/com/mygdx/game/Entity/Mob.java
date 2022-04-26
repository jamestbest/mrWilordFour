package com.mygdx.game.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Screens.GameScreen;

import java.util.HashMap;

public class Mob extends Entity {
    public Mob(int x, int y, String entityType, int width, int height) {
        super(x, y, entityType, width, height);
    }

    public Mob(){

    }

    public boolean hasDroppedFood = false;

    public void draw(SpriteBatch batch, float tileDims, HashMap<String, TextureAtlas> clothes) {
        super.draw(batch, tileDims, clothes, entityType);
    }

    public void drawMini(SpriteBatch batch, int x, int y, int dims, HashMap<String, TextureAtlas> clothes) {
        super.drawMini(batch, x, y, dims, clothes, entityType);
    }

    public void died(){
        GameScreen.updateMobDrops = true;
    }

    public int getHealth(){
        return health;
    }

    public void setHealth(int health){
        this.health = health;
        if (this.health <= 0) {
            this.health = 0;
            died();
        }
        if (this.health > getMaxHealth()) {
            this.health = getMaxHealth();
        }
    }
}
