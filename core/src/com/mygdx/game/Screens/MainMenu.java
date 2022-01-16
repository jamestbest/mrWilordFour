package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
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

    Label title;

    Table table;

    CameraTwo camera;

    MyGdxGame myGdxGame;

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
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, MyGdxGame.initialRes.x, MyGdxGame.initialRes.y);

        table.draw(batch);
        title.draw(batch);

        if (Gdx.input.isButtonPressed(0)) {
            table.update(camera);
        }
        if (table.buttonCollection.lastPressedButtonName.equals(NewGame.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                myGdxGame.setScreen(new ColonistSelectionScreen(myGdxGame));
            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(LoadGame.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                myGdxGame.setScreen(new MapGeneration(myGdxGame));
            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(JoinGame.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                myGdxGame.setScreen(new GameScreen(myGdxGame));
            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(Settings.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                myGdxGame.setScreen(new ColonistSelectionScreen(myGdxGame));
            }
        }
        if (table.buttonCollection.lastPressedButtonName.equals(Exit.name)){
            if (!Gdx.input.isButtonPressed(0)) {
                Gdx.app.exit();
            }
        }
        batch.end();
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
        title.centre(Gdx.graphics.getHeight() / 10 * 3);
        title.setFontColor(Color.BLUE);
        title.setFontScale(2f);
    }

    public void setupTable(){
        table = new Table(Gdx.graphics.getWidth() / 3,Gdx.graphics.getHeight() / 10 * 2, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 10 * 4);
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