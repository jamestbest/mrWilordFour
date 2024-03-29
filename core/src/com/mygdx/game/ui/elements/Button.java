package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.MyGdxGame;

public class Button {
    public int x;
    public int y;

    public int width;
    public int height;

    public String Gda; //general directory addition

    Texture pressedTexture;
    Texture unpressedTexture;

    public boolean pressed;
    public boolean selected;
    public boolean visible = true;

    public String name;

    public int drawLayer = 0;
    public int pressedLayer = 0;
    public boolean wantsSingleCheck = false;

    protected Runnable r;

    public Button(int x, int y, int width, int height, String Gda, String name) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.Gda = Gda;
        this.name = name;

        pressedTexture = new Texture("Textures/ui/buttons/" + Gda + "Pressed.png");
        unpressedTexture = new Texture("Textures/ui/buttons/" + Gda + "UnPressed.png");
    }

    public Button(int x, int y, int width, int height, String name) {
        this(x, y, width, height, "BlueButton", name);
    }

    public Button(String name){
        this(0, 0, 0, 0, "BlueButton", name);
    }

    public Button(String name, String gda, Runnable r){
        this(0, 0, 0, 0, gda, name);
        this.r = r;
    }

    public void draw(SpriteBatch batch, int drawLayer) {
        if(drawLayer == this.drawLayer){
            if (visible) {
                if(pressed){
                    batch.draw(pressedTexture, x, y, width, height);
                }
                else{
                    batch.draw(unpressedTexture, x, y, width, height);
                }
            }
        }
    }

    public boolean checkIfPressed(int x, int y, boolean firstCheck){
        if(visible){
            if (x > this.x && x < this.x + width && y > this.y && y < this.y + height) {
                pressed = true;
                if (firstCheck && r != null) {
                    r.run();
                }
                return true;
            }
        }
        return false;
    }

    public void setPos(Vector2 pos){
        x = (int)pos.x;
        y = (int)pos.y;
    }

    public void setPos(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void setPos(float x, float y){
        this.x = (int) x;
        this.y = (int) y;
    }

    public void setSize(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void setSize(float width, float height){
        this.width = (int) width;
        this.height = (int) height;
    }

    public void centre(int offsetY){
        x = (int) ((MyGdxGame.initialRes.x - width) / 2);
        y = (int) ((MyGdxGame.initialRes.y - height) / 2 + offsetY);
    }

    public void setSelected(boolean selected){
        this.selected = selected;
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
}
