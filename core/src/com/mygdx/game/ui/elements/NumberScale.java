package com.mygdx.game.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Entity.Colonist;
import com.mygdx.game.Game.MyGdxGame;

public class NumberScale extends Button{
    public NumberScale(int x, int y, int width, int height, String Gda, String name) {
        super(x, y, width, height, Gda, name);
    }

    public NumberScale(int x, int y, int width, int height, String name) {
        super(x, y, width, height, name);
    }

    public NumberScale(String name, Colonist c, String skill) {
        super(name);
        this.c = c;
        this.skill = skill;
        setup();
    }

    Colonist c;
    private String skill;

    Texture t;

    String text;
    public static BitmapFont font;
    GlyphLayout glyphLayout;

    public void setup(){
        t = new Texture("Textures/ui/buttons/priorityButton.png");
        text = c.priorityFromType.get(skill).toString();
        glyphLayout = new GlyphLayout(font, text);
    }

    public void draw(SpriteBatch batch, int drawLayer){
        if (drawLayer == this.drawLayer){
            batch.draw(t, x, y, width, height);

            text = c.priorityFromType.get(skill).toString();
            glyphLayout.setText(font, text);
            font.draw(batch, glyphLayout, x + (width - glyphLayout.width) / 2, y + (height + glyphLayout.height) / 2);
        }
    }

    public boolean checkIfPressed(int x, int y, boolean firstCheck){
        if(visible){
            if (x > this.x && x < this.x + width && y > this.y && y < this.y + height) {
                pressed = true;
                if (firstCheck) {
                    if (r != null) {
                        r.run();
                    }
                }
                return true;
            }
        }
        return false;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public void setRunnable(Runnable r){
        this.r = r;
    }
}
