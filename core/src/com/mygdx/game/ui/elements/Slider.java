package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Slider extends TextButton{
    float maxValue = 100f;
    float minValue = 0f;
    public float value;
    float step = 1f;

    ShapeRenderer shapeRenderer;

    public Slider(int x, int y, int width, int height, String Gda, String name) {
        super(x, y, width, height, "", Gda, name);
        shapeRenderer = new ShapeRenderer();
    }

    public Slider(int x, int y, int width, int height, String name) {
        super(x, y, width, height, "", name);
        shapeRenderer = new ShapeRenderer();
        value = minValue;
    }

    public Slider(int x, int y, int width, int height, String name, float maxValue, float minValue, float step) {
        this(x, y, width, height, name);
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.step = step;
    }

    public void draw(SpriteBatch batch){
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.416f, 0.431f, 0.459f, 1);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(0.349f,0.361f,0.38f,1);
        shapeRenderer.rect(x, y, width * (value / maxValue), height);
        shapeRenderer.circle(x + width * (value / maxValue), y + height / 2f, height / 7f * 5f);
        shapeRenderer.end();
        batch.begin();
    }

    public boolean checkIfPressed(int x, int y){
        int extraSpace = (int) (width / 5f);
        if(x > this.x - width && x < this.x + this.width + extraSpace && y > this.y - extraSpace && y < this.y + this.height + extraSpace){
            value = (x - this.x) / (float)width * maxValue;
            System.out.println(value);
            if (value < minValue) value = minValue;
            if (value > maxValue) value = maxValue;
            return true;
        }
        return false;
    }
}
