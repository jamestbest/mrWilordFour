package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import java.io.File;

public class AnimatedButton extends Button {
    public AnimatedButton(int x, int y, int width, int height, String name, String gda) {
        super(x, y, width, height, name);
        this.Gda = gda;
        setup();
    }

    boolean inAnimation;
    int animationPointer;

    Array<Texture> textures = new Array<Texture>();
    Texture background;

    float timer = 0;
    float totalTime = 0.07f;

    public void setup(){
        setAllTextures();
        background = new Texture("Textures/ui/buttons/" + Gda + ".png");
    }

    public void draw(SpriteBatch batch){
        timer += Gdx.graphics.getDeltaTime();
        batch.draw(background, x, y, width, height);
        batch.draw(textures.get(animationPointer), x, y, width, height);
        if (animationPointer < textures.size - 1) {
            if (timer > totalTime) {
                animationPointer++;
                timer = 0;
            }
        }
        else {
            animationPointer = 0;
        }
    }

    public void setAllTextures(){
        File dir = new File("assets/Textures/ui/buttons/animated/" + Gda);
        String[] files = dir.list();
        textures = new Array<>(files.length);
        for (String file : files) {
            textures.add(new Texture("Textures/ui/buttons/animated/" + Gda + "/" + file));
        }
    }
}
