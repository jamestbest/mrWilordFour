package com.mygdx.game.ui.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.ui.elements.Button;

import javax.xml.stream.FactoryConfigurationError;
import java.util.ArrayList;
import java.util.Arrays;

public class ButtonCollection {
    public ArrayList<Button> buttons;

    public String pressedButtonName;
    public String lastPressedButtonName = "";

    public boolean showButtons = true;
    public boolean useWorldCoords = true;
    public boolean followCamera = false;

    public ButtonCollection() {
        buttons = new ArrayList<>();
    }

    public void add(Button... button) {
        buttons.addAll(Arrays.asList(button));
    }

    public void drawButtons(SpriteBatch batch, CameraTwo camera){
        if (showButtons) {
            for (Button button : buttons) {
                if (followCamera) {
                    button.draw(batch, camera);
                }
                else{
                    button.draw(batch, camera);
                }
            }
        }
        setAllToUnpressed();
    }

    public boolean updateButtons(CameraTwo camera){
        if (showButtons) {
            int x = Gdx.input.getX();
            int y = Gdx.input.getY();
            if (useWorldCoords) {
                Vector2 temp = camera.unproject(new Vector2(x, y));
                x = (int) temp.x;
                y = (int) temp.y;
            }
            else {
                y = Gdx.graphics.getHeight() - y;
            }
            for (Button button : buttons) {
                if (button.checkIfPressed(x, y)) {
                    pressedButtonName = button.name;
                    lastPressedButtonName = button.name;
                    return true;
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
}
