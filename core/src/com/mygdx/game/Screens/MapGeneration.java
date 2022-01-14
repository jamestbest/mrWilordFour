package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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

    Label SeedLabel;
    Label ToggleRiverLabel;
    Label WidthLabel;
    Label HeightLabel;
    Label FrequencyLabel;
    Label RiverBendLabel;
    Label TreeDensityLabel;

    Table optionTable;

    boolean typing = false;

    public MapGeneration(MyGdxGame game){
        this.game = game;
        map = new Map((int) MyGdxGame.initialRes.y / 5 * 4,(int) MyGdxGame.initialRes.y / 10, (int) MyGdxGame.initialRes.y / 10, seed);
        map.getAdditionFromSeed(seed);
        map.generateMap();

        batch = new SpriteBatch();

        camera.allowMovement = false;

        initialiseTextures();

        float width = (int) (((MyGdxGame.initialRes.x - MyGdxGame.initialRes.y) / 2f) - (int) MyGdxGame.initialRes.y / 10);
        float height = (int) MyGdxGame.initialRes.y / 5f * 3;
        optionTable = new Table((int) MyGdxGame.initialRes.y, (int) MyGdxGame.initialRes.y / 5, (int) width, (int) height);

        height /= optionTable.numberOfRows;
        slider = new Slider(900, 100, 100, 10, "test");
        inputButtonTwo = new InputButtonTwo(1000, 300, 400, 100, "", "test2");
        toggle = new ToggleButton(1000, 500, 100, 100, "test3", "");
        refreshButton = new Button(1000, 700, 50, 50, "RefreshButton", "test4");

        SeedLabel = new Label(0, 0, 0, 0, "seedLabel", "Seed: ");
        ToggleRiverLabel = new Label(0, 0, 0, 0, "toggleRiverLabel", "Toggle River: ");
        WidthLabel = new Label(0, 0, 0, 0, "widthLabel", "Width: ");
        HeightLabel = new Label(0, 0, 0, 0, "heightLabel", "Height: ");
        FrequencyLabel = new Label(0, 0, 0, 0, "frequencyLabel", "Frequency: ");
        RiverBendLabel = new Label(0, 0, 0, 0, "riverBendLabel", "River Bend: ");
        TreeDensityLabel = new Label(0, 0, 0, 0, "treeDensityLabel", "Tree Density: ");

        optionTable.addAllWithRows(SeedLabel, ToggleRiverLabel, WidthLabel, HeightLabel, FrequencyLabel, RiverBendLabel, TreeDensityLabel);
        optionTable.sort();
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
        optionTable.draw(batch);
        batch.end();

        if(!typing){
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
                map.settings.seed = "testSeed2";
                map.updateMap();
            }
        }

        if (Gdx.input.isButtonPressed(0)){
            optionTable.update(camera);
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
