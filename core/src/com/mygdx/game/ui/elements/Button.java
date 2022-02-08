package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;

import java.util.Vector;

public class Button {
    public int x;
    public int y;

    public int width;
    public int height;

    public String Gda; //general directory addition

    Texture pressedTexture;
    Texture unpressedTexture;

    public boolean pressed;
    public boolean visible = true;

    public String name;

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

    public void draw(SpriteBatch batch, CameraTwo camera) {
        if (visible) {
            if(pressed){
                batch.draw(pressedTexture, x + ((camera.position.x) - (camera.width / 2f)) * camera.zoom, y + ((camera.position.y) - (camera.height / 2f)) * camera.zoom, width , height);
            }
            else{
                batch.draw(unpressedTexture, x + ((camera.position.x) - (camera.width / 2f * camera.zoom)) - (width * (1 - camera.zoom)),
                        y + ((camera.position.y) - (camera.height / 2f * camera.zoom)),
                        width * camera.zoom,
                        height * camera.zoom);
            }
        }
    }

    public boolean checkIfPressed(int x, int y){
        if(visible){
            if (x > this.x && x < this.x + width && y > this.y && y < this.y + height) {
                pressed = true;
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

    public void setSize(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void centre(int offsetY){
        x = (Gdx.graphics.getWidth() - width) / 2;
        y = (Gdx.graphics.getHeight() - height) / 2 + offsetY;
    }
}
