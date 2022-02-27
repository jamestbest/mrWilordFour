package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class Slider extends TextButton{
    float maxValue = 100f;
    float minValue = 0f;
    public float value;
    float step = 1f;
    float percentageAcross = 0f;

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

    public void draw(SpriteBatch batch, int drawLayer){
        if (drawLayer == this.drawLayer) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setColor(0.416f, 0.431f, 0.459f, 1);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.setColor(0.349f,0.361f,0.38f,1);
            float percent = value / maxValue;
            if (percent > 1) percent = 1;
            shapeRenderer.rect(x, y, width * (percent), height);
            shapeRenderer.circle(x + width * (percent), y + height / 2f, height / 7f * 5f);
            shapeRenderer.end();
            batch.begin();
        }
    }

    public boolean checkIfPressed(int x, int y){
        if(x > this.x - width && x < this.x + this.width && y > this.y && y < this.y + this.height){
            value = (x - this.x) / (float)width * maxValue;
            if (value < minValue) value = minValue;
            if (value > maxValue) value = maxValue;
            return true;
        }
        return false;
    }

    public void setSize(int width, int height){
        this.width = width / 3 * 2;
        this.height = height / 10;
    }

    public void setValue(float value){
        this.value = value;
        text = String.valueOf((int)value);
        percentageAcross = (value - minValue) / (maxValue - minValue);
    }
}
