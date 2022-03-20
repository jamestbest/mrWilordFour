package com.mygdx.game.Generation.Things;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.ArrayList;

public class Thing {
    public int x;
    public int y;

    public String type;

    public int width;
    public int height;

    int tileDims;

    public boolean canConnect = false;

    public Thing(int x, int y, int width, int height, String type, int tileDims) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.tileDims = tileDims;

        this.type = type;
    }

    public Thing(){

    }

    public void update(ArrayList<ArrayList<Thing>> t){

    }

    public void draw(SpriteBatch batch, TextureAtlas textureAtlas) {
        batch.draw(textureAtlas.findRegion("0"), x * tileDims, y * tileDims, width, height);
    }

    public void drawMini(SpriteBatch batch, TextureAtlas textureAtlas, float x, float y, float width, float height) {
        batch.draw(textureAtlas.findRegion("0"), x, y, width, height);
    }
}
