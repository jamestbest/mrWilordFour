package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Entity.Colonist;

public class colonistMiniPlate extends Button{
    public colonistMiniPlate(int x, int y, int width, int height, String Gda, String name, Colonist colonist) {
        super(x, y, width, height, Gda, name);
        this.colonist = colonist;
    }

    public colonistMiniPlate(int x, int y, int width, int height, String name, Colonist colonist) {
        super(x, y, width, height, name);
        this.colonist = colonist;
    }

    public colonistMiniPlate(String name, Colonist colonist) {
        super(name);
        this.colonist = colonist;
    }

    Colonist colonist;

    public void draw(SpriteBatch batch, int drawLayer){

    }
}
