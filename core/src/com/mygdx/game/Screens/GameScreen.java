package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import java.util.Arrays;
import java.util.HashMap;

public class GameScreen implements Screen {
    public static int TILES_ON_X = 250;
    public static int TILES_ON_Y = 250;
    public static float TILE_DIMS = 20;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    CameraTwo camera;
    Vector2 moveOrigin = new Vector2(0, 0);

    String seed = "testSeed1"; //testSeed1
    int addition;

    Map map;
    ArrayList<Colonist> colonists;

    MyGdxGame game;

    boolean isHost;
    boolean isMultiplayer;
    private Socket socket;
    String socketID;

    HashMap<String, Texture> tileTextures = new HashMap<>();
    HashMap<String, TextureAtlas> thingTextures = new HashMap<>();
    HashMap<String, TextureAtlas> colonistClothes = new HashMap<>();

    float counter = 0f;
    float counterMax = 1f;
    public static float gameSpeed = 2f; //game speed

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

    public GameScreen(MyGdxGame game, ArrayList<Colonist> colonists, Map map) {
        this.game = game;
        this.isHost = true;
        this.map = map;

        if (isMultiplayer) {
            connectSocket(); //this is placed temp for hosting multiplayer, it will need to happen when they start a multiplayer session
            createSocketListeners();
        }

        Gdx.graphics.setVSync(false);
        Gdx.graphics.setForegroundFPS(Integer.MAX_VALUE);

        this.colonists = colonists;
    }

    public GameScreen(MyGdxGame game){
        this.game = game;
        this.isHost = false;
        connectSocket();
        createSocketListeners();

        Gdx.graphics.setVSync(false);
        Gdx.graphics.setForegroundFPS(Integer.MAX_VALUE);
    }

    @Override
    public void show() {
        initialiseTextures();

        if (isHost) {

        }
        else {
            MapSettings mapSettings = new MapSettings(seed);
            map = new Map(mapSettings);
            map.generateBlank();
            colonists = new ArrayList<>();
            System.out.println("Generating blank map");
        }

        setMapForColonists();
        setupColonistClothes();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new CameraTwo();
        camera.setMinMax(new Vector2(0,0), new Vector2(GameScreen.TILES_ON_X * TILE_DIMS, GameScreen.TILES_ON_Y * TILE_DIMS));

        Gdx.input.setInputProcessor(gameInputProcessor);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        map.drawMap(batch, tileTextures, thingTextures, camera);

        drawAllColonists(batch);
        batch.end();

        for (Colonist colonist : colonists) {
            colonist.drawPathOutline(camera);
        }

        counter += delta * gameSpeed;
        if (counter > counterMax) {
            update();
            counter = 0f;
        }

        Gdx.graphics.setTitle("FPS: " + (Gdx.graphics.getFramesPerSecond() + 30));

        if (Gdx.input.isButtonJustPressed(0)) {
            Vector2 mousePos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            int x = (int) (mousePos.x / TILE_DIMS);
            int y = (int) (mousePos.y / TILE_DIMS);
            map.changeTileType(x, y, "grass");
            System.out.println("Tile at " + x + ", " + y + " changed to grass");
            JSONObject tileChange = new JSONObject();
            try {
                tileChange.put("x", x);
                tileChange.put("y", y);
                tileChange.put("type", "grass");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map.updateBooleanMap();
            if (isMultiplayer){
                socket.emit("changeTileType", tileChange);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            Vector2 mousePos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            int x = (int) (mousePos.x / TILE_DIMS);
            int y = (int) (mousePos.y / TILE_DIMS);
            if (map.isWithinBounds(x, y)) {
                colonists.get(0).setMoveToPos(x,y);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            Gdx.app.log("Multiplayer", "enabling multiplayer");
            connectSocket();
            createSocketListeners();
            isMultiplayer = true;
        }
    }

    public void update(){ //happens at a rate determined by the gameSpeed
        moveColonists();
        batch.begin();

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

    public void initialiseTextures(){
        //loads every texture in the textures map
        File directory= new File("core/assets/Textures/TileTextures");
        String[] files = directory.list();
        assert files != null;
        for (String fileName : files) {
            String[] temp = fileName.split("\\.");
            tileTextures.put(temp[0], new Texture(Gdx.files.internal("core/assets/Textures/TileTextures/" + fileName)));
        }

        File directory2= new File("core/assets/Textures/ThingTextures");
        String[] files2 = directory2.list();
        assert files2 != null;
        for (String fileName : files2) {
            String[] temp = fileName.split("\\.");
            System.out.println(Arrays.toString(temp));
            if (temp[1].equals("atlas")){
                thingTextures.put(temp[0], new TextureAtlas(Gdx.files.internal("core/assets/Textures/ThingTextures/" + fileName)));
            }
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
            try {
                System.out.println(colonists.size());
                socket.emit("loadColonists", json.toJson(colonists));
            }
            catch (Exception e) {
                System.out.println("Error in creating map json");
            }
        });

        socket.on("loadColonists", args -> {
            colonists = json.fromJson(ArrayList.class, args[0].toString());
            System.out.println("Loaded colonists " + colonists.size());
        });

        socket.on("changeTileType", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                int x = (int) data.get("x");
                int y = (int) data.get("y");
                String type = (String) data.get("type");
                map.changeTileType(x, y, type);
                map.updateBooleanMap();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void drawAllColonists(SpriteBatch batch){
        for (Colonist c : colonists) {
            c.draw(batch, GameScreen.TILE_DIMS, colonistClothes);
        }
    }

    public void moveColonists(){
        for (Colonist c : colonists) {
            c.moveColonist();
        }
    }

    public void setMapForColonists(){
        for (Colonist c : colonists) {
            c.setMap(map);
        }
    }

    public void setupColonistClothes(){
        File directory= new File("core/assets/Textures/TAResources");
        String[] files = directory.list();
        assert files != null;
        for (String fileName : files) {
            String[] temp = fileName.split("\\.");
            if (temp[1].equals("atlas")){
                colonistClothes.put(temp[0], new TextureAtlas(Gdx.files.internal("core/assets/Textures/TAResources/" + fileName)));
            }
        }
    }
}
