package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.w3c.dom.Text;

public class NumberInputWithSides extends NumberInput{
    public NumberInputWithSides(int x, int y, int width, int height, String text, String TextureGda, String name) {
        super(x, y, width, height, text, TextureGda, name);
    }

    public NumberInputWithSides(int x, int y, int width, int height, String text, String name, InputMultiplexer inputMultiplexer, int minValue, int maxValue) {
        super(x, y, width, height, text, name, inputMultiplexer, minValue, maxValue);
        this.text = String.valueOf(maxValue);
    }

    int change = 10;

    Texture plus = new Texture("Textures/ui/buttons/sidePlus.png");
    Texture minus = new Texture("Textures/ui/buttons/sideMinus.png");

    public void draw(SpriteBatch batch){
        super.draw(batch);
        batch.draw(plus, x + (width * 1.1f), y, width, height);
        batch.draw(minus, x - (width * 1.1f), y, width, height);
    }

    public boolean checkIfPressed(int x, int y){
        if (super.checkIfPressed(x, y)){
            typing = true;
            pressed = true;
            return true;
        }
        else if (x > this.x + (width * 1.1f) && x < this.x + (width * 2.1f) + width && y > this.y && y < this.y + height && Gdx.input.isButtonJustPressed(0)){
            if (Integer.parseInt(text) + change <= maxValue) {
                text = Integer.toString(Integer.parseInt(text) + change);
            }
            else {
                text = Integer.toString(maxValue);
            }
            pressed = true;
            return true;
        }
        else if (x > this.x - (width * 1.1f) && x < this.x && y > this.y && y < this.y + height && Gdx.input.isButtonJustPressed(0)){
            if (Integer.parseInt(text) - change >= minValue) {
                text = Integer.toString(Integer.parseInt(text) - change);
            }
            else {
                text = Integer.toString(minValue);
            }
            pressed = true;
            return true;
        }
        return false;
    }
}
