package com.mygdx.game.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.Entity.EntityGroup;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.Entity.Colonist;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Map;
import com.mygdx.game.floorDrops.Zone;
import com.mygdx.game.ui.elements.BoxedTextButton;
import com.mygdx.game.ui.elements.Button;
import com.mygdx.game.ui.elements.TextButton;
import com.mygdx.game.ui.extensions.ButtonCollection;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class LoadSaveScreen2 implements Screen {
    MyGdxGame game;

    ArrayList<String> saveNames;
    ArrayList<ArrayList<String>> saveNames2 = new ArrayList<>();

    ButtonCollection buttonCollectionForSaves;
    ButtonCollection buttonCollectionForSelected;
    ButtonCollection buttonCollectionForUI;
    CameraTwo camera;

    SpriteBatch batch;

    int startIndex = 0;
    int numberShown = 8;
    int selectedIndex = 0;

    int offset = 5;

    boolean hasLoadedAMap;

    ArrayList<Colonist> colonists = new ArrayList<>();
    Map map;

    HashMap<String, Texture> textures;
    HashMap<String, TextureAtlas> thingTextures;

    InputProcessor inputProcessor = new InputAdapter(){
        @Override
        public boolean scrolled(float amountX, float amountY) {
            System.out.println(amountY);
            System.out.println(saveNames);
            if(amountY > 0){
                System.out.println("attempting to increase start index");
                if (startIndex + numberShown < saveNames.size()){
                    System.out.println("increasing start index");
                    startIndex++;
                    updateSaveButtons();
//                    updateSelected(true);
                }
            }
            else if(amountY < 0){
                System.out.println("attempting to decrease start index");
                if (startIndex > 0){
                    System.out.println("decreasing start index");
                    startIndex--;
                    updateSaveButtons();
//                    updateSelected(false);
                }
            }
            return false;
        }
    };

    public LoadSaveScreen2(MyGdxGame game){
        this.game = game;
        setup();
    }

    public void setup(){
        batch = new SpriteBatch();
        camera = new CameraTwo();
        camera.allowMovement = false;
        buttonCollectionForSaves = new ButtonCollection();
        buttonCollectionForUI = new ButtonCollection();
        buttonCollectionForSelected = new ButtonCollection();
        setupSaveNames();
        setupAllSaveButtons();
        setupAllSelectedButtons();
        setupUI();
        setupMap();

        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(49/255f, 53/255f, 61/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.begin();
        batch.setProjectionMatrix(camera.projViewMatrix.getGdxMatrix());
        buttonCollectionForSaves.drawButtons(batch);
        buttonCollectionForUI.drawButtons(batch);
        buttonCollectionForSelected.drawButtons(batch);

        map.drawMiniMap(batch, textures, thingTextures);
        batch.end();

        updateSelectedButton();

        if (Gdx.input.isButtonPressed(0)) {
            buttonCollectionForUI.updateButtons(camera, Gdx.input.isButtonJustPressed(0));
            if (buttonCollectionForSaves.updateButtons(camera, Gdx.input.isButtonJustPressed(0))){
                resetSelectedButton();
            }
            if (getSelectedButton() != null) {
                buttonCollectionForSelected.updateButtons(camera, Gdx.input.isButtonJustPressed(0));
            }

            if (Gdx.input.isButtonJustPressed(0)){
                if (buttonCollectionForUI.pressedButtonName.equals("loadButton")){
                    hasLoadedAMap = loadMap();
                }
                else if (buttonCollectionForUI.pressedButtonName.equals("continueButton")){
                    if (hasLoadedAMap){
                        BoxedTextButton b2 = (BoxedTextButton) getSelectedButton();
                        BoxedTextButton b3 = (BoxedTextButton) getSaveSelected();
                        game.setScreen(new GameScreen(game, colonists, map, b2.text, b3.text));
                    }
                }
            }
            selectedIndex = buttonCollectionForSaves.buttons.indexOf(getSelectedButton());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
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

    public void setupAllSaveButtons(){
        int x = (int) (MyGdxGame.initialRes.x / 10);
        int y = (int) (MyGdxGame.initialRes.y / 10);

        int buttonWidth = (int) (MyGdxGame.initialRes.x / 5);
        int buttonHeight = (int) (MyGdxGame.initialRes.y / 10);

        for (int i = startIndex; i < startIndex + numberShown; i++) {
            String text = "";
            if (i < saveNames.size()) {
                text = saveNames.get(i);
            }
            int height = (int) (MyGdxGame.initialRes.y / 10 * 8) + (offset * (numberShown / 2));
            BoxedTextButton b = new BoxedTextButton(x, height - (buttonHeight * i) - (offset * i),
                    buttonWidth, buttonHeight, i + "", text);
            buttonCollectionForSaves.add(b);
        }
        //No one can grow if he does not accept his smallness
    }

    public void setupAllSelectedButtons(){
        int x = (int) (MyGdxGame.initialRes.x / 10);
        int y = (int) (MyGdxGame.initialRes.y / 10);

        int buttonWidth = (int) (MyGdxGame.initialRes.x / 5);
        int buttonHeight = (int) (MyGdxGame.initialRes.y / 10);

        for (int i = 0; i < 4; i++) {
            int height = (int) (MyGdxGame.initialRes.y / 10 * 8) + (offset * (numberShown / 2));
            BoxedTextButton b = new BoxedTextButton((int) (x + buttonWidth * 1.1f), height - (buttonHeight * i) - (offset * i),
                    buttonWidth, buttonHeight, i + "", "");
            buttonCollectionForSelected.add(b);
        }
    }

    public void updateSaveButtons(){
        for (int i = startIndex; i < startIndex + numberShown; i++) {
            if (i < saveNames.size()) {
                System.out.println(i);
                BoxedTextButton b = (BoxedTextButton) buttonCollectionForSaves.buttons.get(i - startIndex);
                b.setText(saveNames.get(i));
            }
        }
    }

    public void updateSelectedButton(){
        Button b = getSelectedButton();
        if (b != null){
            if (selectedIndex < saveNames.size()) {
                ArrayList<String> saves = saveNames2.get(selectedIndex);
                for (int i = 0; i < 4; i++) {
                    BoxedTextButton b3 = (BoxedTextButton) buttonCollectionForSelected.buttons.get(i);
                    if (i > saves.size() - 1){
                        b3.setText("");
                    }
                    else {

                        String name = saves.get(i);
                        if (!name.equals("")) {
                            b3.setText(name);
                        }
                    }
                }
            }else {
                for (int i = 0; i < 4; i++) {
                    BoxedTextButton b3 = (BoxedTextButton) buttonCollectionForSelected.buttons.get(i);
                    b3.setText("");
                }
            }
        }
    }

    public void setupSaveNames(){
        saveNames = new ArrayList<>();
        File dir = new File("core/assets/Saves");
        saveNames.addAll(Arrays.asList(Objects.requireNonNull(dir.list())));

        if (dir.isDirectory()){
            File[] files = dir.listFiles();
            for (File file : Objects.requireNonNull(files)) {
                String[] fileSaves = file.list();
                if (fileSaves != null){
                    saveNames2.add(new ArrayList<>(Arrays.asList(fileSaves)));
                }
            }
        }
    }

    public void setupUI(){
        TextButton loadButton = new TextButton("Load", "loadButton");
        TextButton continueButton = new TextButton("Continue", "continueButton");

        loadButton.setSize(MyGdxGame.initialRes.x / 10f * 2, MyGdxGame.initialRes.y / 10f);
        continueButton.setSize(MyGdxGame.initialRes.x / 10f * 2, MyGdxGame.initialRes.y / 10f);

        float y = (MyGdxGame.initialRes.y / 10 * 8) + (offset * (numberShown / 2f))
                - ((MyGdxGame.initialRes.y / 10) * (numberShown - 1)) - (offset * numberShown);

        loadButton.setPos(MyGdxGame.initialRes.x / 2 - (loadButton.width * 0.85f), y);
        continueButton.setPos(MyGdxGame.initialRes.x - MyGdxGame.initialRes.y * 0.1f - continueButton.width
                , y);

        buttonCollectionForUI.add(loadButton, continueButton);
    }

    public void setupMap(){
        map = new Map((int) (MyGdxGame.initialRes.y / 10 * 7),
                (int) (MyGdxGame.initialRes.x - MyGdxGame.initialRes.y / 10 * 8),
                (int) (MyGdxGame.initialRes.y / 10 * 2f), "sed");
        map.generateBlank();
        setupMapResources();
    }

    public void setupMapResources(){
        textures = new HashMap<>();
        thingTextures = new HashMap<>();
        GameScreen.getAllMapTextures(textures, thingTextures);
    }

    public boolean loadMap(){
        Button bDir = getSelectedButton();
        Button b = getSaveSelected();
        if (b != null && bDir != null){
            BoxedTextButton b2 = (BoxedTextButton) b;
            BoxedTextButton b3 = (BoxedTextButton) bDir;
            if (!b2.text.equals("")) {
                colonists = Map.loadColonists(b3.text, b2.text);
                return Map.loadMap(b3.text, b2.text, map);
            }
            return false;
        }
        return false;
    }

    public Button getSelectedButton(){
        for (Button b : buttonCollectionForSaves.buttons) {
            if (b.selected){
                return b;
            }
        }
        return null;
    }

    public Button getSaveSelected(){
        for (Button b : buttonCollectionForSelected.buttons) {
            if (b.selected){
                return b;
            }
        }
        return null;
    }

    public void resetSelectedButton(){
        for (Button b : buttonCollectionForSelected.buttons) {
            b.selected = false;
        }
    }
}
