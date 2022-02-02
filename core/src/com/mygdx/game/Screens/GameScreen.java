package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Game.CameraTwo;
import com.mygdx.game.Game.Colonist;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Game.Task;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.MapSettings;
import com.mygdx.game.Generation.Tile;
import com.mygdx.game.Saving.RLE;
import com.mygdx.game.ui.elements.Button;
import com.mygdx.game.ui.elements.ImgButton;
import com.mygdx.game.ui.elements.TextButton;
import com.mygdx.game.ui.extensions.ButtonCollection;
import com.sun.tools.javac.Main;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class GameScreen implements Screen {
    public static int TILES_ON_X = 250;
    public static int TILES_ON_Y = 250;
    public static float TILE_DIMS = 20;

    SpriteBatch batch;
    SpriteBatch batchWithNoProj;
    ShapeRenderer shapeRenderer;

    CameraTwo camera;
    Vector2 moveOrigin = new Vector2(0, 0);

    String seed = "testSeed1"; //testSeed1

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
    HashMap<String, Texture> actionSymbols = new HashMap<>();

    float counter = 0f;
    float counterMax = 1f;
    public static float gameSpeed = 2f; //game speed

    ButtonCollection bottomBarButtons;
    ButtonCollection ordersButtons;
    ButtonCollection buildingButtons;

    HashMap<String, ArrayList<String>> orderTypes;

    String taskTypeSelected = "Mine";
    ArrayList<Task> tasks = new ArrayList<>();

    boolean cancelSelection;

    // TODO: 30/01/2022 add the selection rect and then add tasks based on the type and if the tile type is a match
    // TODO: 02/02/2022 Some of the tasks need to be drawn above the things and others below - gl
    // TODO: 02/02/2022 need to change how the colonists get tasks

    Vector2 minSelecting = new Vector2(0, 0);
    Vector2 maxSelecting = new Vector2(0, 0);

    boolean mapLoaded = false;

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
            minSelecting = camera.unproject(new Vector2(screenX, screenY));
            maxSelecting = minSelecting;
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (!cancelSelection) {
                setTasksFromSelection(taskTypeSelected);
            }
            cancelSelection = false;
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (Gdx.input.isButtonPressed(1)){
                Vector2 temp = camera.unproject(new Vector2(screenX, screenY));
                camera.move(moveOrigin.x - temp.x, moveOrigin.y - temp.y);
            }
            if (Gdx.input.isButtonPressed(0)){
                maxSelecting = camera.unproject(new Vector2(screenX, screenY));
                if (cancelSelection){
                    minSelecting = maxSelecting;
                }
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

        Gdx.graphics.setVSync(false);
        Gdx.graphics.setForegroundFPS(Integer.MAX_VALUE);

        this.colonists = colonists;
    }

    public GameScreen(MyGdxGame game){
        this.game = game;
        this.isHost = false;
        this.isMultiplayer = true;
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

        setupColonistClothes();
        setupBBB();
        setupOrdersButtons();

        setupOrderTypes();
        setupActionSymbols();

        batch = new SpriteBatch();
        batchWithNoProj = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new CameraTwo();
        camera.setMinMax(new Vector2(0,0), new Vector2(GameScreen.TILES_ON_X * TILE_DIMS, GameScreen.TILES_ON_Y * TILE_DIMS));

        Gdx.input.setInputProcessor(gameInputProcessor);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//        System.out.println(minSelecting + " :minSelecting");
//        System.out.println(maxSelecting + " :max selecting");

        camera.update();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix);
        map.drawMap(batch, tileTextures, camera);

        drawTaskType(batch);

        drawAllColonists(batch);

        map.drawThings(batch, thingTextures, camera);

        batch.end();

        batchWithNoProj.begin();
        bottomBarButtons.drawButtons(batchWithNoProj);
        ordersButtons.drawButtons(batchWithNoProj);
        batchWithNoProj.end();

        for (Colonist colonist : colonists) {
            colonist.drawPathOutline(camera, shapeRenderer);
        }

        counter += delta * gameSpeed;
        if (counter > counterMax) {
            update();
            counter = 0f;
        }

        Gdx.graphics.setTitle(MyGdxGame.title + "     FPS: " + (Gdx.graphics.getFramesPerSecond()));

        if (Gdx.input.isButtonPressed(0)) {
            if (!(bottomBarButtons.updateButtons(camera) || ordersButtons.updateButtons(camera))){
                drawSelectionScreen(shapeRenderer, camera);
            }
            else {
                cancelSelection = true;
            }
        }

        if (Gdx.input.isButtonJustPressed(0)){
            if (bottomBarButtons.updateButtons(camera)){
                if (bottomBarButtons.pressedButtonName.equals("OrdersButton")){
                    ordersButtons.showButtons = !ordersButtons.showButtons;
                }
            }

            if (ordersButtons.updateButtons(camera)){
                switch (ordersButtons.pressedButtonName) {
                    case "MineButton" -> taskTypeSelected = "Mine";
                    case "CutDownButton" -> taskTypeSelected = "CutDown";
                    case "PlantButton" -> taskTypeSelected = "Plant";
                    case "HarvestButton" -> taskTypeSelected = "Harvest";
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            Vector2 mousePos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            int x = (int) (mousePos.x / TILE_DIMS);
            int y = (int) (mousePos.y / TILE_DIMS);
            if (map.isWithinBounds(x, y)) {
                colonists.get(0).setMoveToPos(x,y, map);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            Gdx.app.log("Multiplayer", "enabling multiplayer");
            connectSocket();
            createSocketListeners();
            isMultiplayer = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            saveGame("save1");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            setCustomCursor(taskTypeSelected);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            setCursorDefault();
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
                JSONObject data = new JSONObject();

                ArrayList<String> packagedTiles = map.packageTiles();
                data.put("tiles", json.toJson(packagedTiles));
                data.put("mapWidth", GameScreen.TILES_ON_X);
                data.put("mapHeight", GameScreen.TILES_ON_Y);
                data.put("tileDims", GameScreen.TILE_DIMS);
                data.put("colonists", json.toJson(colonists));
                data.put("settings", json.toJson(map.settings));

                socket.emit("loadWorld", data);
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
        socket.on("loadWorldClient", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
//                map = json.fromJson(Map.class, data.get("map").toString());
                colonists = json.fromJson(ArrayList.class, data.get("colonists").toString());

                ArrayList<String> packagedTiles = json.fromJson(ArrayList.class, data.get("tiles").toString());
                int mapWidth = data.getInt("mapWidth");
                int mapHeight = data.getInt("mapHeight");
                int tileDims = data.getInt("tileDims");
                GameScreen.TILES_ON_Y = mapHeight;
                GameScreen.TILES_ON_X = mapWidth;
                GameScreen.TILE_DIMS = tileDims;

                map.settings = json.fromJson(MapSettings.class, data.get("settings").toString());
                map.unPackageTiles(packagedTiles);

                System.out.println("Loaded world" + colonists.size());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        socket.on("testTwo", args -> {
            System.out.println("testTwo: " + args[0]);
        });


        socket.on("loadColonists", args -> {
            colonists = json.fromJson(ArrayList.class, Colonist.class, args[0].toString());
            System.out.println("Loaded colonists " + colonists.size());
        });

        socket.on("changeTileType", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                int x = (int) data.get("x");
                int y = (int) data.get("y");
                String type = (String) data.get("type");
                map.changeTileType(x, y, type);
//                map.updateBooleanMap();
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
            c.moveColonist(map);
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

    public void saveGame(String saveName){
        Json json = new Json();

        String tileSave = RLE.encodeTiles(map);
        String thingSave = RLE.encodeThings(map);
        String colonistSave = json.toJson(colonists);

        String mapInfo = MyGdxGame.initialRes.x + " " + MyGdxGame.initialRes.y;

        System.out.println(tileSave);
        System.out.println(thingSave);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        String time = dtf.format(now);

        File file = new File("core/assets/Saves/" + saveName);
        System.out.println(file.mkdir());

        File file2Save = new File("core/assets/Saves/" + saveName + "/save.txt");
        try {
            System.out.println(file2Save.createNewFile());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        FileHandle fileHandle = Gdx.files.local("core/assets/Saves/" + saveName + "/save.sve");
        fileHandle.writeString("" + time + "\n" + tileSave + "\n" + thingSave + "\n" + colonistSave + "\n" + mapInfo, false);

        String s = "d1g2d4g2d5g1d5g1d5g2d4g2d5g1d5g5d7g2d5g5d7g2d4g2d5g5d4g2d7g2d5g5d4g2d7g2d5g1d5g5d7g5d5g2d7g2d4g2d5g5d4g2d7g5d8g5d5g1d5g1d5g2d4g2d13g4d2g3d16g4d1g6d8g2d5g5d13g3d3g5d5g3d6g3d2g6d5g3d5g5d1g4d5g5d7g6d11g4d3g2d5g6d1g5d6g5d9g5d2g4d9g4d1g4d13g4d2g2d17g10d9g2d5g5d13g3d3g5d4g5d5g11d4g5d3g11d5g5d7g7d10g5d2g2d5g13d5g4d10g5d2g4d9g9d1g1d12g3d15g2d5g9d16g5d13g2d5g5d3g6d8g7d3g7d2g11d4g5d8g7d11g4d10g13d38g9d1g2d11g3d15g2d5g9d15g5d21g5d3g6d8g7d1g8d3g10d4g6d5g2d1g7d4g2d6g3d6g2d4g11d10g1d27g5d2g5d11g4d14g2d5g6d18g5d21g5d3g4d11g5d2g7d5g8d5g5d5g3d2g5d5g3d5g3d5g3d6g7d12g3d23g6d6g2d11g4d14g2d5g5d19g5d21g5d4g2d13g3d3g6d7g6d6g6d4g3d3g3d6g3d4g5d4g3d6g6d13g3d23g5d13g3d3g6d3g4d12g5d5g3d2g4d7g5d3g4d6g3d3g5d11g3d14g4d8g4d9g5d24g7d14g3d5g3d12g4d2g4d3g3d3g5d12g14d3g4d8g9d4g11d7g11d5g4d3g4d12g4d14g2d10g1d11g5d23g9d14g1d6g3d11g12d2g3d3g5d10g16d4g5d5g9d4g13d7g12d2g5d3g4d12g6d38g2d3g3d18g10d21g2d10g13d9g4d10g15d5g6d3g9d3g9d1g6d6g11d3g5d4g3d6g2d5g8d13g1d25g5d6g3d8g9d34g13d11g3d8g15d8g4d3g7d5g7d4g6d5g10d5g4d4g3d6g3d4g8d12g3d24g5d6g4d8g8d35g12d11g3d7g6d3g3d13g2d4g6d6g6d7g5d5g2d4g2d7g2d5g4d5g2d7g6d13g2d23g6d6g5d8g6d7g4d26g3d4g5d11g2d7g5d11g2d14g4d8g5d8g5d25g6d14g4d5g2d13g3d3g3d3g3d4g5d8g5d7g5d7g6d4g2d4g3d6g3d10g6d3g3d12g5d12g2d14g3d10g3d10g4d24g9d13g3d4g4d12g7s8g1d2g5d10g4d8g3d8g6d2g11d5g4d11g12d12g3d13g2d14g2d10g5d9g4d23g12d12g2d4g5d11g7s8g1d3g5d10g3d9g1d9g6d2g11d4g6d11g12d32g2d8g4d8g6d9g3d23g13d12g3d4g5d11g6s8g1d3g6d10g1d21g3d4g7d7g9d9g12d31g5d6g4d8g7d8g3d25g12d11g3d5g4d11g6s8d5g5d16g3d6g2d12g5d11g7d10g10d5g3d23g6d6g5d7g7d7g4d26g3d3g5d11g3d6g3d11g5d1s8d4g6d16g3d6g2d11g6d12g6d10g3d3g3d6g4d3g2d4g3d3g3d3g6d8g5d7g5d7g6d3g3d4g2d7g2d11g5d4g3d19g2d4g5d2s8d4g5d4g3d3g3d19g3d3g6d7g2d5g4d5g2d25g4d2g4d3g4d1g6d10g5d7g3d8g6d3g4d1g5d6g2d12g5d2g5d17g5d1g6d2s8d3g5d5g4d1g5d17g5d2g5d8g2d6g3d5g3d25g9d3g10d11g6d17g6d1g13d5g2d11g17d9g2d3g11d2s8d4g4d5g14d12g6d2g4d9g2d7g2d5g6d26g5d4g9d12g6d16g4d3g14d11g1d5g17d8g4d4g9d6s4d4g3d6g15d11g6d3g2d10g1d8g3d4g6d26g5d4g8d14g6d22g14d10g3d3g8d3g8d6g5d4g10d5s4d5g3d5g5d2g9d11g4d24g4d5g6d8g6d12g4d4g6d17g5d22g6d1g5d12g3d3g7d5g7d6g5d5g4d1g4d5s4d5g3d5g4d5g7d11g4d23g5d6g5d8g6d13g2d5g5d4g2d5g1d8g5d4g2d7g2d5g5d4g2d5g1d8g1d5g5d7g5d8g5d5g1d5g1d6s4d4g5d5g1d8g5d5g1d8g1d5g1d5g2d4g2d5g5d7g5d8g5d6g1d11g6d5g3d1g5d7g6d2g4d5g4d3g5d11g3d14g3d9g3d10g5d24g7d14g3d5g3d12g4d2g5d1g4d3g5d9g5d7g4d7g1d9g7d6g10d7g11d4g6d2g4d13g2d14g3d9g3d10g7d20g9d14g2d6g4d11g4d2g11d2g4d9g7d6g3d17g8d6g11d7g5d7g8d19g1d15g4d7g3d11g7d20g7d25g4d11g4d4g8d15g8d5g3d17g7d8g10d7g5d6g8d9g4d13g2d3g3d2g4d7g4d10g6d5g3d13g6d9g6d11g4d12g3d4g7d9g2d5g8d4g4d2g2d12g6d11g8d8g4d7g6d11g5d11g3d3g3d3g4d6g5d9g5d6g5d13g3d10g6d12g3d12g3d5g4d11g4d2g7d6g4d2g3d11g5d13g6d10g2d7g6d12g6d10g3d3g3d4g2d7g5d8g5d7g5d14g2d10g6d13g2d11g5d4g3d12g3d4g6d6g3d3g3d3g4d3g5d5g2d7g5d4g3d12g5d5g3d7g6d2g4d26g5d6g6d8g5d6g3d10g2d5g4d5g3d12g3d3g7d9g3d14g3d20g5d2g5d4g4d7g3d5g4d11g5d5g4d7g11d26g5d6g5d9g5d5g4d8g5d6g1d6g3d12g4d2g8d8g3d15g2d18g7d3g4d4g5d5g4d4g6d9g6d5g6d5g10d11g1d15g5d6g6d8g4d5g6d6g6d28g4d3g7d8g3d33g8d6g3d3g5d5g4d2g7d10g6d6g8d4g6d12g3d13g5d8g5d7g3d7g6d6g5d18g5d7g3d5g5d8g2d7g4d18g3d2g7d7g3d3g4d5g5d2g7d10g6d7g7d5g4d12g4d13g5d9g5d6g3d8g6d5g5d18g5d7g4d4g4d8g3d7g5d16g4d2g4d10g3d3g3d7g2d4g6d13g5d7g6d7g2d11g5d13g5d8g5d7g2d10g6d4g3d19g6d7g4d5g2d8g4d7g6d14g5d4g2d4g3d26g4d7g3d4g6d7g4d14g3d3g5d5g2d8g5d6g6d13g3d4g5d10g3d3g3d7g5d7g6d13g6d7g5d4g3d6g5d11g5d24g3d7g4d5g5d7g4d12g12d6g3d8g4d6g5d13g4d5g9d5g10d5g5d7g7d12g8d7g5d2g4d5g6d10g9d21g2d8g4d6g4d8g3d12g12d7g1d10g3d6g6d11g5d4g10d5g11d5g4d6g8d11g9d9g2d2g5d4g6d11g10d19g4d7g4d6g3d9g4d11g11d31g3d11g6d2g13d2g12d5g4d5g8d5g2d5g10d12g4d4g7d8g1d2g10d19g4d7g4d5g3d5g3d2g4d12g8d13g2d11g2d6g3d11g5d2g13d2g11d12g11d5g3d5g8d13g3d5g4d5g3d3g1d2g4d3g3d8g3d8g5d7g3d5g4d4g3d3g3d13g6d13g4d9g4d5g3d12g3d3g4d2g7d3g3d3g3d13g3d3g5d5g3d6g6d13g5d4g3d6g3d22g6d8g5d13g6d16g2d8g4d7g3d4g6d6g6d10g3d19g5d20g2d11g5d14g4d5g2d7g6d37g7d7g5d12g7d16g3d8g2d8g4d2g7d6g6d10g3d20g3s4d16g3d12g5d14g2d6g2d6g8d34g10d7g4d8g11d15g5d17g15d4g6d10g5d21s4d14g5d13g7d19g2d4g11d12g4d15g13d17g11d5g1d9g6d19g13d5g3d13g4d21s4d14g5d13g7d23g13d11g9d3g3d3g19d12g12d4g3d6g8d20g6d2g5d11g2d7g4d21s4d4g2d6g7d14g6d23g13d10g10d3g3d3g7d1g5d2g4d12g4d2g5d5g3d6g7d14g3d5g3d5g4d11g3d7g3d12g3d2s12g3d5g5d18g5d22g5d1g6d11g4d3g2d4g2d5g5d4g2d5g1d8g1d5g1d5g5d5g1d8g5d5g1d8g5d4g2d7g2d5g1d5g5d7g2d5g1d5g5d1s12d1g1d5g5d5g1d5g1d8g5d5g1d8g1d5g5d5g1d5g1d8g1d18g3d20g4d11g5d14g3d5g3d6g7d17g5d2g7d11g5d1g8s12d6g5d5g4d1g5d7g5d2g4d6g4d1g6d11g4d25g3d20g4d11g5d14g3d5g3d6g8d13g8d2g8d9g15s12d2g1d3g5d4g11d7g5d1g6d5g11d11g4d25g2d13g2d5g5d11g4d33g7d11g10d4g7d8g11s20d10g13d12g7d8g6d11g6d5g1d9g2d6g3d12g4d4g5d6g3d3g2d13g1d7g2d11g7d11g10d5g10d4g11s20d9g8d1g6d11g7d8g6d11g6d4g2d8g3d6g3d11g5d5g4d5g5d16g2d6g3d11g6d12g7d9g3d2g4d5g5d1g4s20g1d7g7d5g4d11g6d11g5d9g6d5g3d7g3d6g3d10g6d6g3d4g5d17g2d7g2d11g5d13g6d10g2d4g3d6g3d3g3s20g1d8g6d6g3d10g6d12g6d8g5d6g3d3g1d18g4d2g6d13g5d5g3d3g3d19g3d3g5d7g3d5g4d5g2d24s27d7g3d14g4d2g6d4g3d7g6d6g5d13g1d18g4d2g5d13g6d5g9d17g5d3g4d8g3d6g2d6g2d24s27d22g6d2g5d5g4d7g5d4g7d12g1d26g5d11g6d5g10d4g1d10g7d3g4d7g5d39s27d21g8d3g2d5g7d4g17d11g2d12g1d15g4d11g5d5g9d5g2d5g1d2g7d5g3d7g5d9g2d15g2d4g2d5s27d21g7d9g9d3g19d10g2d11g2d16g3d11g4d5g7d8g2d4g3d2g5d6g3d8g5d8g3d5g2d6g3d3g3d1s35d17g5d11g10d2g19d11g1d10g3d16g3d12g3d4g6d10g2d4g2d4g3d7g4d8g5d7g3d4g5d4g3d3g3d1s35g1d8g4d5g2d13g3d3g3d3g6d4g3d3g3d16g3d10g2d4g2d10g3d13g5d4g3d25g6d7g6d12g6d14s35g1d7g6d10g2d20g5d19g3d3g7d9g11d8g4d11g5d5g3d25g7d7g6d9g10d12s35g2d6g6d10g3d21g2d20g3d3g7d9g12d7g5d11g3d6g3d25g8d5g8d7g11d12s35g1d7g6d10g4d42g2d4g4d12g12d8g7d17g2d18g2d9g6d4g11d3g12d12s35g1d8g4d5g2d4g5d47g3d6g2d5g11d10g7d15g3d10g1d6g3d11g5d2g13d2g11d12g1s35d9g4d5g2d5g4d12g2d8g3d13g1d8g3d5g4d5g2d3g4d12g6d14g4d9g3d5g3d12g4d2g4d3g6d3g3d3g4d12g1s35d9g4d5g2d6g3d11g4d7g3d11g4d14g6d16g3d7g4d8g2d4g6d6g6d10g3d20g4d20g2d1s43d4g6d19g3d3g6d13g3d3g6d4g2d7g6d16g3d7g3d8g4d3g7d5g7d9g3d20g4d19g4s43d4g7d18g4d1g7d12g5d1g8d1g4d7g6d3g2d11g5d15g5d3g8d4g10d7g2d21g1d20g5s43d4g9d16g12d8g23d7g4d4g4d11g6d12g5d4g8d6g8d50g5d1s43d6g7d18g10d7g25d13g5d11g8d10g4d6g7d4s19d4g2d3g4d2g4d6g4d11g3s47d8g5d12g2d5g7d8g4s16g6d13g5d12g7d10g3d7g6d5s19d4g2d3g4d2g4d6g5d9g4s47d9g4d11g3d5g4d11g4s16g3d9g2d5g5d4g2d7g5d5g2d4g2d7g5d5g2s19d4g2d4g2d5g1d8g5d7g5s47d2g1d8g1d5g1d5g5d5g1d5g2d7g2d1s16g1d11g3d3g5d5g3d8g2d5g4d12g4d6g2s19d26g5d6g5d1s47d1g3d11g5d2g8d8g4d9s16d12g3d3g5d5g3d15g5d11g4s35d18g5d5s54d1g4d9g6d2g9d7g5d4s25d7g2d5g5d4g2d16g6d10g2d2s35d18g5d5s54d1g5d8g6d3g8d7g5d4s25d1g1d12g5d22g5d15s35d17g5d6s54d1g5d9g5d3g8d7g9s25g3d11g5d20g6d16s35d17g5d6s54d1g4d11g3d5g6d8g4d2g3s25d1g2d11g5d20g5d9s51d9g3s62d1g3d12g3d4g6d10g2d4g2s25d7g3d3g5d5g3d6g3d3g5d5g3d2s51d3g2d5g2s62d7g3d12g6d4g3d12s25d7g10d6g4d5g10d6g3d2s51g1d2g2d5g2s62d7g4d10g6d5g5d10s25d8g9d6g5d4g10d3s133d7g6d4g11d3g9d11s21d10g6d8g4d5g8d4s133d8g5d3g13d2g10d10s21d10g5d10g3d5g7d5s133d9g5d2g13d2g10d10s21d10g3d13g2d5g5d7s133d10g3d3g3d4g6d3g3d3g3d10s21d4g3d10g2d13g5d1s136d5g3d19g5d20g2d1s21g7d10g2d11g7d1s136d5g3d20g3d20g3d1s21g7d11g1d9g8d2s136d5g3d44g2d1s21g6d16g2d3g8d3s136g1d23g2d14g2d13s21g5d16g5d2g5d1s136g6d9g3d8g5d12g4d12g3d1s17g6d14g6d2g4d2s136g6d9g3d8g5d12g5d11g3d1s17d1g6d3g3d6g6d9s136g5d4g3d12g6d4g3d7g5d4g2d8s17d2g6d1g5d5g5d10s136d1g3d5g3d12g5d5g3d8g4d3g4d7s17d3g12d4g4d7s137d4g1d8g2d11g5d7g2d9g3d3g6d5s17d4g11d5g2d8s137d3g2d21g5d19g2d4g5d5s17d5g9d16s137d2g3d12g1d8g4d27g4d5s17d6g8d13g3s137d2g3d11g3d8g3d11g3d14g3d5s17d7g5d8g1d5g4s129d2g1d8g1d5g2d4g5d8g1d5g2d4g5d5g2d7g2d4g5d4s9g1d8g2d8g4d2g5s129d16g4d2g8d11g4d3g6d3g3d13g5d4s9g1d16g13s129d16g14d11g5d2g6d2g4d13g4d5s9d16g14s129d2g1d18g9d12g4d10g4d21g1s9d16g6d1g3s47d3g2d6s51g1d6g5d4g5d4g3d4g2d11g9d13g3d10g3d6g1d13g7s5d15g5d5g1s47d2g3d6s51g2d7g3d5g3d5g3d3g3d12g8d13g3d9g4d5g3d12g4d1g2s5d14g5d7s47d3g2d5g1s51g2d7g3d4g5d4g3d3g3d13g6d13g5d8g4d5g2d13g3d3g1s5g1d2g4d6g5d8s47d9g2s51g3d12g7d16g3d7g4d7g4d2g7d6g6d10g3d13s5g1d2g5d5g5d8s39d2g2d11g6d6s35d6g5d12g8d15g4d7g2d7g5d2g7d5g9d8g3d17s1d2g7d5g3d9s39d14g7d6s35d6g4d14g8d13g7d13g6d3g5d5g11d27s1d2g7d10g3d4s39d6g4d5g6d6s35d5g3d18g6d10g13d6g10d4g1d8g11d12g4d11s1d2g6d11g4d3s39d6g5d5g5d6s35d5g3d11g3d4g5d11g13d6g9d15g10d11g5d11s1d1g6d12g3s35g5d8g5d7g5d13s20d12g2d11g5d4g3d12g3d4g6d6g3d3g3d16g3d3g3d11g5d13g5d4g3d7g2s35d1g5d6g6d8g5d6g3d3s20d3g2d13g3d3g6d10g3d13g5d20g2d4g2d17g2d4g5d7g3d3g5d5g4d7g1s35d2g5d5g5d9g5d5g4d3s20d3g3d10g17d7g3d14g3d18g6d3g2d15g5d3g4d7g4d3g6d4g4d6g2s35d2g6d4g5d10g4d4g5d3s20d3g2d8g21d7g2d14g3d17g7d3g2d15g5d3g3d7g5d3g7d2g6d4g3s31d6g7d4g3d3g5d3g3d5g5d11g2d7g2d13g23d21g4d17g6d5g1d4g3d8g5d4g1d7g6d3g7d2g5d5g3s31d8g5d9g7d11g4d11g3d6g2d13g14d3g6d2g2d16g5d2g2d12g5d12g4d8g3d12g6d4g1d2g4d3g3d6g3s31d10g3d9g7d12g3d11g4d5g2d13g3d3g6d6g4d3g2d16g4d3g2d12g5d12g5d7g3d11g5d26g2s31d4g2d10g3d4g5d4g3d13g2d4g6d10g2d14g4d20g3d3g3d16g3d3g6d7g2d5g5d13g3d3g5d5g2d18s31g2d5g4d8g4d5g3d5g4d11g4d3g6d10g3d13g3d21g3d3g3d16g4d1g6d8g2d5g5d13g4d1g6d5g2d18s31g1d5g5d7g5d13g4d10g6d2g6d3g2d5g4d12g3d21g2d4g4d15g10d9g2d5g5d13g11d5g2d5g1d5g1d6s31g1d5g7d5g5d12g6d9g6d10g4d4g5d11g3d28g4d16g7d17g5d13g12d4g2d5g2d3g3d5s31d6g8d5g4d12g6d9g6d9g5d5g4d10g4d7g2d20g5d8g2d5g5d17g5d15g11d5g1d4g3d3g3d1s35d7g7d6g3d11g6d11g5d9g5d6g3d10g5d5g3d20g5d8g3d4g4d7g3d8g5d15g6d1g4d11g1d5g1d2s35g1d7g5d8g1d5g2d4g5d5g2d7g5d8g5d7g2d4g2d5g5d4g2d5g1d5g1d8g5d7g5d5g2d7g5d8g5d4g2d7g5d5g2d6g1d13s35g2d7g4d13g4d3g5d4g4d7g5d6g5d13g4d5g4d10g4d1g4d6g5d7g7d12g7d7g5d3g4d6g5d13g1d3g1d9s31d1g6d6g4d11g7d2g4d5g5d6g5d6g5d12g5d6g3d10g9d5g6d6g8d12g8d7g4d2g6d5g4d16g4d8s31d1g7d4g5d11g7d9g7d5g4d9g5d11g5d11g4d4g7d6g6d7g7d14g7d8g3d3g5d5g3d17g5d7s31d3g6d3g6d10g7d8g8d5g3d10g5d11g5d11g4d5g5d7g6d6g8d14g7d8g2d4g5d6g2d9g3d5g5d7s31d5g4d3g6d12g5d8g7d5g3d11g5d12g6d9g5d5g4d8g6d5g4d19g5d8g3d5g3d7g3d8g5d2g6d3s32g2d7g2d4g6d13g5d7g6d7g2d11g5d13g5d8g5d7g2d10g6d4g3d19g6d7g4d5g2d8g4d7g12d4s32d16g4d7g3d5g5d7g4d14g3d3g5d5g3d7g5d6g5d14g3d5g5d9g3d3g3d6g6d7g6d13g6d8g9d5s32d16g3d8g3d5g6d8g1d11g15d5g3d8g5d4g6d12g5d5g7d7g9d6g5d8g6d12g7d8g9d5s32g1d15g2d9g3d6g6d17g17d16g14d11g7d6g7d6g8d8g5d8g4d11g9d10g5d4g3s28g6d13g2d12g1d6g6d10g2d5g9d1g7d1g2d14g13d11g6d7g8d6g5d10g6d21g9d13g2d5g3s28g6d13g2d12g2d6g4d11g3d3g8d4g5d2g3d14g11d12g5d9g7d7g4d11g5d20g9d21g3s28g6d13g2d11g5d4g3d12g3d4g6d6g3d3g3d16g3d3g3d11g5d13g5d7g3d10g6d19g6d26g2s28d1g4d5g2d13g3d3g6d10g3d13g5d20g2d4g2d17g2d4g5d7g3d4g6d12g3d4g5d4g3d6g3d4g5d4g3d6g3d11g2s28d1g4d5g3d12g3d3g7d9g4d14g2d20g4d3g2d15g12d8g3d5g6d11g11d5g3d5g5d2g5d5g4d4g5d11g1s28d1g4d5g2d13g3d3g6d10g4d37g2d4g2d14g13d8g2d6g7d10g11d6g2d4g7d2g3d6g5d2g6d11g1s28g6d13g2d11g3d11g6d4g3d11g4d36g13d17g6d11g9d11g8d12g5d2g6d2g1d3g3d3s28g6d13g2d25g5d5g5d8g6d8g3d25g12d12g3d4g4d12g6d5g2d1w4g7d14g4d4g4d3g1d2g4d3s20g2d6g6d13g2d24g5d6g5d8g6d8g4d25g4d3g5d11g3d6g2d11g6d6g2w6g4d16g3d6g3d13s20d9g4d5g2d13g3d3g3d4g2d4g5d8g5d8g4d7g6d4g3d3g3d6g3d11g5d3g3d19g3d3g6d8w8g2d5g2d4g3d28s20d10g3d5g3d11g5d2g4d2g4d3g5d9g5d8g3d7g7d2g4d2g5d5g3d11g5d3g3d18g5d2g5d8w10g1d5g3d2g4d28s20d18g2d10g7d2g2d5g2d4g5d11g2d19g13d2g6d4g2d12g5d3g3d17g7d1g4d8w4d3g1w4d5g9d15g3d14s8g1d36g8d9g2d4g5d34g10d3g6d5g1d11g5d5g2d17g7d12w4d6w4d4g8d16g6d11s8d10g3d9g3d12g7d17g5d25g4d7g6d5g4d18g5d16g4d5g6d11w4d8w4d3g7d8g2d6g7d11s8d10g4d8g3d11g6d19g5d25g5d7g5d5g4d18g5d15g5d5g6d10w4d10w4d2g3d12g3d5g4d1g2d4g2d5s8d4g2d4g5d8g1d5g2d4g5d5g2d7g2d4g5d5g2d7g2d4g2d5g5d7g5d5g1d8g1d5g2d4g5d5g2d7g5d8g5d4g2d2w4d5g1d5g1w4d1g2d5g1d8g1d5g5d6g5d1g6d7g3d5g6d11g4d3g5d4g4d5g5d1g6d5g3d5g4d3g2d5g5d8g6d11g5d1g3d5g5d3g3d6g5d9g6d1g5w4d5g4d1g4w4d6g3d11g6d3g16d6g4d6g5d9g7d2g4d5g5d3g12d6g4d4g4d3g2d6g4d8g8d8g10d6g4d3g3d6g4d10g11w4d6g10w4d6g2d10g6d3g18d5g5d5g6d8g6d3g3d6g5d2g12d7g5d4g2d4g1d20g7d8g10d6g3d5g2d20g10w4d7g11w4d17g6d3g9d1g8d5g5d6g9d5g5d4g2d6g3w19d4g5d24g4d4g2w16g5d7g3d26g10w4d8g5d1g6w4d8g4d4g6d2g7d5g7d6g5d5g4d2g3d6g3d5g3d5g2w29d24g5d4w18g3d8g3d26g4d1g4w4d9g4d5g3d1w4d6g5d6g3d4g6d6g6d7g5d4g3d3g3d7g2d5g4d5w31d22g5d4w20g1d8g4d26g3d3g2w4d10g3d6g3d2w4d5g5d6g3d5g3d9g4d8g6d25g6d3w33d1g4d3g3d3g3d3g5d4w22d7g6d3g3d3g4d6g2d11w4g2d2g4d18w4d3g5d16g2d10g2d10g9d21g4w8d8g3d5g3w12g11d2g11w7d3g5d8w4d6g12d4g3d6g2d10w4g9d18g1w4g6d40g11d20g3w8d8g2d19w33d4g6d8w4d5g12d13g2d5g1d3w4g10d4g1d14g1w4g5d26g1d13g12d6g2d5g2d6w8d2g2d5g2d12g4d4w31d5g6d9w4d5g10d20g4w4d2g8d4g3d14g1w4g4d17g2d6g2d12g13d6g3d3g3d5w8d3g3d3g3d11g5d5w29d6g5d11w4d4g7d23g3w4d5g5d5g3d16w4g3d15g5d4g3d13g2d4g6d6g3d4g2w8d8g3d4g2d11g5d6w24d9g5d13w4d3g5d25g2w4d7g5d4g3d17w4g3d14g6d10g2d14g4d15w8d3g3d16g3d3g5d8g2d5g5d13g2d4g5d5g2d7g1w4d1g5d5g2d7g2d4g2d5w4g1d7g6d12g3d4g2d3w4g3d3g3d6g7d10g2d15g3d14w8g2d2g3d16g11d7g3d5g6d10g13d4g3d5g4w4g5d5g2d6g10d3w4g3d7g7d10g4d2g3d4w4g10d5g6d10g2d15g3d13w8g4d1g2d17g11d8g1d6g7d8g13d5g3d4g6w4g3d6g2d7g9d2w4g5d7g6d10g3d4g2d5w4g10d4g3d22g1d7g4d7w8d2g7d21g11d13g11d5g12d4g4d5g7w4g1d18g6d2w4g6d8g6d24w4g11d10g2d14g3d6g4d6w8d4g6d21g11d13g12d6g8d5g5d5g8w5d2g3d5g3d5g4d2w4g7d10g4d7s16d1g1w4d4g6d9g4d13g3d5g5d5w8d6g5d21g5d3g3d13g3d3g6d6g7d6g5d5g3d2g4w5d1g3d4g5d4g3d2w4g6d13g2d8s16g3w4d4g6d1g2d4g6d3g3d13g5d2w11d8g5d3g3d7g2d4g5d11g2d14g4d8g5d8g5d14w5d6g6d8w4d2g4d5g2d13g3d1s16g4w4d4g10d1g7d3g3d13g5d1w7d2g4d7g6d1g5d5g5d1g6d11g3d14g3d9g3d10g4d15w12g5d6w4d3g3d6g3d12s25w4d4g17d3g3d13g4d1w7g6d8g29d11g5d13g2d8g5d10g3d17w11g3d6w4d4g2d8g1d13s26w4d4g14d21g3d1w7g7d7g30d11g5d4g2d8g2d7g5d24g1d6w11g1d6w4d5g2d22s27w4d4g11d24g2w4d2g7d9g31d10g6d3g4d6g2d6g7d14g2d7g2d6w11d3g2w4d5g3d22s28w4d4g8d14g3d9g1w4d3g4d12g4d1g5d1g8d2g5d1g4d12g4d3g4d6g2d6g7d14g3d5g3d6g3d5w11d6g3d11g3d4s32d1w4d5g5d8g1d5g5d7g1w4d5g2d4g2d7g2d5g1d5g5d5g1d5g1d8g1d5g2d4g5d5g2d7g5d5g1d8g5d5g1d8g1d5g1d1w9d8g1d5g1d5g5d3s32d1g1w4d5g3d7g5d1g8d5g1w4g3d9g3d20g3d20g3d12g5d13g3d5g3d6g7d18g4w7g2d11g5d2g7d2s32g3w4d5g2d7g14d4g1w4g6d7g4d19g2d21g3d12g8d11g2d6g2d4g9d18g5w5g4d9g7d1g7d2s32g3d1w4d12g13d6w4g7d6g5d18g2d7g2d14g4d11g9d10g2d9g10d21g13d8g7d9g1s32g1d4w4d7g16d6w4g8d6g5d17g3d6g4d13g5d11g8d10g3d8g9d8g3d12g16d4g7d9g1s32d6w3d7g3d2g5d1g4d6w4g4d1g4d5g5d18g3d5g5d14g5d11g7d10g3d7g6d12g4d12g3d1g6d1g4d6g5d9g1s32d7w2d7g3d3g3d3g3d5w4d1g3d3g3d4g5d19g3d4g6d13g5d13g6d10g3d7g5d13g5d11g2d4g3d3g3d6g6d8g1s32d8w1g3d22g1w4d14g6d4g3d6g3d9g6d7g3d3g5d5g3d7g4d5g3d12g5d5g3d7g5d3g3d26g6d5s31d7g3d2w1g3d18g4w4d15g5d5g4d5g3d9g5d8g3d3g5d5g3d7g3d6g4d11g5d5g3d8g4d3g3d27g6d3g1s31d5g5d2w1g4d16g4w4d17g5d4g5d4g2d12g2d9g3d3g4d7g2d7g2d6g6d11g3d5g3d21g4d20g11s31d3g7d2w1g4d7g2d6g4w4g1d13g1d5g3d5g5d22g2d11g3d15g3d6g6d16g6d21g4d4g3d12g12s31g1d2g6d3w1g5d5g3d6g3w4g1d13g2d6g3d4g5d11g3d8g2d12g3d14g3d5g6d17g5d22g5d2g4d12g8s35g1d2g6d3w1d1g5d4g3d6g2w4d14g4d5g2d7g2d11g5d7g2d11g5d13g3d4g5d19g6d20g5d4g3d12g3d4g1s35g1d3g3d5w1d1g6d13w4g1d4g3d6g6d19g3d3g6d13g3d3g6d4g3d12g6d4g3d3g3d7g5d4g3d6g3d3g6d10g3d13g1s35d12w1d2g5d12w4g1d5g3d6g8d16g5d2g7d11g5d2g7d2g5d10g6d5g3d2g5d7g5d2g4d5g5d2g5d11g4d13s35d12w1d2g5d2g3d6w4d6g4d6g11d12g7d1g6d11g7d1g6d2g7d8g8d4g3d3g5d7g3d2g5d4g6d2g5d10g5d13s31d16w1d9g4d4w4d5g5d9g10d11g7d10g2d6g7d10g6d8g8d10g6d11g4d4g7d15g8d4g3d4g1s31d8g3d5w1g1d7g5d3w4d5g5d11g10d3g3d5g6d9g5d5g6d12g4d9g8d11g4d11g3d5g5d10g4d3g7d5g4d2g2s31d7g4d5w1g1d7g5d2w4d6g6d10g3d3g4d2g4d6g5d8g6d6g5d14g2d10g7d12g3d10g5d4g3d12g4d3g6d6g4d2g2s31d6g5d5w1d7g5d2w4g1d7g6d3g3d26g5d7g5d8g5d7g2d10g3d4g5d4g3d12g3d4g6d10g2d14g4d11s31d3g3d3g5d14g5d1w4g3d7g6d1g4d26g6d5g5d9g5d6g3d10g3d5g3d5g3d11g6d1g8d9g3d13g3d12s31d2g4d3g5d14g5w4g5d7g10d27g6d5g4d10g4d7g1d6g2d3g2d13g6d8g17d7g5d12g2d13s31d1g5d3g5d14g4w4d2g4d7g9d5g2d21g6d5g3d28g4d17g6d8g17d7g6d4g2d4g2d11g1d2s31g6d4g5d14g2w4d3g5d7g8d5g2d21g4s15d2g2d8g3d8g5d16g10d1s4g15d9g6d2g4d3g2d10s35d1g5d4g5d14g1w4d6g3d9g6d5g2d20g5s15d1g3d8g3d8g5d15g6d1g4d1s4d1g7d1g5d11g5d2g4d3g2d10s35d2g4d4g5d13g1w4d8g1d5g2d4g5d5g2d4g2d5g1d8g5s15d2g1d8g5d7g5d5g2d7g5d5g1d2s4d2g5d5g1d8g1d5g1d5g2d4g2d5g1d4s35d3g2d5g5d6g1d6w4g1d13g3d5g5d10g3d2g4d5g6d1s15d10g7d7g5d3g4d5g5d9s4d3g3d13g4d21g4d3s35d9g4d8g2d4w4g1d12g5d6g4d3g1d6g9d4s27d6g7d8g4d2g6d2g7d5s20d6g5d20s39g3d6g1d3g4d7g6w4d14g5d12g4d4g7d4g2s27d7g4d16g14d6s20d4g6d11g2d8s39d7g4d12g6w4d10g1d5g4d12g4d4g6d5g2s27g2d12g5d9g13d6s20d2g8d11g3d7s39d7g4d11g6w4d10g2d6g3d11g5d6g3d5g3s27g3d11g5d11g8d9s20d2g7d11g5d6s39d7g5d9g4d1g1w4d11g2d7g2d11g5d7g2d4s35d9g6d12g6d10g3d1s28d9g2s47d7g5d8g5d1w4g2d3g3d19g3d3g5d14s35d2g4d2g5d8g3d4g4d5g3d7s28d2g3d4g2s47g1d7g5d6g6w4g3d3g3d17g6d2g4d13g2s35g6d2g5d7g4d5g2d6g3d7s28d1g4d5g1s47g1d7g5d6g5d1w3d1g3d3g3d13g11d2g2d12s39g7d2g2d8g5d4g2d12g3d2s82g6d6g4d7g6w2d24g11d12g1d3s39g6d12g5d4g3d12g4d1s82g6d5g3d10g5w1d24g12d11g3d2s39g4d14g4d5g3d11g5d1s82g5d6g3d11g4d26g2d4g5d11g2d3s39g2d16g3d6g3d11g5d1s82g4d7g2d11g5d4g3d3g3d6g3d11g5d3g3d9s39g1d5g2d4g3d19g2d4g5d2s82g4d13g3d3g13d2g5d5g3d12g10d9s39d6g10d15g6d3g5d2s82g2d15g11d1g7d2g6d4g2d13g10d9s39d5g12d13g7d3g4d3s82d17g18d3g7d18g6d13s39d5g12d5g4d3g7d5g3d3s82d21g13d5g4d20g5d7g2d5s39d4g12d5g6d2g5d12g5s74d11g4d11g13d4g3d20g5d7g4d4s39d4g5d2g4d6g6d3g4d12g3d2s74d10g5d12g6d1g6d10g2d4g2d7g5d7g6d3s39d2g6d14g4d14g2d9s74d2g3d4g5d4g3d7g5d2g5d9g3d3g4d6g4d8g7d2s39d2g5d15g4d14g2d9s74d2g4d2g5d5g3d8g3d4g4d9g3d4g2d7g3d7g11s39g6d18g1d15g2d9s70d4g6d3g4d6g2d16g2d9g4d4g2d7g2d7g12s39g6d7g1d37s70d2g7d4g3d34g5d22g13s39d1g6d6g2d3g3d6g2d14g3d5s70g8d6g3d33g5d22g5d1g5d2s39d2g5d5g3d3g3d6g3d5g3d5g3d5s70g7d7g3d25g1d8g5d4g2d7g2d5g5d4g2d3s35d7g5d5g1d5g1d8g1d5g5d5g1d6s63d8g5d7g5d5g1d5g2d6g1d2g4d7g6d1g5d5g4d3g4d11s35d8g5d24g7d11s63g1d7g3d8g7d2g5d1g5d5g1d2g6d5g13d3g6d2g4d11s35d8g7d22g7d11s63g1d7g3d8g7d2g12d8g5d5g21d4g2d12s35d10g5d40s63d8g3d16g15d7g5d3g8d2g12d16g6s31g3d7g5d13g3d22g3d3s51d15g4d16g7d1g8d7g4d3g7d5g5d1g4d15g5d1g1s31g4d8g3d12g4d21g4d3s51d15g5d15g6d5g5d8g3d3g6d7g2d4g2d17g2d4s31g5d8g2d11g5d20g5d3s51d15g5d15g5d6g7d13g4d20g3d3g3d10s31d1g5d13g3d3g5d5g3d6g3d3g5d4s51d2g4d3g2d5g5d3g4d6g5d9g5d14g2d17g8d2g3d14s23d5g7d9g12d6g3d5g11d5s43d10g5d2g2d6g4d2g5d6g4d10g7d30g9d19s23d5g9d6g13d13g12d5s43d8g7d11g2d3g7d19g7d15g2d11g10d9g2d8s23d4g12d5g11d14g11d6s43d7g7d17g7d19g8d14g2d11g8d10g3d8s23d4g13d5g10d14g11d6s43d6g7d18g6d21g2d1g5d13g3d10g6d13g2d11g4d1s12d7g3d3g6d7g3d3g3d16g3d3g3d6s35g2d11g5d20g5d26g6d6g3d10g2d5g4d5g2d13g3d3g6s12g1d13g5d19g3d4g2d16g3s35d6g3d3g6d4g3d6g3d3g6d4g3d6g3d3g3d5g5d5g4d8g4d6g2d5g4d12g12s12g1d14g2d20g10d14g5s35d5g12d6g2d6g11d5g3d6g4d2g3d6g4d4g5d8g4d13g5d11g12s12g1d35g12d12g6s35d4g12d7g2d6g11d6g2d6g4d2g2d7g3d5g6d7g4d13g6d14g10d46g12d3g5d3g3s35d8g12d18g7d16g3d10g3d7g5d8g3d6g2d5g10d11g8d14g1d11g2d7g2d11g11d3g6d2g3s35d1g3d5g9d13g2d5g5d4g3d11g4d9g4d7g5d7g3d5g4d5g2d3g4d12g6d14g4d9g3d5g3d12g4d2g4d3g6d3g2s35d1g3d6g6d14g4d5g2d6g3d11g4d8g6d7g5d13g6d16g3d7g4d8g2d4g6d6g6d10g3d20g4d6s35d11g4d5g2d7g6d19g3d3g6d6g7d7g6d12g7d15g3d7g4d7g5d1g8d5g7d9g4d19g3d7s31g2d14g3d5g3d5g8d18g4d1g7d6g7d9g5d11g9d14g2d8g3d6g16d4g10d6g5d18g2d8s31g3d14g2d5g2d5g10d17g12d6g5d11g5d13g8d23g4d5g16d5g10d4g6d11g2d4g2d8g1s31g3d14g3d4g2d5g11d16g12d6g2d14g5d7g2d6g6d17g4d2g4d5g15d7g9d2g9d9g4d3g2d6g3s31g3d13g4d11g10d9g2d7g9d8g1d7g3d5g4d7g3d7g6d15g5d3g3d6g8d1g5d11g4d3g4d1g4d9g4d3g2d5s35g2d13g5d12g8d9g3d7g6d11";
        ArrayList<ArrayList<Tile>> test = RLE.decodeTiles(s, new Vector2(250, 250));
        map.tiles = test;
    }

    public void setupBBB(){
        bottomBarButtons = new ButtonCollection();
        bottomBarButtons.useWorldCoords = false;
        TextButton orders = new TextButton(0,0,0,0, "Orders", "OrdersButton");
        TextButton building = new TextButton(0,0,0,0, "Building", "BuildingButton");

        bottomBarButtons.add(orders, building);

        for (int i = 0; i < bottomBarButtons.buttons.size(); i++) {
            int size = bottomBarButtons.buttons.size();
            Button b = bottomBarButtons.buttons.get(i);
            b.setPos((int) ((MyGdxGame.initialRes.x / 2f) / size * i), 5);
            b.setSize((int) (MyGdxGame.initialRes.x / 2f / size), (int) (MyGdxGame.initialRes.y / 8f));
        }
    }

    public void setupOrdersButtons(){
        ordersButtons = new ButtonCollection();
        ordersButtons.useWorldCoords = false;
        ordersButtons.showButtons = false;

        float y = (MyGdxGame.initialRes.y / 8f) + 5;
        ImgButton cutDown = new ImgButton("CutDownButton", "CutDown");
        ImgButton plant = new ImgButton("PlantButton", "Plant");
        ImgButton harvest = new ImgButton("HarvestButton", "Harvest");
        ImgButton mine = new ImgButton("MineButton", "Mine");

        ordersButtons.add(cutDown, plant, harvest, mine);

        int row = 0;
        int size = ordersButtons.buttons.size();
        for (int i = 0; i < size; i++) {
            if (i % 2 == 0) {
                row++;
            }

            Button b = ordersButtons.buttons.get(i);
            b.setPos((int) ((MyGdxGame.initialRes.x / 3f / size) * (i % 2)) + 5, (int) (y + (MyGdxGame.initialRes.y / 8f) * (row - 1)));
            b.setSize((int) (MyGdxGame.initialRes.x / 3f / size), (int) (MyGdxGame.initialRes.y / 8f));
        }
    }

    public void setupBuildingButtons(){

    }

    public void setupOrderTypes(){
        orderTypes = new HashMap<>();
        orderTypes.put("Mine", new ArrayList<>(Arrays.asList("stone")));
        orderTypes.put("Plant", new ArrayList<>(Arrays.asList("dirt", "grass")));
        orderTypes.put("CutDown", new ArrayList<>(Arrays.asList("tree")));
        orderTypes.put("Harvest", new ArrayList<>(Arrays.asList()));
    }

    public boolean canUseOrderOnType(String order, String tileType, String thingType){
        if (orderTypes.get(order).contains(tileType)){
            if (orderTypes.get(order).contains(thingType) || Objects.equals(thingType, "")){
                return true;
            }
        }
        return false;
    }

    public void setCustomCursor(String name){
        Pixmap px = new Pixmap(new FileHandle("core/assets/Textures/ui/cursor/" + name + ".png"));
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(px, 0, 0));
        px.dispose();
    }

    public void setCursorDefault(){
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
    }

    public void drawSelectionScreen(ShapeRenderer shapeRenderer, CameraTwo cameraTwo){
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(cameraTwo.projViewMatrix);
        shapeRenderer.setColor(0, 0.4f, 1, 0.5f);
        shapeRenderer.rect(minSelecting.x, minSelecting.y, maxSelecting.x - minSelecting.x, maxSelecting.y - minSelecting.y);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    public void setTasksFromSelection(String taskType){
        int minx = (int) (Math.min(minSelecting.x, maxSelecting.x) / GameScreen.TILE_DIMS);
        int miny = (int) (Math.min(minSelecting.y, maxSelecting.y) / GameScreen.TILE_DIMS);
        int maxx = (int) (Math.max(minSelecting.x, maxSelecting.x) / GameScreen.TILE_DIMS);
        int maxy = (int) (Math.max(minSelecting.y, maxSelecting.y) / GameScreen.TILE_DIMS);

        minx = Math.max(0, minx);
        miny = Math.max(0, miny);
        maxx = Math.min(GameScreen.TILES_ON_X, maxx);
        maxy = Math.min(GameScreen.TILES_ON_Y, maxy);

        for (int i = minx; i < maxx; i++) {
            for (int j = miny; j < maxy; j++) {
                if (canUseOrderOnType(taskType, map.tiles.get(i).get(j).type, map.things.get(i).get(j).type)){
                    map.tiles.get(i).get(j).setTask(taskType);
                }
            }
        }
    }

    public void drawTaskType(SpriteBatch batch){
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_Y; j++) {
                if (map.tiles.get(i).get(j).task != null) {
                    Texture t = actionSymbols.get(map.tiles.get(i).get(j).task.type);
                    batch.draw(t, i * GameScreen.TILE_DIMS, j * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
                }
            }
        }
    }

    public void setupActionSymbols(){
        File dir = new File("core/assets/Textures/ui/imgButtons");
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            Texture t = new Texture(file.getPath());
            actionSymbols.put(file.getName().split("\\.")[0], t);
        }
    }
}
