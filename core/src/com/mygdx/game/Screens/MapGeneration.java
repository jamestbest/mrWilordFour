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

    Slider slider;
    InputButtonTwo inputButtonTwo;
    ToggleButton toggle;
    Button refreshButton;
    Button button;
    Button button1;
    NumberInput numberInput;

    Label SeedLabel;
    Label ToggleRiverLabel;
    Label WidthLabel;
    Label HeightLabel;
    Label FrequencyLabel;
    Label RiverBendLabel;
    Label TreeDensityLabel;

    Table labelTable;
    Table buttonTable;

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

        height = (int) (height / 7f); // divide by the number of buttons

        slider = new Slider(0, 0, (int) (width * 0.9f), (int) (height * 0.2), "test");
        inputButtonTwo = new InputButtonTwo(0, 0, (int) (width * 0.9f), (int) (height * 0.7f), seed, "test2", inputMultiplexer);
        toggle = new ToggleButton(0, 0, (int) (width * 0.5f), (int) (height * 1.3), "test3", "");
        refreshButton = new Button(0, 0, 50, 50, "RefreshButton", "test4");

        button = new Button(0, 0, 50, 50, "RefreshButton", "test5");
        button1 = new Button(0, 0, 50, 50, "RefreshButton", "test6");
        numberInput = new NumberInputWithSides(0, 0, 50, 50, "", "test7", inputMultiplexer);

        SeedLabel = new Label(0, 0, 0, 0, "seedLabel", "Seed: ");
        ToggleRiverLabel = new Label(0, 0, 0, 0, "toggleRiverLabel", "Toggle River: ");
        WidthLabel = new Label(0, 0, 0, 0, "widthLabel", "Width: ");
        HeightLabel = new Label(0, 0, 0, 0, "heightLabel", "Height: ");
        FrequencyLabel = new Label(0, 0, 0, 0, "frequencyLabel", "Frequency: ");
        RiverBendLabel = new Label(0, 0, 0, 0, "riverBendLabel", "River Bend: ");
        TreeDensityLabel = new Label(0, 0, 0, 0, "treeDensityLabel", "Tree Density: ");

        labelTable.addAllWithRows(SeedLabel, ToggleRiverLabel, WidthLabel, HeightLabel, FrequencyLabel, RiverBendLabel, TreeDensityLabel);
        labelTable.sort();

        buttonTable.addAllWithRows(inputButtonTwo, toggle, slider, refreshButton, button, button1, numberInput);
        buttonTable.sortToFit();

        float fontScale = (int) (MyGdxGame.initialRes.y / 7f) / 100f;
        HeightLabel.setFontScale(fontScale);
        WidthLabel.setFontScale(fontScale);
        FrequencyLabel.setFontScale(fontScale);
        RiverBendLabel.setFontScale(fontScale);
        TreeDensityLabel.setFontScale(fontScale);
        SeedLabel.setFontScale(fontScale);
        ToggleRiverLabel.setFontScale(fontScale);

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
        }

        typing = inputButtonTwo.typing;
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
}
