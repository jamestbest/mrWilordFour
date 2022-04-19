package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Label extends TextButton {
    Runnable updateRunnable;

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

    public Label(String name, String text, Runnable updateRunnable) {
        this(0, 0, 0, 0, name, text);
        this.updateRunnable = updateRunnable;
    }

    public boolean drawCentred = true;
    public boolean resizeFontToFit = false;

    public void draw(SpriteBatch batch, int drawLayer){
        if (updateRunnable != null) {
            updateRunnable.run();
        }
        if (drawLayer == this.drawLayer){
            if (resizeFontToFit){
                resizeToFit();
            }
            if (drawCentred) {
                font.draw(batch, text, x + (width - glyphLayout.width) / 2, y + (height + glyphLayout.height) / 2);
            } else {
                font.draw(batch, text, x, y + (height + glyphLayout.height) / 2);
            }
        }
    }

    public void resizeToFit(){
        while (glyphLayout.width > width){
            font.getData().setScale(font.getScaleX() - 0.01f);
            glyphLayout.setText(font, text);
        }
    }

    public boolean checkIfPressed(int x, int y, boolean firstCheck){
        if(visible){
            if (x > this.x && x < this.x + width && y > this.y && y < this.y + height) {
                pressed = true;
                return true;
            }
        }
        return false;
    }

    public Runnable getUpdateRunnable() {
        return updateRunnable;
    }

    public void setUpdateRunnable(Runnable updateRunnable) {
        this.updateRunnable = updateRunnable;
    }
}
