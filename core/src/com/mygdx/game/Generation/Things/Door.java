package com.mygdx.game.Generation.Things;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;

public class Door extends ConnectedThings{
    public Door(int x, int y, int width, int height, String type, int tileDims) {
        super(x, y, width, height, type, tileDims);
    }

    public Door(Thing t, String type) {
        super(t, type);
    }

    boolean isOpening = false;
    boolean isClosing = false;
    boolean isOpen = false;
    boolean isHorizontal = true;
    int state = 1;
    int fps = 6;
    float tpf = 1f / fps;
    float timeCounter = 0;

    int maxFrame = 17;

    int drawLayer = 2;

    public void draw(SpriteBatch batch, TextureAtlas atlas, int drawLayer){
        if (drawLayer == this.drawLayer) {
            if (isHorizontal) {
                if (isOpen) {
                    batch.draw(atlas.findRegion("Horizontal", 17), x * GameScreen.TILE_DIMS - (width / 2f), y * GameScreen.TILE_DIMS, width * 2, height);
                } else if (isOpening || isClosing) {
                    batch.draw(atlas.findRegion("Horizontal", state), x * GameScreen.TILE_DIMS - (width / 2f), y * GameScreen.TILE_DIMS, width * 2, height);
                } else {
                    batch.draw(atlas.findRegion("Horizontal", 1), x * GameScreen.TILE_DIMS - (width / 2f), y * GameScreen.TILE_DIMS, width * 2, height);
                }
            } else {
                if (isOpen) {
                    batch.draw(atlas.findRegion("Vertical", 17), x * GameScreen.TILE_DIMS, y * GameScreen.TILE_DIMS - (height / 2f), width, height * 2);
                } else if (isOpening || isClosing) {
                    batch.draw(atlas.findRegion("Vertical", state), x * GameScreen.TILE_DIMS, y * GameScreen.TILE_DIMS - (height / 2f), width, height * 2);
                } else {
                    batch.draw(atlas.findRegion("Vertical", 1), x * GameScreen.TILE_DIMS, y * GameScreen.TILE_DIMS - (height / 2f), width, height * 2);
                }
            }
            updateAnimation();
        }
    }

    public void drawMini(SpriteBatch batch, TextureAtlas textureAtlas, float x, float y, float width, float height, int drawLayer) {
        if (drawLayer == this.drawLayer) {
            if (isHorizontal) {
                batch.draw(textureAtlas.findRegion("Horizontal_1"), x, y, width, height);
            } else {
                batch.draw(textureAtlas.findRegion("Vertical_1"), x, y, width, height);
            }
        }
    }

    public void updateAnimation(){
        tpf = 1f / (fps * GameScreen.gameSpeed);
        if (isOpening || isClosing) {
            timeCounter += Gdx.graphics.getDeltaTime();
            if (timeCounter >= tpf) {
                System.out.println(isClosing + " " + isOpening + " " + state);
                timeCounter = 0;
                if (isOpening) {
                    state++;
                }
                if (isClosing) {
                    state--;
                }
                if (state == maxFrame && isOpening) {
                    isOpening = false;
                    isOpen = true;
                    triggerClose();
                }
                if (state == 1 && isClosing) {
                    isClosing = false;
                    isOpen = false;
                }
            }
        }
    }

    public void triggerOpen(){
        if (!isOpening && !isOpen && !isClosing){
            isOpening = true;
            state = 2;
        }
    }

    public void triggerClose(){
        if (!isClosing && isOpen) {
            isClosing = true;
            isOpen = false;
            state = maxFrame - 1;
        }
    }

    public void update(ArrayList<ArrayList<Thing>> things){
        neighborCount = 0;
        int[] n = setNeighbourCount(things);
        boolean isConnected = getIfConnectedNeighbour(n);
        setVertHoriz(n, isConnected, neighborCount);
    }

    public void setVertHoriz(int[] n, boolean isConnected, int nCount){
        if(nCount == 1){
            for (int i = 0; i < n.length; i++) {
                if(n[i] == 1){
                    isHorizontal = i % 2 == 0;
                    return;
                }
            }
        }
        else if(nCount == 2 && !isConnected){
            for (int i = 0; i < n.length; i++) {
                if(n[i] == 1){
                    isHorizontal = i % 2 == 0;
                    return;
                }
            }
        }
        else if(nCount == 2){
            isHorizontal = true;
        }
        else if(nCount == 3){
            int VertCount = 0;
            int HorizCount = 0;
            for (int i = 0; i < n.length; i++) {
                if (n[i] == 1) {
                    if (i % 2 == 1) {
                        VertCount++;
                    }
                    else {
                        HorizCount++;
                    }
                }
            }
            isHorizontal = VertCount <= HorizCount;
        }
        else{
            isHorizontal = true;
        }
    }
}
