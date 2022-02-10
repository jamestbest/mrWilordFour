package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;

public class NullButton extends Button {
    public NullButton() {
        super("nullButton");
    }

    public void draw(SpriteBatch batch, boolean drawToScreen, CameraTwo camera) {
        // do nothing
    }

    public void update(CameraTwo camera) {
        // do nothing
    }
}
