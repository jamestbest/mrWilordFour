package com.mygdx.game.ui.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ImgButton extends Button {
    String textureName;
    Texture texture;

    public ImgButton(int x, int y, int width, int height, String Gda, String name, String textureName) {
        super(x, y, width, height, Gda, name);
        this.textureName = textureName;
        setup();
    }

    public ImgButton(int x, int y, int width, int height, String name, String textureName) {
        super(x, y, width, height, name);
        this.textureName = textureName;
        setup();
    }

    public ImgButton(String name, String textureName) {
        super(name);
        this.textureName = textureName;
        setup();
    }

    public void setup(){
        texture = new Texture("Textures/ui/imgButtons/" + textureName + ".png");
    }

    public void draw(SpriteBatch batch, int drawLayer){
        super.draw(batch, drawLayer);
        batch.draw(texture, x, y, width, height);
    }
}
