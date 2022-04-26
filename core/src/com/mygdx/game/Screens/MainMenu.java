package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Jif.GifWithMusicPlayer;
import com.mygdx.game.ui.elements.Label;
import com.mygdx.game.ui.elements.TextButton;
import com.mygdx.game.ui.extensions.Table;

import java.util.Arrays;

public class MainMenu implements Screen {
    Texture background;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    TextButton NewGame;
    TextButton LoadGame;
    TextButton JoinGame;
    TextButton Settings;
    TextButton Exit;

    Label title;

    Table table;

    CameraTwo camera;

    MyGdxGame game;

    GifWithMusicPlayer GWMP;

    boolean playGif = false;

    boolean acceptInput = false;

    InputMultiplexer inputMultiplexer = new InputMultiplexer();

    public MainMenu(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        MyGdxGame.initialRes = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        background = new Texture("Textures/Backgrounds/MainMenu.jpg");
        camera = new CameraTwo();
        camera.allowMovement = false;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        setUpUI();
        setupTable();

        game.clickWaitTimer = 0.2f;
        acceptInput = false;

        game.clearStack();
        game.addToStack(this);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (game.clickWaitTimer <= 0) {
            acceptInput = true;
        }
        else {
            game.clickWaitTimer -= delta;
        }

        camera.update();

        batch.begin();
        batch.draw(background, 0, 0, MyGdxGame.initialRes.x, MyGdxGame.initialRes.y);

        table.draw(batch);
        title.draw(batch, 0);

        batch.end();

        if (playGif) {
            GWMP.render();
        }

        if (Gdx.input.isButtonPressed(0) && acceptInput) {
            table.update(camera);
        }

        if (table.buttonCollection.lastPressedButtonName.equals(NewGame.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                game.setScreen(new MapGeneration(game));
            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(LoadGame.name)){
            game.setScreen(new LoadSaveScreen2(game));
        }
        if (table.buttonCollection.lastPressedButtonName.equals(JoinGame.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                game.setScreen(new JoinGameScreen(game));
            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(Settings.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                game.setScreen(new SettingsScreen(game, false));
            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(Exit.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                Gdx.app.exit();
            }
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

    public void setUpUI(){
        NewGame = new TextButton(0,0,10,10,"New Game", "New Game");
        LoadGame = new TextButton(0,0,10,10,"Load Game", "Load Game");
        JoinGame = new TextButton(0,0,10,10,"Join Game", "Join Game");
        Settings = new TextButton(0,0,10,10,"Settings", "Settings");
        Exit = new TextButton(0,0,10,10,"Exit", "Exit");

        title = new Label(0,0,100,50,"Title", MyGdxGame.title);
        title.centre((int) (MyGdxGame.initialRes.y / 10 * 3));
        title.setFontColor(Color.BLUE);
        title.setFontScale(2f);
    }

    public void setupTable(){
        table = new Table((int) (MyGdxGame.initialRes.x / 3), (int) (MyGdxGame.initialRes.y / 10 * 2),
                (int) (MyGdxGame.initialRes.x / 3), (int) (MyGdxGame.initialRes.y / 10 * 4));
        table.add(NewGame);
        table.row();
        table.add(LoadGame);
        table.row();
        table.add(JoinGame);
        table.row();
        table.add(Settings);
        table.row();
        table.add(Exit);

        table.sort();

        NewGame.resizeFontToCorrectProportionByHeight();
        LoadGame.resizeFontToCorrectProportionByHeight();
        JoinGame.resizeFontToCorrectProportionByHeight();
        Settings.resizeFontToCorrectProportionByHeight();
        Exit.resizeFontToCorrectProportionByHeight();
    }
}
