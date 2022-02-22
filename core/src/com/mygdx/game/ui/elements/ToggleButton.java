package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ToggleButton extends Button{
    public ToggleButton(int x, int y, int width, int height, String name, String type) {
        super(x, y, width, height, name);
        this.type = type;
        setup();
    }

    public ToggleButton(int x, int y, int width, int height, String name, boolean startValue){
        this(x, y, width, height, name, "");
        toggled = startValue;
    }

    public ToggleButton(String name) {
        this(0, 0, 0, 0, name, false);
    }

    public ToggleButton(String name, boolean startValue){
        this(0, 0, 0, 0, name, startValue);
    }

    String type;
    public boolean toggled = false;

    Texture toggledTexture;
    Texture unToggledTexture;

    public void setup(){
        toggledTexture = new Texture("Textures/ui/buttons/SwitchOn" + type + ".png");
        unToggledTexture = new Texture("Textures/ui/buttons/SwitchOff" + type + ".png");
    }

    public void draw(SpriteBatch batch, int drawLayer){
        if (drawLayer == this.drawLayer) {
            batch.draw(toggled ? toggledTexture : unToggledTexture, x, y, width, height);
        }
    }

    public boolean checkIfPressed(int x, int y){
        if (Gdx.input.isButtonJustPressed(0)){
            if (super.checkIfPressed(x, y)){
                toggled = !toggled;
                return true;
            }
        }
        return false;
    }

    public void setSize(int width, int height){
        super.setSize(width / 4, height / 3 * 2);
    }
}
