package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.ui.elements.Button;
import com.mygdx.game.ui.elements.InputButtonTwo;
import com.mygdx.game.ui.elements.TextButton;
import com.mygdx.game.ui.extensions.ButtonCollection;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Scanner;

public class JoinGameScreen implements Screen {
    MyGdxGame myGdxGame;

    String ipAddress = "";

    InputButtonTwo ipInputButton;
    TextButton joinGameButton;
    Button refreshMapButton;

    ButtonCollection buttonCollection;
    CameraTwo cameraTwo;
    InputMultiplexer inputMultiplexer;

    HashMap<String, Texture> tileTextures = new HashMap<>();
    HashMap<String, TextureAtlas> thingTextures = new HashMap<>();

    SpriteBatch batch;

    Map map;

    Socket socket;

    public JoinGameScreen(MyGdxGame myGdxGame){
        this.myGdxGame = myGdxGame;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        inputMultiplexer = new InputMultiplexer();
        cameraTwo = new CameraTwo();

        map = new Map((int) ((int) MyGdxGame.initialRes.y / 10 * 6.8),
                (int) ((int) MyGdxGame.initialRes.x  - (MyGdxGame.initialRes.y / 10 * 7)),
                (int) MyGdxGame.initialRes.y / 10 * 3, "testSeed1");
        map.generateBlank();

        initialiseTextures();
        setupButtons();
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        map.drawMiniMap(batch, tileTextures, thingTextures);

        buttonCollection.drawButtons(batch);
        batch.end();

        if(Gdx.input.isButtonPressed(0)){
            buttonCollection.updateButtons(cameraTwo);

            if(buttonCollection.pressedButtonName.equals("joinGameButton")){
                myGdxGame.setScreen(new GameScreen(myGdxGame));
            }
        }

        if (Gdx.input.isButtonJustPressed(0)){
            if(buttonCollection.pressedButtonName.equals("RefreshButton")){
                System.out.println("Refreshing");
                connectSocket();
                createSocketListeners();
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

    public void initialiseTextures(){
        //loads every texture in the textures map
        GameScreen.getAllMapTextures(tileTextures, thingTextures);
    }

    public void setupButtons(){
        buttonCollection = new ButtonCollection();
        ipInputButton = new InputButtonTwo((int) (MyGdxGame.initialRes.x * 0.1), (int) ((int) MyGdxGame.initialRes.y * 0.8)
                , (int) (MyGdxGame.initialRes.x * 0.2), (int) (MyGdxGame.initialRes.y * 0.1), "8080",
                "ipAddressInputButton", inputMultiplexer);
        joinGameButton = new TextButton((int) (MyGdxGame.initialRes.x * 0.9), 0, (int) (MyGdxGame.initialRes.x * 0.1), (int) (MyGdxGame.initialRes.y * 0.05), "Join Game", "joinGameButton");
        refreshMapButton = new Button((int) ((ipInputButton.x + ipInputButton.width) * 1.05), ipInputButton.y, ipInputButton.height, ipInputButton.height, "RefreshButton", "RefreshButton");
        buttonCollection.add(ipInputButton, joinGameButton, refreshMapButton);
    }

    public void connectSocket() {
        try {
            System.out.println("Connecting to socket: " + "http://" + ipInputButton.text);
            socket = IO.socket("http://" + ipInputButton.text);
            socket.connect();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void createSocketListeners() {
        socket.on("connect", args -> System.out.println("Connected to server"));
        socket.on("connect_error", args -> System.out.println("Socket connect_error"));
    }
}
