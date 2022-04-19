package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Entity.Colonist;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.ui.elements.*;
import com.mygdx.game.ui.extensions.ButtonCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SaveScreen implements Screen {
    Map map;
    ArrayList<Colonist> colonists;
    GameScreen gameScreen;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    HashMap<String, Texture> tileTextures;
    HashMap<String, TextureAtlas> thingTextures;
    HashMap<String, TextureAtlas> colonistClothes;

    MyGdxGame game;

    ButtonCollection buttons;

    CameraTwo camera;

    BitmapFont font;
    GlyphLayout glyphLayout;

    InputMultiplexer inputMultiplexer;

    float displayCount;
    String displayMessage;

    public SaveScreen(MyGdxGame game, Map map, ArrayList<Colonist> colonists, GameScreen gameScreen) {
        this.map = map;
        this.game = game;
        this.colonists = colonists;
        this.gameScreen = gameScreen;
        setup();
    }

    public void setup(){
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera = new CameraTwo();
        camera.allowMovement = false;

        font = new BitmapFont(Gdx.files.internal("Fonts/" + MyGdxGame.fontName + ".fnt"));
        glyphLayout = new GlyphLayout(font, "");

        inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);

        setupButtons();
        setupHashMaps();
        setupMap();
    }

    public void setupHashMaps(){
        tileTextures = new HashMap<>();
        thingTextures = new HashMap<>();
        GameScreen.getAllMapTextures(tileTextures, thingTextures);
        colonistClothes = new HashMap<>();
        GameScreen.getTAResources(colonistClothes, "TAResources");
    }

    public void setupMap(){
        int offset = 10;
        map.drawHeight = (int) (MyGdxGame.initialRes.y / 2f);
        map.setX((int) (MyGdxGame.initialRes.x - map.drawHeight) - offset);
        map.setY((int) (MyGdxGame.initialRes.y - map.drawHeight) - offset);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(79/255f, 109/255f, 158/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        displayCount -= delta;
        if(displayCount <= 0){
            displayCount = 0;
            displayMessage = "";
        }

        camera.update();

        batch.begin();
        map.drawMiniMap(batch, tileTextures, thingTextures);

        float dims = map.drawHeight / 10f;
        if (dims * colonists.size() > map.drawHeight) dims = (float)map.drawHeight / colonists.size();

        drawColonists(map.getX(), (int) (map.getY() - dims), (int) dims);

        buttons.drawButtons(batch);

        font.getData().setScale(3f);
        glyphLayout.setText(font, "Save Game");
        font.draw(batch, glyphLayout, (int) (MyGdxGame.initialRes.x / 2f - glyphLayout.width / 2f), MyGdxGame.initialRes.y);

        if (!Objects.equals(displayMessage, "")) {
            displayMessage();
        }
        batch.end();

        if (Gdx.input.isButtonPressed(0)){
            buttons.updateButtons(camera, Gdx.input.isButtonJustPressed(0));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.escapeScreen();
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

    public void drawColonists(int x, int y, int dims){
        for (int i = 0; i < colonists.size(); i++) {
            Colonist c = colonists.get(i);
            c.drawMini(batch, x + dims * i, y, dims, colonistClothes);
        }
    }

    public void setupButtons(){
        buttons = new ButtonCollection();
        buttons.useWorldCoords = false;

        InputButtonTwo saveNameButton = new InputButtonTwo("SaveName", "saveNameButton", inputMultiplexer);
        Label l = new Label("SaveInfoLabel", "Enter a save name and then click add.");
        Label l2 = new Label("AutoSaveLabel", "toggle autoSave");
        ToggleButton autoSaveButton = new ToggleButton("AutoSave", gameScreen.autoSave);
        autoSaveButton.r = () -> gameScreen.autoSave = autoSaveButton.toggled;
        ImgOnlyButton addButton = new ImgOnlyButton("Add", "add", () -> {
            if(gameScreen.saveGame(saveNameButton.text, "save.sve")){
                display("Save Successful");
                gameScreen.saveDir = saveNameButton.text;
            }
            else{
                display("Save Failed");
            }
        });

        float startX = 10;
        float startY = MyGdxGame.initialRes.y * 0.75f;
        float height = MyGdxGame.initialRes.y * 0.1f;
        l.drawCentred = false;
        l.setPos(startX * 2, startY);
        l.setSize(MyGdxGame.initialRes.x * 0.5f, height);
        l.autoSize();

        saveNameButton.setPos(startX, startY - height);
        saveNameButton.setSize(MyGdxGame.initialRes.x * 0.2f, height);

        addButton.setPos(startX + saveNameButton.width * 1.1f, startY - height + height * 0.25f);
        addButton.setSize(height / 2f, height / 2f);

        autoSaveButton.setPos(startX, startY - height * 3);
        autoSaveButton.setSize(height, height);

        l2.setPos(startX + autoSaveButton.width * 1.1f, startY - height * 3);
        l2.setSize(MyGdxGame.initialRes.x * 0.2f, height);
        l2.autoSize();

        buttons.add(saveNameButton, l, addButton, autoSaveButton, l2);
    }

    public void display(String s){
        displayMessage = s;
        displayCount = 3;
    }

    public void displayMessage(){
        float startX = 10;
        float startY = MyGdxGame.initialRes.y * 0.75f;
        float height = MyGdxGame.initialRes.y * 0.1f;

        font.getData().setScale(1f);
        glyphLayout.setText(font, displayMessage);
        font.draw(batch, glyphLayout, startX * 2, startY - height * 2 + glyphLayout.height * 2);
    }
}
