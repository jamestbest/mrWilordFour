package com.mygdx.game.Generation.Things;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Screens.GameScreen;

import java.util.ArrayList;

public class ConnectedThings extends Thing{
    public ConnectedThings(int x, int y, int width, int height, String type, int tileDims){
        super(x,y,width,height,type,tileDims);
        canConnect = true;
    }

    public ConnectedThings(Thing t, String type){
        super(t.x, t.y, t.width, t.height, type, t.tileDims);
        canConnect = true;
    }

    String drawType; //used to decide which texture to use based on where the neighbours are
    int neighborCount = 0;
    public int rotation;

    public int[] setNeighbourCount(ArrayList<ArrayList<Thing>> thingsArray) {
        int[] n = new int[4];

        if (isInBounds(x + 1, y)){
            if (thingsArray.get(x + 1).get(y).canConnect) {
                neighborCount++;
                n[2] = 1;
            }
        }
        if (isInBounds(x - 1, y)) {
            if (thingsArray.get(x - 1).get(y).canConnect) {
                neighborCount++;
                n[0] = 1;
            }
        }
        if (isInBounds(x, y + 1)) {
            if (thingsArray.get(x).get(y + 1).canConnect) {
                neighborCount++;
                n[1] = 1;
            }
        }
        if (isInBounds(x, y - 1)) {
            if (thingsArray.get(x).get(y - 1).canConnect) {
                neighborCount++;
                n[3] = 1;
            }
        }
        return n;
    }

    public boolean getIfConnectedNeighbour(int[] n)
    {
        if (n[0] == 1 && n[1] == 1){
            return true;
        }
        if (n[0] == 1 && n[3] == 1){
            return true;
        }
        if (n[2] == 1 && n[1] == 1){
            return true;
        }
        return n[2] == 1 && n[3] == 1;
    }

    public boolean isInBounds(int x, int y){
        return x >= 0 && x <= GameScreen.TILES_ON_X - 1 && y >= 0 && y <= GameScreen.TILES_ON_X - 1;
    }

    public void setDrawType(boolean connectedNeighbour){
        if (neighborCount == 1){
            drawType = "three";
        }
        else if (neighborCount == 2){
            if (connectedNeighbour){
                drawType = "twoConnected";
            }
            else{
                drawType = "twoUnconnected";
            }
        }
        else if (neighborCount == 3){
            drawType = "one";
        }
        else if (neighborCount == 4){
            drawType = "zero";
        }
        else {
            drawType = "four";
        }
    }

    public void setRotation(int[] neighbors){
        switch (drawType) {
            case "one":
                for (int i = 0; i < neighbors.length; i++) {
                    if (neighbors[i] == 0) {
                        rotation = 360 - (i * 90);
                        break;
                    }
                }
                break;
            case "twoConnected":
                for (int i = 0; i < neighbors.length; i++) {
                    if (neighbors[i] == 0) {
                        if (neighbors[(i + 1) % neighbors.length] == 0) {
                            rotation = 360 - (i * 90);
                        }
                    }
                }
                break;
            case "twoUnconnected":
                for (int i = 0; i < neighbors.length / 2; i++) {
                    if (neighbors[i] == 1) {
                        rotation = 90 - (i * 90);
                    }
                }
                break;
            case "three":
                for (int i = 0; i < neighbors.length; i++) {
                    if (neighbors[i] == 1) {
                        rotation =  360 - (i * 90) - 90;
                    }
                }
                break;
            case "four":
                rotation = 0;
                break;
        }
    }

    public void draw(SpriteBatch batch, TextureAtlas textureAtlas, int drawLayer){
        if (drawLayer == this.drawLayer) {
            batch.draw(textureAtlas.findRegion(drawType), x * GameScreen.TILE_DIMS, y * GameScreen.TILE_DIMS,
                    GameScreen.TILE_DIMS / 2, GameScreen.TILE_DIMS / 2, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, 1, 1, rotation);
        }
    }

    public void drawMini(SpriteBatch batch, TextureAtlas textureAtlas, float x, float y, float width, float height, int drawLayer) {
        if (drawLayer == this.drawLayer) {
            batch.draw(textureAtlas.findRegion(drawType), x, y, width / 2f, height / 2f, width, height, 1, 1, rotation);
        }
    }

    public void update(ArrayList<ArrayList<Thing>> thingsArray){
        neighborCount = 0;
        int[] n = setNeighbourCount(thingsArray);
        setDrawType(getIfConnectedNeighbour(n));
        setRotation(n);
    }
}
