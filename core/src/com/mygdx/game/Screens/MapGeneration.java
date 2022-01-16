package com.mygdx.game.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.ui.elements.*;
import com.mygdx.game.ui.extensions.ButtonCollection;
import com.mygdx.game.ui.extensions.Table;

import java.io.File;
import java.util.HashMap;

public class MapGeneration implements Screen {
    Map map;
    String seed = "testSeed1";

    CameraTwo camera = new CameraTwo();

    SpriteBatch batch;

    HashMap<String, Texture> textures = new HashMap<>();

    MyGdxGame game;


    InputButtonTwo seedInput;
    ToggleButton riverToggle;
    NumberInput widthInput;
    NumberInput heightInput;
    SliderWithLabel freqSlider;
    SliderWithLabel riverBendSlider;
    SliderWithLabel treeDensitySlider;
    Button RefreshButton;

    Label SeedLabel;
    Label ToggleRiverLabel;
    Label WidthLabel;
    Label HeightLabel;
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

        seedInput = new InputButtonTwo(0, 0, (int) (width * 0.9f), (int) (height * 0.7f), seed, "test2", inputMultiplexer);
        riverToggle = new ToggleButton(0, 0, (int) (width * 0.4f), (int) (height * 1.4), "test3", "");
        widthInput = new NumberInputWithSides(0, 0, (int) (width * 0.25f), (int) (height * 0.5f), "test4", "test5", inputMultiplexer);
        heightInput = new NumberInputWithSides(0, 0, (int) (width * 0.25f), (int) (height * 0.5f), "test6", "test7", inputMultiplexer);
        freqSlider = new SliderWithLabel(0, 0, (int) (width * 0.9f), (int) (height * 0.2), "test", 5, 1, 1);
        riverBendSlider = new SliderWithLabel(0, 0, (int) (width * 0.9f), (int) (height * 0.2f), "test8", 100, 0, 1);
        treeDensitySlider = new SliderWithLabel(0, 0, (int) (width * 0.9f), (int) (height * 0.2f), "test9", 100, 20, 1);
        RefreshButton = new Button(0, 0, (int) (height * 0.85f), (int) (height * 0.85f),
                "RefreshButton", "Refresh");

        SeedLabel = new Label(0, 0, 0, 0, "seedLabel", "Seed: ");
        ToggleRiverLabel = new Label(0, 0, 0, 0, "toggleRiverLabel", "Toggle River: ");
        WidthLabel = new Label(0, 0, 0, 0, "widthLabel", "Width: ");
        HeightLabel = new Label(0, 0, 0, 0, "heightLabel", "Height: ");
        FrequencyLabel = new Label(0, 0, 0, 0, "frequencyLabel", "Frequency: ");
        RiverBendLabel = new Label(0, 0, 0, 0, "riverBendLabel", "River Bend: ");
        TreeDensityLabel = new Label(0, 0, 0, 0, "treeDensityLabel", "Tree Density: ");
        RefreshLabel = new Label(0, 0, 0, 0, "refreshLabel", "Refresh Map: ");

        labelTable.addAllWithRows(SeedLabel, ToggleRiverLabel, WidthLabel, HeightLabel, FrequencyLabel, RiverBendLabel, TreeDensityLabel, RefreshLabel);
        labelTable.sort();

        buttonTable.addAllWithRows(seedInput, riverToggle, widthInput, heightInput, freqSlider, riverBendSlider, treeDensitySlider, RefreshButton);
        buttonTable.sortToFit();

        setAllToCorrectFontSize();

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
        map.drawMiniMap(batch, textures);
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

        typing = seedInput.typing || widthInput.typing || heightInput.typing;
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
        File directory= new File("core/assets/Textures/TileTextures");
        String[] files = directory.list();
        assert files != null;
        for (String fileName : files) {
            String[] temp = fileName.split("\\.");
            textures.put(temp[0], new Texture(Gdx.files.internal("core/assets/Textures/TileTextures/" + fileName)));
        }
    }

    public void setAllToCorrectFontSize(){
        float fontScale = (int) (MyGdxGame.initialRes.y / 7f) / 100f;
        HeightLabel.setFontScale(fontScale);
        WidthLabel.setFontScale(fontScale);
        FrequencyLabel.setFontScale(fontScale);
        RiverBendLabel.setFontScale(fontScale);
        TreeDensityLabel.setFontScale(fontScale);
        SeedLabel.setFontScale(fontScale);
        ToggleRiverLabel.setFontScale(fontScale);
        RefreshLabel.setFontScale(fontScale);


        seedInput.setFontScale(fontScale);
        widthInput.setFontScale(fontScale);
        heightInput.setFontScale(fontScale);
        freqSlider.setFontScale(fontScale);
        riverBendSlider.setFontScale(fontScale);
        treeDensitySlider.setFontScale(fontScale);
    }
}