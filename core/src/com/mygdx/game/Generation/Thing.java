package com.mygdx.game.Generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Game.MyGdxGame;

import java.util.ArrayList;

public class Thing {
    public int x;
    public int y;

    public String type;

    public int width;
    public int height;

    int tileDims;

    boolean canConnect = false;

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

    public void drawMini(SpriteBatch batch, TextureAtlas textureAtlas, int x, int y, int width, int height) {
        batch.draw(textureAtlas.findRegion("0"), x, y, width, height);
    }
}
