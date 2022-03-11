package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Math.CameraTwo;

public class NullButton extends Button {
    public NullButton() {
        super("nullButton");
    }

    public void draw(SpriteBatch batch, int drawLayer) {
        // do nothing
    }

    public void update(CameraTwo camera) {
        // do nothing
    }
}
