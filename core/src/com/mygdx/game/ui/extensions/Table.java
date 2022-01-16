package com.mygdx.game.ui.extensions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.ui.elements.Button;

import java.util.ArrayList;

public class Table {
    public ButtonCollection buttonCollection;
    ArrayList<ArrayList<Button>> grid;

    public int numberOfRows;

    int x;
    int y;

    public int width;
    public int height;

    ShapeRenderer shapeRenderer;

    public Table(int x, int y, int width, int height) {
        buttonCollection = new ButtonCollection();
        grid = new ArrayList<>();
        grid.add(new ArrayList<>());

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        shapeRenderer = new ShapeRenderer();
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

    public void addAllWithRows(Button... buttons) {
        for (int i = 0; i < buttons.length - 1; i++) {
            add(buttons[i]);
            row();
        }
        add(buttons[buttons.length - 1]);
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
        sortToFit();
    }

    public void sortWithoutResize(float offset) {
        for (int i = 0; i < grid.size(); i++) {
            for (int j = 0; j < grid.get(grid.size() - i - 1).size(); j++) {
                Button b = grid.get(grid.size() - i - 1).get(j);
                b.setPos((x + j * (b.width)), (int) (y + i * (b.height + offset)));
            }
        }
    }

    public void sortToFit(){ //given that it is a 1d array
        float height = this.height / (float) grid.size();
        for (int i = 0; i < grid.size(); i++) {
            Button b = grid.get(i).get(0);
            float x = this.x + (this.width - b.width) / 2f;
            float y = this.y + ((grid.size() - i - 1) * (height)) + (height - b.height) / 2f;
            b.setPos((int) x, (int) y);
        }
    }
}
