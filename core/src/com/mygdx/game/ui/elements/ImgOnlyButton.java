package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.lang.reflect.Method;
import java.util.function.Function;

public class ImgOnlyButton extends Button{
    public ImgOnlyButton(int x, int y, int width, int height, String Gda, String name) {
        super(x, y, width, height, Gda, name);
        setup();
    }

    public ImgOnlyButton(int x, int y, int width, int height, String name) {
        super(x, y, width, height, name);
        setup();
    }

    public ImgOnlyButton(String name, String Gda) {
        super(name);
        this.Gda = Gda;
        setup();
    }

    public ImgOnlyButton(String name, String Gda, Runnable r) {
        super(name);
        this.Gda = Gda;
        onClick = r;
        setup();
    }

    public void setup(){
        texture = new Texture("Textures/ui/buttons/" + Gda + ".png");
        wantsSingleCheck = true;
    }

    Texture texture;

    Runnable onClick;

    public void draw(SpriteBatch batch, int drawLayer){
        if(drawLayer == this.drawLayer) {
            batch.draw(texture, x, y, width, height);
        }
    }

    public void clicked(){
        if(onClick != null) onClick.run();
    }

    public boolean checkIfPressed(int x, int y, boolean firstCheck){
        if(visible){
            if (x > this.x && x < this.x + width && y > this.y && y < this.y + height) {
                pressed = true;
                clicked();
                return true;
            }
        }
        return false;
    }

    public void setSize(int width, int height){
        this.width = (int) (width * 0.2f);
        this.height = (int) (height * 0.4f);
    }
}
