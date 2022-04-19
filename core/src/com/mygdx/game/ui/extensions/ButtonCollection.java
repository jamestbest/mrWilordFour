package com.mygdx.game.ui.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.ui.elements.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ButtonCollection {
    public ArrayList<Button> buttons;

    public String pressedButtonName;
    public String lastPressedButtonName = "";

    public boolean showButtons = true;
    public boolean useWorldCoords = true;

    public boolean firstCheck;

    public ButtonCollection() {
        buttons = new ArrayList<>();
    }

    public void add(Button... button) {
        buttons.addAll(Arrays.asList(button));
    }

    public void drawButtons(SpriteBatch batch) {
        if (showButtons) {
            for (int i = 0; i < 3; i++) {
                for (Button button : buttons) {
                    button.draw(batch, i);
                }
            }
        }
        setAllToUnpressed();
    }

    public boolean updateButtons(CameraTwo camera, boolean firstCheck){
        if (showButtons) {
            int x = Gdx.input.getX();
            int y = Gdx.input.getY();
            if (useWorldCoords) {
                Vector2 temp = camera.unproject(new Vector2(x, y));
                x = (int) temp.x;
                y = (int) temp.y;
            }
            else {
                y = (int) (MyGdxGame.initialRes.y - y);
            }
            for (int i = 2; i > -1; i--) {
                for (Button button : buttons) {
                    if (button.pressedLayer == i) {
                        if (firstCheck && button.wantsSingleCheck) {
                            if (button.checkIfPressed(x, y, true)) {
                                setAllToUnSelected(button);
                                button.selected = true;
                                pressedButtonName = button.name;
                                lastPressedButtonName = button.name;
                                return true;
                            }
                        }
                        else if (!button.wantsSingleCheck){
                            if (button.checkIfPressed(x, y, firstCheck)) {
                                setAllToUnSelected(button);
                                button.selected = true;
                                pressedButtonName = button.name;
                                lastPressedButtonName = button.name;
                                return true;
                            }
                        }
                    }
                }
            }

        }
        return false;
    }

    public void setAllToUnpressed(){
        for (Button button : buttons) {
            button.pressed = false;
            pressedButtonName = "";
        }
    }

    public void setAllToUnSelected(Button b){
        for (Button button : buttons) {
            if (button != b) {
                button.setSelected(false);
            }
        }
    }

    public void setAllToUnSelected(){
        for (Button button : buttons) {
            button.setSelected(false);
        }
    }

    public void clear(){
        buttons.clear();
    }

    public Button getSelected(){
        for (Button button : buttons) {
            if (button.selected) {
                return button;
            }
        }
        return null;
    }
}
