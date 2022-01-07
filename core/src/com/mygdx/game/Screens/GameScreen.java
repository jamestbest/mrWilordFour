package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.Colonist;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.MapSettings;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class GameScreen implements Screen {
    public static final int TILES_ON_X = 250;
    public static final int TILES_ON_Y = 250;
    public static final float TILE_DIMS = 20;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    CameraTwo camera;
    Vector2 moveOrigin = new Vector2(0, 0);

    String seed = "testSeed1"; //testSeed1
    int addition;

    Map map;
    ArrayList<Colonist> colonists;

    MyGdxGame game;

    boolean joiningMultiplayer;
    private Socket socket;
    String socketID;

    HashMap<String, Texture> textures = new HashMap<>();

    InputProcessor gameInputProcessor = new InputProcessor() {
        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            moveOrigin = camera.unproject(new Vector2(screenX, screenY));
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (Gdx.input.isButtonPressed(1)){
                Vector2 temp = camera.unproject(new Vector2(screenX, screenY));
                camera.move(moveOrigin.x - temp.x, moveOrigin.y - temp.y);
            }
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            //this moves the camera making sure that the tile that the mouse is over is always under the mouse when scrolling
            //allow you to scroll into a position - see ref/cameraZoom.png
            Vector2 startPos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            camera.handleZoom(amountY);
            Vector2 endPos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            camera.move(startPos.x - endPos.x, startPos.y - endPos.y);
            return false;
        }
    };

    public GameScreen(MyGdxGame game, boolean joiningMultiplayer) {
        this.game = game;
        this.joiningMultiplayer = joiningMultiplayer;

        connectSocket();
        createSocketListeners();

        Gdx.graphics.setVSync(false);
        Gdx.graphics.setForegroundFPS(Integer.MAX_VALUE);
    }

    @Override
    public void show() {
        initialiseTextures();
        MapSettings mapSettings = new MapSettings(seed);
        map = new Map(mapSettings);
        if (!joiningMultiplayer) {
            addition = map.getAdditionFromSeed(seed);
            map.generateMap();
        }
        else {
            map.generateBlank();
            System.out.println("Generating blank map");
        }

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new CameraTwo();
        camera.setMinMax(new Vector2(0,0), new Vector2(map.settings.width * TILE_DIMS, map.settings.height * TILE_DIMS));

        Gdx.input.setInputProcessor(gameInputProcessor);

        Json json = new Json();
        colonists = json.fromJson(ArrayList.class, Gdx.files.internal("ColonistInformation/Backstories"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        map.drawMap(batch, textures, camera);
        batch.end();

        Gdx.graphics.setTitle("FPS: " + Gdx.graphics.getFramesPerSecond());
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

    public void connectSocket() {
        try {
            socket = IO.socket("http://localhost:8080");
            socket.connect();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void createSocketListeners() {
        Json json = new Json();

        socket.on("connect", args -> System.out.println("Connected to server"));
        socket.on("newPlayer", args -> {
            try {
                socket.emit("loadWorld", json.toJson(map));
            }
            catch (Exception e) {
                System.out.println("Error in creating map json");
            }

        });
        socket.on("connect_error", args -> System.out.println("Socket connect_error"));
        socket.on("socketID", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                socketID = (String) data.get("id");
                System.out.println("Socket ID: " + socketID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        socket.on("sayID", args -> System.out.println("ID: " + socketID));
        socket.on("loadWorld", args -> {
            map = json.fromJson(Map.class, args[0].toString());
            System.out.println("Loaded world " + map.addition);
        });
    }
}
