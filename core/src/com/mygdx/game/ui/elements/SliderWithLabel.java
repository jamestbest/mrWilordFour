package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game.MyGdxGame;

public class SliderWithLabel extends Slider {
    public SliderWithLabel(int x, int y, int width, int height, String Gda, String name) {
        super(x, y, width, height, Gda, name);
        setup();
    }

    public SliderWithLabel(int x, int y, int width, int height, String name) {
        super(x, y, width, height, name);
        setup();
    }

    public SliderWithLabel(int x, int y, int width, int height, String name, float maxValue, float minValue, float step) {
        super(x, y, width, height, name, maxValue, minValue, step);
        setup();
    }

    public void setup(){
        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        glyphLayout = new GlyphLayout();
        value = minValue;
    }

    float percentageAcross = 0f;
    
    public void draw(SpriteBatch batch) {
        batch.end();
        float tempWidth = width * 0.75f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.416f, 0.431f, 0.459f, 1);
        shapeRenderer.rect(x, y, tempWidth, height);
        shapeRenderer.setColor(0.349f,0.361f,0.38f,1);
        shapeRenderer.rect(x, y, tempWidth * percentageAcross, height);
        shapeRenderer.circle(x + tempWidth * percentageAcross, y + height / 2f, height / 7f * 5f);
        shapeRenderer.end();
        batch.begin();

        glyphLayout.setText(font, String.valueOf((int)value));
        font.draw(batch, glyphLayout, x + tempWidth * 1.1f, y + height);

        batch.end();
        batch.begin();
    }
    
    public boolean checkIfPressed(int x, int y){
        int extraSpace = (int) (width / 5f);
        float tempWidth = width * 0.75f;

        if(x > this.x - tempWidth && x < this.x + tempWidth + extraSpace && y > this.y - extraSpace && y < this.y + this.height + extraSpace){
            percentageAcross = (x - this.x) / tempWidth;
            if (percentageAcross < 0) percentageAcross = 0;
            if (percentageAcross > 1) percentageAcross = 1;

            value = percentageAcross * (maxValue - minValue) + minValue;
            return true;
        }
        return false;
    }
}
