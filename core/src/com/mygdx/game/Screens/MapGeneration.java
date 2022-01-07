package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.ui.elements.InputButton;
import com.mygdx.game.ui.elements.Slider;
import com.mygdx.game.ui.extensions.ButtonCollection;

import java.io.File;
import java.util.HashMap;

public class MapGeneration implements Screen {
    Map map;
    String seed = "testSeed1";

    CameraTwo camera = new CameraTwo();

    SpriteBatch batch;

    HashMap<String, Texture> textures = new HashMap<>();

    MyGdxGame game;

    ButtonCollection buttonCollection;
    Slider slider;
    InputButton inputButton;

    public MapGeneration(MyGdxGame game){
        this.game = game;
        map = new Map((int) MyGdxGame.initialRes.y / 5 * 4,(int) MyGdxGame.initialRes.y / 10, (int) MyGdxGame.initialRes.y / 10, seed);
        map.getAdditionFromSeed(seed);
        map.generateMap();

        batch = new SpriteBatch();

        camera.allowMovement = false;

        initialiseTextures();

        buttonCollection = new ButtonCollection();
        slider = new Slider(900, 100, 100, 10, "test");
        inputButton = new InputButton(900, 200, 400, 100, "test", "test1");
        buttonCollection.add(inputButton, slider);
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
        map.drawMiniMap(batch, textures);
        batch.end();
        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        buttonCollection.drawButtons(batch);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            map.settings.seed = "testSeed2";
            map.updateMap();
        }

        if (Gdx.input.isButtonPressed(0)){
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
