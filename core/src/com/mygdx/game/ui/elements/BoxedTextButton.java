package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game.MyGdxGame;

public class BoxedTextButton extends Button {
    public String text;
    GlyphLayout glyphLayout;
    BitmapFont font;

    Color color = Color.GRAY;

    ShapeRenderer shapeRenderer;

    Runnable r;

    public BoxedTextButton(int x, int y, int width, int height, String name, String text) {
        super(x, y, width, height, name);
        this.text = text;
        setup();
    }

    public BoxedTextButton(String name, String text, Runnable runnable) {
        super(name);
        this.text = text;
        setup();
        this.r = runnable;
    }

    public void setup(){
        glyphLayout = new GlyphLayout();
        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        shapeRenderer = new ShapeRenderer();
        glyphLayout.setText(font, text);
    }

    public void draw(SpriteBatch batch, int drawLayer){
        if (drawLayer == this.drawLayer) {
            batch.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setColor(color);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            if (selected) {
                shapeRenderer.setColor(Color.BLUE);
            }
            else {
                shapeRenderer.setColor(Color.WHITE);
            }
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();

            batch.begin();
            font.draw(batch, glyphLayout, x + (width - glyphLayout.width) / 2, y + (height + glyphLayout.height) / 2);
        }
    }

    public boolean checkIfPressed(int x, int y, boolean firstCheck){
        if (x > this.x && x < this.x + this.width && y > this.y && y < this.y + this.height) {
            pressed = true;
            if (r != null) {
                r.run();
            }
            return true;
        }
        return false;
    }

    public void setText(String text){
        this.text = text;
        glyphLayout.setText(font, text);
    }

    public Runnable getR() {
        return r;
    }

    public void setR(Runnable r) {
        this.r = r;
    }
}
