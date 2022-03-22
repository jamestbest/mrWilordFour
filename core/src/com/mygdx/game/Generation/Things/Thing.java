package com.mygdx.game.Generation.Things;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Generation.MapComponent;

import java.util.ArrayList;

public class Thing extends MapComponent {
    public int width;
    public int height;

    int tileDims;

    public boolean canConnect = false;

    int drawLayer = 1;

    public Thing(int x, int y, int width, int height, String type, int tileDims) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.tileDims = tileDims;

        this.type = type;
    }

    public Thing(Thing t){
        this.x = t.x;
        this.y = t.y;
        this.width = t.width;
        this.height = t.height;
        this.tileDims = t.tileDims;
    }

    public Thing(){

    }

    public void update(ArrayList<ArrayList<Thing>> t){

    }

    public void draw(SpriteBatch batch, TextureAtlas textureAtlas, int drawLayer) {
        if (this.drawLayer == drawLayer) {
            batch.draw(textureAtlas.findRegion("0"), x * tileDims, y * tileDims, width, height);
        }
    }

    public void drawMini(SpriteBatch batch, TextureAtlas textureAtlas, float x, float y, float width, float height, int drawLayer) {
        if (this.drawLayer == drawLayer) {
            batch.draw(textureAtlas.findRegion("0"), x, y, width, height);
        }
    }
}
