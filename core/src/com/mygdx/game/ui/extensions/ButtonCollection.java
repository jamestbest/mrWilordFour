package com.mygdx.game.ui.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.ui.elements.Button;

import java.util.ArrayList;
import java.util.Arrays;

public class ButtonCollection {
    ArrayList<Button> buttons;

    public String pressedButtonName;
    public String lastPressedButtonName = "";

    public ButtonCollection() {
        buttons = new ArrayList<Button>();
    }

    public void add(Button... button) {
        buttons.addAll(Arrays.asList(button));
    }

    public void drawButtons(SpriteBatch batch) {
        for (Button button : buttons) {
            button.draw(batch);
        }
        setAllToUnpressed();
    }

    public void updateButtons(CameraTwo camera){
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        Vector2 temp = camera.unproject(new Vector2(x, y));
        x = (int) temp.x;
        y = (int) temp.y;
        for (Button button : buttons) {
            if(button.checkIfPressed(x, y)){
                pressedButtonName = button.name;
                lastPressedButtonName = button.name;
                break;
            }
        }
    }

    public void setAllToUnpressed(){
        for (Button button : buttons) {
            button.pressed = false;
            pressedButtonName = "";
        }
    }
}
