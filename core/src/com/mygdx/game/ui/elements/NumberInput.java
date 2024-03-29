package com.mygdx.game.ui.elements;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NumberInput extends TextButton{
    public NumberInput(int x, int y, int width, int height, String text, String TextureGda, String name) {
        super(x, y, width, height, text, TextureGda, name);
    }

    public NumberInput(int x, int y, int width, int height, String text, String name, InputMultiplexer inputMultiplexer, int minValue, int maxValue) {
        super(x, y, width, height, text, name);
        this.text = text;
        this.minValue = minValue;
        this.maxValue = maxValue;
        setup(inputMultiplexer);
    }

    int maxValue;
    int minValue;

    public void setup(InputMultiplexer inputMultiplexer){
        super.setup();
        inputProcessor = new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if(typing){
                    if (character == '0' || character == '1' || character == '2' || character == '3' || character == '4' || character == '5' || character == '6' || character == '7' || character == '8' || character == '9') {
                        if (text.length() < maxLength) {
                            text += character;
                            if (Integer.parseInt(text) > maxValue) {
                                text = String.valueOf(maxValue);
                            }
                        }
                        if (Integer.parseInt(text) < minValue){
                            text = String.valueOf(minValue);
                        }
                    }
                    if (character == '\b') {
                        if (text.length() > 0) {
                            text = text.substring(0, text.length() - 1);
                        }
                    }
                }
                return false;
            }
        };
        inputMultiplexer.addProcessor(inputProcessor);
    }

    public String text;
    Texture texture = new Texture("Textures/ui/buttons/InputButton.png");

    InputProcessor inputProcessor;

    int maxLength = 3;

    public boolean typing = false;

    public void draw(SpriteBatch batch, int drawLayer){
        if (drawLayer == this.drawLayer) {
            batch.draw(texture, x, y, width, height);

            glyphLayout.setText(font, text);
            font.draw(batch, glyphLayout, x + (width - glyphLayout.width) / 2, y + (height + glyphLayout.height) / 2);

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                typing = false;
                if (text.length() == 0) {
                    text = String.valueOf(minValue);
                }
            }
        }
    }

    public boolean checkIfPressed(int x, int y, boolean firstCheck){
        if (super.checkIfPressed(x, y, firstCheck)) {
            typing = true;
        }
        else {
            if (text.length() == 0) {
                text = String.valueOf(minValue);
            }
            typing = false;
        }
        return typing;
    }
}
