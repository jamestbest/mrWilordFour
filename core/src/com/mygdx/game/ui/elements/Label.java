package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Label extends TextButton {
    String text;

    public Label(int x, int y, int width, int height, String fontGda, String name, String text) {
        super(x, y, width, height, text, fontGda, name);
        this.text = text;
//        autoSize();
    }

    public Label(int x, int y, int width, int height, String name, String text) {
        super(x, y, width, height, text, name);
        this.text = text;
//        autoSize();
    }

    public Label(String name, String text) {
        this(0, 0, 0, 0, name, text);
    }

    public void draw(SpriteBatch batch, int drawLayer){
        if (drawLayer == this.drawLayer){
            font.draw(batch, text, x + (width - glyphLayout.width) / 2, y + (height + glyphLayout.height) / 2);
        }
    }
}
