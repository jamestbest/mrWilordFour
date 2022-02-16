package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Jif.GifWithMusicPlayer;
import com.mygdx.game.ui.elements.DropdownButton;
import com.mygdx.game.ui.elements.Label;
import com.mygdx.game.ui.elements.TextButton;
import com.mygdx.game.ui.extensions.Table;

public class MainMenu implements Screen {
    // TODO: 12/01/2022 change the button doing if clicked to adding in a function to each button
    Texture background;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    TextButton NewGame;
    TextButton LoadGame;
    TextButton JoinGame;
    TextButton Settings;
    TextButton Exit;

    DropdownButton test;

    Label title;

    Table table;

    CameraTwo camera;

    MyGdxGame myGdxGame;

    GifWithMusicPlayer GWMP;

    boolean playGif = false;

    InputMultiplexer inputMultiplexer = new InputMultiplexer();

    public MainMenu(MyGdxGame game) {
        myGdxGame = game;
    }

    @Override
    public void show() {
        background = new Texture("Textures/Backgrounds/MainMenu.jpg");
        camera = new CameraTwo();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        setUpUI();
        setupTable();

        test = new DropdownButton(100, 400, 200, 75, "test", "test1", inputMultiplexer);

        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.begin();
        batch.draw(background, 0, 0, MyGdxGame.initialRes.x, MyGdxGame.initialRes.y);

        table.draw(batch);
        title.draw(batch, 0);

        test.draw(batch, 0);

        batch.end();

        if (playGif) {
            GWMP.render();
        }

        if (Gdx.input.isButtonPressed(0)) {
            table.update(camera);
            int x = Gdx.input.getX();
            int y = Gdx.input.getY();
            y = (int) (MyGdxGame.initialRes.y - y);
            test.checkIfPressed(x, y);
        }
        if (table.buttonCollection.lastPressedButtonName.equals(NewGame.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                myGdxGame.setScreen(new MapGeneration(myGdxGame));
            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(LoadGame.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                if (!playGif) {
                    GWMP = new GifWithMusicPlayer("Globama", "Globama", "mp3");
                    playGif = true;
                }

            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(JoinGame.name)){
            if (!Gdx.input.isButtonPressed(0)) {
//                myGdxGame.setScreen(new GameScreen(myGdxGame));
                myGdxGame.setScreen(new JoinGameScreen(myGdxGame));
            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(Settings.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                myGdxGame.setScreen(new SettingsScreen(myGdxGame));
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

        title = new Label(0,0,100,50,"Title", "mR. Wilord");
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
