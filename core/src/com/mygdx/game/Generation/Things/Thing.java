package com.mygdx.game.Generation.Things;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Generation.MapComponent;
import com.mygdx.game.Lighting.Light;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;

public class Thing extends MapComponent {
    public int width;
    public int height;

    int tileDims;

    public boolean canConnect = false;

    public int drawLayer = 1;

    public boolean emitsLight = false;
    public boolean hasBeenSetup = false;
    public String lightTextureName = "default";

    public boolean builtByColonist;

    public Thing(int x, int y, int width, int height, String type, int tileDims) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.tileDims = tileDims;

        this.type = type;
        setup();
    }

    public Thing(Thing t){
        this.x = t.x;
        this.y = t.y;
        this.width = t.width;
        this.height = t.height;
        this.tileDims = t.tileDims;
        setup();
    }

    public Thing(Thing t, String type){
        this(t);
        this.type = type;
        setup();
    }

    public Thing(){

    }

    public void setup(){
        if (emitsLight){
            GameScreen.lightManager.addLight(new Light((int) (x * GameScreen.TILE_DIMS + (GameScreen.TILE_DIMS / 2f)),
                    (int) (y * GameScreen.TILE_DIMS + (GameScreen.TILE_DIMS / 2f)), (int) (GameScreen.TILE_DIMS * GameScreen.TILES_ON_X), lightTextureName));
            hasBeenSetup = true;
        }
    }

    public void update(ArrayList<ArrayList<Thing>> t){

    }

    public void updateDims(){
        Vector2 mults = GameScreen.getMultiplierFromThings(this.type);
        this.width = (int) (mults.x * GameScreen.TILE_DIMS);
        this.height = (int) (mults.y * GameScreen.TILE_DIMS);
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
