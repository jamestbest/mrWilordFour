package com.mygdx.game.ui.extensions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.ui.elements.Button;

import java.util.ArrayList;

public class Table {
    public ButtonCollection buttonCollection;
    ArrayList<ArrayList<Button>> grid;

    int numberOfRows;

    int x;
    int y;

    int width;
    int height;

    public Table(int x, int y, int width, int height) {
        buttonCollection = new ButtonCollection();
        grid = new ArrayList<>();
        grid.add(new ArrayList<>());

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(SpriteBatch batch) {
        buttonCollection.drawButtons(batch);
    }

    public void update(CameraTwo camera) {
        buttonCollection.updateButtons(camera);
    }

    public void add(Button... button) {
        buttonCollection.add(button);
        for (Button b: button) {
            grid.get(numberOfRows).add(b);
        }
    }

    public void row(){
        numberOfRows++;
        grid.add(new ArrayList<>());
    }

    public void sort() {
        int max = 0;
        for (ArrayList<Button> buttonArrayList : grid) {
            if (buttonArrayList.size() > max) {
                max = buttonArrayList.size();
            }
        }
        float offset = height / (float) max / 10f;
        float buttonWidth = width / (float) max;
        float buttonHeight = (height - ((grid.size() - 1) * offset)) / (float) grid.size();

        for (Button b: buttonCollection.buttons) {
            b.setSize((int) buttonWidth, (int) buttonHeight);
        }
        for (int i = 0; i < grid.size(); i++) {
            for (int j = 0; j < grid.get(i).size(); j++) {
                grid.get(grid.size() - i - 1).get(j).setPos((int) (x + j * (buttonWidth)), (int) (y + i * (buttonHeight + offset)));
            }
        }
    }
}
