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

    String fontGda;

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

    public void setup(){
        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        font.setColor(fontColor);
        glyphLayout = new GlyphLayout(font, text);
    }

    public void draw(SpriteBatch batch) {
        super.draw(batch);
        drawText(batch);
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

    public void setFontColor(Color color){
        fontColor = color;
        font.setColor(fontColor);
    }

    public void setFontScale(float scale){
        fontScale = scale;
        font.getData().setScale(fontScale);
        glyphLayout.width = glyphLayout.width * fontScale;
    }
}
