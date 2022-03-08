package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.ui.elements.*;
import com.mygdx.game.ui.elements.Label;
import com.mygdx.game.ui.extensions.Table;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

public class SettingsScreen implements Screen {

    // TODO: 27/02/2022 add the font drop down box
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
    DropdownButton fontDropDown;
    ToggleButton loopToggle;
    Label addSongLabel;
    ImgOnlyButton addSongButton;

    ToggleButton fPSToggle;
    SliderWithLabel fPSSlider;
    ToggleButton vSyncToggle;

    Label fontLabel;
    InputButtonTwo setTitleInputButton;

    InputMultiplexer inputMultiplexer;

    SpriteBatch batch;
    CameraTwo camera;
    Viewport viewport;

    Texture background = new Texture("Textures/Backgrounds/optionsMenu.jpg");

    MyGdxGame game;

    int RRBeforeVS = -1;
    int fpsBeforeSwitchingToUnCapped = 145;

    boolean fromGame;

    boolean fontChanged = false;

    public SettingsScreen(MyGdxGame game, boolean fromGame){
        MyGdxGame.initialRes = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.game = game;
        this.fromGame = fromGame;

        inputMultiplexer = new InputMultiplexer();
        batch = new SpriteBatch();
        camera = new CameraTwo();
        camera.allowMovement = false;

        viewport = new StretchViewport(MyGdxGame.initialRes.x, MyGdxGame.initialRes.y);

        optionsTable = new Table(0, 0, (int) MyGdxGame.initialRes.x, (int) MyGdxGame.initialRes.y);
        volumeSlider = new SliderWithLabel("VolumeSlider");
        volumeSlider.setValue(game.volume);
        muteToggle = new ToggleButton("MuteToggle", game.mute);
        currentSongDropDown = new DropdownButton("currentSongDropDown", inputMultiplexer);
        fontDropDown = new DropdownButton("fontDropDown", inputMultiplexer);
        loopToggle = new ToggleButton("LoopToggle", game.loop);
        addSongLabel = new Label( "addSongLabel", "add custom song");
        addSongButton = new ImgOnlyButton("addSongButton", "uploadButton");

        currentSongDropDown.pressedLayer = 1;
        fontDropDown.pressedLayer = 1;

        currentSongDropDown.setDropDownsForMusic(getSelectableSongs(), game);
        fontDropDown.setDropDownsForFont(getSelectableFonts());
        fontDropDown.drawDown = false;
        fontDropDown.isForFont = true;

        fPSToggle = new ToggleButton("FPSToggle", game.fpsCounter);
        fPSSlider = new SliderWithLabel("FPSSlider", 145, 30, 1, game.fpsCap);
        if (game.fpsCap == Integer.MAX_VALUE){
            handleMaxFps();
        }
        vSyncToggle = new ToggleButton("VsyncToggle", game.vsyncEnabled);

        fontLabel = new Label( "FontLabel", "Font: ");
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
        optionsTable.add(addSongLabel, addSongButton, fontLabel, fontDropDown);

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
                case "VsyncToggle" -> {
                    game.vsyncEnabled = (vSyncToggle.toggled);
                    Gdx.graphics.setVSync(vSyncToggle.toggled);
                    if (!game.vsyncEnabled) {
                        revertToOldFPS();
                    }
                    else {
                        setFPSToMonitorRR();
                    }
                }
                case "LoopToggle" -> game.loop = (loopToggle.toggled);
                case "SetTitleInputButton" -> {
                    updateTitle(setTitleInputButton.text);
                }
                case "VolumeSlider" -> {
                    game.volume = volumeSlider.value;
                    game.updateMusicInfo();
                }
                case "FPSSlider" -> {
                    game.fpsCap = (int) fPSSlider.value;
                    Gdx.graphics.setForegroundFPS(game.fpsCap);
                    handleMaxFps();
                }
                case "currentSongDropDown" -> {
                    if (currentSongDropDown.newItemSelected){
                        game.songPlaying = currentSongDropDown.getSelectedItem();
                        game.changeSong();
                    }
                }
                case "addSongButton" -> {
                    String fileName = getCustomSongLocation();
                    addCustomSong(fileName);
                    currentSongDropDown.setDropDownsForMusic(getSelectableSongs(), game);
                }
                case "fontDropDown" -> {
                    if (!Objects.equals(fontDropDown.getSelectedItem(), MyGdxGame.fontName)){
                        MyGdxGame.fontName = fontDropDown.getSelectedItem();
                        game.setScreen(new SettingsScreen(game, false));
                    }
                }
            }
        }

        if (setTitleInputButton.typing){
            updateTitle(setTitleInputButton.text);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            MyGdxGame.initialRes = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            if (fromGame) {
                game.setScreen(game.currentGameScreen);
            }
            else {
                game.setScreen(game.mainMenu);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
                songs.add(file.split("\\.")[0]);
            }
        }
        return songs;
    }

    public ArrayList<String> getSelectableFonts(){
        File dir = new File("core/assets/Fonts");
        String[] files = dir.list();
        ArrayList<String> fonts = new ArrayList<String>();
        assert files != null;
        for (String file : files) {
            if (file.endsWith(".fnt")) {
                fonts.add(file.split("\\.")[0]);
            }
        }
        return fonts;
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

    public void updateTitle(String title){
        MyGdxGame.title = title;
        Gdx.graphics.setTitle(MyGdxGame.title);
    }

    public void setFPSToMonitorRR() {
        System.out.println("Setting FPS to monitor refresh rate");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        DisplayMode dm = gs.getDisplayMode();
        int refreshRate = dm.getRefreshRate();

        RRBeforeVS = game.fpsCap;
        game.fpsCap = refreshRate;
        fPSSlider.setValue(refreshRate);
    }

    public void revertToOldFPS(){
        System.out.println("Reverting to old FPS " + RRBeforeVS);
        game.fpsCap = RRBeforeVS;
        fPSSlider.setValue(RRBeforeVS);
    }

    public void handleMaxFps(){
        if (game.fpsCap >= fpsBeforeSwitchingToUnCapped){
            game.fpsCap = Integer.MAX_VALUE;
            Gdx.graphics.setForegroundFPS(game.fpsCap);
            fPSSlider.setText("no cap");
        }
    }
}
