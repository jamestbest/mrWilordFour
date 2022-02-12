package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.ui.elements.*;
import com.mygdx.game.ui.extensions.Table;

public class Settings implements Screen {
    Table optionsTable;

    Label volumeLabel;
    Label muteLabel;
    Label currenSongLabel;
    Label loopLabel;
    Label removeSongLabel;

    Label fPSCounterLabel;
    Label maxFPSLabel;
    Label vsyncLabel;

    Label titleLabel;

    SliderWithLabel volumeSlider;
    ToggleButton muteToggle;
    Label placeholderLabel;
    ToggleButton loopToggle;
    Label placeholderTwoLabel;

    ToggleButton fPSToggle;
    SliderWithLabel fPSSlider;
    ToggleButton vSyncToggle;

    Label placeholderThreeLabel;
    InputButtonTwo setTitleInputButton;

    InputMultiplexer inputMultiplexer;

    SpriteBatch batch;
    CameraTwo camera;

    Texture background = new Texture("Textures/Backgrounds/optionsMenu.jpg");

    MyGdxGame game;

    public Settings(MyGdxGame game){
        this.game = game;

        inputMultiplexer = new InputMultiplexer();
        batch = new SpriteBatch();
        camera = new CameraTwo();
        camera.allowMovement = false;

        optionsTable = new Table(0, 0, (int) Gdx.graphics.getWidth(), (int) Gdx.graphics.getHeight());

        volumeSlider = new SliderWithLabel(0, 0,  0,  0,"VolumeSlider");
        muteToggle = new ToggleButton(0, 0,  0,  0,"MuteToggle");
        placeholderLabel = new Label(0, 0,  0,  0, "PlaceholderLabel", "Placeholder");
        loopToggle = new ToggleButton(0, 0,  0,  0,"LoopToggle");
        placeholderTwoLabel = new Label(0, 0,  0,  0, "PlaceholderTwoLabel", "Placeholder");

        fPSToggle = new ToggleButton(0, 0,  0,  0,"FPSToggle");
        fPSSlider = new SliderWithLabel(0, 0,  0,  0,"FPSSlider");
        vSyncToggle = new ToggleButton(0, 0,  0,  0,"VsyncToggle");

        placeholderThreeLabel = new Label(0, 0,  0,  0, "PlaceholderThreeLabel", "Placeholder");
        setTitleInputButton = new InputButtonTwo(0, 0,  0,  0, MyGdxGame.title, "SetTitleInputButton", inputMultiplexer);

        volumeLabel = new Label(0, 0,  0,  0, "VolumeLabel", "Volume: ");
        muteLabel = new Label(0, 0,  0,  0, "MuteLabel", "Mute: ");
        currenSongLabel = new Label(0, 0,  0,  0, "CurrenSongLabel", "Current Song: ");
        loopLabel = new Label(0, 0,  0,  0, "LoopLabel", "Loop: ");
        removeSongLabel = new Label(0, 0,  0,  0, "RemoveSongLabel", "Remove Song: ");

        fPSCounterLabel = new Label(0, 0,  0,  0, "FPSCounterLabel", "FPS: ");
        maxFPSLabel = new Label(0, 0,  0,  0, "MaxFPSLabel", "Max FPS: ");
        vsyncLabel = new Label(0, 0,  0,  0, "VsyncLabel", "Vsync: ");

        titleLabel = new Label(0, 0,  0,  0, "TitleLabel", "Title: ");

        optionsTable.add(volumeLabel, volumeSlider, fPSCounterLabel, fPSToggle);
        optionsTable.row();
        optionsTable.add(muteLabel, muteToggle, maxFPSLabel, fPSSlider);
        optionsTable.row();
        optionsTable.add(currenSongLabel, placeholderLabel, vsyncLabel,vSyncToggle);
        optionsTable.row();
        optionsTable.add(loopLabel ,loopToggle, titleLabel, setTitleInputButton);
        optionsTable.row();
        optionsTable.add(placeholderTwoLabel, new NullButton(), placeholderThreeLabel);

        optionsTable.sort();

        Gdx.input.setInputProcessor(inputMultiplexer);
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
        batch.draw(background, 0, 0, (int) Gdx.graphics.getWidth(), (int) Gdx.graphics.getHeight());

        optionsTable.draw(batch);
        batch.end();

        if (Gdx.input.isButtonPressed(0)) {
            optionsTable.update(camera);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenu(game));
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
}
