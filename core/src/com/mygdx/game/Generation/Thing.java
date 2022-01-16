package com.mygdx.game.Generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Game.MyGdxGame;

public class Thing {
    public int x;
    public int y;

    public String type;

    int width;
    int height;

    int tileDims;

    int atlasPos = 0;
    float totalTime = 1f;
    float timeCounter = 0;

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

    public void draw(SpriteBatch batch, TextureAtlas textureAtlas) {
        timeCounter += Gdx.graphics.getDeltaTime();

        batch.draw(textureAtlas.findRegion(String.valueOf(atlasPos)), x * tileDims, y * tileDims, width, height);

        if (timeCounter >= totalTime) {
            atlasPos++;
            if (atlasPos > textureAtlas.getRegions().size - 1) {
                atlasPos = 0;
            }
            timeCounter = 0;
        }
    }
}
