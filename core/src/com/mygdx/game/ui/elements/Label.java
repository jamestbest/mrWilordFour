package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;

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

    public void draw(SpriteBatch batch, CameraTwo camera){
        font.draw(batch, text, x + (width - glyphLayout.width) / 2, y + (height + glyphLayout.height) / 2);
    }
}
