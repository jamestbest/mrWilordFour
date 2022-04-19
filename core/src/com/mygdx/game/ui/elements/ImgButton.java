package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ImgButton extends Button {
    String textureName;
    Texture texture;

    Runnable r;

    public boolean drawButton = true;

    public ImgButton(int x, int y, int width, int height, String Gda, String name, String textureName) {
        super(x, y, width, height, Gda, name);
        this.textureName = textureName;
        setup();
    }

    public ImgButton(int x, int y, int width, int height, String name, String textureName) {
        super(x, y, width, height, name);
        this.textureName = textureName;
        setup();
    }

    public ImgButton(String name, String textureName) {
        super(name);
        this.textureName = textureName;
        setup();
    }

    public ImgButton(String name, String textureName, Runnable r) {
        super(name);
        this.textureName = textureName;
        this.r = r;
        setup();
    }

    public void setup(){
        texture = new Texture("Textures/ui/imgButtons/" + textureName + ".png");
    }

    public void draw(SpriteBatch batch, int drawLayer){
        if (drawButton) {
            super.draw(batch, drawLayer);
        }
        batch.draw(texture, x, y, width, height);
    }

    public boolean checkIfPressed(int x, int y, boolean firstCheck){
        if(visible){
            if (x > this.x && x < this.x + width && y > this.y && y < this.y + height) {
                pressed = true;
                if (firstCheck) {
                    clicked();
                }
                return true;
            }
        }
        return false;
    }

    public void clicked(){
        if(r != null) r.run();
    }
}
