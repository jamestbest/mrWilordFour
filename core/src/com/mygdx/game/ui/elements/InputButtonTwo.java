package com.mygdx.game.ui.elements;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class InputButtonTwo extends TextButton{
    public InputButtonTwo(int x, int y, int width, int height, String text, String TextureGda, String name) {
        super(x, y, width, height, text, TextureGda, name);
    }

    public InputButtonTwo(String text, String name, InputMultiplexer inputMultiplexer) {
        super(0, 0, 0, 0, text, name);
        this.text = text;
        wantsSingleCheck = true;
        setup(inputMultiplexer);
    }

    public InputButtonTwo(int x, int y, int width, int height, String text, String name, InputMultiplexer inputMultiplexer) {
        super(x, y, width, height, text, name);
        this.text = text;
        wantsSingleCheck = true;
        setup(inputMultiplexer);
    }

    Texture texture = new Texture("Textures/ui/buttons/InputButton.png");

    int startDrawPos;
    int endDrawPos;

    int cursorPos;

    final float textAllowance = 0.8f;

    InputProcessor inputProcessor;

    ShapeRenderer shapeRenderer;

    float totalWaitTime = 0.08f;
    float waitTimer = 0;

    float lineTotalWaitTime = 2.4f;
    float lineWaitTimer = 0;

    public boolean typing = false;

    public String text;

    public void setup(InputMultiplexer inputMultiplexer){
        super.setup();
        int textWidth = text.length();
        cursorPos = textWidth;
        startDrawPos = 0;
        endDrawPos = textWidth;

        shapeRenderer = new ShapeRenderer();

        inputProcessor = new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                System.out.println(" key was typed and i heard it!" + character);
                if(typing){
                    if (character != ' '){
                        lineWaitTimer = (float) (0.5 * lineTotalWaitTime);
                    }
                    if (character == '\b') {
                        if (cursorPos > 0) {
                            String temp = text.substring(0, cursorPos - 1);
                            text = temp + text.substring(cursorPos);
                            cursorPos--;
                            endDrawPos--;
                            glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
                            resizeForReduction();
                        }
                    }
                    else if (character == '\r' || character == '\t' || character == '\0' || character == '\f' || character == '\n') {
                        Gdx.app.log("InputButton", "error invalid character");
                    }
                    else {
                        String temp = text.substring(0, cursorPos);
                        text = temp + character + text.substring(cursorPos);
                        cursorPos++;
                        endDrawPos++;
                        glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
                        resizeForIncrease(true);
                        if (cursorPos - 1 == 0 && startDrawPos > 0){
                            startDrawPos--;
                            glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
                            resizeForIncrease(false);
                        }
                    }
                }
                return false;
            }
        };

        inputMultiplexer.addProcessor(inputProcessor);
    }

    public void resizeForReduction(){
        while (glyphLayout.width < width * textAllowance) {
            if (startDrawPos == 0){
                if (endDrawPos < text.length()) {
                    endDrawPos++;
                }
                else {
                    break;
                }
            }
            if (startDrawPos > 0){
                startDrawPos--;
            }
            else {
                break;
            }
            if (glyphLayout.width > width * textAllowance) {
                glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
                startDrawPos++;
            }
            glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
        }
    }

    public void resizeForIncrease(boolean affectStart){
        while (glyphLayout.width > width * textAllowance) {
            if (affectStart) {
                startDrawPos++;
            }else{
                endDrawPos--;
            }
            glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
        }
    }

    public void draw(SpriteBatch batch, int drawLayer){
        if (drawLayer == this.drawLayer){
            batch.draw(texture, x, y, width, height);

            String temp = text.substring(startDrawPos, endDrawPos);
            glyphLayout.setText(font, temp);
            font.draw(batch, temp,x + (width * ((1 - textAllowance) /2f)), y + height/2f + glyphLayout.height/2);

            batch.end();

            lineWaitTimer += Gdx.graphics.getDeltaTime();

            glyphLayout.setText(font, text.substring(startDrawPos, cursorPos));
            float Pos = x + (width * ((1 - textAllowance) /2f)) + glyphLayout.width;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            float cursorHeight = height * 0.70f;
            if (typing) {
                if (lineWaitTimer > lineTotalWaitTime / 2f) {
                    if (lineWaitTimer > lineTotalWaitTime) {
                        lineWaitTimer = 0;
                    }
                    shapeRenderer.rect(Pos + 2f, y + (cursorHeight / 4f), 4, cursorHeight);
                }
            }
            shapeRenderer.end();

            batch.begin();

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                typing = false;
            }

            handleArrowKeys();
        }
    }

    public void handleArrowKeys(){
        waitTimer += Gdx.graphics.getDeltaTime();
        if ((waitTimer > totalWaitTime || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) && typing) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                lineWaitTimer = (float) (0.5 * lineTotalWaitTime);
                if (cursorPos > 0) {
                    cursorPos--;
                }
                if (cursorPos - 1 < startDrawPos) {
                    if (startDrawPos > 0) {
                        startDrawPos--;
                        glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
                        while (glyphLayout.width > width * textAllowance) {
                            endDrawPos--;
                            glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
                        }
                    }
                }
                waitTimer = 0;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                lineWaitTimer = (float) (0.5 * lineTotalWaitTime);
                if (cursorPos < text.length()) {
                    cursorPos++;
                }
                if (cursorPos > endDrawPos) {
                    if (endDrawPos < text.length()) {
                        endDrawPos++;
                        glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
                        while (glyphLayout.width > width * textAllowance) {
                            startDrawPos++;
                            glyphLayout.setText(font, text.substring(startDrawPos, endDrawPos));
                        }
                    }
                }
                waitTimer = 0;
            }
        }
    }

    public boolean checkIfPressed(int x, int y, boolean firstCheck){
        if(visible){
            System.out.println(" i have been pressed and i am an input button");
            if (x > this.x && x < this.x + width && y > this.y && y < this.y + height) {
                pressed = true;
                typing = true;
                lineWaitTimer = lineTotalWaitTime / 2f;
                return true;
            }
            else {
                typing = false;
            }
        }
        return false;
    }

    public void setSize(int width, int height){
        this.width = (int) (width * 0.75);
        this.height = (int) (height * 0.35);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        this.typing = selected;
    }
}
