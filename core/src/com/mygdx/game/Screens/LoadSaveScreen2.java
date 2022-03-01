package com.mygdx.game.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.ui.elements.BoxedTextButton;
import com.mygdx.game.ui.extensions.ButtonCollection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;

public class LoadSaveScreen2 implements Screen {
    MyGdxGame game;

    ArrayList<String> saveNames;

    ButtonCollection buttonCollection;
    CameraTwo camera;

    SpriteBatch batch;

    int startIndex = 0;
    int numberShown = 8;

    InputProcessor inputProcessor = new InputAdapter(){
        @Override
        public boolean scrolled(float amountX, float amountY) {
            System.out.println(amountY);
            if(amountY > 0){
                if (startIndex + numberShown < saveNames.size() - 1){
                    startIndex++;
                    updateSaveButtons();
                }
            }
            else if(amountY < 0){
                if (startIndex > 0){
                    startIndex--;
                    updateSaveButtons();
                }
            }
            return false;
        }
    };

    public LoadSaveScreen2(MyGdxGame game){
        this.game = game;
        setup();
    }

    public void setup(){
        batch = new SpriteBatch();
        camera = new CameraTwo();
        buttonCollection = new ButtonCollection();
        setupSaveNames();
        setupAllSaveButtons();

        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        buttonCollection.drawButtons(batch);
        batch.end();

        if (Gdx.input.isButtonPressed(0)) {
            buttonCollection.updateButtons(camera);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public void setupAllSaveButtons(){


        int x = (int) (MyGdxGame.initialRes.x / 10);
        int y = (int) (MyGdxGame.initialRes.y / 10);

        int buttonWidth = (int) (MyGdxGame.initialRes.x / 5);
        int buttonHeight = (int) (MyGdxGame.initialRes.y / 10);

        for (int i = startIndex; i < startIndex + numberShown; i++) {
            if (i < saveNames.size()) {
                BoxedTextButton b = new BoxedTextButton(x, y + (buttonHeight * i) + (5 * i), buttonWidth, buttonHeight, i + "", saveNames.get(i));
                buttonCollection.add(b);
            }
        }
    }

    public void updateSaveButtons(){
        for (int i = startIndex; i < startIndex + numberShown; i++) {
            if (i < saveNames.size()) {
                System.out.println(i);
                BoxedTextButton b = (BoxedTextButton) buttonCollection.buttons.get(i);
                b.setText(saveNames.get(i));
            }
        }
    }

    public void setupSaveNames(){
        saveNames = new ArrayList<String>();
        File dir = new File("core/assets/Saves");
        saveNames.addAll(Arrays.asList(Objects.requireNonNull(dir.list())));
    }
}
