package com.mygdx.game.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.ui.elements.BoxedTextButton;
import com.mygdx.game.ui.elements.Button;
import com.mygdx.game.ui.extensions.ButtonCollection;

import java.io.File;
import java.util.ArrayList;

public class LoadSaveScreen implements Screen {
    MyGdxGame game;

    GlyphLayout glyphLayout;
    BitmapFont font;
    SpriteBatch batch;

    String[] saveNames;

    InputMultiplexer inputMultiplexer;
    CameraTwo cameraTwo;

    ButtonCollection saveNameButtons;
    ArrayList<BoxedTextButton> allButtons;

    int startIndex = 0;
    int numberShown = 5;

    int selectedIndex;

    InputProcessor inputProcessor = new InputAdapter(){
        @Override
        public boolean scrolled(float amountX, float amountY) {
            if (amountY > 0) {
                if (startIndex + numberShown < saveNames.length) {
                    startIndex += 1;
                    updateSaveNameButtons();
                }
            }
            else {
                if (startIndex > 0) {
                    startIndex -= 1;
                    updateSaveNameButtons();
                }
            }
            return false;
        }
    };

    public LoadSaveScreen(MyGdxGame game){
        this.game = game;
        cameraTwo = new CameraTwo();
        cameraTwo.allowMovement = false;

        glyphLayout = new GlyphLayout();
        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        batch = new SpriteBatch();

        saveNames = getAllSaveName();

        inputMultiplexer = new InputMultiplexer();
        numberShown = Math.min(numberShown, saveNames.length);
        setupSaveNameButtons();
        inputMultiplexer.addProcessor(inputProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        cameraTwo.update();

        batch.begin();
        batch.setProjectionMatrix(cameraTwo.projViewMatrix);
        saveNameButtons.drawButtons(batch);
        batch.end();

        if (Gdx.input.isButtonPressed(0)) {
            saveNameButtons.updateButtons(cameraTwo);
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

    public String[] getAllSaveName(){
        File dir = new File("core/assets/Saves");
        return dir.list();
    }

    public void setupSaveNameButtons(){
        saveNameButtons = new ButtonCollection();
        allButtons = new ArrayList<>();
        float x = MyGdxGame.initialRes.x / 10f;
        float y = MyGdxGame.initialRes.y / 10f;
        float width = MyGdxGame.initialRes.x / 10 * 2f;
        float height = MyGdxGame.initialRes.y / 10 * 5f;
        float buttonHeight = height / 8f;
        for (int i = 0; i < saveNames.length; i++) {
            String s = saveNames[i];
            BoxedTextButton textButton = new BoxedTextButton("saveNameButton" + i, s);
            textButton.setPos(x, y + height - ((buttonHeight + 5) * i));
            textButton.setSize(width, buttonHeight);
            allButtons.add(textButton);
        }
        for (int i = startIndex; i < startIndex + numberShown; i++) {
            saveNameButtons.add(allButtons.get(i));
        }
    }

    public void getSelectedIndex(){
        for (int i = startIndex; i < startIndex + numberShown; i++) {
            Button b = allButtons.get(i);
            if (b.selected) {
                selectedIndex = i;
            }
        }
    }

    public void updateSaveNameButtons(){
        for (int i = startIndex; i < startIndex + numberShown; i++) {
            saveNameButtons.buttons.clear();
            saveNameButtons.add(allButtons.get(i));
        }
    }
}
