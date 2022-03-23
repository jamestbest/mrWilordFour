package com.mygdx.game.Generation.Things;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Screens.GameScreen;

public class AnimatedThings extends Thing{
    int atlasPos = 0;
    final float totalTime = 2f;
    float timeCounter = 0;

    public AnimatedThings(int x, int y, int width, int height, String type, int dims) {
        super(x, y, width, height, type, dims);
    }

    public void draw(SpriteBatch batch, TextureAtlas atlas, int drawLayer) {
        if (drawLayer == this.drawLayer) {
            updateTimer(atlas);
            batch.draw(atlas.findRegion(atlasPos + ""), x * tileDims, y * tileDims, width, height);
        }
    }

    public void drawMini(SpriteBatch batch, TextureAtlas textureAtlas, float x, float y, float width, float height, int drawLayer) {
        if (drawLayer == this.drawLayer) {
            updateTimer(textureAtlas);
            batch.draw(textureAtlas.findRegion(atlasPos + ""), x, y, width, height);
        }
    }

    public void updateTimer(TextureAtlas atlas){
        timeCounter += (Gdx.graphics.getDeltaTime() * GameScreen.gameSpeed);
        if (timeCounter >= totalTime) {
            atlasPos++;
            if (atlasPos > atlas.getRegions().size - 1) {
                atlasPos = 0;
            }
            timeCounter = 0;
        }
    }
}
