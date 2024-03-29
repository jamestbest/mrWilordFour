package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game.MyGdxGame;

public class TextButton extends Button {
    String text;

    BitmapFont font;
    GlyphLayout glyphLayout;

    Color fontColor = Color.WHITE;
    float fontScale = 1f;

    public TextButton(int x, int y, int width, int height, String text, String TextureGda, String name) {
        super(x, y, width, height, TextureGda);
        this.text = text;
        this.name = name;
        setup();
    }

    public TextButton(int x, int y, int width, int height, String text, String name) {
        this(x, y, width, height, text,"BlueButton", name);
    }

    public TextButton(String text, String name) {
        this(0,0,0,0, text,"BlueButton", name);
    }

    public TextButton(String text, String name, Runnable r) {
        this(0,0,0,0, text,"BlueButton", name);
        this.r = r;
    }

    public void setup(){
        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        font.setColor(fontColor);
        font.getData().setScale(fontScale);
        glyphLayout = new GlyphLayout(font, text);
    }

    public void draw(SpriteBatch batch, int drawLayer) {
        if (drawLayer == this.drawLayer) {
            super.draw(batch, drawLayer);
            drawText(batch);
        }
    }

    public void drawText(SpriteBatch batch){
        if (visible){
            if (pressed){
                font.draw(batch, text, x + (width - glyphLayout.width) / 2, y + (height + glyphLayout.height) / 2 - 2);
            }
            else{
                font.draw(batch, text, x + (width - glyphLayout.width) / 2, y + (height + glyphLayout.height) / 2);
            }
        }
    }

    public void setText(String text) {
        this.text = text;
        glyphLayout.setText(font, text);
    }

    public void autoSize(){
        width = (int) (glyphLayout.width * 1.25f);
    }

    public void translate(float x, float y){
        this.x += x;
        this.y += y;
    }

    public void setFontColor(Color color){
        fontColor = color;
        font.setColor(fontColor);
    }

    public void setFontScale(float scale){
        fontScale = scale;
        font.getData().setScale(fontScale);
        glyphLayout.setText(font, text);
//        glyphLayout.width = glyphLayout.width * fontScale;
    }

    public void resizeFontToCorrectProportionByWidth(){
        glyphLayout.setText(font, text);
        while (glyphLayout.width > width * 0.85f) {
            setFontScale(fontScale - 0.01f);
        }
    }

    public void resizeFontToCorrectProportionByHeight(){
        glyphLayout.setText(font, text);
        while (glyphLayout.height > height * 0.75f) {
            setFontScale(fontScale - 0.01f);
        }
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
