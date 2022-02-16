package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class InputButton extends TextButton{
    Texture texture = new Texture("Textures/ui/buttons/InputButton.png");

    int startPos = 0;
    int endPos = 4;
    int currentPos = 4;

    float offset = 0.75f;

    InputProcessor inputProcessor;

    ShapeRenderer shapeRenderer;

    public InputButton(int x, int y, int width, int height, String text, String TextureGda, String name) {
        super(x, y, width, height, text, TextureGda, name);
        setup();
    }

    public InputButton(int x, int y, int width, int height, String text, String name) {
        super(x, y, width, height, text, name);
        setup();
    }

    public void setup(){
        super.setup();
        this.text = "test";
        shapeRenderer = new ShapeRenderer();
        Gdx.input.setInputProcessor(inputProcessor);

        inputProcessor = new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if(character != '\b' && character != '\n' && character != '\r' && character != '\t' && character != '\0' && character != '\f') {
                    text = text.substring(0, currentPos);
                    text += character;
                    if (currentPos < endPos) {
                        text += text.substring(currentPos + 1, endPos);
                    }
                    updateText();
                }
                if(character == 0x08 && currentPos > 0) {
                    endPos--;
                    String temp = text.substring(0, currentPos - 1);
                    text = temp + text.substring(currentPos);
                    currentPos = endPos;
                    if (startPos > 0){
                        startPos--;
                    }
                }
                if (character == 0x25) {
                    System.out.println("c");
                }

                return false;
            }
        };

    }

    public void draw(SpriteBatch batch, int drawLayer){
        if (drawLayer == this.drawLayer){
            batch.draw(texture, x, y, width, height);
            StringBuilder temp = new StringBuilder();
            for (int i = startPos; i < endPos; i++) {
                temp.append(text.charAt(i));
            }
            glyphLayout.setText(font, temp.toString());
            font.draw(batch, glyphLayout, x + width/2f - glyphLayout.width/2, y + height/2f + glyphLayout.height/2);
            batch.end();


            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float tempWidth = glyphLayout.width;
            glyphLayout.setText(font, text.substring(startPos, currentPos));
            float x = glyphLayout.width;
            float heightOffset = 0.6f;
            shapeRenderer.rect(x + this.x + ((width - tempWidth) / 2) + 5, y + (height * ((1 - heightOffset) / 2)), 5, height * heightOffset);
            shapeRenderer.end();

            System.out.println(text + " " + startPos + " " + endPos + " " + currentPos);
            updateLine();

            batch.begin();
        }
    }



    public void updateText(){
        glyphLayout.setText(font, text.substring(startPos, endPos + 1));
        endPos++;
        currentPos++;
        if (glyphLayout.width > width * offset) {
            startPos++;
            glyphLayout.setText(font, text.substring(startPos, endPos));
            while (glyphLayout.width > width * offset) {
                startPos++;
                glyphLayout.setText(font, text.substring(startPos, endPos));
            }
        }
    }

    public void updateLine(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            currentPos --;
            if (currentPos < startPos){
                currentPos = startPos;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            currentPos ++;
            if (currentPos > endPos){
                currentPos = endPos;
            }
        }
    }
}
