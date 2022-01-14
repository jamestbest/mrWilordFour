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

    String type;
    boolean toggled = false;

    Texture toggledTexture;
    Texture unToggledTexture;

    public void setup(){
        toggledTexture = new Texture("Textures/ui/buttons/SwitchOn" + type + ".png");
        unToggledTexture = new Texture("Textures/ui/buttons/SwitchOff" + type + ".png");
    }

    public void draw(SpriteBatch batch){
        batch.draw(toggled ? toggledTexture : unToggledTexture, x, y, width, height);
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
}
