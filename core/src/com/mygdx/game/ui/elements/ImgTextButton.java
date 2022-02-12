package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;

public class ImgTextButton extends Button{
    String text;
    Texture img;

    BitmapFont font;
    GlyphLayout glyphLayout;

    public ImgTextButton(int x, int y, int width, int height, String Gda, String name) {
        super(x, y, width, height, Gda, name);
        setup();
    }

    public ImgTextButton(int x, int y, int width, int height, String name) {
        super(x, y, width, height, name);
        setup();
    }

    public ImgTextButton(String name, String text, String imgName) {
        super(name);
        this.text = text;
        this.img = new Texture("Textures/Resources/" + imgName + ".png");
        setup();
    }

    public void setup(){
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();
        glyphLayout.setText(font, text);
    }

    public void draw(SpriteBatch batch){
        float imgDims = height / 5f * 4f;
        float imgX = x + (width - imgDims) / 2f;
        float imgY = y + (height - imgDims) / 2f;

        batch.draw(img, imgX, imgY, imgDims, imgDims);

        font.draw(batch, glyphLayout, (int) (imgX + (imgDims * 1.1)), y + height - (height / 5f));
    }

    public void updateText(String text){
        this.text = text;
        glyphLayout.setText(font, text);
    }
}
