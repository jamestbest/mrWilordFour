package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.ui.extensions.NotificationCollection;

public class Notification{
    public Notification(int id, String text, String type) {
        this.id = id;
        this.text = text;
        this.type = type;
        setup();
    }

    public Notification(){

    }

//    BitmapFont font;
//    GlyphLayout glyphLayout;

    private String type;
    private String text;
    private int id;

    private int x;
    private int y;

    private int width;
    private int height;

    private int drawLayer = 0;

//    Texture texture;

    public boolean toBeRemoved = false;

    public void setup(){

    }

    public void draw(SpriteBatch batch, int drawLayer, GlyphLayout glyphLayout, BitmapFont font, Texture t){
        if(drawLayer == this.drawLayer){
            batch.draw(t, x, y, width, height);
            glyphLayout.setText(font, text);
            font.setColor(Color.WHITE);
            font.draw(batch, glyphLayout, x + width/2f - glyphLayout.width/2, y + height/2f + glyphLayout.height/2);
        }
    }

    public boolean checkIfPressed(int x, int y){
        return x > this.x && x < this.x + width && y > this.y && y < this.y + height;
    }

    public boolean update(boolean left, boolean right, int x, int y){
        boolean clicked = checkIfPressed(x,y);
        if (clicked) {
            if (right) {
                toBeRemoved = true;
            }
            return left || right;
        }
        return false;
    }

    public void setPos(float x, float y){
        this.x = (int) x;
        this.y = (int) y;
    }

    public void setSize(float width, float height){
        this.width = (int) width;
        this.height = (int) height;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDrawLayer() {
        return drawLayer;
    }

    public void setDrawLayer(int drawLayer) {
        this.drawLayer = drawLayer;
    }
}
