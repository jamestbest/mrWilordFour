package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;

public class ToggleButton extends Button{
    public ToggleButton(int x, int y, int width, int height, String name, String type) {
        super(x, y, width, height, name);
        this.type = type;
        setup();
    }

    public ToggleButton(int x, int y, int width, int height, String name){
        this(x, y, width, height, name, "");
    }

    String type;
    public boolean toggled = true;

    Texture toggledTexture;
    Texture unToggledTexture;

    public void setup(){
        toggledTexture = new Texture("Textures/ui/buttons/SwitchOn" + type + ".png");
        unToggledTexture = new Texture("Textures/ui/buttons/SwitchOff" + type + ".png");
    }

    public void draw(SpriteBatch batch, CameraTwo camera){
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

    public void setSize(int width, int height){
        super.setSize(width / 4, height / 3 * 2);
    }
}
