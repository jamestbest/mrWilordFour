package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.Entity.Colonist;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.MapSettings;
import com.mygdx.game.ui.elements.Button;
import com.mygdx.game.ui.elements.InputButtonTwo;
import com.mygdx.game.ui.elements.TextButton;
import com.mygdx.game.ui.extensions.ButtonCollection;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@SuppressWarnings("unchecked")
public class JoinGameScreen implements Screen {
    MyGdxGame myGdxGame;

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
    ArrayList<Colonist> colonists = new ArrayList<>();
    HashMap<String, Integer> resources = new HashMap<>();
    HashMap<String, TextureRegion> colonistTextures;
    HashMap<String, Texture> resourceTextures;
    Json json = new Json();

    GlyphLayout glyphLayout;
    BitmapFont font;

    Socket socket;
    boolean hasJoinedASocket;

    public JoinGameScreen(MyGdxGame myGdxGame){
        this.myGdxGame = myGdxGame;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        inputMultiplexer = new InputMultiplexer();
        cameraTwo = new CameraTwo();
        cameraTwo.allowMovement = false;

        map = new Map((int) ((int) MyGdxGame.initialRes.y / 10 * 6.8),
                (int) ((int) MyGdxGame.initialRes.x  - (MyGdxGame.initialRes.y / 10 * 7)),
                (int) MyGdxGame.initialRes.y / 10 * 3, "testSeed1");
        map.generateBlank();

        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        glyphLayout = new GlyphLayout();

        setupColonistTextures();
        setupResourceTextures();
        initialiseTextures();

        setupButtons();
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(64/255f,87/255f,132/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        cameraTwo.update();

        batch.begin();
        batch.setProjectionMatrix(cameraTwo.projViewMatrix.getGdxMatrix());
        map.drawMiniMap(batch, tileTextures, thingTextures);

        buttonCollection.drawButtons(batch);

        drawAllInformation(batch);
        batch.end();

        if(Gdx.input.isButtonPressed(0)){
            buttonCollection.updateButtons(cameraTwo, Gdx.input.isButtonJustPressed(0));

            if(buttonCollection.pressedButtonName.equals("joinGameButton")){
                myGdxGame.setScreen(new GameScreen(myGdxGame, ipInputButton.text));
            }
        }

        if (Gdx.input.isButtonJustPressed(0)){
            if(buttonCollection.pressedButtonName.equals("RefreshButton")){
                System.out.println("Refreshing");
                map.generateBlank();
                if (hasJoinedASocket){
                    socket.disconnect();
                    hasJoinedASocket = false;
                }
                connectSocket();
                createSocketListeners();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            myGdxGame.setScreen(myGdxGame.mainMenu);
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
                , (int) (MyGdxGame.initialRes.x * 0.2), (int) (MyGdxGame.initialRes.y * 0.1), "localhost:8080",
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
        socket.on("connect", args -> {
            System.out.println("Connected to server");
            hasJoinedASocket = true;
        });
        socket.on("connect_error", args -> {
            System.out.println("Socket connect_error");
            hasJoinedASocket = false;
        });

        socket.on("loadWorldClient", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
//                map = json.fromJson(Map.class, data.get("map").toString());
//                colonists = json.fromJson(ArrayList.class, data.get("colonists").toString());

                ArrayList<String> packagedTiles = json.fromJson(ArrayList.class, data.get("tiles").toString());
                int mapWidth = data.getInt("mapWidth");
                int tileDims = data.getInt("tileDims");
                GameScreen.TILES_ON_X = mapWidth;
                GameScreen.TILE_DIMS = tileDims;

                map.settings = json.fromJson(MapSettings.class, data.get("settings").toString());
                map.unPackageTiles(packagedTiles);

                this.colonists = json.fromJson(ArrayList.class, data.get("colonists").toString());
                this.resources = json.fromJson(HashMap.class, data.get("resources").toString());

                System.out.println("Loaded world");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void drawColonistInformation(SpriteBatch batch, int x, int y, int width, int height){
        System.out.println(colonists.size());
        glyphLayout.setText(font, "Colonists: ");
        font.draw(batch, glyphLayout, x, y + (height * 0.95f) + glyphLayout.height + GameScreen.TILE_DIMS / 2f);

        for (int i = 0; i < colonists.size(); i++) {
            Colonist c = colonists.get(i);
            batch.draw(colonistTextures.get(c.getClotheName()), (x + ((width / 10f) * i + glyphLayout.width)), (y + (height * 0.95f)), width / 10f, height / 10f);
        }
    }

    public void drawResources(SpriteBatch batch, int x, int y, int width, int height){
        glyphLayout.setText(font, "Resources: ");
        font.draw(batch, glyphLayout, x, y + (height * 0.85f) + glyphLayout.height);
        float titleWidth = glyphLayout.width;
        Set<String> s = resources.keySet();
        int count = 0;
        for (String key : s) {
            float v = y + (height * 0.80f) - ((height / 10f) * count);
            batch.draw(resourceTextures.get(key), x + titleWidth, v, width / 10f, height / 10f);
            String text = String.valueOf(resources.get(key));
            glyphLayout.setText(font, text);
            font.draw(batch, glyphLayout, x + titleWidth + (width / 100f * 12), v + glyphLayout.height / 2f + height / 20f);
            count ++;
        }
    }

    public void drawAllInformation(SpriteBatch batch){
        int height = (int) (MyGdxGame.initialRes.y * 0.6);
        int width = (int) (MyGdxGame.initialRes.x * 0.3);
        float x = (int) (MyGdxGame.initialRes.x * 0.1);
        float y = (int) (MyGdxGame.initialRes.x * 0.05);
        drawColonistInformation(batch, (int) x, (int) y, width, height);
        drawResources(batch, (int) x, (int) y, width, height);
    }

    public void setupColonistTextures(){
        colonistTextures = new HashMap<>();
        String direction = "front";
        File directory= new File("core/assets/Textures/TAResources");
        String[] files = directory.list();
        assert files != null;
        for (String fileName : files) {
            String[] temp = fileName.split("\\.");
            if (temp[1].equals("atlas")){
                TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("core/assets/Textures/TAResources/" + fileName));
                TextureAtlas.AtlasRegion t = atlas.findRegion(direction);
                colonistTextures.put(temp[0], t);
            }
        }
    }

    public void setupResourceTextures(){
        resourceTextures = new HashMap<>();
        File dir = new File("core/assets/Textures/Resources");
        String[] files = dir.list();
        for (int i = 0; i < (files != null ? files.length : 0); i++) {
            String fileName = files[i];
            Texture texture = new Texture(Gdx.files.internal("core/assets/Textures/Resources/" + fileName));
            resourceTextures.put(fileName.split("\\.")[0], texture);
        }
    }
}
