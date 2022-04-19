package com.mygdx.game.Lighting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Edge {
    public int startX;
    public int startY;

    public int endX;
    public int endY;

    public int edgeID;

    public Edge(int startX, int startY, int endX, int endY, int edgeID) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.edgeID = edgeID;
    }
    public Edge(int x, int y, int edgeID) {
        this.startX = x;
        this.startY = y;
        this.endX = x;
        this.endY = y;
        this.edgeID = edgeID;
    }

    public void draw(ShapeRenderer shapeRenderer, int tileDims) {
        shapeRenderer.setColor(Color.PURPLE);
        shapeRenderer.line(startX * tileDims, startY * tileDims, endX * tileDims, endY * tileDims);
        shapeRenderer.setColor(Color.BLACK);
//        shapeRenderer.circle(startX * tileDims, startY * tileDims, 2);
//        shapeRenderer.circle(endX * tileDims, endY * tileDims, 2);
    }
}
