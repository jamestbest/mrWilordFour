package com.mygdx.game.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.ui.elements.*;
import com.mygdx.game.ui.extensions.ButtonCollection;
import com.mygdx.game.ui.extensions.Table;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class MapGeneration implements Screen {
    Map map;
    String seed = "testSeed1";

    CameraTwo camera = new CameraTwo();

    SpriteBatch batch;

    HashMap<String, Texture> tileTextures = new HashMap<>();
    HashMap<String, TextureAtlas> thingTextures = new HashMap<>();

    MyGdxGame game;

    InputButtonTwo seedInput;
    ToggleButton riverToggle;
    NumberInput dimsInput;
    SliderWithLabel freqSlider;
    SliderWithLabel riverBendSlider;
    SliderWithLabel treeDensitySlider;
    Button refreshButton;

    TextButton continueButton;

    Label SeedLabel;
    Label ToggleRiverLabel;
    Label dimsLabel;
    Label FrequencyLabel;
    Label RiverBendLabel;
    Label TreeDensityLabel;
    Label RefreshLabel;

    Label Title;

    Table labelTable;
    Table buttonTable;
    ButtonCollection extraUI;

    boolean typing = false;

    InputMultiplexer inputMultiplexer;

    public MapGeneration(MyGdxGame game){

        // FIXED: 17/01/2022 The slider doesn't appear if the window is resized before it spawns in
        // TODO: 25/01/2022 AMOGUS MAP
        this.game = game;

        inputMultiplexer = new InputMultiplexer();

        map = new Map((int) MyGdxGame.initialRes.y / 5 * 4,(int) MyGdxGame.initialRes.y / 10, (int) MyGdxGame.initialRes.y / 10, seed);
        map.getAdditionFromSeed(seed);
        map.generateMap();

        batch = new SpriteBatch();

        camera.allowMovement = false;

        initialiseTextures();

        float width = (int) (((MyGdxGame.initialRes.x - MyGdxGame.initialRes.y) / 2f) - (int) MyGdxGame.initialRes.y / 10);
        float height = (int) MyGdxGame.initialRes.y / 5f * 3;
        labelTable = new Table((int) MyGdxGame.initialRes.y, (int) MyGdxGame.initialRes.y / 5, (int) width, (int) height);
        buttonTable = new Table((int) (MyGdxGame.initialRes.y + (width * 1.3f)), (int) MyGdxGame.initialRes.y / 5, (int) width, (int) height);
        extraUI = new ButtonCollection();

        Title = new Label((int) MyGdxGame.initialRes.x / 47 * 32, (int) MyGdxGame.initialRes.y / 5 * 4, (int) (MyGdxGame.initialRes.x / 10 * 5),
                (int) MyGdxGame.initialRes.y / 4, "Title", "Edit your map");
        Title.setSize((int) (MyGdxGame.initialRes.x / 10 * 2), (int) MyGdxGame.initialRes.y / 5);
        Title.setFontScale(5f);
        Title.resizeFontToCorrectProportionByWidth();

        extraUI.add(Title);

        height = (int) (height / 8f); // divide by the number of buttons

        seedInput = new InputButtonTwo(0, 0, (int) (width * 0.9f), (int) (height * 0.7f), seed, "seedInput", inputMultiplexer);
        riverToggle = new ToggleButton(0, 0, (int) (width * 0.4f), (int) (height * 1.4), "riverToggle", true);
        dimsInput = new NumberInputWithSides(0, 0, (int) (width * 0.30f), (int) (height * 0.5f), "test4", "widthInput", inputMultiplexer, 1, 500, 250);
        freqSlider = new SliderWithLabel(0, 0, (int) (width * 0.9f), (int) (height * 0.2), "freqSlider", 7, 1, 1, 3);
        riverBendSlider = new SliderWithLabel(0, 0, (int) (width * 0.9f), (int) (height * 0.2f), "riverBendSlider", 100, 0, 1, 50);
        treeDensitySlider = new SliderWithLabel(0, 0, (int) (width * 0.9f), (int) (height * 0.2f), "treeDensitySlider", 50, 0, 1, 5);
        refreshButton = new Button(0, 0, (int) (height * 0.85f), (int) (height * 0.85f),
                "RefreshButton", "RefreshButton");

        continueButton = new TextButton((int) (MyGdxGame.initialRes.x), (int) (height * 0.3), (int) (width * 0.5f), (int) (height * 0.8f), "Continue", "Continue");
        continueButton.autoSize();
        continueButton.translate(-continueButton.width * 1.5f, continueButton.height * 0.5f);
        extraUI.add(continueButton);

        SeedLabel = new Label("seedLabel", "Seed: ");
        ToggleRiverLabel = new Label("toggleRiverLabel", "Toggle River: ");
        dimsLabel = new Label("widthLabel", "Dims: ");
        FrequencyLabel = new Label("frequencyLabel", "Frequency: ");
        RiverBendLabel = new Label("riverBendLabel", "River Bend: ");
        TreeDensityLabel = new Label("treeDensityLabel", "Tree Density: ");
        RefreshLabel = new Label("refreshLabel", "Refresh Map: ");

        labelTable.addAllWithRows(SeedLabel, ToggleRiverLabel, dimsLabel, FrequencyLabel, RiverBendLabel, TreeDensityLabel, RefreshLabel);
        labelTable.sort();

        buttonTable.addAllWithRows(seedInput, riverToggle, dimsInput, freqSlider, riverBendSlider, treeDensitySlider, refreshButton);
        buttonTable.sortToFit();

        setAllToCorrectFontSize();

        System.out.println(freqSlider.x + " " + freqSlider.y + " " + freqSlider.width + " " + freqSlider.height);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(64/255f,87/255f,132/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        map.drawMiniMap(batch, tileTextures, thingTextures);
        batch.end();
        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        labelTable.draw(batch);
        buttonTable.draw(batch);
        extraUI.drawButtons(batch);
        batch.end();

        if(!typing){
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
                map.settings.seed = "testSeed2";
                map.updateMap();
            }
        }

        if (Gdx.input.isButtonPressed(0)){
            labelTable.update(camera);
            buttonTable.update(camera);
            extraUI.updateButtons(camera);
        }

        if (buttonTable.buttonCollection.pressedButtonName.equals("RefreshButton") && Gdx.input.isButtonJustPressed(0)){
            map.settings.seed = seedInput.text;
            GameScreen.TILES_ON_X = Integer.parseInt(dimsInput.text);
            GameScreen.TILES_ON_X = Integer.parseInt(dimsInput.text);
            map.settings.perlinFrequency = (int) freqSlider.value;
            map.settings.riverBend = (int) riverBendSlider.value * 50;
            map.settings.treeFreq = (int) treeDensitySlider.value;
            map.settings.riverToggle = riverToggle.toggled;

            map.updateMap();
        }

        if (extraUI.pressedButtonName.equals("Continue") && Gdx.input.isButtonJustPressed(0)){
            game.setScreen(new ColonistSelectionScreen(game, map));
        }

        typing = seedInput.typing || dimsInput.typing;
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

    public void initialiseTextures(){
        //loads every texture in the textures map
        FileHandle directory = Gdx.files.internal("core/assets/Textures/TileTextures");
        FileHandle[] files = directory.list();
        assert files != null;
        for (FileHandle fileName : files) {
            String[] temp = fileName.name().split("\\.");
            tileTextures.put(temp[0], new Texture(Gdx.files.internal(fileName.path())));
        }
        FileHandle directory2= Gdx.files.internal("core/assets/Textures/ThingTextures");
        FileHandle[] files2 = directory2.list();
        assert files2 != null;
        for (FileHandle fileName : files2) {
            String[] temp = fileName.name().split("\\.");
            if (temp[1].equals("atlas")) {
                thingTextures.put(temp[0], new TextureAtlas(Gdx.files.internal(fileName.path())));
            }
        }
    }

    public void setAllToCorrectFontSize(){
        float fontScale = (int) (MyGdxGame.initialRes.y / 7f) / 100f;
        dimsLabel.setFontScale(fontScale);
        FrequencyLabel.setFontScale(fontScale);
        RiverBendLabel.setFontScale(fontScale);
        TreeDensityLabel.setFontScale(fontScale);
        SeedLabel.setFontScale(fontScale);
        ToggleRiverLabel.setFontScale(fontScale);
        RefreshLabel.setFontScale(fontScale);


        seedInput.setFontScale(fontScale);
        dimsInput.setFontScale(fontScale);
        freqSlider.setFontScale(fontScale);
        riverBendSlider.setFontScale(fontScale);
        treeDensitySlider.setFontScale(fontScale);
    }
}
