package com.mygdx.game.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Entity.*;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Generation.MapSettings;
import com.mygdx.game.Generation.Things.*;
import com.mygdx.game.Generation.Tile;
import com.mygdx.game.Lighting.EdgeController;
import com.mygdx.game.Lighting.LightManager;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.Saving.RLE;
import com.mygdx.game.Sound.Sound;
import com.mygdx.game.Sound.SoundManager;
import com.mygdx.game.Weapons.Weapon;
import com.mygdx.game.floorDrops.FloorDrop;
import com.mygdx.game.floorDrops.Zone;
import com.mygdx.game.ui.elements.*;
import com.mygdx.game.ui.extensions.ButtonCollection;
import com.mygdx.game.ui.extensions.NotificationCollection;
import com.mygdx.game.ui.extensions.TutorialTracker;
import com.mygdx.game.ui.items.Clock;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class GameScreen implements Screen {
    public static int TILES_ON_X = 250;
    public static float TILE_DIMS = 20;
    public static int MAX_GAME_SPEED = 100;

    int count = 0;
    int nextEntityID;
    int nextEntityGroupID = 0;

    SpriteBatch batch;
    SpriteBatch batchWithNoProj;
    ShapeRenderer shapeRenderer;
    ShapeRenderer shapeRendererWithNoProj;

    CameraTwo camera;
    Vector2 moveOrigin = new Vector2(0, 0);

    String seed = "testSeed1"; //testSeed1

    Map map;
    ArrayList<Colonist> colonists;
    ArrayList<EntityGroup> barbarians = new ArrayList<>();
    ArrayList<EntityGroup> mobs = new ArrayList<>();

    ArrayList<Entity> allEntities = new ArrayList<>();

    MyGdxGame game;

    boolean isHost;
    boolean isMultiplayer;
    boolean endGame;
    private Socket socket;
    String socketID;

    HashMap<String, Texture> tileTextures = new HashMap<>();
    HashMap<String, TextureAtlas> thingTextures = new HashMap<>();
    HashMap<String, TextureAtlas> colonistClothes = new HashMap<>();
    HashMap<String, TextureAtlas> mobTextures = new HashMap<>();
    HashMap<String, Texture> actionSymbols = new HashMap<>();
    HashMap<String, Weapon> weaponPresets = new HashMap<>();
    HashMap<String, Texture> floorDropTextures = new HashMap<>();
    public static HashMap<String, ArrayList<Texture>> fireMap = new HashMap<>();

    String[] listOfColonistClothes;

    public Texture selectionIcon;

    float counter = 0f;
    float counterMax = 1f;
    float delta = 0f;
    boolean allowUpdate = true;

    public static int gameSpeed = 2; //game speed
    public static int lastGameSpeed = gameSpeed;

    static Random random = new Random();

    ButtonCollection bottomBarButtons;
    ButtonCollection ordersButtons;
    ButtonCollection buildingButtons;
    ButtonCollection resourceButtons;
    ButtonCollection optionsButtons;
    ButtonCollection gameSpeedButtons;
    ButtonCollection zoneButtons;
    ButtonCollection selectedColonistButtons;
    ButtonCollection selectedColonistSkills;
    ButtonCollection priorityButtons;
    public static NotificationCollection notifications;
    ButtonCollection startMultiplayerButtons;

    TutorialTracker tutorialTracker;

    int priorityStart;
    int priorityHeight = 7;

    Label gameSpeedLabel;

    HashMap<String, ArrayList<String>> orderTypes;

    String selectionMode = ""; //can be "building" or "orders" or "zones", "zoneDemolish", "priorities"
    String taskTypeSelected = "Mine";
    String buildingSelected = "stoneWall";

    boolean cancelSelection;
    boolean drawColonistPath;
    boolean drawBarbarianPath;
    boolean drawMobPath;
    boolean inCancelTaskMode;

    boolean showReservedOverlay;
    boolean showCanWalkOverlay;
    boolean showCanSpawnOverlay;
    boolean showTaskOverlay;

    public static boolean updateMobDrops;

    static boolean paused;

    boolean deanNorrisMode;
    Texture neanDorris;

    String lastMouseType = "";

    public static SoundManager soundManager = new SoundManager();
    boolean addSounds;
    String[] soundsToAdd;

    EdgeController ec;
    public static LightManager lightManager;
    boolean shouldSetupLights = false;

    Clock clock;

    // FIXED: 30/01/2022 add the selection rect and then add tasks based on the type and if the tile type is a match
    // TODO: 02/02/2022 Some of the tasks need to be drawn above the things and others below - gl
    // FIXED: 02/02/2022 need to change how the colonists get tasks

    Vector2 minSelecting = new Vector2(0, 0);
    Vector2 maxSelecting = new Vector2(0, 0);

    public static Entity selectedColonist;
    boolean shouldRemoveSelectedColonist = false;
    public boolean shouldShowSelectedColonistInfo = false;
    public static boolean followingSelected;
    public boolean showSelectedSkills;
    public boolean attackSelection;
    public boolean healSelection;
    BitmapFont font;
    GlyphLayout glyphLayout;

    float raidChance;

    boolean shouldUpdatePriorities = false;

    public static int score;

    public boolean autoSave = false;

    float totalTime;
    float timeSinceLastAutoSave;
    float timeSinceLastTimeSycn;
    float timeSinceLastCorpseRemoval;
    int autoSaveCount;
    String saveDir = "";

    public static final Json json = new Json();

    InputMultiplexer inputMultiplexer;

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
            if (!paused) {
                moveOrigin = camera.unproject(new Vector2(screenX, screenY));
                minSelecting = camera.unproject(new Vector2(screenX, screenY));
                maxSelecting = minSelecting;
            }
            lastMouseType = (Gdx.input.isButtonPressed(0) ? "left" : "right");

            if (lastMouseType.equals("right")){
                if (attackSelection) {
                    attackSelection = false;
                    setCursorDefault();
                }
                if (healSelection) {
                    healSelection = false;
                    setCursorDefault();
                }
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (lastMouseType.equals("left")) {
                if (!cancelSelection) {
                    System.out.println(selectionMode);
                    if (selectionMode.equals("Building")) {
                        setTasksFromSelection("Build", buildingSelected);
                    }
                    if (selectionMode.equals("Orders")) {
                        setTasksFromSelection(taskTypeSelected);
                    }
                    if (selectionMode.equals("Zones")) {
                        changeZone(minSelecting.x, minSelecting.y, maxSelecting.x, maxSelecting.y, true);
                    }
                    if (selectionMode.equals("ZoneDemolish")) {
                        changeZone(minSelecting.x, minSelecting.y, maxSelecting.x, maxSelecting.y, false);
                    }
                }
            }
            cancelSelection = false;
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            GameScreen.followingSelected = false;
            if (Gdx.input.isButtonPressed(1) && !paused) {
                Vector2 temp = camera.unproject(new Vector2(screenX, screenY));
                camera.move(moveOrigin.x - temp.x, moveOrigin.y - temp.y);
            }
            if (Gdx.input.isButtonPressed(0) && !paused) {
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
            if (!paused && !priorityButtons.showButtons) {
                Vector2 startPos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                camera.handleZoom(amountY);
                Vector2 endPos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                camera.move(startPos.x - endPos.x, startPos.y - endPos.y);
            }
            else if (priorityButtons.showButtons){
                System.out.println("Scrolling while priority buttons are showing");
                System.out.println(priorityStart);
                priorityStart = amountY > 0 ? priorityStart - 1 : priorityStart + 1;
                if (priorityStart + priorityHeight > colonists.size()){
                    priorityStart = colonists.size() - priorityHeight;
                }
                if (priorityStart < 0){
                    priorityStart = 0;
                }
                shouldUpdatePriorities = true;
            }
            return false;
        }
    };

    public GameScreen(MyGdxGame game, ArrayList<Colonist> colonists, Map map, String dirName, String saveName){
        this(game, colonists, map);
        loadEntities(dirName, saveName);
    }

    public GameScreen(MyGdxGame game, ArrayList<Colonist> colonists, Map map) {
        MyGdxGame.initialRes = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.isHost = true;

        this.game = game;
        game.currentGameScreen = this;

        this.map = map;

        int radius = (int) (Gdx.graphics.getWidth() * 0.05);
        clock = new Clock(Gdx.graphics.getWidth() - radius, Gdx.graphics.getHeight() - radius, radius, "old");

        Gdx.graphics.setForegroundFPS(game.fpsCap);
        Gdx.graphics.setVSync(game.vsyncEnabled);

        this.colonists = colonists;
        setup();
        System.out.println("hello world");
    }

    public GameScreen(MyGdxGame game, String ip, Map map){
        MyGdxGame.initialRes = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.isHost = false;

        this.game = game;
        game.currentGameScreen = this;
        this.isMultiplayer = true;
        this.map = map;
        int radius = (int) (Gdx.graphics.getWidth() * 0.05);
        clock = new Clock(Gdx.graphics.getWidth() - radius, Gdx.graphics.getHeight() - radius, radius, "old");
        connectSocket(ip);
        createSocketListeners();

        Gdx.graphics.setForegroundFPS(game.fpsCap);
        Gdx.graphics.setVSync(game.vsyncEnabled);
        setup();
    }

    public void setup(){
        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        glyphLayout = new GlyphLayout(font, "");

        RLE.setThingNameCode();

        tutorialTracker = new TutorialTracker();

        Colonist.deanTexture = new Texture(Gdx.files.internal("core/assets/Textures/msc/deanNorris.jpg"));
        selectionIcon = new Texture(Gdx.files.internal("core/assets/Textures/ui/selection/selectedIcon.png"));

        Entity.setHealthFromType();

        listOfColonistClothes = ColonistSelectionScreen.getListOfClothes();

        FloorDrop.font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        FloorDrop.font.getData().setScale(0.2f);

        Zone.font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        Zone.font.getData().setScale(0.5f);

        initialiseAllTextures();

        setupWeaponPresets();

        fireMap = Fire.setupFireMap();

        if (isHost) {
            setColonistIDs();
            giveAllConsistsRandomWeapons();
        }

        map.setupResourceHashMap();
        setupResourceButtons();

        inputMultiplexer = new InputMultiplexer();

        setupBBB();
        setupOrdersButtons();
        setupOptionsButtons();
        setupBuildButtons();
        setupOrderTypes();
        setupGameSpeedButtons(clock);
        setupZoneButtons();
        setupSelectedColonistsButtonCollection();
        setupNotifications();

        optionsButtons.showButtons = paused;

        batch = new SpriteBatch();
        batchWithNoProj = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRendererWithNoProj = new ShapeRenderer();
        camera = new CameraTwo();
        camera.setMinMax(new Vector2(0,0), new Vector2(GameScreen.TILES_ON_X * TILE_DIMS, GameScreen.TILES_ON_X * TILE_DIMS));
        camera.update();

        neanDorris = new Texture(Gdx.files.internal("core/assets/Textures/msc/neanDorris.jpg"));

        inputMultiplexer.addProcessor(gameInputProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);

        setupLights();
        refreshAllLights();

        setupPriorityButtons();

        if (isHost) {
            map.addFloorDrop(2, 2, "stone", 100, socket, true);
            map.addFloorDrop(2, 3, "wood", 100, socket, true);
            map.addFloorDrop(2, 4, "berry", 100, socket, true);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
        gameSpeed = 2;
        paused = false;
        optionsButtons.showButtons = false;
        gameSpeedLabel.setText(gameSpeed + "x");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(79/255f, 109/255f, 158/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.delta += delta;
        totalTime += delta;
        timeSinceLastAutoSave += delta;
        timeSinceLastTimeSycn += delta;
        timeSinceLastCorpseRemoval += delta;

        if ((int) totalTime % 300 == 0 && autoSave && timeSinceLastAutoSave > 2){
            autoSaveGame();
            timeSinceLastAutoSave = 0;
        }

        if ((int) totalTime % 60 == 0 && isMultiplayer && isHost && timeSinceLastTimeSycn > 2){
            socket.emit("syncTime", clock.getTime());
            timeSinceLastTimeSycn = 0;
        }

        if ((int) totalTime % 120 == 0 && timeSinceLastCorpseRemoval > 2){
            removeCorpses();
            System.out.println("removing corpses");
            timeSinceLastCorpseRemoval = 0;
        }

        if (shouldRemoveSelectedColonist){
            shouldShowSelectedColonistInfo = false;
            selectedColonist = null;
            showSelectedSkills = false;
        }
        
        boolean isLeftJustClicked = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean isRightJustClicked = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        
        Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector2 unprojectedMousePos = camera.unproject(mousePos);

        camera.allowMovement = !paused;
        camera.update();

        updateRaids();
        map.update(delta);

        updateAttackSelection();
        updateHealSelection();

        calculateResources();
        updateResourceButtons();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix.getGdxMatrix());

        if (!deanNorrisMode){
            map.drawMap(batch, tileTextures, camera);
        }
        else {
            drawDeanOver(batch);
        }

        map.drawThings(batch, thingTextures, camera, 0, 1);
        drawAllMobs(batch, shapeRenderer);
        drawAllBarbarians(batch, shapeRenderer);
        allowUpdate = true;
        if (!deanNorrisMode){
            drawAllColonists(batch, shapeRenderer);
        }
        else {
            drawAllColonistsAsDeanNorris(batch);

        }

        map.drawThings(batch, thingTextures, camera, 1, 3);

        drawTaskType(batch);

        map.drawFires(batch, fireMap);
        map.updateFires(map);

        map.drawFloorDrops(batch, floorDropTextures);

        batch.end();

        shapeRenderer.setProjectionMatrix(camera.projViewMatrix.getGdxMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (attackSelection){
            highLightAttackAble(shapeRenderer);
        }
        else if (healSelection){
            highLightHealAble(shapeRenderer);
        }
        shapeRenderer.end();

        map.drawZones(shapeRenderer, batch, floorDropTextures);

        drawAllTaskPercentages(shapeRenderer);

        if (showReservedOverlay){
            highlightAllReserved(shapeRenderer);
        }
        if (showCanSpawnOverlay){
            highlightAllCanSpawnOn(shapeRenderer);
        }
        if (showCanWalkOverlay){
            highlightAllCanWalkOn(shapeRenderer);
        }
        if (showTaskOverlay){
            highlightAllTasks(shapeRenderer);
        }

        drawColonistsPath(shapeRenderer);
        drawAllBarbarianPaths(shapeRenderer);
        drawAllMobsPaths(shapeRenderer);

        batchWithNoProj.begin();
        bottomBarButtons.drawButtons(batchWithNoProj);
        ordersButtons.drawButtons(batchWithNoProj);
        buildingButtons.drawButtons(batchWithNoProj);
        resourceButtons.drawButtons(batchWithNoProj);
        gameSpeedButtons.drawButtons(batchWithNoProj);
        gameSpeedLabel.draw(batchWithNoProj, 0);
        zoneButtons.drawButtons(batchWithNoProj);
        notifications.draw(batchWithNoProj, shapeRenderer, map, allEntities);
        priorityButtons.drawButtons(batchWithNoProj);

        drawColonistsAtTop(batchWithNoProj, shapeRendererWithNoProj);

        batchWithNoProj.end();

        if (shouldShowSelectedColonistInfo) {
            drawSelectedColonistUI(batchWithNoProj, shapeRendererWithNoProj);
        }
        updateSelectedColonist(batchWithNoProj);

        if (paused) {
            Gdx.gl.glEnable(GL30.GL_BLEND);
            Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
            shapeRendererWithNoProj.begin(ShapeRenderer.ShapeType.Filled);
            shapeRendererWithNoProj.setColor(160 / 255f, 160 / 255f, 160 / 255f, 0.8f);
            shapeRendererWithNoProj.rect(0, 0, MyGdxGame.initialRes.x, MyGdxGame.initialRes.y);
            shapeRendererWithNoProj.end();
            Gdx.gl.glDisable(GL30.GL_BLEND);
        }

        counter += delta * gameSpeed;
        if (counter > counterMax) {
            update();
            counter = 0f;
        }

        batchWithNoProj.begin();
        clock.draw(delta, batchWithNoProj, shapeRendererWithNoProj);
        batchWithNoProj.end();

        drawTimeCover(shapeRendererWithNoProj, clock);

        lightManager.updateLights(ec, GameScreen.TILE_DIMS);
        lightManager.drawLights(batch);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        ec.drawEdges(shapeRenderer, GameScreen.TILE_DIMS);
        shapeRenderer.end();

        batchWithNoProj.begin();
        optionsButtons.drawButtons(batchWithNoProj);
        startMultiplayerButtons.drawButtons(batchWithNoProj);
        batchWithNoProj.end();

        if (map.lightShouldBeUpdated){
            map.lightShouldBeUpdated = false;
            lightManager.setAllToShouldUpdate();
            ec.update(map.things);
        }

        if (shouldSetupLights){
            setupNewLights();
            shouldSetupLights = false;
        }

        soundManager.updateSounds(camera.position.x / GameScreen.TILE_DIMS, camera.position.y / GameScreen.TILE_DIMS);

        Gdx.graphics.setTitle(MyGdxGame.title + "     FPS: " + (Gdx.graphics.getFramesPerSecond()));

        boolean notisClicked = false;
        if (isLeftJustClicked || isRightJustClicked) {
            notisClicked = notifications.updateNotis(Gdx.input.isButtonJustPressed(0), Gdx.input.isButtonJustPressed(1),
                    Gdx.input.getX(), (int) (MyGdxGame.initialRes.y - Gdx.input.getY()), camera, map, allEntities);
        }

        if (Gdx.input.isButtonPressed(0)) {
            if (!paused){
                if (!(bottomBarButtons.updateButtons(camera, isLeftJustClicked)
                        || ordersButtons.updateButtons(camera, isLeftJustClicked)
                        || buildingButtons.updateButtons(camera, isLeftJustClicked)
                        || gameSpeedButtons.updateButtons(camera, isLeftJustClicked)
                        || zoneButtons.updateButtons(camera, isLeftJustClicked)
                        || notisClicked
                        || priorityButtons.updateButtons(camera, isLeftJustClicked))){
                    drawSelectionScreen(shapeRenderer, camera);
                    batch.begin();
                    batch.setProjectionMatrix(camera.projViewMatrix.getGdxMatrix());
                    highlightSelected(batch);
                    batch.end();
                }
                else {
                    cancelSelection = true;
                }
                if (selectedColonistButtons.updateButtons(camera, Gdx.input.isButtonJustPressed(0)) && shouldShowSelectedColonistInfo){
                    cancelSelection = true;
                }
                gameSpeedLabel.setText(gameSpeed + "x");
            }
            else {
                if (!startMultiplayerButtons.updateButtons(camera, isLeftJustClicked)) {
                    optionsButtons.updateButtons(camera, Gdx.input.isButtonJustPressed(0));
                }
            }
        }

        if (isLeftJustClicked && !attackSelection) {
            Entity toSelect = getEntityAtUsingFull(unprojectedMousePos.x, unprojectedMousePos.y);
            if (toSelect != null) {
                selectedColonist = toSelect;
                shouldShowSelectedColonistInfo = true;
                followingSelected = true;
                hideAllButtons();
            }
        }

        if (isLeftJustClicked){
            if (!paused) {
                if (bottomBarButtons.updateButtons(camera, true)) {
                    switch (bottomBarButtons.pressedButtonName) {
                        case "OrdersButton" -> {
                            selectionMode = "Orders";
                            updateShowingButtons(ordersButtons);
                        }
                        case "BuildingButton" -> {
                            selectionMode = "Building";
                            updateShowingButtons(buildingButtons);
                        }
                        case "ZonesButton" -> {
                            selectionMode = "Zones";
                            updateShowingButtons(zoneButtons);
                        }
                        case "PrioritiesButton" -> {
                            tutorialTracker.prioritiesButtonsPressed = true;
                            selectionMode = "Priority";
                            updateShowingButtons(priorityButtons);
                        }
                    }
                    if (!ordersButtons.showButtons || !buildingButtons.showButtons) {
                        setCursorDefault();
                    }
                }

                if (ordersButtons.updateButtons(camera, true)) {
                    inCancelTaskMode = false;
                    taskTypeSelected = ordersButtons.pressedButtonName.replace("Button", "");
                    if (taskTypeSelected.equals("Cancel")) {
                        inCancelTaskMode = true;
                    }
                    setCustomCursor(taskTypeSelected);
                }
                if (buildingButtons.updateButtons(camera, true)) {
                    inCancelTaskMode = false;
                    buildingSelected = buildingButtons.pressedButtonName.replace("Button", "");
                } // TODO: 09/04/2022 remove button from the name

//                gameSpeedButtons.updateButtons(camera, Gdx.input.isButtonJustPressed(0));
            }
            if (optionsButtons.updateButtons(camera, false)){
                switch (optionsButtons.pressedButtonName) {
                    case "ResumeButton" -> {
                        optionsButtons.showButtons = false;
                        setPause(false);
                    }
                    case "OptionsButton" -> {
                        setPause(false);
                        game.setScreen(new SettingsScreen(game, true));
                    }
                    case "SaveButton" -> {
                        game.setScreen(new SaveScreen(game, map, colonists, this));
                        setPause(false);
                    }
                    case "LoadButton" -> game.setScreen(new LoadSaveScreen2(game));
                    case "MainMenuButton" -> {
                        if (isMultiplayer){
                            if (isHost){
                                socket.emit("endGame", "");
                            }
                            socket.disconnect();
                        }
                        game.setScreen(game.mainMenu);
                    }
                    case "ExitButton" -> Gdx.app.exit();
                }
            }
            System.out.println(optionsButtons.showButtons);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (gameSpeed == 0){
                gameSpeed = lastGameSpeed;
                gameSpeedLabel.setText(gameSpeed + "x");
            }
            else {
                lastGameSpeed = gameSpeed;
                gameSpeed = 0;
                gameSpeedLabel.setText(gameSpeed + "x");
            }
            if (isMultiplayer) {
                socket.emit("updateGameSpeed", GameScreen.gameSpeed);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            int x = (int) (unprojectedMousePos.x / TILE_DIMS);
            int y = (int) (unprojectedMousePos.y / TILE_DIMS);
            if (map.isWithinBounds(x, y)) {
                colonists.get(0).setMoveToPos(x,y, map, allEntities);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            drawColonistPath = !drawColonistPath;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            spawnRaid(5);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.G)){
            deanNorrisMode = !deanNorrisMode;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)){
            int x = (int) (unprojectedMousePos.x / TILE_DIMS);
            int y = (int) (unprojectedMousePos.y / TILE_DIMS);

            if (map.isWithinBounds(x, y)) {
                Door door = new Door(x, y, (int) TILE_DIMS, (int) TILE_DIMS, "stoneDoor", (int) TILE_DIMS);
                map.addThing(door, x, y, false, socket, isHost);
                door.triggerOpen();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)){
            if (isHost) {
                spawnMobs("poong", 10, 50, 50);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)){
            if (isHost) {
                spawnRaid(3);
                notifications.add(socket, isHost, new Notification(notifications.getNextId(), "Raid", "raid"));
//                spawnBarbarians("barbarian", 4, 20, 50);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)){
            for (EntityGroup eg : barbarians) {
                for (Entity b : eg.entities) {
                    if (b.isAlive()) {
                        b.setDefender(colonists.get(count));
                    }
                }
            }
            count++;
        }

//        System.out.println(colonists.get(0).getHealth());

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)){
            drawBarbarianPath = !drawBarbarianPath;
            drawMobPath = !drawMobPath;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)){
            showReservedOverlay = !showReservedOverlay;
            System.out.println("showReservedOverlay: " + showReservedOverlay);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) {
            showCanSpawnOverlay = !showCanSpawnOverlay;
            System.out.println("showCanSpawnOverlay: " + showCanSpawnOverlay);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) {
            showCanWalkOverlay = !showCanWalkOverlay;
            System.out.println("showCanWalkOverlay: " + showCanWalkOverlay);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
            showTaskOverlay = !showTaskOverlay;
            System.out.println("showTaskOverlay: " + showTaskOverlay);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M) || totalTime % 5 == 0 && isHost && isMultiplayer) {
            Json json = new Json();
            String colonistsJson = json.toJson(colonists);
            socket.emit("checkMovementSync", colonistsJson);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.RED);
            for (Sound s : soundManager.soundEffects) {
                shapeRenderer.circle(s.getX() * GameScreen.TILE_DIMS, s.getY() * GameScreen.TILE_DIMS, s.getRadius() * GameScreen.TILE_DIMS);
            }
            shapeRenderer.end();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            int x = (int) (unprojectedMousePos.x / GameScreen.TILE_DIMS);
            int y = (int) (unprojectedMousePos.y / GameScreen.TILE_DIMS);
            map.addFloorDrop(x,y, "stone", socket, isHost);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.U)){
            addNotification("Fire", "fire");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)){
            addNotification("Raid", "raid");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)){
            System.out.println("endiong game");
            game.setScreen(new EndScreen(game, this));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)){
            game.setScreen(new SaveScreen(game, map, colonists, this));
        }

        if (endGame){
            game.setScreen(new MainMenu(game));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            optionsButtons.showButtons = !optionsButtons.showButtons;
            startMultiplayerButtons.showButtons = false;
            setPause(optionsButtons.showButtons);
        }
    }

    public void update(){ //happens at a rate determined by the gameSpeed
        moveColonists();
        moveMobs();
        moveBarbarians();

        checkNotEnd();

        if (updateMobDrops){
            dropFoodForDeadMobs();
            updateMobDrops = false;
        }

        if (isHost){
            randomlySpawnMobs();
            destroyMobs();
        }

        if (isHost && isMultiplayer){
            try {
                sendColonistMovement();
                // TODO: 13/04/2022 send mob movement
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (shouldUpdatePriorities){
            updatePriorityButtons();
            shouldUpdatePriorities = false;
        }
    }

    public void setPause(boolean pause){
        if (pause){
            lastGameSpeed = gameSpeed;
            gameSpeed = 0;
        }
        else {
            gameSpeed = lastGameSpeed;
        }
        if (isMultiplayer){
            socket.emit("updateGameSpeed", GameScreen.gameSpeed);
        }
        paused = pause;
    }

    @Override
    public void resize(int width, int height) {
        boolean keepShow = false;
        if (optionsButtons.showButtons){
            keepShow = true;
        }
        setupOptionsButtons();
        optionsButtons.showButtons = keepShow;
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
        soundManager.dispose();
        shapeRenderer.dispose();
        batch.dispose();
    }

    public void initialiseAllTextures(){
        initialiseTextures();
        setupActionSymbols();
        setupColonistClothes();
        setupMobTextures();
        setupFloorDropHashMap();
    }

    public void initialiseTextures(){
        //loads every texture in the textures map
        getAllMapTextures(tileTextures, thingTextures);
    }

    static void getAllMapTextures(HashMap<String, Texture> tileTextures, HashMap<String, TextureAtlas> thingTextures) {
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

    public void setupNewLights(){
        for (int i = 0; i < TILES_ON_X; i++) {
            for (int j = 0; j < TILES_ON_X; j++) {
                Thing t = map.things.get(i).get(j);
                if (t != null){
                    if (t.emitsLight && !t.hasBeenSetup){
                        t.setup();
                        map.lightShouldBeUpdated = true;
                    }
                }
            }
        }
    }

    public boolean connectSocket(String ip){
//        Scanner sc = new Scanner(System.in);
//        System.out.println("Enter IP address: ");
//        String ip = sc.nextLine();
        System.out.println("Connecting to " + ip);
        try {
            socket = IO.socket("http://" + ip);
            socket.connect();
            return true;
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e){
            System.out.println("Could not connect to server");
        }
        return false;
    }

    public void createSocketListeners() {
        Json json = new Json();

        socket.on("connect", args -> System.out.println("Connected to server"));
        socket.on("newPlayer", args -> {
            try {
                JSONObject data = new JSONObject();

                data.put("tiles", json.toJson(map.packageTiles()));
                data.put("things", json.toJson(map.packageThings()));
                data.put("mapWidth", GameScreen.TILES_ON_X);
                data.put("mapHeight", GameScreen.TILES_ON_X);
                data.put("tileDims", GameScreen.TILE_DIMS);
                data.put("colonists", json.toJson(colonists));
                data.put("settings", json.toJson(map.settings));
                data.put("resources", json.toJson(map.resources));
                data.put("tasks", json.toJson(map.tasks));
                data.put("time", json.toJson(clock.getTime()));
                data.put("mapFloorDrops", json.toJson(map.floorDrops));
                data.put("zones", json.toJson(map.zones));
                data.put("nextZoneID", map.getZoneID());
                data.put("mobs", json.toJson(mobs));
                data.put("barbarians", json.toJson(barbarians));
                data.put("nextEntityGroupID", nextEntityGroupID);
                data.put("gameSpeed", gameSpeed);
                data.put("fire", json.toJson(map.fire));

                socket.emit("loadWorld", data);
            }
            catch (Exception e) {
                System.out.println("Error in creating map json");
            }
        });

        socket.on("endGame", args -> {
            System.out.println("Game ended");
            endGame = true;
        });

        socket.on("playSound", args -> {
            String soundName = (String) args[0];
            int x = (int) args[1];
            int y = (int) args[2];
            soundManager.addSound(soundName, x, y, socket, false);
//            soundManager.updateSounds(camera.position.x, camera.position.y);
        });

        socket.on("stopSound", args -> {
            String soundName = (String) args[0];
            int x = (int) args[1];
            int y = (int) args[2];
            soundManager.removeSound(soundName, x, y);
//            soundManager.updateSounds(camera.position.x, camera.position.y);
        });

        socket.on("updateHealth", args -> {
            Entity e = getEntityWithId((int) args[0]);
            if (e != null) {
                e.setHealth((int) args[1]);
            }
        });

        socket.on("updatePriority", args -> {
            int colonistID = (int) args[0];
            String priority = (String) args[1];
            int value = (int) args[2];
            Colonist c = getColonistWithID(colonistID);
            if (c != null) {
                c.setPriorityValue(priority, value);
            }
        });

        socket.on("addNoti", args -> {
            String noti = (String) args[0];
            Notification notis = json.fromJson(Notification.class, noti);
            notifications.add(socket, false, notis);
        });

        socket.on("removeNoti", args -> {
            Notification n = json.fromJson(Notification.class, (String) args[0]);
            notifications.remove(n, socket, false);
        });

        socket.on("updateGameSpeed", args -> {
            gameSpeed = (int) args[0];
            gameSpeedLabel.setText(gameSpeed + "x");
        });

        socket.on("addFloorDrop", args -> {
            int x = (int) args[0];
            int y = (int) args[1];
            String type = (String) args[2];
            int amount = (int) args[3];
            map.addFloorDrop(x, y, type, amount, socket, false);
        });

        socket.on("entityAttacking", args -> {
            int attackerID = (int) args[0];
            int defenderID = (int) args[1];
            Entity attacker = null;
            Entity defender = null;
            for (Entity e : allEntities) {
                if (e.getEntityID() == attackerID) {
                    attacker = e;
                    e.isAttacking = true;
                }
                if (e.getEntityID() == defenderID) {
                    defender = e;
                    e.isAttacking = true;
                }
            }
            assert attacker != null;
            attacker.defender = defender;
            assert defender != null;
            defender.addAttacker(attacker);
        });

        socket.on("setTasksFromSelection", args -> {
            String type = (String) args[0];
            String subType = (String) args[1];
            int minXCoord = (int) args[2];
            int minYCoord = (int) args[3];
            int maxXCoord = (int) args[4];
            int maxYCoord = (int) args[5];
            setTasksFromInput(type, subType, minXCoord, minYCoord, maxXCoord, maxYCoord);
        });

        socket.on("cancelTasksFromSelection", args -> {
            int minXCoord = (int) args[0];
            int minYCoord = (int) args[1];
            int maxXCoord = (int) args[2];
            int maxYCoord = (int) args[3];
            boolean past = inCancelTaskMode;
            inCancelTaskMode = true;
            setTasksFromInput("", "", minXCoord, minYCoord, maxXCoord, maxYCoord);
            inCancelTaskMode = past;
        });

        socket.on("spawnMobs", args -> {
            EntityGroup eg = json.fromJson(EntityGroup.class, args[0].toString());
            allEntities.addAll(eg.entities);
            mobs.add(eg);
        });

        socket.on("destroyMobs", args -> {
            int Id = (int) args[0];
            EntityGroup eg = getEntityGroupWithID(Id, mobs);
            if (eg != null) {
                mobs.remove(eg);
                for (Entity e : eg.entities) {
                    allEntities.remove(e);
                }
            }
        });

        socket.on("spawnBarbarians", args -> {
            EntityGroup eg = json.fromJson(EntityGroup.class, args[0].toString());
            allEntities.addAll(eg.entities);
            barbarians.add(eg);
        });

        socket.on("taskReservation", args -> {
            int x = (int) args[0];
            int y = (int) args[1];
            String type = (String) args[2];
            map.getTaskAt(x, y, type).reserved = true;
        });

        socket.on("completeTask", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                int x = data.getInt("x");
                int y = data.getInt("y");
                String type = data.getString("type");
                map.tasks.remove(map.getTaskAt(x, y, type));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        socket.on("getUpdatedEntities", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                getEntityMovement(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        socket.on("checkMovementSync", args -> {
            String data = (String) args[0];
            ArrayList<Colonist> colonistList = json.fromJson(ArrayList.class, data);
            boolean desynchronized = false;
            for (Colonist colonist : colonistList) {
                for (Colonist colonist2 : colonists) {
                    if (colonist.getEntityID() == colonist2.getEntityID()) {
                        float distance = (float) Math.sqrt(Math.pow(colonist.getX() - colonist2.getX(), 2) + Math.pow(colonist.getY() - colonist2.getY(), 2));
                        if (distance > 0.1f) {
                            desynchronized = true;
                            System.out.println("Colonist "+ colonist.getEntityID() + " is out of sync");
                            if (isHost){
                                System.out.println("hosts colonists is at " + colonist.getX() + " " + colonist.getY());
                                System.out.println("others colonists is at " + colonist2.getX() + " " + colonist2.getY());
                                System.out.println("distance is " + distance);
                                System.out.println("re-syncing to host");
                                colonist2.setX(colonist.getX());
                                colonist2.setY(colonist.getY());
                            }
                        }
                    }
                }
            }
            if (!desynchronized) {
                System.out.println("Colonists are in sync");
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
                setColonistIDs();

                mobs = json.fromJson(ArrayList.class, EntityGroup.class, data.get("mobs").toString());
                barbarians = json.fromJson(ArrayList.class, EntityGroup.class, data.get("barbarians").toString());

                String packagedTiles = json.fromJson(String.class, data.get("tiles").toString());
                String packagedThings = json.fromJson(String.class, data.get("things").toString());
                int mapWidth = data.getInt("mapWidth");
                int tileDims = data.getInt("tileDims");
                GameScreen.TILES_ON_X = mapWidth;
                GameScreen.TILE_DIMS = tileDims;

                map.settings = json.fromJson(MapSettings.class, data.get("settings").toString());
                map.tiles = RLE.decodeTiles(packagedTiles, GameScreen.TILES_ON_X);
                map.things = RLE.decodeThings(packagedThings, GameScreen.TILES_ON_X);

                map.updateAllTilesWithNewThings();

                map.tasks = json.fromJson(ArrayList.class, data.get("tasks").toString());
                map.resources = json.fromJson(HashMap.class, data.get("resources").toString());
                map.floorDrops = json.fromJson(ArrayList.class, FloorDrop.class, data.get("mapFloorDrops").toString());
                map.refreshFloorDrops();
                map.zones = json.fromJson(ArrayList.class, Zone.class, data.get("zones").toString());
                map.setZoneID(json.fromJson(Integer.class, data.get("nextZoneID").toString()));
                map.fire = json.fromJson(ArrayList.class, Fire.class, data.get("fire").toString());

                nextEntityGroupID = json.fromJson(Integer.class, data.get("nextEntityGroupID").toString());

                clock.setTime(json.fromJson(String.class, data.get("time").toString()));

                game.currentGameScreen = this;
                gameSpeed = data.getInt("gameSpeed");
                System.out.println("Loaded world" + colonists.size());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        socket.on("syncTime", args -> {
            String time = (String) args[0];
            clock.setTime(time);
        });

        socket.on("colonistTask", args -> {
           JSONObject data = (JSONObject) args[0];
           try {
               int colonistID = data.getInt("colonistID");
               String task = data.getString("task");
               Task t = json.fromJson(Task.class, task);
               for (Colonist colonist : colonists) {
                   if (colonist.colonistID == colonistID) {
                       colonist.completingTask = true;
                       Task fromMap = map.getTaskAt(t.getX(), t.getY(), t.type);
                       if (fromMap != null) {
                           fromMap.reserved = true;
                           colonist.setCurrentTask(fromMap);
                       }
                   }
               }
           } catch (JSONException e) {
               e.printStackTrace();
           }
        });

        socket.on("addDropToZone", args -> {
            int colX = (int) args[0];
            int colY = (int) args[1];
            String type = (String) args[2];
            int amount = (int) args[3];
            Zone z = map.findNearestZone(colX, colY, type, amount);
            if (z != null) {
                z.addFloorDrop(new FloorDrop(0,0,type, amount), map, colX, colY);
            }
        });

        socket.on("updateTaskPercentage", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                int x = data.getInt("x");
                int y = data.getInt("y");
                String type = data.getString("type");
                float percentage = (float) data.getDouble("percentage");
                Task t = map.getTaskAt(x, y, type);
                t.setPercentageComplete(percentage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        socket.on("addTask", args -> {
            String type = (String) args[0];
            String subType = (String) args[1];
            int x = (int) args[2];
            int y = (int) args[3];
            map.tasks.add(new Task(type, subType, x, y));
        });

        socket.on("addFire", args -> {
            int x = (int) args[0];
            int y = (int) args[1];
            String name = (String) args[2];
            map.fire.add(new Fire(x, y, name, fireMap.get(name).size()));
        });

        socket.on("removeFire", args -> {
            int x = (int) args[0];
            int y = (int) args[1];
            map.removeFire(x, y, socket, false);
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

        socket.on("changeThingType", args -> {
            int x = (int) args[0];
            int y = (int) args[1];
            String type = (String) args[2];
            int height = (int) args[3];
            boolean emitsLight = (boolean) args[4];
            String thingType = RLE.thingClassType.get(type);
            switch (thingType) {
                case "Thing" -> {
                    Thing t = new Thing(x, y, (int) GameScreen.TILE_DIMS, (int) (height * GameScreen.TILE_DIMS), type, (int) GameScreen.TILE_DIMS);
                    map.addThing(t, x, y, true, socket, isHost);
                }
                case "AnimatedThing" -> {
                    AnimatedThings at = new AnimatedThings(x, y, (int) GameScreen.TILE_DIMS, (int) (height * GameScreen.TILE_DIMS), type, (int) GameScreen.TILE_DIMS);
                    map.addThing(at, x, y, true, socket, isHost);
                }
                case "ConnectedThing" -> {
                    ConnectedThings ct = new ConnectedThings(x, y, (int) GameScreen.TILE_DIMS, (int) (height * GameScreen.TILE_DIMS), type, (int) GameScreen.TILE_DIMS);
                    map.addThing(ct, x, y, true, socket, isHost);
                }
                case "Door" -> {
                    Door d = new Door(x, y, (int) GameScreen.TILE_DIMS, (int) (height * GameScreen.TILE_DIMS), type, (int) GameScreen.TILE_DIMS);
                    map.addThing(d, x, y, true, socket, isHost);
                }
            }
            if (emitsLight) {
                shouldSetupLights = true;
                map.things.get(x).get(y).emitsLight = true;
//                Task.setupLight(type, map.things.get(x).get(y), map);
            }
//            map.changeThingType(x, y, type, height, true);
        });

        socket.on("addZone", args -> {
            int x = (int) args[0];
            int y = (int) args[1];
            int x2 = (int) args[2];
            int y2 = (int) args[3];
            addZonePartTwo(x, y, x2, y2);
        });

        socket.on("removeZone", args -> {
            int x = (int) args[0];
            int y = (int) args[1];
            int x2 = (int) args[2];
            int y2 = (int) args[3];
            removeZonePartTwo(x, y, x2, y2);
        });

        socket.on("removeFloorDrop", args -> {
            int x = (int) args[0];
            int y = (int) args[1];
            String type = (String) args[2];
            map.removeFloorDrop(x, y, type);
        });

        socket.on("getTasks", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                ArrayList<Task> tasks = json.fromJson(ArrayList.class, Task.class, data.get("tasks").toString());
                map.tasks.clear();
                map.tasks.addAll(tasks);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void startMultiplayer(String ip) {
        Gdx.app.log("Multiplayer", "enabling multiplayer");
        if(connectSocket(ip)) {
            createSocketListeners();
            socket.emit("joinRoom", "test1");
            isMultiplayer = true;
        }
    }

    public void drawAllColonists(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        for (Colonist c : colonists) {
            c.draw(batch, GameScreen.TILE_DIMS, colonistClothes);
        }
        batch.end();

        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0.2f, 1f, 0.5f);
        for (Colonist c : colonists) {
            c.drawPathOutline(shapeRenderer);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);

        batch.begin();
    }

    public void drawAllColonistsAsDeanNorris(SpriteBatch batch){
        for (Colonist c : colonists) {
            c.drawAsDeanNorris(batch, GameScreen.TILE_DIMS);
        }
    }

    public void moveColonists(){
        if (isHost) {
            for (Colonist c : colonists) {
                c.moveColonist(map, socket, allEntities, isHost);
            }
        }
    }

    public void moveMobs(){
        if (isHost) {
            for (EntityGroup eg : mobs) {
                eg.moveGroup(map, allEntities, socket, isHost);
            }
        }
    }

    public void moveBarbarians(){
        if (isHost) {
            for (EntityGroup eg : barbarians) {
                eg.moveGroup(map, allEntities, socket, isHost);
            }
        }
    }

    public void setupColonistClothes(){
        getTAResources(colonistClothes, "TAResources");
    }

    public void setupMobTextures(){
        getTAResources(mobTextures, "mobTAResources");
    }

    static void getTAResources(HashMap<String, TextureAtlas> hashMap, String location) {
        File directory= new File("core/assets/Textures/" + location);
        String[] files = directory.list();
        assert files != null;
        for (String fileName : files) {
            String[] temp = fileName.split("\\.");
            if (temp[1].equals("atlas")){
                hashMap.put(temp[0], new TextureAtlas(Gdx.files.internal("core/assets/Textures/" + location + "/" + fileName)));
            }
        }
    }

    public boolean saveGame(String dirName, String saveName){
        Json json = new Json();

        String tileSave = RLE.encodeTiles(map);
        String thingSave = RLE.encodeThings(map);
        String colonistSave = json.toJson(colonists);
        String mobSave = json.toJson(mobs);
        String barbarianSave = json.toJson(barbarians);
        String resourcesSave = json.toJson(map.resources);
        String zonesSave = json.toJson(map.zones);
        String fireSave = json.toJson(map.fire);

        String mapInfo = MyGdxGame.initialRes.x + " " + MyGdxGame.initialRes.y;
        String mapDims = String.valueOf(GameScreen.TILES_ON_X);

        System.out.println(tileSave);
        System.out.println(thingSave);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        String time = dtf.format(now);

        File file = new File("core/assets/Saves/" + dirName);
        file.mkdir();


        if (!file.mkdir()){
            File save = new File("core/assets/Saves/" + dirName + "/" + saveName);
            save.delete();
        }

        FileHandle fileHandle = Gdx.files.local("core/assets/Saves/" + dirName + "/" + saveName);
        fileHandle.writeString("date: " + time + "\nmapDims: " + mapDims
                + "\ntiles: " + tileSave + "\nthings: " + thingSave
                + "\ncolonists: " + colonistSave + "\nmobs: " + mobSave
                + "\nbarbarians: " + barbarianSave + "\nmapInfo: " + mapInfo
                + "\nresources: " + resourcesSave + "\nzones: " + zonesSave
                + "\nfires: " + fireSave, false);

        File save = new File("core/assets/Saves/" + dirName + "/" + saveName);
        return save.exists();
    }

    public void autoSaveGame(){
        String[] autoSaveNames = new String[]{"autoSave1", "autoSave2", "autoSave3"};
        String name = autoSaveNames[autoSaveCount];
        autoSaveCount++;
        if (autoSaveCount > 2){
            autoSaveCount = 0;
        }
        if (!saveDir.equals("")) {
            saveGame(saveDir, name);
        }
    }

    public void setupBBB(){
        bottomBarButtons = new ButtonCollection();
        bottomBarButtons.useWorldCoords = false;
        TextButton orders = new TextButton("Orders", "OrdersButton");
        TextButton building = new TextButton("Building", "BuildingButton");
        TextButton zones = new TextButton("Zones", "ZonesButton");
        TextButton priorities = new TextButton("Priorities", "PrioritiesButton");

        bottomBarButtons.add(orders, building, zones, priorities);

        for (int i = 0; i < bottomBarButtons.buttons.size(); i++) {
            Button b = bottomBarButtons.buttons.get(i);
            b.setPos((int) ((MyGdxGame.initialRes.x / 4f) * i), 5);
            b.setSize((int) (MyGdxGame.initialRes.x / 4f), (int) (MyGdxGame.initialRes.y / 12f));
        }
    }

    public void setupOrdersButtons(){
        ordersButtons = new ButtonCollection();
        ordersButtons.useWorldCoords = false;
        ordersButtons.showButtons = false;

        float y = (MyGdxGame.initialRes.y / 12f) + 5;
        ImgButton cutDown = new ImgButton("CutDownButton", "CutDown");
        ImgButton plant = new ImgButton("PlantButton", "Plant");
        ImgButton harvest = new ImgButton("HarvestButton", "Harvest");
        ImgButton mine = new ImgButton("MineButton", "Mine");
        ImgButton demolish = new ImgButton("DemolishButton", "Demolish");
        ImgButton cancel = new ImgButton("CancelButton", "Cancel");
        ImgButton move = new ImgButton("PickUpButton", "PickUp");
        ImgButton pickBerries = new ImgButton("PickBerriesButton", "PickBerries");
        ImgButton fishing = new ImgButton("FishingButton", "Fishing");

        ordersButtons.add(cutDown, plant, mine, demolish, cancel, move, pickBerries, fishing);

        int numberOfButtonsPerRow = 5;
        int row = 0;
        int size = ordersButtons.buttons.size();
        for (int i = 0; i < size; i++) {
            if (i % numberOfButtonsPerRow == 0) {
                row++;
            }

            Button b = ordersButtons.buttons.get(i);
            b.setPos((int) (((MyGdxGame.initialRes.y / 12f)) * (i % numberOfButtonsPerRow)) + 5, (int) (y + (MyGdxGame.initialRes.y / 12f) * (row - 1)));
            b.setSize((int) ((MyGdxGame.initialRes.y / 12f)), (int) (MyGdxGame.initialRes.y / 12f));
        }
    }

    public void setupBuildButtons(){
        buildingButtons = new ButtonCollection();
        buildingButtons.useWorldCoords = false;
        buildingButtons.showButtons = false;

        float y = (MyGdxGame.initialRes.y / 12f) + 5;
        ImgButton stoneWall = new ImgButton("stoneWallButton", "stoneWall");
        ImgButton woodWall = new ImgButton("woodWallButton", "woodWall");
        ImgButton stoneDoor = new ImgButton("stoneDoorButton", "stoneDoor");
        ImgButton torch = new ImgButton("torchButton", "torch");
        ImgButton lamp = new ImgButton("lampButton", "lamp");
        ImgButton woodFloor = new ImgButton("woodFloorButton", "woodFloor");
        ImgButton stoneFloor = new ImgButton("stoneFloorButton", "stoneFloor");

        buildingButtons.add(stoneWall, stoneFloor, woodWall, woodFloor, stoneDoor, torch, lamp);

        for (int i = 0; i < buildingButtons.buttons.size(); i++) {
            Button b = buildingButtons.buttons.get(i);
            b.setPos((int) ((MyGdxGame.initialRes.y / 12f) * i + 5), (int) y);
            b.setSize((int) (MyGdxGame.initialRes.y / 12f), (int) (MyGdxGame.initialRes.y / 12f));
        }
    }

    public void setupOptionsButtons(){
        optionsButtons = new ButtonCollection();
        optionsButtons.useWorldCoords = false;
        optionsButtons.showButtons = false;

        TextButton resumeButton = new TextButton("Resume", "ResumeButton", () -> startMultiplayerButtons.showButtons = false);
        TextButton optionsButton = new TextButton("Options", "OptionsButton");
        TextButton multiplayerButton = new TextButton("Start Multiplayer", "MultiplayerButton", () -> {
            if (socket == null) startMultiplayerButtons.showButtons = true;
        });
        TextButton saveButton = new TextButton("Save", "SaveButton");
        TextButton loadButton = new TextButton("Load", "LoadButton");
        TextButton mainMenuButton = new TextButton("Main Menu", "MainMenuButton");
        TextButton exitButton = new TextButton("Exit", "ExitButton");
        TextButton tutorialButton = new TextButton("Tutorial", "TutorialButton", () -> game.setScreen(new TutorialsScreen(game)));

        optionsButtons.buttons.clear();
        optionsButtons.add(exitButton, mainMenuButton, tutorialButton, loadButton, saveButton, multiplayerButton, optionsButton, resumeButton);
        float buttonWidth = MyGdxGame.initialRes.x / 5f;
        float buttonHeight = MyGdxGame.initialRes.y / 12f;

        float x = (MyGdxGame.initialRes.x / 2f) - (buttonWidth / 2f);
        float y = (MyGdxGame.initialRes.y / 12f) * 2f;
        for (int i = 0; i < optionsButtons.buttons.size(); i++) {
            Button b = optionsButtons.buttons.get(i);
            b.setSize((int) buttonWidth, (int) buttonHeight);
            b.setPos((int) x, (int) (y + (i * buttonHeight)));
        }

        setupStartMultiplayerButtons(x, y + (buttonHeight * 5), buttonHeight, buttonWidth);
    }

    public void setupStartMultiplayerButtons(float x, float y, float height, float width){
        startMultiplayerButtons = new ButtonCollection();
        startMultiplayerButtons.useWorldCoords = false;
        startMultiplayerButtons.showButtons = false;

        InputButtonTwo ipInput = new InputButtonTwo("localhost:8080", "ipInputButton", inputMultiplexer);
        ipInput.setSize(width, height);
        ipInput.setPos(x, y);

        Button connectButton = new Button("connectButton", "RefreshButton", () -> startMultiplayer(ipInput.text));
        connectButton.setSize(height / 2f, height / 2f);
        connectButton.setPos(x + ipInput.width + 10, y + (height / 4f));

        Label ipLabel = new Label("ipLabel", "enter IP:");
        ipLabel.setSize(width, height);
        ipLabel.autoSize();
        ipLabel.setPos(x - ipLabel.width - 10, y);

        startMultiplayerButtons.add(ipLabel, ipInput, connectButton);
    }

    public void setupResourceButtons(){
        resourceButtons = new ButtonCollection();
        resourceButtons.useWorldCoords = false;

        float length = MyGdxGame.initialRes.x / 50f;
        float height = MyGdxGame.initialRes.y / 50f;

        String[] resourceNames = map.resources.keySet().toArray(new String[0]);
        for (int i = 0; i < resourceNames.length; i++) {
            ImgTextButton t = new ImgTextButton(resourceNames[i] + "Button", "0", resourceNames[i]);
            t.setPos(5, (int) (MyGdxGame.initialRes.y - (height * (i + 2))));
            t.setSize((int) length, (int) height);
            resourceButtons.add(t);
        }
    }

    public void setupOrderTypes(){
        orderTypes = new HashMap<>();
        orderTypes.put("Mine", new ArrayList<>(Arrays.asList("stone")));
        orderTypes.put("Plant", new ArrayList<>(Arrays.asList("dirt", "grass")));
        orderTypes.put("CutDown", new ArrayList<>(Arrays.asList("tree")));
        orderTypes.put("Harvest", new ArrayList<>(Arrays.asList()));
        orderTypes.put("Demolish", new ArrayList<>(Arrays.asList("stoneWall", "woodWall", "stoneDoor", "lamp", "torch", "stoneFloor", "woodFloor")));
        orderTypes.put("Build", new ArrayList<>(Arrays.asList("dirt", "grass")));
        orderTypes.put("Cancel", new ArrayList<>(List.of()));
        orderTypes.put("PickUp", new ArrayList<>(List.of()));
        orderTypes.put("PickBerries", new ArrayList<>(List.of("berryBush")));
    }

    public boolean canUseOrderOnType(String order, Thing thing, Tile tile){
        if (order.equals("Fishing")){
            return (!tile.hasBeenFished && tile.type.equals("water"));
        }
        String thingType = thing.type;
        String tileType = tile.type;
        return (orderTypes.get(order).contains(tileType) || (orderTypes.get(order).contains(thingType))) && !thingType.equals("edgeBouncer");
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
        shapeRenderer.setProjectionMatrix(cameraTwo.projViewMatrix.getGdxMatrix());
        shapeRenderer.setColor(0, 0.4f, 1, 0.5f);
        shapeRenderer.rect(minSelecting.x, minSelecting.y, maxSelecting.x - minSelecting.x, maxSelecting.y - minSelecting.y);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    public void highlightSelected(SpriteBatch batch){
        float minX = Math.min(minSelecting.x, maxSelecting.x);
        float minY = Math.min(minSelecting.y, maxSelecting.y);
        float maxX = Math.max(minSelecting.x, maxSelecting.x);
        float maxY = Math.max(minSelecting.y, maxSelecting.y);

        int minXCoord = (int) (minX / TILE_DIMS);
        int minYCoord = (int) (minY / TILE_DIMS);
        int maxXCoord = (int) (maxX / TILE_DIMS) + 1;
        int maxYCoord = (int) (maxY / TILE_DIMS) + 1;

        minXCoord = Math.max(0, minXCoord);
        minYCoord = Math.max(0, minYCoord);
        maxXCoord = Math.min(TILES_ON_X, maxXCoord);
        maxYCoord = Math.min(TILES_ON_X, maxYCoord);

        for (int i = minXCoord; i < maxXCoord; i++) {
            for (int j = minYCoord; j < maxYCoord; j++) {
                if (selectionMode.equals("Orders")){
                    if (taskTypeSelected.equals("PickUp")){
                        if (map.tiles.get(i).get(j).hasFloorDropOn){
                            batch.draw(selectionIcon, i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                        }
                    }
                    else {
                        if (canUseOrderOnType(taskTypeSelected, map.things.get(i).get(j), map.tiles.get(i).get(j))) {
                            batch.draw(selectionIcon, i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                        }
                    }
                }
                else if (selectionMode.equals("Building")){
                    if (canUseOrderOnType("Build", map.things.get(i).get(j), map.tiles.get(i).get(j))) {
                        batch.draw(selectionIcon, i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                    }
                }
                if (inCancelTaskMode){
                    if (findTaskAtLocation(i, j) != null){
                        batch.draw(selectionIcon, i * TILE_DIMS, j * TILE_DIMS, TILE_DIMS, TILE_DIMS);
                    }
                }
            }
        }
    }

    public Task findTaskAtLocation(int x, int y){
        for (Task t : map.tasks) {
            if (t.getX() == x && t.getY() == y) {
                return t;
            }
        }
        return null;
    }

    public void setTasksFromSelection(String taskType){
        setTasksFromSelection(taskType, "");
    }

    public void setTasksFromSelection(String taskType, String taskSubType) {
        float minX = Math.min(minSelecting.x, maxSelecting.x);
        float minY = Math.min(minSelecting.y, maxSelecting.y);
        float maxX = Math.max(minSelecting.x, maxSelecting.x);
        float maxY = Math.max(minSelecting.y, maxSelecting.y);

        int minXCoord = (int) (minX / TILE_DIMS);
        int minYCoord = (int) (minY / TILE_DIMS);
        int maxXCoord = (int) (maxX / TILE_DIMS) + 1;
        int maxYCoord = (int) (maxY / TILE_DIMS) + 1;

        minXCoord = Math.max(0, minXCoord);
        minYCoord = Math.max(0, minYCoord);
        maxXCoord = Math.min(TILES_ON_X, maxXCoord);
        maxYCoord = Math.min(TILES_ON_X, maxYCoord);

        if (isMultiplayer) {
            if (!inCancelTaskMode) {
                socket.emit("setTasksFromSelection", taskType, taskSubType, minXCoord, minYCoord, maxXCoord, maxYCoord);
            }else {
                socket.emit("cancelTasksFromSelection", minXCoord, minYCoord, maxXCoord, maxYCoord);
            }
        }

        setTasksFromInput(taskType, taskSubType, minXCoord, minYCoord, maxXCoord, maxYCoord);
    }
    public void setTasksFromInput(String taskType, String taskSubType, int minXCoord, int minYCoord, int maxXCoord, int maxYCoord){
        ArrayList<Task> toAdd = new ArrayList<>();
        ArrayList<Task> toRemove = new ArrayList<>();

        if (!inCancelTaskMode) {
            for (int i = minXCoord; i < maxXCoord; i++) {
                for (int j = minYCoord; j < maxYCoord; j++) {
                    if (taskType.equals("PickUp")){
                        if (map.tiles.get(i).get(j).hasFloorDropOn){
                            Task t = new Task(taskType, taskSubType, i,j);
                            ArrayList<Task> copy = new ArrayList<>(map.tasks);
                            boolean shouldAdd = true;
                            for (Task t2 : copy) {
                                if (t2.getX() == t.getX() && t2.getY() == t.getY()) {
                                    if (!t2.isIndependent) {
                                        boolean shouldRemove = true;
                                        for (Colonist c : colonists) {
                                            Task t3 = c.getCurrentTask();
                                            if (t3 != null) {
                                                if (t3.getX() == t2.getX() && t3.getY() == t2.getY()) {
                                                    if (t3.type.equals(t2.type)) {
                                                        shouldRemove = false;
                                                        shouldAdd = false;
                                                    }
                                                }
                                            }
                                        }
                                        if (shouldRemove) {
                                            toRemove.add(t2);
                                        }
                                    }
                                }
                            }
                            if (shouldAdd) {
                                toAdd.add(t);
                            }
                        }
                    }
                    else {
                        if (canUseOrderOnType(taskType, map.things.get(i).get(j), map.tiles.get(i).get(j))) {
                            boolean shouldExchange = true;
                            for (Colonist c : colonists) {
                                Task t = c.getCurrentTask();
                                if (t != null) {
                                    if (t.getX() == i && t.getY() == j) {
                                        if (t.type.equals(taskType)) {
                                            shouldExchange = false;
                                        }
                                    }
                                }
                            }
                            if (shouldExchange) {
                                if (taskType.equals("Build") || taskType.equals("Plant")) {
                                    String resource = getResourceFromBuilding(taskSubType);
                                    if (map.resources.get(resource) < 1) {
                                        continue;
                                    } else {
                                        map.decreaseResource(resource, 1);
                                        calculateResources();
                                    }
                                }
                                Task t = new Task(taskType, taskSubType, i, j);
                                ArrayList<Task> copy = new ArrayList<>(map.tasks);
                                for (Task t2 : copy) {
                                    if (t2.getX() == t.getX() && t2.getY() == t.getY()) {
                                        if (!t2.isIndependent) {
                                            toRemove.add(t2);
                                        }
                                    }
                                }
                                toAdd.add(t);
                            }
                        }
                    }
                }
            }
        }
        if (inCancelTaskMode){
            ArrayList<Task> copy = new ArrayList<>(map.tasks);
            for (Task t : copy) {
                if (t.getX() >= minXCoord && t.getX() < maxXCoord && t.getY() >= minYCoord && t.getY() < maxYCoord) {
                    if (t.reserved) {
                        for (Colonist c : colonists) {
                            if (c.getCurrentTask() == t) {
                                if (!t.type.equals("FireFight")) {
                                    c.removeCurrentTask();
                                    if (isMultiplayer) {
                                        JSONObject json = new JSONObject();
                                        try {
                                            json.put("colonistID", c.colonistID);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!t.type.equals("FireFight")) {
                        if (t.type.equals("Build")) {
                            String resource = getResourceFromBuilding(t.subType);
                            if (!resource.equals("")) {
                                map.addFloorDrop(t.getX(), t.getY(), resource, 1, socket, isHost);
                            }
                        }
                        toRemove.add(t);
                    }
                }
            }
        }
        map.tasks.addAll(toAdd);
        map.tasks.removeAll(toRemove);
    }

    public static String getResourceFromBuilding(String subType){
        switch (subType){
            case "lamp", "stoneWall", "stoneDoor", "stoneFloor":
                return "stone";
            case "woodWall", "torch", "woodFloor":
                return "wood";
        }
        return "";
    }

    public void drawTaskType(SpriteBatch batch) {
        for (Task t : map.tasks) {
            Texture tx;
            if (Objects.equals(t.type, "FireFight")){
                continue;
            }
            if (t.type.equals("Build")) {
                tx = actionSymbols.get(t.subType);
            } else {
                tx = actionSymbols.get(t.type);
            }
            batch.draw(tx, t.getX() * GameScreen.TILE_DIMS, t.getY() * GameScreen.TILE_DIMS,
                        GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
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

    public void updateResourceButtons(){
        for (Button button : resourceButtons.buttons) {
            ImgTextButton b = (ImgTextButton) button;
            b.updateText(map.resources.get(b.imgName).toString());
        }
    }

    public void setColonistIDs(){
        for (int i = 0; i < colonists.size(); i++) {
            Colonist c = colonists.get(i);
            allEntities.add(c);
            System.out.println(i + " showing colonist id");
            c.colonistID = i;
            c.setEntityID(getNextEntityID());
        }
    }

    public void sendColonistMovement() throws JSONException {
        JSONObject obj = new JSONObject();
        for (Entity e : allEntities
             ) {
            System.out.println("Colonist: " + e.getEntityID() + " " + e.getX() + " " + e.getY());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("x", e.getX());
            jsonObject.put("y", e.getY());
            jsonObject.put("nextX", e.getNextX());
            jsonObject.put("nextY", e.getNextY());
            jsonObject.put("timer", e.getTimer());
            int endX = -1;
            int endY = -1;
            if (e.pathToComplete.size() > 1) {
                endX = (int) e.pathToComplete.get(e.pathToComplete.size() - 1).x;
                endY = (int) e.pathToComplete.get(e.pathToComplete.size() - 1).y;
            }
            jsonObject.put("endX", endX);
            jsonObject.put("endY", endY);
            obj.put(String.valueOf(e.getEntityID()), jsonObject);
        }
        socket.emit("getUpdatedEntities", obj);
    }

    public void getEntityMovement(JSONObject jsonObject) throws JSONException {
        for (Entity e : allEntities) {
            JSONObject colonistInput = jsonObject.getJSONObject(String.valueOf(e.getEntityID()));
            e.setX(colonistInput.getInt("x"));
            e.setY(colonistInput.getInt("y"));
            e.setNextX(colonistInput.getInt("nextX"));
            e.setNextY(colonistInput.getInt("nextY"));
            e.setTimer(colonistInput.getInt("timer"));
            int endX = colonistInput.getInt("endX");
            int endY = colonistInput.getInt("endY");
            if (endX != -1 && endY != -1) {
                e.pathToComplete = AStar.pathFindForEntities(new Vector2(e.getX(), e.getY()), new Vector2(endX, endY), map.tiles, allEntities, e.getEntityID());
            }
        }
    }

    public static Vector2 getMultiplierFromThings(String s){
        //noinspection SwitchStatementWithTooFewBranches
        return switch (s) {
            case "tree" -> new Vector2(1,2);
            default -> new Vector2(1,1);
        };
    }

    public void drawColonistsPath(ShapeRenderer shapeRenderer){
        if (!paused && drawColonistPath) {
            Gdx.gl.glEnable(GL30.GL_BLEND);
            Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(camera.projViewMatrix.getGdxMatrix());
            shapeRenderer.setColor(0, 0, 1, 0.5f);
            for (Colonist colonist : colonists) {
                colonist.drawPath = true;
                colonist.drawPathOutline(shapeRenderer);
                colonist.drawPath = false;
            }
            shapeRenderer.end();
            Gdx.gl.glDisable(GL30.GL_BLEND);
        }
    }

    public void drawAllBarbarianPaths(ShapeRenderer shapeRenderer){
        if (!paused && drawBarbarianPath) {
            for (EntityGroup eg : barbarians) {
                drawAllEntitiesPaths(eg.entities, shapeRenderer, new Color(1, 0, 0, 0.5f));
            }
        }
    }

    public void drawAllMobsPaths(ShapeRenderer shapeRenderer){
        if (!paused && drawMobPath) {
            for (EntityGroup eg : mobs) {
                drawAllEntitiesPaths(eg.entities, shapeRenderer, new Color(0, 1, 0, 0.5f));
            }
        }
    }

    public void drawAllEntitiesPaths(ArrayList<Entity> entities, ShapeRenderer shapeRenderer, Color color){
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(camera.projViewMatrix.getGdxMatrix());
        shapeRenderer.setColor(color);
        for (Entity e : entities) {
            e.drawPath = true;
            e.drawPathOutline(shapeRenderer);
            e.drawPath = false;
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    public void drawDeanOver(SpriteBatch batch){
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                batch.draw(neanDorris, i * GameScreen.TILE_DIMS, j * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
            }
        }
    }

    public void hideAllButtons(){
        buildingButtons.showButtons = false;
        ordersButtons.showButtons = false;
        zoneButtons.showButtons = false;
        priorityButtons.showButtons = false;
    }

    public void updateShowingButtons(ButtonCollection b){
        if (b.showButtons) {
            b.showButtons = false;
            selectionMode = "";
        } else {
            hideAllButtons();
            b.showButtons = true;
        }
    }

    public void drawColonistsAtTop(SpriteBatch batch, ShapeRenderer shapeRenderer){
        float dims = MyGdxGame.initialRes.x * 0.03f;
        float y = MyGdxGame.initialRes.y - (dims * 1.1f);
        float x = (MyGdxGame.initialRes.x - ((dims * 1.1f) * colonists.size())) / 2f;

        if (colonists.size() > ((MyGdxGame.initialRes.x * 0.8f) / dims)) {
            dims = (MyGdxGame.initialRes.x * 0.8f) / colonists.size();
        }

        if (Gdx.input.isButtonJustPressed(0)) {
            Colonist colonistSelected = getSelectedColonist(Gdx.input.getX(), (int) (MyGdxGame.initialRes.y - Gdx.input.getY()), dims, x, y);
            if (colonistSelected != null) {
                shouldShowSelectedColonistInfo = true;
                hideAllButtons();
                if (selectedColonist != colonistSelected){
                    selectedColonist = colonistSelected;
                }
                followingSelected = true;
                camera.moveTo(selectedColonist.getFullX(), selectedColonist.getFullY());
            }
            else {
                checkIfShouldCloseColonistInfo();
            }
        }

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(64/255f, 64/255f, 64/255f, 1f);
        for (int i = 0; i < colonists.size(); i++) {
            shapeRenderer.rect((x + ((dims * 1.1f)) * i), y, dims, dims);
        }

        shapeRenderer.end();

        batch.begin();

        for (int i = 0; i < colonists.size(); i++) {
            Colonist c = colonists.get(i);
            c.drawMini(batch, (int) (x + ((dims * 1.1f)) * i), (int) y, (int) dims, colonistClothes);
        }
    }

    public Colonist getSelectedColonist(int MouseX, int MouseY, float dims, float x, float y){
        int index = (int) ((MouseX - x) / (dims * 1.1f));
        if (MouseY > y && MouseY < y + dims) {
            if (index < colonists.size() && index >= 0) {
                return colonists.get(index);
            }
        }
        return null;
    }


    public void setupWeaponPresets(){
        Json json = new Json();
        ArrayList<Weapon> weaponPresetsArray = json.fromJson(ArrayList.class, Weapon.class, Gdx.files.internal("core/assets/info/weaponInfo/weaponInfo.txt"));
        for (Weapon w : weaponPresetsArray) {
            weaponPresets.put(w.getName(), w);
        }
        System.out.println("test");
    }

    public void giveAllConsistsRandomWeapons(){
        for (Colonist c: colonists) {
            c.copyWeapon(getRandomWeapon());
        }
    }

    public void randomlySpawnMobs(){
        String[] mobNames = {"sheep", "poong"};
        int chance = random.nextInt(300);
        if (chance == 0){
            String type = mobNames[random.nextInt(mobNames.length)];
            ArrayList<Vector2> spawnLocs = map.canSpawnHere(random.nextInt(TILES_ON_X), random.nextInt(TILES_ON_X), 10, 10);
            if (spawnLocs.size() <= 0){
                return;
            }
            EntityGroup eg = new EntityGroup(type, (int) spawnLocs.get(0).x, (int) spawnLocs.get(0).y, 10, getNextEntityGroupID());
            for (int i = 0; i < random.nextInt(6) + 1; i++) {
                Mob m = new Mob((int) spawnLocs.get(i).x, (int) spawnLocs.get(i).y, type, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS);
                m.copyWeapon(weaponPresets.get("punch"));
                m.setEntityID(getNextEntityID());
                eg.add(m);
                allEntities.add(m);
            }
            mobs.add(eg);
            if (isMultiplayer && isHost){
                socket.emit("spawnMobs", json.toJson(eg));
            }
        }
    }

    public void destroyMobs(){
        if (mobs.size() > 4){
            EntityGroup toDestroy = getEntityGroupFurthestFromPlayer(mobs);
            if (toDestroy != null){
                if (isMultiplayer && isHost){
                    socket.emit("destroyMobs", json.toJson(toDestroy.getId()));
                }
                mobs.remove(toDestroy);
                for (Entity e : toDestroy.entities) {
                    allEntities.remove(e);
                }
            }
        }
    }

    public EntityGroup getEntityGroupFurthestFromPlayer(ArrayList<EntityGroup> groups){
        float maxDistance = 0;
        EntityGroup toDestroy = null;
        for (EntityGroup eg : groups){
            float minDistanceFromAll = Float.MAX_VALUE;
            for (Colonist c : colonists){
                float dist = distance(c.getX(), c.getY(), eg.getX(), eg.getY());
                if (dist < minDistanceFromAll){
                    minDistanceFromAll = dist;
                }
            }
            if (minDistanceFromAll > maxDistance){
                maxDistance = minDistanceFromAll;
                toDestroy = eg;
            }
        }
        return toDestroy;
    }

    public void spawnMobs(String type, int amount, int x, int y){
        EntityGroup group = new EntityGroup(type,x , y, 10, getNextEntityGroupID());
        for (int i = 0; i < amount; i++) {
            Mob b = new Mob(x + random.nextInt(10) - 5, y + random.nextInt(10) - 5, type,
                    (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS);
            b.copyWeapon(weaponPresets.get("punch"));
            b.setEntityID(getNextEntityID());
            allEntities.add(b);
            group.add(b);
        }
        mobs.add(group);

        if (isMultiplayer && isHost){
            socket.emit("spawnMobs", json.toJson(group));
        }
    }

    public void spawnBarbarians(String type, int amount, int x, int y){
        EntityGroup group = new EntityGroup(type, x, y, 10, getNextEntityGroupID());
        for (int i = 0; i < amount; i++) {
            Barbarian b = createBarbarian(x + random.nextInt(10) - 5, y + random.nextInt(10) - 5, type);
            group.add(b);
            allEntities.add(b);
        }
        barbarians.add(group);
        if (isHost && socket != null){
            socket.emit("spawnBarbarians", json.toJson(group));
        }
    }

    public void spawnBarbarians(String type, ArrayList<Vector2> locs, int amount){
        Colonist target = colonists.get(random.nextInt(colonists.size()));
        EntityGroup group = new EntityGroup(type, target.getX(), target.getY(), 10, getNextEntityGroupID());
        for (int i = 0; i < amount; i++) {
            Vector2 loc = locs.get(i);
            Barbarian b = createBarbarian((int) loc.x, (int) loc.y, type);
            group.add(b);
            allEntities.add(b);
        }
        barbarians.add(group);
        if (isHost && socket != null){
            socket.emit("spawnBarbarians", json.toJson(group));
        }
    }

    public Weapon getRandomWeapon(){
        Object[] t = weaponPresets.keySet().toArray();
        return weaponPresets.get(t[random.nextInt(t.length)]);
    }

    public void drawAllMobs(SpriteBatch batch, ShapeRenderer shapeRenderer){
        for (EntityGroup e : mobs) {
            e.draw(batch, mobTextures, shapeRenderer);
        }
    }

    public void drawAllBarbarians(SpriteBatch batch, ShapeRenderer shapeRenderer){
        for (EntityGroup e : barbarians) {
            e.draw(batch, colonistClothes, shapeRenderer);
        }
    }

    public Barbarian createBarbarian(int x, int y, String type){
        Barbarian b = new Barbarian(x, y, type, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS);
        b.copyWeapon(getRandomWeapon());
        b.setClotheName(listOfColonistClothes[random.nextInt(listOfColonistClothes.length)]);
        b.setEntityID(getNextEntityID());
        return b;
    }

    public int getNextEntityID(){
        return nextEntityID++;
    }

    public int getNextEntityGroupID(){
        return nextEntityGroupID++;
    }

    public void drawAllTaskPercentages(ShapeRenderer shapeRenderer){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        drawTaskPercentagesBoundBox(shapeRenderer);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        drawTaskPercentagesFillBox(shapeRenderer);
        shapeRenderer.end();
    }

    public void drawTaskPercentagesBoundBox(ShapeRenderer shapeRenderer){
        for (Task t : map.tasks) {
            if (t.getPercentageComplete() > 0) {
                shapeRenderer.rect(t.getX() * GameScreen.TILE_DIMS + GameScreen.TILE_DIMS * 0.05f,
                        t.getY() * GameScreen.TILE_DIMS + GameScreen.TILE_DIMS * 0.05f,
                        GameScreen.TILE_DIMS * 0.9f, GameScreen.TILE_DIMS * 0.2f);
            }
        }

        for (Entity e : allEntities) {
            if (e instanceof Barbarian){
                if (e.getCurrentTask() != null){
                    ((Barbarian) e).drawTaskPercentageBoundBox(shapeRenderer);
                }
            }
        }
    }

    public void drawTaskPercentagesFillBox(ShapeRenderer shapeRenderer){
        for (Task t : map.tasks) {
            if (t.getPercentageComplete() > 0) {
                shapeRenderer.rect(t.getX() * GameScreen.TILE_DIMS + GameScreen.TILE_DIMS * 0.05f + 1,
                        t.getY() * GameScreen.TILE_DIMS + 1 + GameScreen.TILE_DIMS * 0.05f,
                        (GameScreen.TILE_DIMS * 0.9f - 2) * ((t.getPercentageComplete() / t.getMaxPercentage())), GameScreen.TILE_DIMS * 0.2f - 2);
            }
        }

        for (Entity e : allEntities) {
            if (e instanceof Barbarian){
                Barbarian b = (Barbarian) e;
                if (b.getCurrentTask() != null){
                    b.drawTaskPercentageFillBox(shapeRenderer);
                }
            }
        }
    }

    // FIXED: 14/04/2022  : when going to options menu and then returning the gamespeed is changed to one but not updated for the clock

    public void highlightAllReserved(ShapeRenderer shapeRenderer){
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f,69/255f,0, 0.5f);
        for (Task t : map.tasks) {
            if (t.reserved){
                shapeRenderer.rect(t.getX() * GameScreen.TILE_DIMS, t.getY() * GameScreen.TILE_DIMS,
                                    GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    public void highlightAllCanSpawnOn(ShapeRenderer shapeRenderer){
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(106/255f,13/255f,173/255f, 0.5f);
        for (int i = 0; i < map.tiles.size(); i++) {
            for (int j = 0; j < map.tiles.get(i).size(); j++) {
                if (map.tiles.get(i).get(j).canSpawnOn){
                    shapeRenderer.rect(i * GameScreen.TILE_DIMS, j * GameScreen.TILE_DIMS,
                            GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
                }
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    public void highlightAllCanWalkOn(ShapeRenderer shapeRenderer){
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0,1,1, 0.5f);
        for (int i = 0; i < map.tiles.size(); i++) {
            for (int j = 0; j < map.tiles.get(i).size(); j++) {
                if (map.tiles.get(i).get(j).canWalkOn){
                    shapeRenderer.rect(i * GameScreen.TILE_DIMS, j * GameScreen.TILE_DIMS,
                            GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
                }
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    public void highlightAllTasks(ShapeRenderer shapeRenderer){
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(204/255f, 237/255f,0, 0.5f);
        for (Task t : map.tasks) {
            shapeRenderer.rect(t.getX() * GameScreen.TILE_DIMS, t.getY() * GameScreen.TILE_DIMS,
                    GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    public void drawTimeCover(ShapeRenderer shapeRenderer, Clock clock){
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

        float hour = clock.getHour();
        float minute = clock.getMinute();
        float totalHours = (hour + minute / 60);
        float alpha = 1;
        if (hour >= 6 && hour <= 18) {
            alpha = 0;
        }
        else if (hour > 18){
            alpha = (totalHours - 18) / 6;
        }
        else if (hour < 6){
            alpha = (6 - totalHours) / 6;
        }
        if (alpha > 0.85f) {
            alpha = 0.85f;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0,0,0,alpha);
        shapeRenderer.rect(0,0,MyGdxGame.initialRes.x, MyGdxGame.initialRes.y);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL30.GL_BLEND);
    }

    public void setupLights(){
        ec = new EdgeController();
        ec.setupEdgeBouncers(map.things);
        ec.update(map.things); //update must be called when a new Thing is added to the map

        lightManager = new LightManager();
        lightManager.updateLights(ec, GameScreen.TILE_DIMS);

        lightManager.setAllToShouldUpdate();
        ec.update(map.things);
    }

    public void setupGameSpeedButtons(Clock clock){
        gameSpeedButtons = new ButtonCollection();
        gameSpeedButtons.useWorldCoords = false;
        Function<Integer, Void> changeSpeed = (Integer f) -> {
            if (GameScreen.gameSpeed == 0) {
                GameScreen.gameSpeed = GameScreen.lastGameSpeed + f;
            } else {
                GameScreen.gameSpeed += f;
            }
            if (GameScreen.gameSpeed < 0) {
                GameScreen.gameSpeed = 0;
            }
            if (GameScreen.gameSpeed > GameScreen.MAX_GAME_SPEED) {
                GameScreen.gameSpeed = GameScreen.MAX_GAME_SPEED;
            }
            GameScreen.lastGameSpeed = GameScreen.gameSpeed;
            if (isMultiplayer) {
                socket.emit("updateGameSpeed", GameScreen.gameSpeed);
            }
            return null;
        };
        ImgOnlyButton btwo = new ImgOnlyButton("btwo", "backwardDouble", () -> changeSpeed.apply(-2));
        ImgOnlyButton bone = new ImgOnlyButton("bone", "backwardSingle", () -> changeSpeed.apply(-1));
        ImgOnlyButton paulay = new ImgOnlyButton("pulay", "pausePlay", () -> {
            if (GameScreen.gameSpeed == 0) {
                GameScreen.gameSpeed = GameScreen.lastGameSpeed;
            }
            else {
                GameScreen.lastGameSpeed = GameScreen.gameSpeed;
                GameScreen.gameSpeed = 0;
            }
            if (isMultiplayer) {
                socket.emit("updateGameSpeed", GameScreen.gameSpeed);
            }
        });
        ImgOnlyButton fone = new ImgOnlyButton("fone", "forwardSingle", () -> changeSpeed.apply(1));
        ImgOnlyButton ftwo = new ImgOnlyButton("ftwo", "forwardDouble", () -> changeSpeed.apply(2));
        gameSpeedLabel = new Label("gameSpeedLabel", gameSpeed + "x");

        gameSpeedButtons.add(btwo, bone, paulay, fone, ftwo);

        float width = (clock.getRadius() * 2) / 5f;
        float height = MyGdxGame.initialRes.y * 0.02f;
        float x = (clock.getX() - clock.getRadius());
        float y = (clock.getY() - clock.getRadius());

        for (int i = 0; i < gameSpeedButtons.buttons.size(); i++) {
            Button b = gameSpeedButtons.buttons.get(i);
            b.setPos(x + width * i, y - height);
            b.setSize(width, height);
        }

        gameSpeedLabel.setPos(x - width, y - height);
        gameSpeedLabel.setSize(width / 2f, height);
    }

    // FIXED: 08/04/2022 the lights are not properly saved and so dont appear
    // BUG: 08/04/2022 when you save and a colonist is completing a task it has a weird effect of just standing there for a while

    public void setupZoneButtons(){
        zoneButtons = new ButtonCollection();
        zoneButtons.useWorldCoords = false;
        zoneButtons.showButtons = false;

        ImgButton zoneButton = new ImgButton("zoneButton", "zone", () -> selectionMode = "Zones");
        ImgButton zoneDemolishButton = new ImgButton("zoneDemolishButton", "zoneDemolish", () -> selectionMode = "ZoneDemolish");

        zoneButtons.add(zoneButton, zoneDemolishButton);
        float y = (MyGdxGame.initialRes.y / 12f) + 5;
        for (int i = 0; i < zoneButtons.buttons.size(); i++) {
            Button b = zoneButtons.buttons.get(i);
            b.setPos((int) ((MyGdxGame.initialRes.y / 12f) * i + 5), (int) y);
            b.setSize((int) (MyGdxGame.initialRes.y / 12f), (int) (MyGdxGame.initialRes.y / 12f));
        }
    }

    public void changeZone(float x, float y, float x2, float y2, boolean isAddZone){
        float minX = Math.min(x, x2);
        float minY = Math.min(y, y2);
        float maxX = Math.max(x, x2);
        float maxY = Math.max(y, y2);

        int minXCoord = (int) (minX / TILE_DIMS);
        int minYCoord = (int) (minY / TILE_DIMS);
        int maxXCoord = (int) (maxX / TILE_DIMS) + 1;
        int maxYCoord = (int) (maxY / TILE_DIMS) + 1;

        minXCoord = Math.max(0, minXCoord);
        minYCoord = Math.max(0, minYCoord);
        maxXCoord = Math.min(TILES_ON_X, maxXCoord);
        maxYCoord = Math.min(TILES_ON_X, maxYCoord);

        if (isAddZone) {
            addZone(minXCoord, minYCoord, maxXCoord, maxYCoord);
        }
        else {
            removeZone(minXCoord, minYCoord, maxXCoord, maxYCoord);
        }
    }

    public void addZone(int minXCoord, int minYCoord, int maxXCoord, int maxYCoord){
        addZonePartTwo(minXCoord, minYCoord, maxXCoord, maxYCoord);
        if (isMultiplayer) {
            socket.emit("addZone", minXCoord, minYCoord, maxXCoord, maxYCoord);
        }
    }

    public void addZonePartTwo(int minXCoord, int minYCoord, int maxXCoord, int maxYCoord) {
        Zone z1 = new Zone(minXCoord, minYCoord, maxXCoord - minXCoord, maxYCoord - minYCoord, map.getNextZoneColor());
        int[][] verts = new int[][]{{minXCoord, minYCoord}, {maxXCoord, minYCoord}, {maxXCoord, maxYCoord}, {minXCoord, maxYCoord}};
        boolean notInZone = true;
        for (Zone z : map.zones) {
            int[][] zVerts = new int[][]{{z.getX(), z.getY()}, {z.getX() + z.getWidth(), z.getY()},
                                        {z.getX() + z.getWidth(), z.getY() + z.getHeight()}, {z.getX(), z.getY() + z.getHeight()}};
            for (int[] v : verts) {
                if (z.isInZone(v[0], v[1], map)) {
                    notInZone = false;
                }
            }
            for (int[] v : zVerts) {
                if (z1.isInZone(v[0], v[1], map)) {
                    notInZone = false;
                }
            }
        }

        boolean isAllowed = (maxXCoord - minXCoord) > 1 && (maxYCoord - minYCoord) > 1;
        if (notInZone && isAllowed) {
            z1.setup(map.getNextZoneID());
            map.addZone(z1);
        }
    }

    public void removeZone(int minXCoord, int minYCoord, int maxXCoord, int maxYCoord){
        removeZonePartTwo(minXCoord, minYCoord, maxXCoord, maxYCoord);
        if (isMultiplayer) {
            socket.emit("removeZone", minXCoord, minYCoord, maxXCoord, maxYCoord);
        }
    }

    public void removeZonePartTwo(int minXCoord, int minYCoord, int maxXCoord, int maxYCoord) {
        ArrayList<Zone> toRemove = new ArrayList<>();

        Zone z1 = new Zone(minXCoord, minYCoord, maxXCoord - minXCoord, maxYCoord - minYCoord, map.getNextZoneColor());
        int[][] verts = new int[][]{{minXCoord, minYCoord}, {maxXCoord, minYCoord}, {maxXCoord, maxYCoord}, {minXCoord, maxYCoord}};
        for (Zone z : map.zones) {
            int[][] zVerts = new int[][]{{z.getX(), z.getY()}, {z.getX() + z.getWidth(), z.getY()},
                    {z.getX() + z.getWidth(), z.getY() + z.getHeight()}, {z.getX(), z.getY() + z.getHeight()}};
            for (int[] v : verts) {
                if (z.isInZone(v[0], v[1], map)) {
                    toRemove.add(z);
                    break;
                }
            }
            for (int[] v : zVerts) {
                if (z1.isInZone(v[0], v[1], map)) {
                    toRemove.add(z);
                    break;
                }
            }
        }

        if (isHost) {
            for (Zone z : toRemove) {
                for (FloorDrop f : z.getDrops()) {
                    map.addFloorDrop(f, socket, isHost);
                }
            }
        }
        map.removeZones(toRemove);
    }

    public void setupFloorDropHashMap(){
        floorDropTextures = new HashMap<>();
        File dir = new File("core/assets/Textures/Resources");
        String[] files = dir.list();
        assert files != null;
        for (String file : files) {
            if (file.endsWith(".png")) {
                String[] split = file.split("\\.");
                String name = split[0];
                floorDropTextures.put(name, new Texture("Textures/Resources/" + file));
            }
        }
    }

    public void calculateResources(){
        map.resources.replaceAll((k, v) -> 0);
        for (Zone z : map.zones) {
            z.calculateResources(map.resources);
        }
    }

    // FIXED: 09/04/2022 sometimes the colonists will ignore the available tasks and move randomly
    // FIXED: 09/04/2022 building no longer works

    public void updateSelectedColonist(SpriteBatch batch){
        if (selectedColonist != null) {
            batch.begin();
            if (shouldShowSelectedColonistInfo) {
                selectedColonistButtons.drawButtons(batch);
            }
            if (showSelectedSkills) {
                changeSkills();
                selectedColonistSkills.drawButtons(batch);
            }
            batch.end();
        }
    }

    public void drawSelectedColonistUI(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        int x = 10;
        int y = (int) ((MyGdxGame.initialRes.y / 12f) * 1.1f);
        int width = (int) (MyGdxGame.initialRes.x / 3f);
        int height = (int) (MyGdxGame.initialRes.y / 4f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x - 2, y, width + 4, height + 4);
        shapeRenderer.end();

        batch.begin();
        if (selectedColonist != null) {
            HashMap<String, TextureAtlas> temp;
            if (selectedColonist instanceof Mob) {
                temp = mobTextures;
            } else {
                temp = colonistClothes;
            }
            selectedColonist.drawMini(batch, x, y + height - width / 3, width / 4, temp);

            if (selectedColonist instanceof Colonist) {
                Colonist c = (Colonist) selectedColonist;
                glyphLayout.setText(font, "Name: " + c.firstName + " " + c.lastName);
                font.draw(batch, glyphLayout, x + width / 4f, y + (height * 0.85f) - (glyphLayout.height));

                glyphLayout.setText(font, "Age: " + c.age);
                font.draw(batch, glyphLayout, x + width / 4f, y + (height * 0.7f) - (glyphLayout.height));

                glyphLayout.setText(font, "Health: " + c.getHealth() + "\\" + Colonist.getHealthFromType(c.getEntityType()));
                font.draw(batch, glyphLayout, x + width / 4f, y + (height * 0.55f) - (glyphLayout.height));

                glyphLayout.setText(font, "Level: " + c.getLevel());
                font.draw(batch, glyphLayout, x + width / 4f, y + (height * 0.4f) - (glyphLayout.height));

                glyphLayout.setText(font, getCurrentAction(c));
                font.draw(batch, glyphLayout, x + width / 4f, y + (height * 0.25f) - (glyphLayout.height));
            } else if (selectedColonist instanceof Mob || selectedColonist instanceof Barbarian) {
                glyphLayout.setText(font, "Type: " + selectedColonist.getEntityType());
                font.draw(batch, glyphLayout, x + width / 4f, y + (height * 0.85f) - (glyphLayout.height));

                glyphLayout.setText(font, "Health: " + selectedColonist.getHealth() + "\\" + selectedColonist.getMaxHealth());
                font.draw(batch, glyphLayout, x + width / 4f, y + (height * 0.7f) - (glyphLayout.height));

                glyphLayout.setText(font, "Level: " + selectedColonist.getLevel());
                font.draw(batch, glyphLayout, x + width / 4f, y + (height * 0.55f) - (glyphLayout.height));
            }
            batch.end();
        }
    }

    public void setupSelectedColonistsButtonCollection(){
        selectedColonistButtons = new ButtonCollection();
        selectedColonistButtons.useWorldCoords = false;
        selectedColonistButtons.firstCheck = true;
        ImgButton followButton = new ImgButton("followButton", "follow", () -> GameScreen.followingSelected = !GameScreen.followingSelected);
        TextButton skillsButton = new TextButton("Show Skills", "skillsButton", () -> showSelectedSkills = !showSelectedSkills);
        ImgButton closeButton = new ImgButton("closeButton", "close", () -> {
            shouldShowSelectedColonistInfo = false;
            showSelectedSkills = false;
        });
        ImgButton pathButton = new ImgButton("pathButton", "path", () -> selectedColonist.drawPath = !selectedColonist.drawPath);
        ImgButton attackButton = new ImgButton("attackButton", "attack", () -> {
            attackSelection = !attackSelection;
            if (attackSelection){
                setCustomCursor("attack");
            }
            else {
                setCursorDefault();
            }
        });
        ImgButton healButton = new ImgButton("healButton", "heal", () -> {
            healSelection = !healSelection;
            if (healSelection){
                setCustomCursor("heal");
            }
            else {
                setCursorDefault();
            }
        });

        followButton.setPos(10 + (MyGdxGame.initialRes.x / 3f) - (MyGdxGame.initialRes.y / 4f / 8f),
                ((MyGdxGame.initialRes.y / 12f) * 1.1f) + (MyGdxGame.initialRes.y / 4f) - (MyGdxGame.initialRes.y / 4f / 8f));
        followButton.setSize((MyGdxGame.initialRes.y / 4f / 8f), (MyGdxGame.initialRes.y / 4f / 8f));

        pathButton.setPos(followButton.x - followButton.width - 2, followButton.y);
        pathButton.setSize(followButton.width, followButton.height);

        attackButton.setPos(pathButton.x - pathButton.width - 2, pathButton.y);
        attackButton.setSize(pathButton.width, pathButton.height);

        healButton.setPos(attackButton.x - attackButton.width - 2, attackButton.y);
        healButton.setSize(attackButton.width, attackButton.height);

        int widtht = (int) (MyGdxGame.initialRes.x / 10f);
        skillsButton.setPos(healButton.x - widtht, healButton.y);
        skillsButton.setSize(widtht, healButton.height);

        closeButton.setPos(10, followButton.y);
        closeButton.setSize(followButton.width, followButton.height);

        selectedColonistButtons.add(followButton, skillsButton, closeButton, pathButton, attackButton, healButton);

        selectedColonistSkills = new ButtonCollection();
        selectedColonistSkills.useWorldCoords = false;
        String[] skills = new String[]{"Mining", "FireFighting", "Deconstruction", "Construction",
                "Shooting", "Moving", "Foraging", "Melee", "Chopping trees", "Fishing", "Planting"};
        float x = 10;
        float y = ((MyGdxGame.initialRes.y / 12f) * 1.1f) + (MyGdxGame.initialRes.y / 4f);
        float width = (MyGdxGame.initialRes.x / 6f);
        float height = (MyGdxGame.initialRes.y / 20f);
        for (int i = 0; i < skills.length; i++) {
            String skill = skills[i];
            Label temp = new Label(skill, "");

            temp.setPos(x, y + (height * i));
            temp.setSize(width, height);
            selectedColonistSkills.add(temp);
        }
    }

    public void changeSkills(){
        for (Button b : selectedColonistSkills.buttons) {
            Label l = (Label) b;
            if (selectedColonist instanceof Colonist) {
                Colonist colonist = (Colonist) selectedColonist;
                l.setText((l.name + ": " + colonist.skills.get(l.name)));
            }
        }
    }

    public void checkIfShouldCloseColonistInfo(){
        int mX = Gdx.input.getX();
        int mY = (int) (MyGdxGame.initialRes.y - Gdx.input.getY());
        int x = 10;
        int y = (int) ((MyGdxGame.initialRes.y / 12f) * 1.1f);
        int width = (int) (MyGdxGame.initialRes.x / 3f);
        int height = (int) (MyGdxGame.initialRes.y / 4f);

        if (!(mX > x && mX < x + width && mY > y && mY < y + height) && !attackSelection && !healSelection) {
            shouldShowSelectedColonistInfo = false;
            showSelectedSkills = false;
        }
    }

    public void highLightAttackAble(ShapeRenderer shapeRenderer){
        Vector2 mousePos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        float offset = GameScreen.TILE_DIMS / 2f;

        boolean onSelected = false;
        for (Entity e : allEntities) {
            if (e.isAlive() && e != selectedColonist) {
                shapeRenderer.setColor(Color.RED);
                float eX = e.getFullX() + GameScreen.TILE_DIMS / 2f;
                float eY = e.getFullY() + GameScreen.TILE_DIMS / 2f;
                if (distance(mousePos.x, mousePos.y, eX, eY) <= (GameScreen.TILE_DIMS / 2f) && !onSelected) {
                    onSelected = true;
                    shapeRenderer.setColor(Color.BLUE);
                }

                float x = (e.getX() + ((e.getNextX() - e.getX()) * e.getTimer())) * GameScreen.TILE_DIMS;
                float y = (e.getY() + ((e.getNextY() - e.getY()) * e.getTimer())) * GameScreen.TILE_DIMS;
                shapeRenderer.circle(x + offset, y + offset, GameScreen.TILE_DIMS / 2f);
            }
        }
    }

    public void highLightHealAble(ShapeRenderer shapeRenderer){
        shapeRenderer.setColor(Color.GREEN);
        for (Zone z : map.zones) {
            for (FloorDrop fd : z.getDrops()) {
                if (fd.isConsumable){
                    shapeRenderer.circle((fd.getX() + 0.5f) * GameScreen.TILE_DIMS, (fd.getY() + 0.5f) * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS / 2f);
                }
            }
        }
        for (FloorDrop fd : map.floorDrops) {
            if (fd.isConsumable){
                shapeRenderer.circle((fd.getX() + 0.5f) * GameScreen.TILE_DIMS, (fd.getY() + 0.5f) * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS / 2f);
            }
        }
    }

    public float distance(float x, float y, float x2, float y2){
        return (float) Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2));
    }

    public Entity getEntityAtUsingFull(float x, float y){
        for (Entity e : allEntities) {
            float eX = e.getFullX() + GameScreen.TILE_DIMS / 2f;
            float eY = e.getFullY() + GameScreen.TILE_DIMS / 2f;
            if (distance(x, y, eX, eY) <= (GameScreen.TILE_DIMS / 2f)) {
                return e;
            }
        }
        return null;
    }

    public Entity getEntityAt(int x, int y){
        for (Entity e : allEntities) {
            if (e.getX() == x && e.getY() == y) {
                return e;
            }
        }
        return null;
    }

    public Entity getEntityWithId(int id){
        for (Entity e : allEntities) {
            if (e.getEntityID() == id) {
                return e;
            }
        }
        return null;
    }

    public FloorDrop getFloorDropAt(int x, int y){
        for (Zone z : map.zones) {
            for (FloorDrop fd : z.getDrops()) {
                if (fd.getX() == x && fd.getY() == y){
                    return fd;
                }
            }
        }
        for (FloorDrop fd : map.floorDrops) {
            if (fd.getX() == x && fd.getY() == y){
                return fd;
            }
        }
        return null;
    }

    public void updateRaids(){
        if (clock.newDay) {
            clock.newDay = false;
            GameScreen.score += clock.getDay() * 10;
            raidChance += (0.005 * Math.min(clock.getDay(), 10)) + (0.001 * Math.min(map.totalRaidChanceAffector,100));

            float chance = random.nextFloat();
            if (raidChance >= chance) {
                raidChance = 0;
                int amount = random.nextInt(3) + 1;
                int add = clock.getDay() / 10;
                amount += add;

                spawnRaid(amount);
                notifications.add(socket, isHost, new Notification(notifications.getNextId(), "Raid", "raid"));
            }
        }
    }

    public void spawnRaid(int amount){
        ArrayList<Vector2> temp = map.getRandomPlaceForEntities(random);

        if (temp != null) {
            if (temp.size() >= amount){
                spawnBarbarians("barbarian", temp, amount);
            }
        }
    }

    public void setupNotifications(){
        float y = MyGdxGame.initialRes.y * 0.6f;
        float x = MyGdxGame.initialRes.x * 0.95f;
        float dims = MyGdxGame.initialRes.x * 0.04f;
        notifications = new NotificationCollection(x, y, dims);
    }

    public void addNotification(String text, String type){
        Notification temp = new Notification(notifications.getNextId() ,text, type);
        notifications.add(socket, isHost, temp);
    }

    public void setupPriorityButtons(){
        priorityButtons = new ButtonCollection();
        priorityButtons.useWorldCoords = false;
        NumberScale.font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        priorityButtons.showButtons = false;
        updatePriorityButtons();
    }

    public void updatePriorityButtons(){
        priorityButtons.clear();

        int y = (int) (MyGdxGame.initialRes.y / 12f) + 5;
        int dims = (int) (MyGdxGame.initialRes.y / 24f);

        if (colonists.size() > 0) {
            Colonist c = colonists.get(0);
            Set<String> skills = c.priorityFromType.keySet();
            int num = Math.min(colonists.size(), priorityHeight);
            int ySkill = y + dims * (num + 1);
            int xSkill = (int) (MyGdxGame.initialRes.x / 12f) + 5;

            for (int i = 0; i < skills.size(); i++) {
                ImgButton skill = new ImgButton("skill:" + skills.toArray()[i], skills.toArray()[i].toString());
                skill.drawButton = false;
                skill.setSize(dims, dims);
                skill.setPos(xSkill + (i * (dims + 10)), ySkill);
                priorityButtons.add(skill);
            }
        }

        int end = Math.min(colonists.size(), priorityHeight + priorityStart);

        for (int i = priorityStart; i < end; i++) {
            Colonist c = colonists.get(i);

            int temp = i - priorityStart;

            Label name = new Label(c.getEntityID() + ":name", c.getFullName());
            name.setPos(5, y + (temp * (dims + 5)));
            name.setSize((MyGdxGame.initialRes.x / 12f), dims);
            name.drawCentred = false;
            name.resizeFontToFit = true;
            priorityButtons.add(name);

            int x = (int) (MyGdxGame.initialRes.x / 12f) + 5;
            Set<String> skills = c.priorityFromType.keySet();
            for (int j = 0; j < skills.size(); j++) {
                String s = skills.toArray(new String[0])[j];
                NumberScale scale = new NumberScale(c.getEntityID() + ":" + s, c, s);
                scale.setRunnable(() -> {
                    c.incrementPriority(scale.getSkill());
                    if (socket != null) {
                        socket.emit("updatePriority", c.getEntityID(), scale.getSkill(), c.getPriorityValue(scale.getSkill()));
                    }
                });
                scale.setPos(x + (dims + 10) * j, y + temp * (dims + 5));
                scale.setSize(dims, dims);
                priorityButtons.add(scale);
            }
        }
    }
    
    public void updateAttackSelection(){
        if (attackSelection && selectedColonist != null) {
            if (Gdx.input.isButtonJustPressed(0)){
                if (selectedColonist instanceof Colonist) {
                    Vector2 mousePos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                    Entity toAttack = getEntityAtUsingFull(mousePos.x, mousePos.y);
                    if (selectedColonist != null && toAttack != null) {
                        selectedColonist.setDefender(toAttack);
                        if (socket != null) {
                            socket.emit("entityAttacking", toAttack.getEntityID(), selectedColonist.getEntityID());
                        }
                    }
                    if (toAttack != null) {
                        System.out.println(toAttack.getEntityID() + " showing to attack");
                    } else {
                        System.out.println("No entity to attack");
                    }
                }
            }
        }
    }
    
    public void updateHealSelection(){
        if (healSelection && selectedColonist != null){
            if (selectedColonist instanceof Colonist) {
                if (Gdx.input.isButtonJustPressed(0)) {
                    Colonist c = (Colonist) selectedColonist;
                    Vector2 mousePos = camera.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                    int x = (int) (mousePos.x / TILE_DIMS);
                    int y = (int) (mousePos.y / TILE_DIMS);
                    FloorDrop toHeal = getFloorDropAt(x, y);
                    if (c != null && toHeal != null) {
                        c.stopWhatYoureDoing(map);
                        Task t = new Task("Heal", "", x, y);
                        map.tasks.add(t);
                        c.setTask(t, map, allEntities);
                    }
                }
            }
        }
    }

    public String getCurrentAction(Entity e){
        if (e instanceof Colonist){
            Colonist c = (Colonist) e;
            if (c.completingTask && c.getCurrentTask() != null){
                return "Doing a task at " + c.getCurrentTask().getX() + "," + c.getCurrentTask().getY();
            }
            if (c.isAttacking){
                return "Attacking with a " + c.getWeapon().getName();
            }
            if (c.movingAcrossPath && c.pathToComplete.size() > 1){
                Vector2 end = c.pathToComplete.get(c.pathToComplete.size() - 1);
                return "Moving to " + end.x + "," + end.y;
            }
        }
        return "";
    }

    public void updateColonistsMoveTo(JSONObject data) throws JSONException {
        int entityID = data.getInt("entityID");
        int x = data.getInt("x");
        int y = data.getInt("y");
        for (Entity e : allEntities) {
            if (e.getEntityID() == entityID) {
                e.setMoveToPos(x, y, map, allEntities);
            }
        }
    }

    public void updateColonistsMovement(JSONObject data) throws JSONException {
        int entityID = data.getInt("entityID");
        int nextX = data.getInt("nextX");
        int nextY = data.getInt("nextY");
        for (Entity e : allEntities) {
            if (e.getEntityID() == entityID) {
                e.setNextX(nextX);
                e.setNextY(nextY);
            }
        }
    }

    public static void sendColonistTask(Socket socket, Task task, int colonistID){
         String jsonText = json.toJson(task, Task.class);
         JSONObject jsonObject = new JSONObject();
         try {
             jsonObject.put("task", jsonText);
             jsonObject.put("colonistID", colonistID);
             socket.emit("colonistTask", jsonObject);
         } catch (JSONException e) {
             e.printStackTrace();
         }
    }

    public static void completeTaskNotifyServer(Socket socket, int x, int y, String type){
        JSONObject data = new JSONObject();
        try {
            data.put("x", x);
            data.put("y", y);
            data.put("type", type);
            socket.emit("completeTask", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void refreshAllLights(){
        for (int i = 0; i < TILES_ON_X; i++) {
            for (int j = 0; j < TILES_ON_X; j++) {
                Thing t = map.things.get(i).get(j);
                if (t.emitsLight){
                    t.setup();
                    map.lightShouldBeUpdated = true;
                }
            }
        }
    }

    public Colonist getColonistWithID(int id){
        for (Colonist c : colonists) {
            if (c.getEntityID() == id){
                return c;
            }
        }
        return null;
    }

    public EntityGroup getEntityGroupWithID(int id, ArrayList<EntityGroup> groups){
        for (EntityGroup e : groups) {
            if (e.getId() == id){
                return e;
            }
        }
        return null;
    }

    public void loadEntities(String dirName, String saveName) {
        try {
            ArrayList<String> save = Map.getSaveString(dirName, saveName);

            for (String s : save) {
                switch (s.split(" ")[0]) {
                    case "mobs:" -> mobs = json.fromJson(ArrayList.class, EntityGroup.class, s.split(" ")[1]);
                    case "barbarians:" -> barbarians = json.fromJson(ArrayList.class, EntityGroup.class, s.split(" ")[1]);
                }
            }
            for (EntityGroup eg : mobs) {
                allEntities.addAll(eg.entities);
            }
            for (EntityGroup eg : barbarians) {
                allEntities.addAll(eg.entities);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int countColonistsAlive(){
        int amount = 0;
        for (Colonist c : colonists) {
            if (c.isAlive()){
                amount++;
            }
        }
        return amount;
    }

    public void checkNotEnd(){
        if (countColonistsAlive() < 1){
            game.setScreen(new EndScreen(game, this));
        }
    }

    public void dropFoodForDeadMobs(){
        for (EntityGroup eg : mobs) {
            for (Entity e : eg.entities) {
                Mob m = (Mob) e;
                if (!m.isAlive()){
                    if (!m.hasDroppedFood){
                        int amount = random.nextInt(2) + 1;
                        map.addFloorDrop(m.getX(), m.getY(), "meat", amount, socket, isHost);
                        m.hasDroppedFood = true;
                    }
                }
            }
        }
    }

    public void removeCorpses(){
        ArrayList<Entity> toRemove = new ArrayList<>();
        for (EntityGroup eg : mobs) {
            for (Entity e : eg.entities) {
                if (!e.isAlive()){
                    toRemove.add(e);
                }
            }
            if (toRemove.contains(selectedColonist)){
                shouldRemoveSelectedColonist = true;
            }
            eg.removeAll(toRemove);
            toRemove.clear();
        }
        for (EntityGroup eg : barbarians) {
            for (Entity e : eg.entities) {
                if (!e.isAlive()){
                    toRemove.add(e);
                }
            }
            if (toRemove.contains(selectedColonist)){
                shouldRemoveSelectedColonist = true;
            }
            eg.removeAll(toRemove);
            toRemove.clear();
        }
    }
}