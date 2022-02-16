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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

public class SettingsScreen implements Screen {
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
    DropdownButton currentSongDropDown;
    ToggleButton loopToggle;
    Label addSongLabel;
    ImgOnlyButton addSongButton;

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

    public SettingsScreen(MyGdxGame game){
        this.game = game;

        inputMultiplexer = new InputMultiplexer();
        batch = new SpriteBatch();
        camera = new CameraTwo();
        camera.allowMovement = false;

        optionsTable = new Table(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        volumeSlider = new SliderWithLabel("VolumeSlider");
        volumeSlider.setValue(game.volume);
        muteToggle = new ToggleButton("MuteToggle");
        currentSongDropDown = new DropdownButton("currentSongDropDown", inputMultiplexer);
        loopToggle = new ToggleButton("LoopToggle");
        addSongLabel = new Label( "addSongLabel", "add custom song");
        addSongButton = new ImgOnlyButton("addSongButton", "uploadButton");

        currentSongDropDown.setDropDowns(getSelectableSongs());

        fPSToggle = new ToggleButton("FPSToggle");
        fPSSlider = new SliderWithLabel("FPSSlider", 144, 30, 1, 60);
        vSyncToggle = new ToggleButton("VsyncToggle");

        placeholderThreeLabel = new Label( "PlaceholderThreeLabel", "Placeholder");
        setTitleInputButton = new InputButtonTwo(0, 0,  0,  0, MyGdxGame.title, "SetTitleInputButton", inputMultiplexer);

        volumeLabel = new Label("VolumeLabel", "Volume: ");
        muteLabel = new Label( "MuteLabel", "Mute: ");
        currenSongLabel = new Label( "CurrenSongLabel", "Current Song: ");
        loopLabel = new Label("LoopLabel", "Loop: ");
        removeSongLabel = new Label( "RemoveSongLabel", "Remove Song: ");

        fPSCounterLabel = new Label("FPSCounterLabel", "FPS Counter: ");
        maxFPSLabel = new Label("MaxFPSLabel", "Max FPS: ");
        vsyncLabel = new Label("VsyncLabel", "Vsync: ");

        titleLabel = new Label("TitleLabel", "Title: ");

        optionsTable.add(volumeLabel, volumeSlider, fPSCounterLabel, fPSToggle);
        optionsTable.row();
        optionsTable.add(muteLabel, muteToggle, maxFPSLabel, fPSSlider);
        optionsTable.row();
        optionsTable.add(currenSongLabel, currentSongDropDown, vsyncLabel,vSyncToggle);
        optionsTable.row();
        optionsTable.add(loopLabel ,loopToggle, titleLabel, setTitleInputButton);
        optionsTable.row();
        optionsTable.add(addSongLabel, addSongButton, placeholderThreeLabel);

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
        batch.setProjectionMatrix(camera.projViewMatrix);
        batch.draw(background, 0, 0, MyGdxGame.initialRes.x, MyGdxGame.initialRes.y);

        optionsTable.draw(batch);
        batch.end();

        if (Gdx.input.isButtonPressed(0)) {
            optionsTable.update(camera);
        }

        if (Gdx.input.isButtonPressed(0)) {
            switch (optionsTable.buttonCollection.pressedButtonName) {
                case "MuteToggle" -> {
                    game.mute = (muteToggle.toggled);
                    game.updateMusicInfo();
                }
                case "FPSToggle" -> game.fpsCounter = (fPSToggle.toggled);
                case "VsyncToggle" -> Gdx.graphics.setVSync(vSyncToggle.toggled);
                case "LoopToggle" -> game.loop = (loopToggle.toggled);
                case "SetTitleInputButton" -> MyGdxGame.title = setTitleInputButton.text;
                case "VolumeSlider" -> {
                    game.volume = volumeSlider.value;
                    game.updateMusicInfo();
                }
                case "FPSSlider" -> game.fpsCap = (int) fPSSlider.value;
                case "currentSongDropDown" -> {
                    if (currentSongDropDown.newItemSelected){
                        game.songPlaying = currentSongDropDown.getSelectedItem();
                        game.changeSong();
                    }
                }
                case "addSongButton" -> {
                    String fileName = getCustomSongLocation();
                    addCustomSong(fileName);
                    currentSongDropDown.setDropDowns(getSelectableSongs());
                }
            }
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

    public ArrayList<String> getSelectableSongs(){
        File dir = new File("core/assets/Music");
        String[] files = dir.list();
        ArrayList<String> songs = new ArrayList<String>();
        assert files != null;
        for (String file : files) {
            if (file.endsWith(".mp3")) {
                songs.add(file);
            }
        }
        return songs;
    }

    public void addCustomSong(String fileName){
        if (fileName != null) {
            File source = new File(fileName);
            String[] test2 = fileName.split("\\\\");
            String test3 = test2[test2.length - 1];
            String[] test4 = test3.split("\\.");
            String test5 = test4[0];
            File dest = new File("C:\\Users\\jamescoward\\Desktop\\Java\\MrWilordFour\\core\\assets\\Music\\" + test5 + ".mp3");
            try {
                Files.copy(source.toPath(), dest.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getCustomSongLocation(){
        JFileChooser fileChooser = new JFileChooser();
        FileFilter filter = new FileNameExtensionFilter("MP3 File","mp3");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(filter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showOpenDialog(null);
        return Objects.equals(String.valueOf(fileChooser.getSelectedFile()), "null") ? null : String.valueOf(fileChooser.getSelectedFile());
    }
}
